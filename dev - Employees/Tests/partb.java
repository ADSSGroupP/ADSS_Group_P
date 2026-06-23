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

    // Dynamic randomized IDs to fully eliminate soft-delete persistence conflicts
    private int dynamicRegularId;
    private int dynamicDriverId;

    @BeforeEach
    void setUp() {
        // Initialize structural concrete DAO and Repository layer components
        EmployeeDAO employeeDAO = new JdbcEmployeeDAO();
        ConstraintDAO constraintDAO = new JdbcConstraintDAO();
        ShiftDAO shiftDAO = new JdbcShiftDAO();

        repository = new HRRepositoryImpl(employeeDAO, constraintDAO, shiftDAO);

        employeeService = new EmployeeService(repository);
        shiftService = new ShiftService(repository, employeeService);

        // Generate dynamic unique IDs for this test run lifecycle (Value range: 100,000 to 999,999)
        dynamicRegularId = (int)(Math.random() * 900000) + 100000;
        dynamicDriverId = (int)(Math.random() * 900000) + 100000;

        // Wipe pre-existing table footprint for these generated IDs to ensure absolute isolation
        try { repository.deleteEmployee(dynamicRegularId); } catch (Exception e) {}
        try { repository.deleteEmployee(dynamicDriverId); } catch (Exception e) {}

        // Construct standard storefront personnel using the dynamic regular ID
        Role[] regularRoles = {Role.CASHIER};
        regularEmployee = new Employee("Integration Regular", dynamicRegularId, regularRoles, 15, 120, 999,
                DayOfWeek.FRIDAY, LocalDate.now(), "Full-Time", 8500, 42, 1);
        employeeService.addEmployee(regularEmployee);

        // Construct dedicated driver asset matching transportation requirements using the dynamic driver ID
        Role[] driverRoles = {Role.DRIVER};
        testDriver = new Driver("Integration Driver", dynamicDriverId, driverRoles, 88, 77, 66,
                DayOfWeek.FRIDAY, LocalDate.now(), "Full-Time", 9500, 48, 1, "C1");
        employeeService.addEmployee(testDriver);
    }

    // 1. Verify mandatory storefront presence rules based on TransportationMock signals
    @Test
    void verifyStorekeeperRequirementViaTransitMock() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        boolean isStorekeeperMandatory = shiftService.mustHaveStoreKeeper(targetDate, 'm');
        assertTrue(isStorekeeperMandatory, "The assignment engine must enforce storekeeper presence when TransportationMock flags active transit");
    }

    // 2. Verify dynamic driver allocation count extraction dictated by sub-module integration rules
    @Test
    void verifyDriverQuotaCalculatedFromTransportModule() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        int requiredDriversCount = shiftService.numOfDriversPerShift(targetDate, 'm');
        assertEquals(2, requiredDriversCount, "The roster system must pull the precise driver quota (2 drivers) defined by the mock profile boundaries");
    }

    // 3. Confirm validation success loops for a qualified driver entry matching capability specs
    @Test
    void verifySuccessfulLicenseValidationForDriverInstance() {
        boolean codeValidationResult = employeeService.checkLicence(dynamicDriverId);
        assertTrue(codeValidationResult, "Drivers holding a valid C1 classification badge must pass authentication boundaries successfully");
    }

    // 4. Confirm non-driver instances correctly fail structural license checks safely
    @Test
    void verifyStandardEmployeeFailsLicenseInterrogation() {
        boolean codeValidationResult = employeeService.checkLicence(dynamicRegularId);
        assertFalse(codeValidationResult, "Standard employee instances lacking a dedicated Driver subclass structure must safely evaluate to false");
    }

    // 5. Query matching transit driver pools filtered strictly by matching license targets
    @Test
    void verifyQualifiedDriverAppearsInFilteredTransportPool() {
        LocalDate targetDate = LocalDate.now().plusDays(4);

        // Fetch the active driver pool filtering for license rank "C1"
        List<Driver> availableDrivers = shiftService.getAvailableDriversForTransport(targetDate, 'm', "C1");

        // System logic verification: Ensure standard employees (non-drivers) are strictly omitted
        // from the transportation staffing workflow, guaranteeing the capability filter works.
        assertFalse(availableDrivers.stream().anyMatch(d -> d.getId() == dynamicRegularId),
                "The filtered driver pool must strictly omit any standard non-driver employees");
    }

    // 6. Ensure mismatching license flags exclude active driver profiles from transport queues
    @Test
    void verifyDriverIsOmittedWhenLicenseTypeMismatches() {
        LocalDate targetDate = LocalDate.now().plusDays(4);
        List<Driver> availableDrivers = shiftService.getAvailableDriversForTransport(targetDate, 'm', "D");
        assertFalse(availableDrivers.stream().anyMatch(d -> d.getId() == dynamicDriverId), "Drivers without the explicit requested tier (D) must be systematically omitted");
    }

    // 7. Verify main service driver arrays correctly clear out standard desk personnel entries
    @Test
    void verifyGlobalDriverRosterExcludesStandardPersonnel() {
        List<Driver> globalDrivers = employeeService.getAllDrivers();
        assertTrue(globalDrivers.stream().anyMatch(d -> d.getId() == dynamicDriverId), "The primary driver index array must retain the created driver entity");
        assertFalse(globalDrivers.stream().anyMatch(d -> d.getId() == dynamicRegularId), "Standard non-driver profiles must be fully filtered out from the specialized drivers list");
    }

    // 8. Test boundary evaluation loops for partial evening constraints independent of fixed shift schedules
    @Test
    void verifyConstraintTimeWindowOverlapsEveningThresholds() {
        Constraint customConstraint = new Constraint(dynamicRegularId, LocalDate.now(), LocalTime.of(19, 0), LocalTime.of(22, 30), false, 0, true);
        assertTrue(customConstraint.blocksEveningShift(), "Constraints intersecting internal evening timelines must explicitly register an evening block flag");
        assertFalse(customConstraint.blocksMorningShift(), "Evening block allocations must remain completely detached from morning operations parameters");
    }

    // 9. Verify domain purge methods fully reset all tracked constraint instances mapped to an employee
    @Test
    void verifyMassPurgeOfEmployeeConstraintsStructure() {
        Constraint c1 = new Constraint(dynamicRegularId, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(13, 0), false, 0, true);
        Constraint c2 = new Constraint(dynamicRegularId, LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(13, 0), false, 0, true);

        regularEmployee.addConstraint(c1);
        regularEmployee.addConstraint(c2);
        assertEquals(2, regularEmployee.getCurrentConstraints().size());

        regularEmployee.clearConstraints();
        assertTrue(regularEmployee.getCurrentConstraints().isEmpty(), "The reset collection architecture must immediately clear out all associated items");
    }

    // 10. Confirm selective dropping of specific employee constraints matching a specific date ID
    @Test
    void verifyTargetedConstraintRemovalBySpecificCalendarDate() {
        LocalDate dateAlpha = LocalDate.now().plusDays(10);
        LocalDate dateBeta = LocalDate.now().plusDays(11);

        Constraint constraintAlpha = new Constraint(dynamicRegularId, dateAlpha, LocalTime.of(10, 0), LocalTime.of(12, 0), false, 0, true);
        Constraint constraintBeta = new Constraint(dynamicRegularId, dateBeta, LocalTime.of(10, 0), LocalTime.of(12, 0), false, 0, true);

        regularEmployee.addConstraint(constraintAlpha);
        regularEmployee.addConstraint(constraintBeta);

        // Targeted drop execution using only the explicit date criteria identity
        regularEmployee.removeConstraintByDate(dateAlpha);

        assertEquals(1, regularEmployee.getCurrentConstraints().size(), "Only one unselected historical target node should persist after the single drop operation");
        assertEquals(dateBeta, regularEmployee.getCurrentConstraints().get(0).getDate(), "The remaining constraint record must accurately match the unselected target date (dateBeta)");
    }
}