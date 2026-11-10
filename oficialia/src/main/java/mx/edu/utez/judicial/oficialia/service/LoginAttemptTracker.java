package mx.edu.utez.judicial.oficialia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Counts consecutive failed sign ins per email and locks the email when it reaches the limit.
 * Unknown emails are counted too, so the lock message does not reveal which accounts exist.
 * The counters live in memory and are lost when the application restarts.
 */
public class LoginAttemptTracker {

    private static final Duration ENTRY_LIFETIME = Duration.ofMinutes(AuthSettings.LOCK_MINUTES);
    private static final LoginAttemptTracker INSTANCE = new LoginAttemptTracker(Instant::now);

    private final Map<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();
    private final Supplier<Instant> clock;

    public LoginAttemptTracker(Supplier<Instant> clock) {
        this.clock = clock;
    }

    public static LoginAttemptTracker getInstance() {
        return INSTANCE;
    }

    /** Returns how long the email stays locked, or empty when it is not locked. */
    public Optional<Duration> remainingLock(String email) {
        Instant now = clock.get();
        Attempts attempts = attemptsByEmail.get(EmailValidator.normalize(email));
        if (attempts == null || !attempts.isLocked() || attempts.isExpired(now)) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(now, attempts.expiresAt()));
    }

    public void recordFailure(String email) {
        Instant now = clock.get();
        // Every failure cleans up, so emails that are tried once and never again do not pile up
        attemptsByEmail.values().removeIf(attempts -> attempts.isExpired(now));
        // compute() updates one email atomically, so simultaneous failures are all counted
        attemptsByEmail.compute(EmailValidator.normalize(email), (key, current) -> {
            boolean isActive = current != null && !current.isExpired(now);
            if (isActive && current.isLocked()) {
                return current;
            }
            int previousFailures = isActive ? current.failures() : 0;
            return new Attempts(previousFailures + 1, now.plus(ENTRY_LIFETIME));
        });
    }

    public void reset(String email) {
        attemptsByEmail.remove(EmailValidator.normalize(email));
    }

    int trackedEmailCount() {
        return attemptsByEmail.size();
    }

    private record Attempts(int failures, Instant expiresAt) {

        boolean isLocked() {
            return failures >= AuthSettings.MAX_FAILED_ATTEMPTS;
        }

        boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }
    }
}
