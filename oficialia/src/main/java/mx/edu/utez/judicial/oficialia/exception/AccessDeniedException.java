package mx.edu.utez.judicial.oficialia.exception;

/**
 * The record exists but belongs to another court, or the user's profile cannot do the action.
 * Controllers answer it with a 403 page.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
