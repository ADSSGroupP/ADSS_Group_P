package DomainLayer;

import java.util.HashMap;
import java.util.Map;


public class StaffingRequirement {
    private Map<Role, Integer> shift_model;

    public StaffingRequirement() {
        this.shift_model = new HashMap<>();
        this.shift_model.put(Role.CASHIER, 2); //Default
        this.shift_model.put(Role.STOREKEEPER, 2); //Default
        this.shift_model.put(Role.SHIFTMANAGER, 1);
    }

    public void setRequirement(Role role, int amount) {
        this.shift_model.put(role, amount);
    }

    public Map<Role, Integer> getModel() {
        return shift_model;
    }

    public int getRequiredAmount(String role) {
        return shift_model.getOrDefault(role, 0);
    }
}



