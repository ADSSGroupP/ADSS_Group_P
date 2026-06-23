package DataAccessLayer;

import DTO.EmployeeDTO;
import java.util.List;
/**
 * Data Access Object (DAO) interface for managing employee profiles.
 * Defines the contract for CRUD operations required to persist, update,
 * and retrieve employee information from the database layer.
 */
public interface EmployeeDAO {
    // Inserts a new employee record into the database
    void insertEmployee(EmployeeDTO dto);
    // Updates existing employee details such as roles, status, or job terms
    void updateEmployee(EmployeeDTO dto);
    // Retrieves a single employee's data transfer object by their unique ID
    EmployeeDTO getEmployeeById(int id);
    // Retrieves a list of all employees registered in the database
    List<EmployeeDTO> getAllEmployees();
    // Deletes or archives an employee record from the database using their ID
    void deleteEmployee(int id); // Can be used for archiving/hard delete
}