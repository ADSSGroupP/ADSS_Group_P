package devEmployees.DomainLayer;

/**
 * Represents the system administrator with high-level privileges.
 * Handles authentication for the Personnel Management module.
 */

public class PersonnelManager {
    private String username;
    private String password;
    private int branch_id;

    public PersonnelManager(int branch_id) {
        this.username = "admin";
        this.password = "6789";
        this.branch_id=branch_id;
    }

    //Verifies if the provided password matches the manager's password.
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    //Getters and Setters
    public void setPassword(String oldPassword, String newPassword) {
        if (authenticate(oldPassword)) {
            this.password = newPassword;
        }
    }

    public String getUsername() {
        return username;
    }
}
