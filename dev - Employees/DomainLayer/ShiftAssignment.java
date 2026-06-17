package DomainLayer;

import java.util.HashMap;
import java.util.Map;

public class ShiftAssignment {
    private Map<Employee,Role> shift_roles;
    private Map<Integer,Driver> shift_transportation;

    public ShiftAssignment() {
        this.shift_roles = new HashMap<>();
        this.shift_transportation = new HashMap<>();
    }
    public void assign(Employee e, Role role) {
        this.shift_roles.put(e, role);
    }

    public void remove(Employee e) {
        this.shift_roles.remove(e);
    }

    public Map<Employee, Role> getAssignments() {
        return shift_roles;
    }

    public boolean isEmployeeAssigned(int employeeId) {
        return shift_roles.keySet().stream()
                .anyMatch(e -> e.getId() == employeeId);
    }

    public long countRoleAssignments(Role role) {
        return shift_roles.values().stream()
                .filter(r -> r.equals(role))
                .count();
    }
    public void assignDriverToTransport(int transportId, Driver driver) {
        this.shift_transportation.put(transportId, driver);
    }

    public Map<Integer, Driver> getTransportationAssignments() {
        return shift_transportation;
    }
}
