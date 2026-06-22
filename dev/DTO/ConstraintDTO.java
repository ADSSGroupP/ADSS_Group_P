package DTO;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Data Transfer Object (DTO) representing a scheduling constraint record.
 * Used for flat, structural data serialization and transfer between the database
 * access layer (DAOs) and the inner business domain repository logic.
 */
public class ConstraintDTO {
    public int employeeId;
    public LocalDate date;
    public LocalTime startTime;
    public LocalTime endTime;
    public boolean doubleShift;
    public int extraHours;
    public boolean isChangeable;

    public ConstraintDTO() {}
}
