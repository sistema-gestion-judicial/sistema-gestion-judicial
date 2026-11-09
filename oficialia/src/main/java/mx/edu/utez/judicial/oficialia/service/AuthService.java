package mx.edu.utez.judicial.oficialia.service;

import java.time.Duration;
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
    private static final long SECONDS_PER_MINUTE = 60;

    private final UserDao userDao;
    private final LoginAttemptTracker attemptTracker;

    public AuthService() {
        this(InMemoryUserDao.getInstance(), LoginAttemptTracker.getInstance());
    }

    public AuthService(UserDao userDao, LoginAttemptTracker attemptTracker) {
        this.userDao = userDao;
        this.attemptTracker = attemptTracker;
    }

    public SignInResult signIn(String email, String password) {
        if (email == null || password == null) {
            return SignInResult.invalidCredentials();
        }
        Optional<Duration> lock = attemptTracker.remainingLock(email);
        if (lock.isPresent()) {
            return locked(lock.get());
        }

        Optional<UserAccount> account = userDao.findByEmail(email);
        String storedHash = account.map(UserAccount::passwordHash).orElse(PLACEHOLDER_HASH);
        boolean passwordMatches = PasswordHasher.matches(password, storedHash);

        if (account.isEmpty() || !passwordMatches) {
            attemptTracker.recordFailure(email);
            return attemptTracker.remainingLock(email)
                    .map(AuthService::locked)
                    .orElseGet(SignInResult::invalidCredentials);
        }

        attemptTracker.reset(email);
        // Checked only after the password, so nobody can learn an account's status without knowing it
        if (!account.get().active()) {
            return SignInResult.inactiveAccount();
        }
        return SignInResult.signedIn(account.get().user());
    }

    /** Tells whether the account still exists and can keep using the system. */
    public boolean isActive(String email) {
        return userDao.findByEmail(email).map(UserAccount::active).orElse(false);
    }

    private static SignInResult locked(Duration remaining) {
        // Rounded up, so the message never says 0 minutes while the lock is still on
        return SignInResult.locked(Math.ceilDiv(remaining.toSeconds(), SECONDS_PER_MINUTE));
    }
}
