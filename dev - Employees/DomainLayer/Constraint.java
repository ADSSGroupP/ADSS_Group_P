package DomainLayer;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a work constraint submitted by an employee.
 * It defines time slots where the employee is unavailable or has special conditions.
 */
public class Constraint {
    private int employee_ID;
    LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Boolean double_shift;
    private int extra_hours;
    private boolean isChangeable;


    public Constraint(int ID, LocalDate date, LocalTime start, LocalTime end, boolean double_shift, int extra_hours, boolean isChangeable){
        this.employee_ID = ID;
        this.date = date;
        this.start = start;
        this.end = end;
        this.double_shift = double_shift;
        this.extra_hours = extra_hours;
        this.isChangeable = isChangeable;

    }


    /**
     * Determines if a given shift type conflicts with this constraint.
     * @return true if the shift timing overlaps with the constraint hours.
     */
    public boolean overlapsWith(LocalTime shiftStart, LocalTime shiftEnd) {
        // A constraint overlaps if:
        // The constraint starts before the shift ends AND ends after the shift starts.
        return this.start.isBefore(shiftEnd) && this.end.isAfter(shiftStart);
    }

    /**
     * Specific helper for the two shift types defined in the system.
     */
    public boolean blocksMorningShift() {
        return overlapsWith(LocalTime.of(6, 0), LocalTime.of(14, 0));
    }

    public boolean blocksEveningShift() {
        return overlapsWith(LocalTime.of(14, 0), LocalTime.of(22, 0));
    }

    public void set_hours(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
    }

    public LocalTime getStartTime(){
        return this.start;
    }

    public LocalTime getEndTime(){
        return this.end;
    }
    public void set_double_shift(){
        this.double_shift = !double_shift;
    }

    public void set_extra_hours(int extra_hours){
        this.extra_hours = extra_hours;
    }
    public LocalDate getDate(){
        return this.date;
    }

    public int getExtraHours() {
        return this.extra_hours; // getter to access the extra hours field
    }
    public boolean isChangeable() {
        return isChangeable;
    }
    public boolean isDoubleShiftApproved() {
        return double_shift;
    }

}


