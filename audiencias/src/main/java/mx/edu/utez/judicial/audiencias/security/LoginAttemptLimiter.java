package mx.edu.utez.judicial.audiencias.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locks an account for a while after too many failed logins inside a time window. It lives in
 * memory, so it protects one server instance and is cleared when the application restarts.
 */
public final class LoginAttemptLimiter {

    private static final int PURGE_THRESHOLD = 10_000;

    private record State(int failures, Instant windowStart, Instant lockedUntil) {
    }

    private final int maxFailures;
    private final Duration window;
    private final Duration lockDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(int maxFailures, Duration window, Duration lockDuration, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public static LoginAttemptLimiter standard() {
        return new LoginAttemptLimiter(5, Duration.ofMinutes(15), Duration.ofMinutes(15), Clock.systemUTC());
    }

    /** Seconds left until the account can try again; 0 when it is not locked. */
    public long secondsLocked(String key) {
        State state = states.get(key);
        if (state == null || state.lockedUntil() == null) {
            return 0;
        }
        Duration left = Duration.between(clock.instant(), state.lockedUntil());
        if (left.isNegative() || left.isZero()) {
            return 0;
        }
        return left.toSeconds() + (left.toNanosPart() > 0 ? 1 : 0);
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        if (states.size() > PURGE_THRESHOLD) {
            states.values().removeIf(state -> expired(state, now));
        }
        states.compute(key, (k, old) -> {
            if (old == null || expired(old, now)) {
                return new State(1, now, maxFailures <= 1 ? now.plus(lockDuration) : null);
            }
            int failures = old.failures() + 1;
            return new State(failures, old.windowStart(),
                failures >= maxFailures ? now.plus(lockDuration) : null);
        });
    }

    public void recordSuccess(String key) {
        states.remove(key);
    }

    private boolean expired(State state, Instant now) {
        if (state.lockedUntil() != null) {
            return !now.isBefore(state.lockedUntil());
        }
        return now.isAfter(state.windowStart().plus(window));
    }
}
