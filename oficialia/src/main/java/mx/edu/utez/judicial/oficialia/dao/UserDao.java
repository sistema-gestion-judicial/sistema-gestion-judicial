package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.model.UserUpdate;

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

    /** The user with that id, with the name of their court; empty when there is none. */
    Optional<UserDetails> findById(long id);

    /**
     * Corrects the name, e-mail, phone and profile of a user and records who did it in the audit trail
     * (RF 1.6). The password, the court and the account status are left as they are. The e-mail is stored
     * normalized. Does nothing when the id is unknown.
     *
     * @param actor the administrator who made the change
     * @throws DuplicateEmailException when another account already has the new e-mail
     */
    void update(UserUpdate update, User actor);
}
