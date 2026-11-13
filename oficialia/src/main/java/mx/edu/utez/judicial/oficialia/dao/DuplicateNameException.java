package mx.edu.utez.judicial.oficialia.dao;

/**
 * A catalog record cannot take the name because another record of the same catalog already has it.
 * Thrown by {@link CatalogDao} writes so a write that raced with another one is reported like any
 * other duplicate.
 */
public class DuplicateNameException extends DataAccessException {

    public DuplicateNameException(String name, Throwable cause) {
        super("The name " + name + " already belongs to another record", cause);
    }
}
