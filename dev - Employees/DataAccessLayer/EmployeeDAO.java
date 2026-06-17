package DataAccessLayer;

import DTO.EmployeeDTO;
import java.util.List;

public interface EmployeeDAO {
    void insertEmployee(EmployeeDTO dto);
    void updateEmployee(EmployeeDTO dto);
    EmployeeDTO getEmployeeById(int id);
    List<EmployeeDTO> getAllEmployees();
    void deleteEmployee(int id); // Can be used for archiving/hard delete
}