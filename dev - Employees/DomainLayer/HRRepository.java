package DomainLayer;


import java.time.LocalDate;
import java.util.List;

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
}
