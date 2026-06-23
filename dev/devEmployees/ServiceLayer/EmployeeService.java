package ServiceLayer;
import java.util.ArrayList;
import java.util.List;
import DomainLayer.*;
import Transportation.TransportationMock;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing employee-related operations.
 * Handles employee registration, archival (firing), and constraint submission deadlines.
 */
public class EmployeeService {
    private final HRRepository repository;
    private LocalDateTime constraintDeadline;

    public EmployeeService(HRRepository repository) {
        this.repository = repository;
    }

    // Registers a new work constraint for an employee. Returns success or deadline/not found error.
    public String insertConstraint(int employeeId, Constraint constraint) {
        if (isDeadlinePassed()) {
            return "Error: The deadline for submitting/editing constraints has passed.";
        }
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {return "Error: Employee not found.";}

        emp.addConstraint(constraint);
        repository.saveEmployee(emp);
        return "Success: Constraint added/updated.";
    }


    // Adds a new employee to the system
    public void addEmployee(Employee emp) {
        if (getEmployeeById(emp.getId()) != null) {
            throw new IllegalArgumentException("Employee ID already exists.");
        }
        repository.saveEmployee(emp);
    }

    // Finding an employee by ID using Stream
    public Employee getEmployeeById(int id) {
        return repository.getEmployeeById(id);
    }

    // Returns all employees for display purposes
    public List<Employee> getAllEmployees() {
        return repository.getAllEmployees();
    }

    // Configures the dynamic scheduling lock deadline rule parameter
    public void setConstraintDeadline(LocalDateTime deadline) {
        this.constraintDeadline = deadline;
    }

    // Evaluates the deadline system timer status bounds
    public boolean isDeadlinePassed() {
        if (constraintDeadline == null) return false; // If no deadline set, it's open
        return LocalDateTime.now().isAfter(constraintDeadline);
    }

    //Updates an existing constraint by removing the old one and adding the new entry.
    public String editConstraint(int employeeId, LocalDate date, Constraint newConstraint) {
        // Enforce deadline check for editing as well
        if (isDeadlinePassed()) {
            return "Error: The deadline for editing constraints has passed.";
        }

        Employee emp = getEmployeeById(employeeId);
        if (emp == null) return "Error: Employee not found.";

        repository.removeConstraint(employeeId, date);
        emp.removeConstraintByDate(date);

        emp.addConstraint(newConstraint);
        repository.saveEmployee(emp);

        return "Success: Constraint updated for " + date;
    }

    //Transitions an employee from the active roster to the fired archive.
    public String fireEmployee(int id) {
        // 1. Find the employee in the active list
        Employee empToFire = getEmployeeById(id);

        if (empToFire == null) {
            return "Error: Active employee with ID " + id + " not found.";
        }

        // 2. Remove from active list and add to fired list
        empToFire.setActive(false);
        repository.saveEmployee(empToFire);

        return "Success: Employee " + empToFire.getName() + " (ID: " + id + ") has been moved to fired records.";
    }

    // Fetches all soft-deleted/archived employee rows matching inactive criteria
    public List<Employee> getFiredEmployees() {
        // REFACTORED: Directly fetch filtered fired records from the repository layer
        return repository.getFiredEmployees();
    }

    // Validates if an employee holds the target license level matching active transport sequences
    public boolean checkLicence(int empID) {
        Employee emp = getEmployeeById(empID);
        // Ensure the polymorphic type instance matches the Driver specialized subclass mapping
        if (emp instanceof Driver) {
            Driver driver = (Driver) emp;
            return driver.getLicense().equals(TransportationMock.getRequiredLicenseForTransport( TransportationMock.getTransportId()));
        }
        return false;
    }
    // Filters and returns all registered records possessing a valid Driver type subclass
    public List<Driver> getAllDrivers() {
        // Utilizes standard stream filter casts to compile specialized rosters safely
        return repository.getAllEmployees().stream()
                .filter(e -> e instanceof Driver)
                .map(e -> (Driver) e)
                .collect(Collectors.toList());
    }
}