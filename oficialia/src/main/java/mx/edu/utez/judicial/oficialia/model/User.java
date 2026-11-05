package mx.edu.utez.judicial.oficialia.model;

/**
 * Person who signs in to the system. Stored in the HTTP session after login.
 */
public class User {

    private final String email;
    private final String fullName;
    private final String roleLabel;
    private final String courtName;

    public User(String email, String fullName, String roleLabel, String courtName) {
        this.email = email;
        this.fullName = fullName;
        this.roleLabel = roleLabel;
        this.courtName = courtName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public String getCourtName() {
        return courtName;
    }
}
