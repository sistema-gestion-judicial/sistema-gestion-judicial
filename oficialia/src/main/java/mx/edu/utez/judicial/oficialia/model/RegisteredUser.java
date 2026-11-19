package mx.edu.utez.judicial.oficialia.model;

/**
 * A user that was just registered, with the temporary password the administrator must hand over.
 * The password exists only here and in the confirmation shown once: the database keeps just its hash.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class RegisteredUser {

    private final String fullName;
    private final String email;
    private final String temporaryPassword;

    public RegisteredUser(String fullName, String email, String temporaryPassword) {
        this.fullName = fullName;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    /** Leaves the password out, so it cannot reach a log by accident. */
    @Override
    public String toString() {
        return "RegisteredUser[email=" + email + "]";
    }
}
