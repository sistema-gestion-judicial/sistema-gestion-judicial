package mx.edu.utez.judicial.oficialia.model;

/**
 * Person who signs in to the system. Stored in the HTTP session after login.
 */
public class User {

    private final Long id;
    private final String email;
    private final String fullName;
    private final UserRole role;
    private final String courtName;

    /** A user without a database id: the in-memory demo accounts and the tests. */
    public User(String email, String fullName, UserRole role, String courtName) {
        this(null, email, fullName, role, courtName);
    }

    public User(Long id, String email, String fullName, UserRole role, String courtName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.courtName = courtName;
    }

    /** The users.id of the account, or null when there is no database. */
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleLabel() {
        return role.getLabel();
    }

    public String getCourtName() {
        return courtName;
    }

    /** Only the Oficial de Partes rules demand requests (RF 3.5). */
    public boolean isClerk() {
        return role == UserRole.CLERK;
    }
}
