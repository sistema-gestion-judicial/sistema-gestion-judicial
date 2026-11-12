package mx.edu.utez.judicial.oficialia.exception;

/**
 * The requested record does not exist. Controllers answer it with a 404 page.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
