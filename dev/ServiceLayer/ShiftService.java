package ServiceLayer;

import DomainLayer.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import Transportation.*;


/**
 * Service class handling shift scheduling logic.
 * Manages shift history, employee availability, and complex assignment rules (e.g., double shifts, qualifications).
 */
public class ShiftService {
    private final HRRepository repository;
    private EmployeeService employeeService; // Reference to the employee manager

    public ShiftService(HRRepository repository,EmployeeService employeeService) {
        this.repository = repository;
        this.employeeService = employeeService;
    }

    //Checks if an employee is available based on their day off and shift constraints.
    public boolean isEmployeeAvailableForShift(Employee emp, LocalDate date, char type) {
        if (emp.getDay_off() != null && date.getDayOfWeek().equals(emp.getDay_off())) {
            return false; // Not available on their day off
        }

        if (emp.getCurrentConstraints() == null || emp.getCurrentConstraints().isEmpty()) {
            return true;
        }

        return emp.getCurrentConstraints().stream()
                .filter(c -> c.getDate().equals(date)) // Only look at constraints for the same day
                .noneMatch(c -> {
                    if (type == 'm') return c.blocksMorningShift();
                    if (type == 'e') return c.blocksEveningShift();
                    return false;
                });
    }

    // The main logic for assigning an employee to a specific shift
    public String assignEmployeeToShift(int employeeId, LocalDate date, char type, Role role, int extraHours) {
        // 1. First, check if employee and shift even exist (Avoid NullPointerException)
        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) return "Error: Employee not found.";

        Shift shift =  findShiftByDateAndType(date, type);
        if (shift == null) return "Error: Shift not found.";

        // 2. Double Shift Check
        boolean alreadyWorkingThatDay = false;
        for (Shift s :getShiftHistory()) {
            // We check if the employee is already assigned to ANY shift on that date
            if (s.getDate().equals(date) && s.isEmployeeAssigned(employeeId)) {
                alreadyWorkingThatDay = true;
                break;
            }
        }

        if (alreadyWorkingThatDay) {
            boolean approvedDouble = false;
            // Search specifically for a constraint that allows double shift on THIS date
            for (Constraint c : emp.getCurrentConstraints()) {
                if (c.getDate().equals(date) && c.isDoubleShiftApproved()) {
                    approvedDouble = true;
                    break;
                }
            }

            if (!approvedDouble) {
                return "Error: Employee " + emp.getName() + " is already working on this date and did NOT approve a double shift in their constraints.";
            }
        }

        // 3. Qualification Check
        if (!Arrays.asList(emp.getRoles()).contains(role)) {
            return "Error: Employee is not qualified for " + role;
        }

        // 4. Availability Check (Morning/Evening specific constraint)
        if (!isEmployeeAvailableForShift(emp, date, type)) {
            return "Error: Employee has a constraint for this specific shift (" + type + ").";
        }

        long currentCount = shift.getShift_roles().values().stream().filter(r -> r.equals(role)).count();
        int requiredCount = shift.getShift_model().getOrDefault(role, 0);

        if (currentCount >= requiredCount) {
            return "Error: Role " + role + " is already fully staffed according to the shift model.";
        }
        // 5. Final Assignment
        shift.setShift_roles(emp, role);

        if (extraHours > 0) {
            shift.addExtraHoursAssignment(emp, extraHours);
        }

        repository.saveShift(shift);
        return "Success: " + emp.getName() + " assigned as " + role;
    }


    public void createDefaultShift(LocalDate date, char type, Employee manager, int branch_id, int driversNeeded) {
        Shift newShift = new Shift(date, type, manager, branch_id);
        if (driversNeeded>0){
            newShift.setShift_model(Role.DRIVER,driversNeeded);
        }
        repository.saveShift(newShift);
    }

    public void createCustomShift(LocalDate date, char type, Employee manager, Map<Role, Integer> customModel, int branch_id) {
        Shift newShift = new Shift(date, type, manager, branch_id);

        if (customModel != null) {
            customModel.forEach(newShift::setShift_model);
        }
        repository.saveShift(newShift);
    }
// finds all available employees for shift
    public List<Employee> getAvailableEmployeesForShift(LocalDate date, char type) {
        List<Employee> allEmployees = employeeService.getAllEmployees();

        return allEmployees.stream().filter(emp -> isEmployeeAvailableForShift(emp, date, type)).collect(Collectors.toList());
    }

    //Searches for a specific shift in the history based on date and time of day.
    public Shift findShiftByDateAndType(LocalDate date, char type) {
        return repository.getShiftByDateAndType(date, type);
    }

    //Returns a copy of the shift history list
    public List<Shift> getShiftHistory() {
        return repository.getShiftHistory();
    }

    public void addShiftToHistory(Shift shift) {
        repository.saveShift(shift);
    }

    // Extracts the maximum extra hours an employee offered to work for a specific date.
    public int getPotentialExtraHours(Employee emp, LocalDate date) {
        // method to extract the extra hours defined in employee's constraints for a specific day
        return emp.getCurrentConstraints().stream().filter(c -> c.getDate().equals(date)).mapToInt(Constraint::getExtraHours).max().orElse(0);
    }

    // Handles manager-forced assignments.
    public String assignExceptionalShift(int employeeId, LocalDate date, char type, Role role) {
        Employee emp = employeeService.getEmployeeById(employeeId);
        Shift shift = findShiftByDateAndType(date, type);

        if (emp == null || shift == null) return "Error: Employee or Shift not found.";

        // Qualification check
        if (!Arrays.asList(emp.getRoles()).contains(role)) return "Error: Qualification mismatch.";

        // CHANGE: Find the specific constraint for this date
        Constraint constraint = emp.getCurrentConstraints().stream()
                .filter(c -> c.getDate().equals(date))
                .findFirst()
                .orElse(null);

        // Logic: If there's no constraint, it's a regular assignment.
        // If there is one, check if it's changeable.
        if (constraint != null && !constraint.isChangeable()) {
            return "Error: This is a HARD constraint. Assignment is strictly blocked.";
        }

        // Assign even if a flexible constraint exists
        shift.setShift_roles(emp, role);
        return "Success: Exceptional assignment completed for " + emp.getName();
    }

    //Checks if a specific employee has authorized double shifts for a given date.
    public boolean canWorkDoubleShift(int employeeId, LocalDate date) {
        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) return false;

        // Search for a constraint on this specific date
        for (Constraint c : emp.getCurrentConstraints()) {
            if (c.getDate().equals(date)) {
                return c.isDoubleShiftApproved(); // Returns true only if the employee checked the box
            }
        }
        return false;
    }

    public boolean mustHaveStoreKeeper(LocalDate date, char shiftType){
        return TransportationMock.hasTransportInShift(date,shiftType);
    }

    public int numOfDriversPerShift(LocalDate date, char shiftType){
       return TransportationMock.getTransportCount(date, shiftType);
    }

    public List<Driver> getAvailableDriversForTransport(LocalDate date, char type, String requiredLicense) {
        return employeeService.getAllDrivers().stream()
                .filter(d -> isEmployeeAvailableForShift(d, date, type))
                .filter(d -> d.getLicense().equals(requiredLicense))
                .collect(Collectors.toList());
    }


}
