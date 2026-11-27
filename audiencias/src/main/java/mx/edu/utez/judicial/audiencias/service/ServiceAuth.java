package mx.edu.utez.judicial.audiencias.service;

import java.util.Locale;
import java.util.Optional;
import mx.edu.utez.judicial.audiencias.dao.user.UserDao;
import mx.edu.utez.judicial.audiencias.model.user.User;
import mx.edu.utez.judicial.audiencias.security.LoginAttemptLimiter;
import mx.edu.utez.judicial.audiencias.security.PasswordHasher;

/** Checks an email and password against the users table. */
public class ServiceAuth {

    public enum Outcome { SUCCESS, INVALID_CREDENTIALS, LOCKED }

    /** The user is set only on SUCCESS; retryAfterSeconds only on LOCKED. */
    public record AuthResult(Outcome outcome, User user, long retryAfterSeconds) {
        static AuthResult success(User user) {
            return new AuthResult(Outcome.SUCCESS, user, 0);
        }

        static AuthResult invalid() {
            return new AuthResult(Outcome.INVALID_CREDENTIALS, null, 0);
        }

        static AuthResult locked(long seconds) {
            return new AuthResult(Outcome.LOCKED, null, seconds);
        }
    }

    private static final int MAX_EMAIL_LENGTH = 150; // users.email VARCHAR(150)

    /**
     * Checked when the email does not exist, so a missing account takes as long to answer as a wrong
     * password and the response time does not reveal which emails are registered.
     */
    private static final String DUMMY_HASH = PasswordHasher.hash("not-a-real-password");

    private final UserDao userDao;
    private final LoginAttemptLimiter limiter;

    public ServiceAuth(UserDao userDao) {
        this(userDao, LoginAttemptLimiter.standard());
    }

    public ServiceAuth(UserDao userDao, LoginAttemptLimiter limiter) {
        this.userDao = userDao;
        this.limiter = limiter;
    }

    public AuthResult authenticate(String email, String password) {
        String key = normalizeEmail(email);
        if (key == null || !PasswordHasher.isUsable(password)) {
            return AuthResult.invalid();
        }

        long lockedFor = limiter.secondsLocked(key);
        if (lockedFor > 0) {
            return AuthResult.locked(lockedFor);
        }

        String storedHash = userDao.authenticate(key);
        Optional<User> user = storedHash == null ? Optional.empty() : userDao.findByEmail(key);
        boolean passwordMatches = PasswordHasher.matches(password, storedHash != null ? storedHash : DUMMY_HASH);
        boolean active = user.map(u -> Boolean.TRUE.equals(u.getStatus())).orElse(false);

        // One answer for "no such account", "wrong password" and "account disabled"
        if (storedHash == null || !passwordMatches || !active) {
            limiter.recordFailure(key);
            return AuthResult.invalid();
        }
        limiter.recordSuccess(key);
        return AuthResult.success(user.get());
    }

    /** Trimmed and lower case, the way emails are stored; null when it cannot be an email. */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.length() > MAX_EMAIL_LENGTH ? null : normalized;
    }
}
