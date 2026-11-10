package mx.edu.utez.judicial.oficialia.dao;

import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.UserAccount;

/**
 * Access to the user accounts. TEMPORARY: it must be reviewed when the users table exists (#28);
 * a JDBC version then replaces the in-memory one and services do not change.
 */
public interface UserDao {

    Optional<UserAccount> findByEmail(String email);
}
