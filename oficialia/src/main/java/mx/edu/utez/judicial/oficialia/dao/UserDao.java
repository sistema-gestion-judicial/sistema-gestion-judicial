package mx.edu.utez.judicial.oficialia.dao;

import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.UserAccount;

/**
 * Access to the user accounts: {@link JdbcUserDao} with a database, {@link InMemoryUserDao} without one.
 * Services get it from {@link OficialiaDaos#users()}.
 */
public interface UserDao {

    Optional<UserAccount> findByEmail(String email);

    /** Replaces the stored password hash of an account; does nothing when the email is unknown. */
    void updatePasswordHash(String email, String passwordHash);
}
