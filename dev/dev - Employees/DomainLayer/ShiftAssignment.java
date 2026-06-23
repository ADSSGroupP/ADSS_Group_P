package DomainLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain component managing the tracking and orchestration of active personnel assignments
 * and logistics/transportation driver scheduling within a specific work shift.
 */
public class ShiftAssignment {
    private Map<Employee,Role> shift_roles;
    private Map<Integer,Driver> shift_transportation;

    // Constructor to initialize empty assignment and transportation trackers
    public ShiftAssignment() {
        this.shift_roles = new HashMap<>();
        this.shift_transportation = new HashMap<>();
    }
    // Registers a specific employee to a professional role within the shift roster
    public void assign(Employee e, Role role) {
        this.shift_roles.put(e, role);
    }

    // Evicts and removes a specific employee from the active shift roster
    public void remove(Employee e) {
        this.shift_roles.remove(e);
    }


    // Checks if a specific national identifier is already logged into the active roster
    public boolean isEmployeeAssigned(int employeeId) {
        return shift_roles.keySet().stream()
                .anyMatch(e -> e.getId() == employeeId);
    }

    // Tallies the total number of personnel currently assigned to a specific role type
    public long countRoleAssignments(Role role) {
        return shift_roles.values().stream()
                .filter(r -> r.equals(role))
                .count();
    }
    // Links a specific driver to an operational delivery or transportation dispatch ID
    public void assignDriverToTransport(int transportId, Driver driver) {
        this.shift_transportation.put(transportId, driver);
    }

    //Getters
    public Map<Employee, Role> getAssignments() {
        return shift_roles;
    }
    public Map<Integer, Driver> getTransportationAssignments() {
        return shift_transportation;
    }
}
