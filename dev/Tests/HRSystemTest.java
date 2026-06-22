
package Tests;
import DomainLayer.*;
import ServiceLayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class HRSystemTest {

    private EmployeeService employeeService;
    private ShiftService shiftService;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        shiftService = new ShiftService(employeeService);

        // Create a basic employee for testing
        String[] roles = {"Cashier", "Shift Manager"};
        testEmployee = new Employee("Test User", 101, roles, 12, 345, 6789,
                DayOfWeek.SUNDAY, LocalDate.now(), "Full", 10000, 50);
        employeeService.addEmployee(testEmployee);
    }

    // 1. Test Firing Logic
    @Test
    void testFireEmployee() {
        employeeService.fireEmployee(101);
        assertNull(employeeService.getEmployeeById(101), "Employee should not be in active list");
        assertEquals(1, employeeService.getFiredEmployees().size(), "Employee should be in fired list");
    }

    // 2. Prevent Duplicate IDs
    @Test
    void testDuplicateIdPrevention() {
        String[] roles = {"Cashier"};
        Employee duplicate = new Employee("Other", 101, roles, 1, 1, 1, DayOfWeek.MONDAY, LocalDate.now(), "Part", 5000, 40);
        assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(duplicate));
    }

    // 3. Test Promotion
    @Test
    void testPromoteToShiftManager() {
        testEmployee.setRoles(new String[]{"Cashier"});

        java.util.List<String> rolesList = new java.util.ArrayList<>(java.util.Arrays.asList(testEmployee.getRoles()));
        if (!rolesList.contains("Shift Manager")) {
            rolesList.add("Shift Manager");
            testEmployee.setRoles(rolesList.toArray(new String[0]));
        }

        assertTrue(java.util.Arrays.asList(testEmployee.getRoles()).contains("Shift Manager"));
    }

    // 4. Block Constraint after Deadline
    @Test
    void testDeadlineBlocking() {
        employeeService.setConstraintDeadline(LocalDateTime.now().minusHours(1)); // Deadline in the past
        Constraint c = new Constraint(101, LocalDate.now().plusDays(1), LocalTime.of(8,0), LocalTime.of(12,0), false, 0, true);
        String result = employeeService.insertConstraint(101, c);
        assertTrue(result.contains("Error"), "Should block assignment after deadline");
    }

    // 5. Test Time Overlaps
    @Test
    void testConstraintOverlaps() {
        // Constraint from 13:00 to 15:00
        Constraint c = new Constraint(101, LocalDate.now(), LocalTime.of(13,0), LocalTime.of(15,0), false, 0, true);
        assertTrue(c.blocksMorningShift(), "Should block morning shift (ends at 14:00)");
        assertTrue(c.blocksEveningShift(), "Should block evening shift (starts at 14:00)");
    }

    // 6. Edit Existing Constraint
    @Test
    void testEditConstraint() {
        LocalDate date = LocalDate.now().plusDays(2);
        Constraint oldC = new Constraint(101, date, LocalTime.of(8,0), LocalTime.of(10,0), false, 0, true);
        Constraint newC = new Constraint(101, date, LocalTime.of(14,0), LocalTime.of(18,0), false, 0, true);

        employeeService.insertConstraint(101, oldC);
        employeeService.editConstraint(101, date, newC);

        assertEquals(1, testEmployee.getCurrentConstraints().size());
        assertEquals(LocalTime.of(14,0), testEmployee.getCurrentConstraints().get(0).getStartTime());
    }

    // 7. Block Assignment on Day Off
    @Test
    void testDayOffBlocking() {
        LocalDate sunday = LocalDate.of(2026, 4, 19); // Employee's day off
        Map<String, Integer> model = new HashMap<>();
        model.put("Cashier", 1);
        shiftService.createShift(sunday, 'm', testEmployee, model);

        String result = shiftService.assignEmployeeToShift(101, sunday, 'm', "Cashier", 0);
        assertTrue(result.contains("Error"), "Should not allow assignment on Day Off");
    }

    // 8. Exceptional Assignment (Flexible vs Hard)
    @Test
    void testExceptionalAssignment() {
        LocalDate date = LocalDate.now().plusDays(3);
        Map<String, Integer> model = new HashMap<>();
        model.put("Cashier", 1);
        shiftService.createShift(date, 'm', testEmployee, model);

        // Case A: Flexible constraint - should succeed
        Constraint flex = new Constraint(101, date, LocalTime.of(8,0), LocalTime.of(12,0), false, 0, true);
        testEmployee.addConstraint(flex);
        String resFlex = shiftService.assignExceptionalShift(101, date, 'm', "Cashier");
        assertTrue(resFlex.contains("Success"));

        // Case B: Hard constraint - should fail
        testEmployee.getCurrentConstraints().clear();
        Constraint hard = new Constraint(101, date, LocalTime.of(8,0), LocalTime.of(12,0), false, 0, false);
        testEmployee.addConstraint(hard);
        String resHard = shiftService.assignExceptionalShift(101, date, 'm', "Cashier");
        assertTrue(resHard.contains("Error"));
    }

    // 9. Qualification Check
    @Test
    void testQualificationMismatch() {
        LocalDate date = LocalDate.now().plusDays(4);
        Map<String, Integer> model = new HashMap<>();
        model.put("Driver", 1);
        shiftService.createShift(date, 'm', testEmployee, model);

        String result = shiftService.assignEmployeeToShift(101, date, 'm', "Driver", 0);
        assertTrue(result.contains("not qualified"));
    }

    // 10. Extra Hours Retrieval
    @Test
    void testExtraHoursRetrieval() {
        LocalDate date = LocalDate.now().plusDays(5);
        Constraint c = new Constraint(101, date, LocalTime.of(8,0), LocalTime.of(12,0), false, 3, true);
        testEmployee.addConstraint(c);

        int potentialExtra = shiftService.getPotentialExtraHours(testEmployee, date);
        assertEquals(3, potentialExtra, "Should correctly retrieve 3 extra hours from constraint");
    }
}