package mx.edu.utez.judicial.oficialia.dao;

/**
 * A court cannot take the official number because another court already has it. Thrown by
 * {@link CatalogDao#insertCourt} so a registration that raced with another one is reported like any other
 * duplicate.
 */
public class DuplicateNumberException extends DataAccessException {

    public DuplicateNumberException(int number, Throwable cause) {
        super("The court number " + number + " already belongs to another court", cause);
    }
}
