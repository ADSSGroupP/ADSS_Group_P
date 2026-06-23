package DomainLayer;

import java.time.LocalDate;
import java.util.List;
/**
 * Centralized Repository interface for data persistence abstraction in the HR system.
 * Defines the contract for CRUD operations across Employees, Drivers, Constraints, and Shifts,
 * ensuring decoupled access between the domain layer and data access objects.
 */
public interface HRRepository {
    // Employee & Driver Operations
    void saveEmployee(Employee emp);
    Employee getEmployeeById(int id);
    List<Employee> getAllEmployees();
    void deleteEmployee(int id);

    // Constraint Operations
    void saveConstraint(Constraint constraint);
    void removeConstraint(int employeeId, LocalDate date);

    // Shift Operations
    void saveShift(Shift shift);
    Shift getShiftByDateAndType(LocalDate date, char type);
    List<Shift> getShiftHistory();
    List<Employee> getFiredEmployees(); // NEW METHOD SIGNATURE: To retrieve only archived/fired employees


}