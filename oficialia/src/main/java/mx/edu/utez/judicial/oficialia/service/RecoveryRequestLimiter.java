package mx.edu.utez.judicial.oficialia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Limits how many password recovery codes can be requested for one e-mail: a minimum wait between two codes and
 * a maximum per hour. Without it, every request would replace the code and restart its failed attempts, so the
 * code could be guessed forever, and anyone could fill a mailbox and use up the daily quota of the mail server.
 * Unregistered e-mails are counted too, so being limited does not reveal which accounts exist.
 * The counters live in memory and are lost when the application restarts.
 */
public class RecoveryRequestLimiter {

    private static final Duration WINDOW = Duration.ofHours(1);
    private static final Duration COOLDOWN = Duration.ofSeconds(AuthSettings.RECOVERY_REQUEST_COOLDOWN_SECONDS);
    private static final RecoveryRequestLimiter INSTANCE = new RecoveryRequestLimiter(Instant::now);

    private final Map<String, List<Instant>> requestsByEmail = new ConcurrentHashMap<>();
    private final Supplier<Instant> clock;

    public RecoveryRequestLimiter(Supplier<Instant> clock) {
        this.clock = clock;
    }

    public static RecoveryRequestLimiter getInstance() {
        return INSTANCE;
    }

    /**
     * Counts a request for the e-mail when it is allowed. A refused request is not counted, so asking again
     * does not make the wait longer.
     *
     * @return false when the e-mail asked too soon after its last code or already got the hourly maximum
     */
    public boolean tryAcquire(String email) {
        Instant now = clock.get();
        Instant windowStart = now.minus(WINDOW);
        // Every request cleans up, so e-mails that are tried once and never again do not pile up
        requestsByEmail.values().removeIf(times -> !times.get(times.size() - 1).isAfter(windowStart));
        AtomicBoolean allowed = new AtomicBoolean();
        // compute() updates one e-mail atomically, so simultaneous requests cannot all pass the limit
        requestsByEmail.compute(EmailValidator.normalize(email), (key, current) -> {
            List<Instant> recent = current == null
                ? List.of()
                : current.stream().filter(time -> time.isAfter(windowStart)).toList();
            boolean tooSoon = !recent.isEmpty() && now.isBefore(recent.get(recent.size() - 1).plus(COOLDOWN));
            boolean tooMany = recent.size() >= AuthSettings.RECOVERY_MAX_REQUESTS_PER_HOUR;
            if (tooSoon || tooMany) {
                return recent;
            }
            allowed.set(true);
            List<Instant> updated = new ArrayList<>(recent);
            updated.add(now);
            return List.copyOf(updated);
        });
        return allowed.get();
    }

    int trackedEmailCount() {
        return requestsByEmail.size();
    }
}
