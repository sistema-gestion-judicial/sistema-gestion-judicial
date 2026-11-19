package mx.edu.utez.judicial.tocas.service;

/** The requested record does not exist or does not belong to the current user. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
