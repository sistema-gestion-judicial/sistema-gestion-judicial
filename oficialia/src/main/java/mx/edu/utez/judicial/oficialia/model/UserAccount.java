package mx.edu.utez.judicial.oficialia.model;

/**
 * A user together with the stored password hash and account status. The hash stays here,
 * out of the {@link User} that is kept in the HTTP session.
 *
 * @param active whether the account can be used: the user and their court are both active
 * @param courtActive whether the user's court is active, to tell why an account cannot be used (DFR 2.12)
 */
public record UserAccount(User user, String passwordHash, boolean active, boolean courtActive) {

    /** An account whose court is active, or that has no court. */
    public UserAccount(User user, String passwordHash, boolean active) {
        this(user, passwordHash, active, true);
    }

    /** Leaves the hash out: a record prints every field by default, so it could reach a log. */
    @Override
    public String toString() {
        return "UserAccount[email=" + user.getEmail() + ", active=" + active + ", courtActive=" + courtActive + "]";
    }
}
