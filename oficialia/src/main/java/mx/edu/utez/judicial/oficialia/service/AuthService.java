package mx.edu.utez.judicial.oficialia.service;

import java.util.Optional;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;

/**
 * Checks credentials against the stored password hashes.
 */
public class AuthService {

    /** Checked when the email is unknown, so that case takes as long as a real password check. */
    private static final String PLACEHOLDER_HASH = PasswordHasher.hash("placeholder");

    private final UserDao userDao;

    public AuthService() {
        this(InMemoryUserDao.getInstance());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        Optional<UserAccount> account = userDao.findByEmail(email);
        String storedHash = account.map(UserAccount::passwordHash).orElse(PLACEHOLDER_HASH);
        boolean passwordMatches = PasswordHasher.matches(password, storedHash);

        if (account.isEmpty() || !passwordMatches) {
            return Optional.empty();
        }
        return Optional.of(account.get().user());
    }
}
