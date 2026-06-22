package DomainLayer;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Domain entity representing an availability constraint submitted by an employee.
 * Encapsulates specific date and time blocks where the employee is unavailable,
 * alongside scheduling preferences like overtime limits, double-shift approvals,
 * and constraint flexibility levels (hard vs soft blocks).
 */
public class Constraint {
    private int employee_ID;
    LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Boolean double_shift;
    private int extra_hours;
    private boolean isChangeable;

    // Constructor to initialize a fully defined employee availability constraint
    public Constraint(int ID, LocalDate date, LocalTime start, LocalTime end, boolean double_shift, int extra_hours, boolean isChangeable){
        this.employee_ID = ID;
        this.date = date;
        this.start = start;
        this.end = end;
        this.double_shift = double_shift;
        this.extra_hours = extra_hours;
        this.isChangeable = isChangeable;

    }


    // Evaluates if a given shift's timeframe overlaps with the locked constraint window
    public boolean overlapsWith(LocalTime shiftStart, LocalTime shiftEnd) {
        // A constraint overlaps if:
        // The constraint starts before the shift ends AND ends after the shift starts.
        return this.start.isBefore(shiftEnd) && this.end.isAfter(shiftStart);
    }

    // Checks if this specific constraint overlaps with the default morning shift hours (06:00 - 14:00)
    public boolean blocksMorningShift() {
        return overlapsWith(LocalTime.of(6, 0), LocalTime.of(14, 0));
    }

    // Checks if this specific constraint overlaps with the default evening shift hours (14:00 - 22:00)
    public boolean blocksEveningShift() {
        return overlapsWith(LocalTime.of(14, 0), LocalTime.of(22, 0));
    }

    //Getters and Setters
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
    public int getExtraHours() {return this.extra_hours; }
    public boolean isChangeable() {return isChangeable;}
    public boolean isDoubleShiftApproved() {return double_shift;}
}


