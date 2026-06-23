package DataAccessLayer;

import DTO.ShiftDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
/**
 * Data Access Object (DAO) interface for managing shift records.
 * Defines the contract for persistence, metadata management, staffing requirement quotas,
 * and employee position assignments inside the database layer.
 */
public interface ShiftDAO {
    // Inserts or replaces a basic shift master record in the database
    void insertShift(ShiftDTO dto);
    // Retrieves a specific shift's core metadata using its composite keys (date and type)
    ShiftDTO getShift(LocalDate date, char type);
    // Retrieves a list of all basic shift records registered in the system
    List<ShiftDTO> getAllShifts();

    // Saves the structured staffing minimum quotas needed for a specific shift type
    void saveShiftRequirements(LocalDate date, char type, Map<String, Integer> requirements);
    // Fetches the defined staffing template requirements for a specific shift date and type
    Map<String, Integer> getShiftRequirements(LocalDate date, char type);
    // Persists the final roster configuration, coupling workers to their roles and recording any overtime
    void saveShiftAssignments(LocalDate date, char type, Map<Integer, String> assignments, Map<Integer, Integer> extraHours);
    // Returns a Map pairing employee IDs with their scheduled role names for a specific shift
    Map<Integer, String> getShiftAssignments(LocalDate date, char type);
    // Returns a Map tracking the specific overtime hours allocated to each assigned employee ID
    Map<Integer, Integer> getShiftExtraHours(LocalDate date, char type);
}


