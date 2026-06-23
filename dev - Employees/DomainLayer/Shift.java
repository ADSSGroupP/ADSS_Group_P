package DomainLayer;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.lang.String;


/**
 * Represents a single work shift in a specific branch.
 * Manages the assignment of employees to roles for a specific date and time.
 */
public class Shift {
    LocalDate date;
    private char type; // "m" (morning) or "e" (evening)
    private Employee shift_manager;

    private StaffingRequirement staffingRequirement;
    private ShiftAssignment shiftAssignment;

    private Map<Employee, Integer> extra_hours_assignments = new HashMap<>(); // map to store extra hours specifically
    private int branch_id;


// Constructor to initialize a scheduled shift with its structural components
    public Shift (LocalDate date, char type, Employee shift_manager,int branch_id){
        this.date=date;
        this.type=type;
        this.shift_manager = shift_manager;
        this.staffingRequirement = new StaffingRequirement();
        this.shiftAssignment = new ShiftAssignment();
        this.branch_id=branch_id;
    }


    // Generates a descriptive string visualization of the shift metadata and roster assignments
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Shift Details ===\n"));
        sb.append(String.format("Date: %s | Type: %s | Branch ID: %d\n",
                date, (type == 'm' ? "Morning" : "Evening"), branch_id));
        sb.append(String.format("Manager: %s\n", (shift_manager != null ? shift_manager.getName() : "None")));

        sb.append("Assignments: ");
        Map<Employee, Role> roles = shiftAssignment.getAssignments();
        if (roles.isEmpty()) {
            sb.append("No assignments yet.");
        } else {
            roles.forEach((emp, role) -> {
                sb.append(String.format("[%s: %s] ", role, emp.getName()));
            });
        }

        if (!extra_hours_assignments.isEmpty()) {
            sb.append("\nExtra Hours: ");
            extra_hours_assignments.forEach((emp, hours) -> {
                sb.append(String.format("[%s: +%d hrs] ", emp.getName(), hours));
            });
        }

        return sb.toString();
    }

    //Getters and Setters
    public void setShift_model(Role role, int amount) {
        this.staffingRequirement.setRequirement(role, amount);
    }
    public void setShift_roles(Employee e, Role role) {
        this.shiftAssignment.assign(e, role);
    }
    public Map<Employee,Role> getShift_roles(){
        return this.shiftAssignment.getAssignments();
    }
    public Map<Role,Integer> getShift_model(){
        return this.staffingRequirement.getModel();
    }
    public StaffingRequirement getStaffingRequirement(){
        return this.staffingRequirement;
    }
    public Employee getShift_manager(){
        return this.shift_manager;
    }
    public LocalDate getDate(){
        return this.date;
    }
    public char getType(){
        return this.type;
    }
    public ShiftAssignment getShiftAssignment(){
        return this.shiftAssignment;
    }
    public Map<Employee, Integer> getExtraHoursAssignments() {
        return this.extra_hours_assignments;
    }

    // Records an allocated overtime allotment linked to a working employee
    public void addExtraHoursAssignment(Employee e, int hours) {
        this.extra_hours_assignments.put(e, hours);
    }

    // Checks if a specific national identifier is already logged into the active roster
    public boolean isEmployeeAssigned(int employeeId) {
        return this.shiftAssignment.isEmployeeAssigned(employeeId);
    }
}
