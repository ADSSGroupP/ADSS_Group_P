package DataAccessLayer;

import DTO.ConstraintDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object (DAO) interface for managing employee availability constraints.
 * Defines the abstract CRUD operations required to persist, remove, and query
 * scheduling constraints within the database layer.
 */
public interface ConstraintDAO {
    // Saves a new constraint record into the database
    void insertConstraint(ConstraintDTO dto);
    // Deletes a specific constraint using employee ID and date
    void deleteConstraint(int employeeId, LocalDate date);
    // Retrieves all constraints submitted by a specific employee
    List<ConstraintDTO> getConstraintsByEmployee(int employeeId);
    // Retrieves all employee constraints registered for a specific date
    List<ConstraintDTO> getConstraintsByDate(LocalDate date);
}