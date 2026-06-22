package Tests;

import DomainLayer.*;
import ServiceLayer.*;
import DTO.*;
import DataAccessLayer.*;
import Transportation.TransportationMock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class partb {

    private EmployeeService employeeService;
    private ShiftService shiftService;
    private Employee regularEmployee;
    private Driver testDriver;
    private HRRepository repository;

    @BeforeEach
    void setUp() {
        // Correctly initializing the repository components to fix the constructor errors
        EmployeeDAO employeeDAO = new JdbcEmployeeDAO();
        ConstraintDAO constraintDAO = new JdbcConstraintDAO();
        ShiftDAO shiftDAO = new JdbcShiftDAO();

        repository = new HRRepositoryImpl(employeeDAO, constraintDAO, shiftDAO);

        // Passing the required HRRepository argument to services
        employeeService = new EmployeeService(repository);
        shiftService = new ShiftService(repository, employeeService);

        // Clear previous database data for these test IDs to keep tests isolated
        try { repository.deleteEmployee(999); } catch (Exception e) {}
        try { repository.deleteEmployee(888); } catch (Exception e) {}

        Role[] regularRoles = {Role.CASHIER};
        regularEmployee = new Employee("Regular Emp", 999, regularRoles, 11, 22, 33,
                DayOfWeek.SATURDAY, LocalDate.now(), "Full", 8000, 45, 1);
        employeeService.addEmployee(regularEmployee);

        Role[] driverRoles = {Role.DRIVER};
        testDriver = new Driver("Pro Driver", 888, driverRoles, 44, 55, 66,
                DayOfWeek.SATURDAY, LocalDate.now(), "Full", 9000, 50, 1, "C1");
        employeeService.addEmployee(testDriver);
    }

    // 1. Test verification of Transportation module communication regarding storefront constraints
    @Test
    void testMustHaveStoreKeeperBasedOnTransportationMock() {
        LocalDate testDate = LocalDate.now().plusDays(2);
        // Uses TransportationMock internally to verify if storekeeper is mandatory
        boolean requiresStorekeeper = shiftService.mustHaveStoreKeeper(testDate, 'm');
        assertTrue(requiresStorekeeper, "The system must enforce storekeeper presence if TransportationMock signals active transport");
    }

    // 2. Test extraction of needed driver allocations from integration rules
    @Test
    void testNumOfDriversPerShiftFromMock() {
        LocalDate testDate = LocalDate.now().plusDays(2);
        // Verifies the system polls dynamic driver needs from TransportationMock.getTransportCount
        int recommendedDrivers = shiftService.numOfDriversPerShift(testDate, 'm');
        assertEquals(2, recommendedDrivers, "System should pull exactly 2 drivers as specified by the mock profile limits");
    }

    // 3. Test successful tracking and filtering of drivers by dynamic license checks
    @Test
    void testDriverLicenseAuthenticationSuccess() {
        // Driver has "C1" license, checking validation parameters
        boolean hasCorrectLicense = employeeService.checkLicence(888);
        assertTrue(hasCorrectLicense, "Driver holding C1 license should successfully validate against current transport demands");
    }

    // 4. Test failure of driver validation when employee is not a driver instance
    @Test
    void testRegularEmployeeFailsLicenseCheck() {
        // Employee 999 is a regular cashier, not a driver
        boolean hasCorrectLicense = employeeService.checkLicence(999);
        assertFalse(hasCorrectLicense, "Non-driver instances must safely evaluate to false during transportation matching operations");
    }

    // 5. Test dynamic retrieval of available drivers filtered by valid matching license criteria
    @Test
    void testGetAvailableDriversWithCorrectLicense() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        // Checks capability filter for transport staffing sequences
        List<Driver> matchingDrivers = shiftService.getAvailableDriversForTransport(targetDate, 'm', "C1");
        assertTrue(matchingDrivers.stream().anyMatch(d -> d.getId() == 888), "The filtered driver pool must include qualified drivers");
    }

    // 6. Test omission of drivers from list if the required license string differs
    @Test
    void testGetAvailableDriversOmitsMismatchedLicenses() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        // Searching for license type "D" while driver only has "C1"
        List<Driver> matchingDrivers = shiftService.getAvailableDriversForTransport(targetDate, 'm', "D");
        assertFalse(matchingDrivers.stream().anyMatch(d -> d.getId() == 888), "Drivers lacking specific license ranks should be omitted");
    }

    // 7. Test bulk driver query retrieval parameters from service layer
    @Test
    void testGetAllDriversFiltersRosterCorrectly() {
        List<Driver> allDrivers = employeeService.getAllDrivers();
        assertTrue(allDrivers.stream().anyMatch(d -> d.getId() == 888), "The specialized driver list must contain the driver entry");
        assertFalse(allDrivers.stream().anyMatch(d -> d.getId() == 999), "The driver stream should clean out standard non-driver employees");
    }

    // 8. Test constraint matching bounds for partial evening shift overlap blocks
    @Test
    void testConstraintOverlapsEveningBoundaries() {
        Constraint eveningConstraint = new Constraint(999, LocalDate.now(), LocalTime.of(18, 0), LocalTime.of(23, 0), false, 0, true);
        // Verifies the underlying boundary checker works independently from internal shift range definitions
        assertTrue(eveningConstraint.blocksEveningShift(), "Constraint overlapping internal shift ranges must declare an evening block");
        assertFalse(eveningConstraint.blocksMorningShift(), "Evening window bounds should completely clear morning shift operations");
    }

    // 9. Test clearing all recorded constraints dynamically through domain methods
    @Test
    void testClearConstraintsRemovesAllTrackedData() {
        Constraint c1 = new Constraint(999, LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(12, 0), false, 0, true);
        Constraint c2 = new Constraint(999, LocalDate.now().plusDays(2), LocalTime.of(8, 0), LocalTime.of(12, 0), false, 0, true);

        regularEmployee.addConstraint(c1);
        regularEmployee.addConstraint(c2);
        assertEquals(2, regularEmployee.getCurrentConstraints().size());

        regularEmployee.clearConstraints();
        assertTrue(regularEmployee.getCurrentConstraints().isEmpty(), "Constraints structure must instantly clear tracking instances");
    }

    // 10. Test selective removal of individual employee constraints mapped by targets dates
    @Test
    void testRemoveConstraintBySpecificDate() {
        LocalDate date1 = LocalDate.now().plusDays(5);
        LocalDate date2 = LocalDate.now().plusDays(6);

        Constraint constraint1 = new Constraint(999, date1, LocalTime.of(9, 0), LocalTime.of(11, 0), false, 0, true);
        Constraint constraint2 = new Constraint(999, date2, LocalTime.of(9, 0), LocalTime.of(11, 0), false, 0, true);

        regularEmployee.addConstraint(constraint1);
        regularEmployee.addConstraint(constraint2);

        // Target and drop a single constraint instance by its date identity
        regularEmployee.removeConstraintByDate(date1);
        assertEquals(1, regularEmployee.getCurrentConstraints().size(), "Only one unselected tracking day should survive deletion");
        assertEquals(date2, regularEmployee.getCurrentConstraints().get(0).getDate(), "Surviving index must reflect the unselected date parameters");
    }
}