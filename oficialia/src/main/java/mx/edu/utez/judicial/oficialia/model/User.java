package mx.edu.utez.judicial.oficialia.model;

/**
 * Person who signs in to the system. Stored in the HTTP session after login.
 */
public class User {

    /** DEMO (temporary): court the judicial body administrator works in while showing the clerk workflow. */
    static final String DEMO_COURT_NAME = "Juzgado de Cuautla";

    private final String email;
    private final String fullName;
    private final UserRole role;
    private final String courtName;

    public User(String email, String fullName, UserRole role, String courtName) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.courtName = courtName;
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
        // DEMO (temporary): the judicial body administrator works as the Oficial de Partes of Cuautla
        if (courtName == null && role == UserRole.JUDICIAL_BODY_ADMIN) {
            return DEMO_COURT_NAME;
        }
        return courtName;
    }

    /** Only the Oficial de Partes rules demand requests (RF 3.5). */
    public boolean isClerk() {
        // DEMO (temporary): the judicial body administrator can do everything the Oficial de Partes does
        return role == UserRole.CLERK || role == UserRole.JUDICIAL_BODY_ADMIN;
    }
}
