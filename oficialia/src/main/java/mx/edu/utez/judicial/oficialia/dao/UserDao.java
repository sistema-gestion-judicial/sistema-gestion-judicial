package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserSummary;

/**
 * Access to the user accounts: {@link JdbcUserDao} with a database, {@link InMemoryUserDao} without one.
 * Services get it from {@link OficialiaDaos#users()}.
 */
public interface UserDao {

    Optional<UserAccount> findByEmail(String email);

    /** The users of a court, ordered by name. */
    List<UserSummary> findByCourt(String courtName);

    /**
     * Creates the account; the e-mail is stored normalized.
     *
     * @throws DuplicateEmailException when another account already has that e-mail
     */
    void insert(NewUserAccount account);

    /** Replaces the stored password hash of an account; does nothing when the email is unknown. */
    void updatePasswordHash(String email, String passwordHash);
}
