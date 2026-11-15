package mx.edu.utez.judicial.oficialia.dao;

import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.RecoveryCode;

/**
 * Access to the password recovery codes, one per account. The in-memory version is replaced by a JDBC one when
 * the users table exists; services do not change.
 */
public interface RecoveryCodeDao {

    /** Saves the code of an account, replacing the one it had. */
    void save(RecoveryCode code);

    Optional<RecoveryCode> findByEmail(String email);

    /** Removes the code of an account; does nothing when it has none. */
    void delete(String email);
}
