package DomainLayer;

import java.util.HashMap;
import java.util.Map;

public class ShiftAssignment {
    private Map<Employee,Role> shift_roles;

    public ShiftAssignment() {
        this.shift_roles = new HashMap<>();
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

}
