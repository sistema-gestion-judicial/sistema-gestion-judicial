package mx.edu.utez.judicial.oficialia.model;

/**
 * A user together with the stored password hash. The hash stays here, out of the
 * {@link User} that is kept in the HTTP session.
 */
public record UserAccount(User user, String passwordHash) {
}
