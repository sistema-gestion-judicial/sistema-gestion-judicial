package mx.edu.utez.judicial.oficialia.model;

/**
 * The data needed to create a row in the users table. The court is null only for the judicial body
 * administrator, who oversees every court (V005 enforces it). The phone is optional.
 */
public record NewUserAccount(String firstName, String lastName, String email, String phone, UserRole role,
                             String courtCode, String passwordHash, boolean mustChangePassword, boolean active) {

    /** An account without a phone number. */
    public NewUserAccount(String firstName, String lastName, String email, UserRole role, String courtCode,
                          String passwordHash, boolean mustChangePassword, boolean active) {
        this(firstName, lastName, email, null, role, courtCode, passwordHash, mustChangePassword, active);
    }

    /** Leaves the hash out: a record prints every field by default, so it could reach a log. */
    @Override
    public String toString() {
        return "NewUserAccount[email=" + email + ", role=" + role + ", court=" + courtCode + "]";
    }
}
