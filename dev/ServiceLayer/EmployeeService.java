package ServiceLayer;

import java.util.ArrayList;
import java.util.List;
import DomainLayer.*;
import Transportation.TransportationMock;

import java.time.LocalDateTime;
import java.time.LocalDate;

import static Transportation.TransportationMock.getTransportId;
import java.util.stream.Collectors;
/**
 * Service class responsible for managing employee-related operations.
 * Handles employee registration, archival (firing), and constraint submission deadlines.
 */
public class EmployeeService {
    // Memory-based list of all employees (including fired ones)
    private List<Employee> employees;
    private List<Employee> firedEmployees;
    private LocalDateTime constraintDeadline;

    public EmployeeService() {
        this.employees = new ArrayList<>();
        this.firedEmployees = new ArrayList<>();
    }
    /**
     * Registers a new work constraint for an employee.
     * @return Success message or an Error message if the deadline has passed or employee not found.
     */
    public String insertConstraint(int employeeId, Constraint constraint) {
        if (isDeadlinePassed()) {
            return "Error: The deadline for submitting/editing constraints has passed.";
        }
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) {return "Error: Employee not found.";}

        emp.addConstraint(constraint);
        return "Success: Constraint added/updated.";
    }


    // Adds a new employee to the system
    public void addEmployee(Employee emp) {
        if (getEmployeeById(emp.getId()) != null) {
            throw new IllegalArgumentException("Employee ID already exists.");
        }
        employees.add(emp);
    }

    // Finding an employee by ID using Stream
    public Employee getEmployeeById(int id) {
        return employees.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    // Returns all employees for display purposes
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public void setConstraintDeadline(LocalDateTime deadline) {
        this.constraintDeadline = deadline;
    }

    public boolean isDeadlinePassed() {
        if (constraintDeadline == null) return false; // If no deadline set, it's open
        return LocalDateTime.now().isAfter(constraintDeadline);
    }

    //Updates an existing constraint by removing the old one and adding the new entry.
    public String editConstraint(int employeeId, LocalDate date, Constraint newConstraint) {
        // CHANGE: Enforce deadline check for editing as well
        if (isDeadlinePassed()) {
            return "Error: The deadline for editing constraints has passed.";
        }

        Employee emp = getEmployeeById(employeeId);
        if (emp == null) return "Error: Employee not found.";

        emp.removeConstraintByDate(date);
        emp.addConstraint(newConstraint);

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
        employees.remove(empToFire);
        firedEmployees.add(empToFire);

        return "Success: Employee " + empToFire.getName() + " (ID: " + id + ") has been moved to fired records.";
    }

    public List<Employee> getFiredEmployees() {
        return new ArrayList<>(firedEmployees);
    }

    public boolean checkLicence(int empID) {
        Employee emp = getEmployeeById(empID);
        if (emp instanceof Driver) {
            Driver driver = (Driver) emp;
            return driver.getLicense().equals(TransportationMock.getRequiredLicenseForTransport( TransportationMock.getTransportId()));
        }
        return false;
    }

    public List<Driver> getAllDrivers() {
        return employees.stream()
                .filter(e -> e instanceof Driver)
                .map(e -> (Driver) e)
                .collect(Collectors.toList());
    }
}