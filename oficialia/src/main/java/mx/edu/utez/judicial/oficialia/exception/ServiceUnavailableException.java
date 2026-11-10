package mx.edu.utez.judicial.oficialia.exception;

/**
 * Another system did not answer. The caller decides how to continue without it.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
