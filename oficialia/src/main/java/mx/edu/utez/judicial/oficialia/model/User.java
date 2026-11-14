package mx.edu.utez.judicial.oficialia.model;

/**
 * Person who signs in to the system. Stored in the HTTP session after login.
 */
public class User {

    private final String email;
    private final String fullName;
    private final UserRole role;
    private final String courtName;
    private final boolean mustChangePassword;

    public User(String email, String fullName, UserRole role, String courtName) {
        this(email, fullName, role, courtName, false);
    }

    public User(String email, String fullName, UserRole role, String courtName, boolean mustChangePassword) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.courtName = courtName;
        this.mustChangePassword = mustChangePassword;
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

    /** Only the Administrador de Juzgado registers the users of the court (RF 1.4). */
    public boolean isCourtAdmin() {
        return role == UserRole.COURT_ADMIN;
    }

    /** True until the user replaces the temporary password they received when they were registered. */
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    /** The same user once the temporary password has been replaced. */
    public User withPasswordChanged() {
        return mustChangePassword ? new User(email, fullName, role, courtName, false) : this;
    }
}
