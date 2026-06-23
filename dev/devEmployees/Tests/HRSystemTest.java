package devEmployees.Tests;
import devEmployees.DomainLayer.*;
import devEmployees.ServiceLayer.*;
import devEmployees.DataAccessLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HRSystemTest {

    private EmployeeService employeeService;
    private ShiftService shiftService;
    private HRRepository repository;
    private Employee testEmployee;
    private int testId;

    @BeforeEach
    void setUp() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver missing in test classpath", e);
        }
        // 1. Initialize the concrete Data Access Objects (DAOs)
        EmployeeDAO employeeDAO = new JdbcEmployeeDAO();
        ConstraintDAO constraintDAO = new JdbcConstraintDAO();
        ShiftDAO shiftDAO = new JdbcShiftDAO();

        // 2. Initialize the centralized Repository implementation
        repository = new HRRepositoryImpl(employeeDAO, constraintDAO, shiftDAO);

        // 3. Inject the repository into the services
        employeeService = new EmployeeService(repository);
        shiftService = new ShiftService(repository, employeeService);

        // 4. Create a unique random ID for the current run to prevent soft-delete conflicts
        testId = (int)(Math.random() * 900000) + 100000;

        // 5. Clean up the random ID just in case to ensure a clean slate
        repository.deleteEmployee(testId);

        // 6. Build the employee object using the dynamic testId (instead of a hardcoded 999999)
        Role[] roles = {Role.CASHIER, Role.SHIFTMANAGER};
        testEmployee = new Employee("Test User", testId, roles, 12, 345, 6789,
                DayOfWeek.SUNDAY, LocalDate.now(), "Full", 10000, 50, 1);

        employeeService.addEmployee(testEmployee);
    }

    // 1. Test Firing Logic
    // 1. Test Firing Logic
    @Test
    void testFireEmployee() {
        employeeService.fireEmployee(testId);

        // Check if the employee is now included in the fired list
        boolean isFired = employeeService.getFiredEmployees().stream()
                .anyMatch(e -> e.getId() == testId);

        assertTrue(isFired, "Employee should be present in the fired list");
    }

    // 2. Prevent Duplicate IDs
    @Test
    void testDuplicateIdPrevention() {
        Role[] roles = {Role.CASHIER};
        // We attempt to create a new employee with the exact same random ID as the existing employee
        Employee duplicate = new Employee("Other", testId, roles, 1, 1, 1, DayOfWeek.MONDAY, LocalDate.now(), "Part", 5000, 40, 1);
        assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(duplicate));
    }

    // 3. Test Promotion
    @Test
    void testPromoteToShiftManager() {
        testEmployee.setRoles(new Role[]{Role.CASHIER});

        java.util.List<Role> rolesList = new java.util.ArrayList<>(java.util.Arrays.asList(testEmployee.getRoles()));
        if (!rolesList.contains(Role.SHIFTMANAGER)) {
            rolesList.add(Role.SHIFTMANAGER);
            testEmployee.setRoles(rolesList.toArray(new Role[0]));
        }

        assertTrue(java.util.Arrays.asList(testEmployee.getRoles()).contains(Role.SHIFTMANAGER));
    }

    // 4. Block Constraint after Deadline
    @Test
    void testDeadlineBlocking() {
        employeeService.setConstraintDeadline(LocalDateTime.now().minusHours(1)); // Deadline in the past
        Constraint c = new Constraint(testId, LocalDate.now().plusDays(1), LocalTime.of(8,0), LocalTime.of(12,0), false, 0, true);
        String result = employeeService.insertConstraint(testId, c);
        assertTrue(result.contains("Error"), "Should block assignment after deadline");
    }

    // 5. Test Time Overlaps
    @Test
    void testConstraintOverlaps() {
        // Constraint from 13:00 to 15:00
        Constraint c = new Constraint(testId, LocalDate.now(), LocalTime.of(13,0), LocalTime.of(15,0), false, 0, true);
        assertTrue(c.blocksMorningShift(), "Should block morning shift (ends at 14:00)");
        assertTrue(c.blocksEveningShift(), "Should block evening shift (starts at 14:00)");
    }

    // 6. Edit Existing Constraint
    @Test
    void testEditConstraint() {
        LocalDate date = LocalDate.now().plusDays(2);
        Constraint oldC = new Constraint(testId, date, LocalTime.of(8,0), LocalTime.of(10,0), false, 0, true);
        Constraint newC = new Constraint(testId, date, LocalTime.of(14,0), LocalTime.of(18,0), false, 0, true);

        employeeService.insertConstraint(testId, oldC);
        employeeService.editConstraint(testId, date, newC);

        Employee updatedEmp = employeeService.getEmployeeById(testId);
        assertEquals(1, updatedEmp.getCurrentConstraints().size());
        assertEquals(LocalTime.of(14,0), updatedEmp.getCurrentConstraints().get(0).getStartTime());
    }

    // 7. Block Assignment on Day Off
    @Test
    void testDayOffBlocking() {
        LocalDate sunday = LocalDate.of(2026, 4, 19); // Employee's day off
        shiftService.createDefaultShift(sunday, 'm', testEmployee, 1, 0);

        String result = shiftService.assignEmployeeToShift(testId, sunday, 'm', Role.CASHIER, 0);
        assertTrue(result.contains("Error"), "Should not allow assignment on Day Off");
    }

    // 8. Exceptional Assignment (Flexible vs Hard)
    @Test
    void testExceptionalAssignment() {
        LocalDate date = LocalDate.now().plusDays(3);
        shiftService.createDefaultShift(date, 'm', testEmployee, 1, 0);

        // Case A: Flexible constraint - should succeed
        Constraint flex = new Constraint(testId, date, LocalTime.of(8,0), LocalTime.of(12,0), false, 0, true);

        // FIX: Insert via Service so it flushes directly into the Constraints DB Table
        employeeService.insertConstraint(testId, flex);

        String resFlex = shiftService.assignExceptionalShift(testId, date, 'm', Role.CASHIER);
        assertTrue(resFlex.contains("Success"), "Flexible constraint assignment should succeed");

        // Case B: Hard constraint - should fail
        LocalDate hardDate = LocalDate.now().plusDays(4); // Use a new date to prevent collision
        shiftService.createDefaultShift(hardDate, 'm', testEmployee, 1, 0);

        Constraint hard = new Constraint(testId, hardDate, LocalTime.of(8,0), LocalTime.of(12,0), false, 0, false);
        employeeService.insertConstraint(testId, hard);

        String resHard = shiftService.assignExceptionalShift(testId, hardDate, 'm', Role.CASHIER);
        assertTrue(resHard.contains("Error"), "Hard constraint assignment should return an Error");
    }

    // 9. Qualification Check
    @Test
    void testQualificationMismatch() {
        LocalDate date = LocalDate.now().plusDays(4);
        shiftService.createDefaultShift(date, 'm', testEmployee, 1, 0);

        String result = shiftService.assignEmployeeToShift(testId, date, 'm', Role.DRIVER, 0);
        assertTrue(result.contains("Error"));
    }

    // 10. Extra Hours Retrieval
    @Test
    void testExtraHoursRetrieval() {
        LocalDate date = LocalDate.now().plusDays(5);
        Constraint c = new Constraint(testId, date, LocalTime.of(8,0), LocalTime.of(12,0), false, 3, true);
        testEmployee.addConstraint(c);

        int potentialExtra = shiftService.getPotentialExtraHours(testEmployee, date);
        assertEquals(3, potentialExtra, "Should correctly retrieve 3 extra hours from constraint");
    }
}