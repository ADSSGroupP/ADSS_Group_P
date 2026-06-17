package DataAccessLayer;

import DTO.ShiftDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

    public interface ShiftDAO {
        void insertShift(ShiftDTO dto);
        ShiftDTO getShift(LocalDate date, char type);
        List<ShiftDTO> getAllShifts();

        // Requirements and Assignments handling
        void saveShiftRequirements(LocalDate date, char type, Map<String, Integer> requirements);
        Map<String, Integer> getShiftRequirements(LocalDate date, char type);

        void saveShiftAssignments(LocalDate date, char type, Map<Integer, String> assignments, Map<Integer, Integer> extraHours);
        // Returns Map of <EmployeeID, RoleName>
        Map<Integer, String> getShiftAssignments(LocalDate date, char type);
        // Returns Map of <EmployeeID, ExtraHours>
        Map<Integer, Integer> getShiftExtraHours(LocalDate date, char type);
    }


