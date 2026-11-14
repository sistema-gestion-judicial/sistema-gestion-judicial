package mx.edu.utez.judicial.oficialia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Remembers for a short time that an account is active, so {@code AuthFilter} does not ask the database on every
 * page. Without it every click of every user spends a query, and the JawsDB plan of production allows 3,600 per
 * hour.
 *
 * <p>Only active accounts are remembered: an inactive one loses its session at once, so there is nothing to
 * remember about it. The price is that an account (or its court) deactivated directly in the database keeps
 * working for up to {@link AuthSettings#ACTIVE_ACCOUNT_CHECK_SECONDS}. Code that deactivates accounts or courts
 * calls {@link #forget(String)} or {@link #clear()} so the change applies on the next click.
 *
 * <p>Lives in memory, one per application, which is enough while production runs a single dyno.
 */
public class ActiveAccountCache {

    private static final ActiveAccountCache INSTANCE =
        new ActiveAccountCache(Instant::now, Duration.ofSeconds(AuthSettings.ACTIVE_ACCOUNT_CHECK_SECONDS));

    private final Map<String, Instant> activeUntil = new ConcurrentHashMap<>();
    private final Supplier<Instant> clock;
    private final Duration lifetime;

    public ActiveAccountCache(Supplier<Instant> clock, Duration lifetime) {
        this.clock = clock;
        this.lifetime = lifetime;
    }

    public static ActiveAccountCache getInstance() {
        return INSTANCE;
    }

    /** A cache that remembers nothing, so every check reaches the database. */
    public static ActiveAccountCache disabled() {
        return new ActiveAccountCache(Instant::now, Duration.ZERO);
    }

    /**
     * Tells whether the account is active, asking {@code lookup} (with the normalized e-mail) only when the
     * last confirmation is older than the lifetime.
     */
    public boolean isActive(String email, Predicate<String> lookup) {
        String key = EmailValidator.normalize(email);
        Instant now = clock.get();
        Instant until = activeUntil.get(key);
        if (until != null && now.isBefore(until)) {
            return true;
        }
        if (lookup.test(key)) {
            activeUntil.put(key, now.plus(lifetime));
            return true;
        }
        activeUntil.remove(key);
        return false;
    }

    /** Records that the account was just found active, for example by a successful sign in. */
    public void remember(String email) {
        activeUntil.put(EmailValidator.normalize(email), clock.get().plus(lifetime));
    }

    /** Makes the next check of this account ask the database; call it after deactivating the account. */
    public void forget(String email) {
        activeUntil.remove(EmailValidator.normalize(email));
    }

    /** Makes the next check of every account ask the database; call it after deactivating a court. */
    public void clear() {
        activeUntil.clear();
    }
}
