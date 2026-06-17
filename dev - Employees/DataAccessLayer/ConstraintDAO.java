package DataAccessLayer;

import DTO.ConstraintDTO;
import java.time.LocalDate;
import java.util.List;

public interface ConstraintDAO {
    void insertConstraint(ConstraintDTO dto);
    void deleteConstraint(int employeeId, LocalDate date);
    List<ConstraintDTO> getConstraintsByEmployee(int employeeId);
    List<ConstraintDTO> getConstraintsByDate(LocalDate date);
}