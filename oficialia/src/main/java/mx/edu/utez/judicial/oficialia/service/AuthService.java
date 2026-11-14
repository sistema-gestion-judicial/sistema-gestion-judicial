package mx.edu.utez.judicial.oficialia.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.model.PasswordChangeForm;
import mx.edu.utez.judicial.oficialia.model.SignInResult;
import mx.edu.utez.judicial.oficialia.model.User;
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
        this(OficialiaDaos.users(), LoginAttemptTracker.getInstance());
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

    /**
     * Replaces the temporary password the user received when they were registered (RF 1.4). Nothing changes
     * when the form has errors or the new password is the same one.
     *
     * @return the field errors, empty when the password was changed
     */
    public Map<String, String> changeTemporaryPassword(User user, PasswordChangeForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        Optional<UserAccount> account = userDao.findByEmail(user.getEmail());
        if (account.isEmpty()) {
            errors.put("newPassword", "No se encontró su cuenta.");
        } else if (!errors.containsKey("newPassword")
                && PasswordHasher.matches(form.getNewPassword(), account.get().passwordHash())) {
            errors.put("newPassword", "La nueva contraseña debe ser distinta de la temporal.");
        }
        if (errors.isEmpty()) {
            userDao.updatePasswordHash(user.getEmail(), PasswordHasher.hash(form.getNewPassword()));
        }
        return errors;
    }

    private static SignInResult locked(Duration remaining) {
        // Rounded up, so the message never says 0 minutes while the lock is still on
        return SignInResult.locked(Math.ceilDiv(remaining.toSeconds(), SECONDS_PER_MINUTE));
    }
}
