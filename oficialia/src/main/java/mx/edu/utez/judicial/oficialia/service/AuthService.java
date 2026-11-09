package mx.edu.utez.judicial.oficialia.service;

import java.util.Optional;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.model.SignInResult;
import mx.edu.utez.judicial.oficialia.model.UserAccount;

/**
 * Checks credentials against the stored password hashes and decides the outcome of a sign in.
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

    public SignInResult signIn(String email, String password) {
        if (email == null || password == null) {
            return SignInResult.invalidCredentials();
        }
        Optional<UserAccount> account = userDao.findByEmail(email);
        String storedHash = account.map(UserAccount::passwordHash).orElse(PLACEHOLDER_HASH);
        boolean passwordMatches = PasswordHasher.matches(password, storedHash);

        if (account.isEmpty() || !passwordMatches) {
            return SignInResult.invalidCredentials();
        }
        // Checked only after the password, so nobody can learn an account's status without knowing it
        if (!account.get().active()) {
            return SignInResult.inactiveAccount();
        }
        return SignInResult.signedIn(account.get().user());
    }
}
