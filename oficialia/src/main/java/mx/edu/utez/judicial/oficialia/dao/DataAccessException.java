package mx.edu.utez.judicial.oficialia.dao;

/**
 * A database operation failed. Wraps SQLException so services do not depend on JDBC.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
