package mx.edu.utez.judicial.oficialia.exception;

/**
 * The action breaks a rule of the DFR, for example ruling a request that was already ruled.
 * The message is shown to the user as is.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
