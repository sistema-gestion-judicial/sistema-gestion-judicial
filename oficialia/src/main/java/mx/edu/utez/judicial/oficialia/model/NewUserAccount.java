package mx.edu.utez.judicial.oficialia.model;

/**
 * The data needed to create a row in the users table. The court is null only for the judicial body
 * administrator, who oversees every court (V005 enforces it).
 */
public record NewUserAccount(String firstName, String lastName, String email, UserRole role, String courtCode,
                             String passwordHash, boolean mustChangePassword, boolean active) {

    /** Leaves the hash out: a record prints every field by default, so it could reach a log. */
    @Override
    public String toString() {
        return "NewUserAccount[email=" + email + ", role=" + role + ", court=" + courtCode + "]";
    }
}
