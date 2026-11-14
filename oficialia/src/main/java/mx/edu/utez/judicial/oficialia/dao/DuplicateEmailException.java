package mx.edu.utez.judicial.oficialia.dao;

/**
 * Another account already uses the e-mail. Thrown by {@link UserDao#insert} so a registration that raced
 * with another one is reported like any other duplicate.
 */
public class DuplicateEmailException extends DataAccessException {

    public DuplicateEmailException(String email, Throwable cause) {
        super("The e-mail " + email + " is already registered", cause);
    }
}
