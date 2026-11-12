package mx.edu.utez.judicial.common.service;

/** The requested record does not exist or does not belong to the current user. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
