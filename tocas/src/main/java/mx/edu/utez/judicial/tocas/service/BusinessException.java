package mx.edu.utez.judicial.tocas.service;

/** A business rule was broken; the message is safe to show to the user. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
