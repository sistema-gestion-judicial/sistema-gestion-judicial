package mx.edu.utez.judicial.oficialia.dao;

/**
 * A catalog record cannot take the code because another record of the same catalog already has it.
 * Thrown by {@link CatalogDao} inserts so an insert that raced with another one is reported like any
 * other duplicate.
 */
public class DuplicateCodeException extends DataAccessException {

    public DuplicateCodeException(String code, Throwable cause) {
        super("The code " + code + " already belongs to another record", cause);
    }
}
