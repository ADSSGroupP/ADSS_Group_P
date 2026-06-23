package devEmployees.DomainLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain component that encapsulates and handles the structural staffing model for a shift.
 * Defines the corporate baseline configuration (default targets) and holds the required
 * quantities of specific personnel roles needed to safely operate a shift.
 */
public class StaffingRequirement {
    private Map<Role, Integer> shift_model;

    // Constructor to initialize the shift staffing structure with factory-default corporate targets
    public StaffingRequirement() {
        this.shift_model = new HashMap<>();
        this.shift_model.put(Role.CASHIER, 2); //Default
        this.shift_model.put(Role.STOREKEEPER, 2); //Default
        this.shift_model.put(Role.SHIFTMANAGER, 1);
    }

    //Getters and Setters
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



