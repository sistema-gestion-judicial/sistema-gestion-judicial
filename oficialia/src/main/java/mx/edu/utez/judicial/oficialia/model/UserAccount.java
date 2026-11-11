package mx.edu.utez.judicial.oficialia.model;

/**
 * A user together with the stored password hash and account status. The hash stays here,
 * out of the {@link User} that is kept in the HTTP session.
 */
public record UserAccount(User user, String passwordHash, boolean active) {

    /** Leaves the hash out: a record prints every field by default, so it could reach a log. */
    @Override
    public String toString() {
        return "UserAccount[email=" + user.getEmail() + ", active=" + active + "]";
    }
}
