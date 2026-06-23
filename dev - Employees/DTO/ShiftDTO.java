package DTO;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a work shift record.
 * Used to transfer relational shift metadata, tracking timestamps, and managerial assignments
 * between the persistent storage tier (DAOs) and the internal domain repository logic.
 */
public class ShiftDTO {
    public LocalDate date;
    public char type; // 'm' or 'e'
    public int managerId;
    public int branchId;

    public ShiftDTO() {}
}
