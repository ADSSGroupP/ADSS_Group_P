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


    //constructor
    public Shift (LocalDate date, char type, Employee shift_manager,int branch_id){
        this.date=date;
        this.type=type;
        this.shift_manager = shift_manager;
        this.staffingRequirement = new StaffingRequirement();
        this.shiftAssignment = new ShiftAssignment();
        this.branch_id=branch_id;
    }


    /**
     * Generates a string representation of the shift, including the manager
     * and a list of all current employee assignments.
     */
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



    public void addExtraHoursAssignment(Employee e, int hours) {
        // CHANGE: Method to record extra hours assignment
        this.extra_hours_assignments.put(e, hours);
    }

    public Map<Employee, Integer> getExtraHoursAssignments() {
        // CHANGE: Getter for displaying extra hours later
        return this.extra_hours_assignments;
    }

    /**
     * Validation method to check if an employee is already scheduled for this shift.
     * Prevents double-booking an employee in multiple roles within the same shift.
     * * @param employeeId The ID of the employee to check.
     * @return true if the employee is already assigned, false otherwise.
     */
    public boolean isEmployeeAssigned(int employeeId) {
        return this.shiftAssignment.isEmployeeAssigned(employeeId);
    }
}
