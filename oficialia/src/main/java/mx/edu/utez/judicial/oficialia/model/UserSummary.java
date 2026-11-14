package mx.edu.utez.judicial.oficialia.model;

/**
 * A row of the users listing of a court: who they are and whether they can sign in.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class UserSummary {

    private final String fullName;
    private final String email;
    private final String phone;
    private final UserRole role;
    private final boolean active;

    public UserSummary(String fullName, String email, String phone, UserRole role, boolean active) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    /** Null when the user has no phone number. */
    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleLabel() {
        return role.getLabel();
    }

    public boolean isActive() {
        return active;
    }
}
