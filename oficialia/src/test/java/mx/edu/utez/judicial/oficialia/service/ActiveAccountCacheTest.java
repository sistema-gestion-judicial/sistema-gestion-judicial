package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class ActiveAccountCacheTest {

    private static final String EMAIL = "ana@utez.edu.mx";

    private Instant now = Instant.parse("2026-11-10T10:00:00Z");
    private final ActiveAccountCache cache = new ActiveAccountCache(() -> now, Duration.ofSeconds(60));
    private final List<String> lookups = new ArrayList<>();
    private boolean active = true;
    private final Predicate<String> database = email -> {
        lookups.add(email);
        return active;
    };

    @Test
    void asksTheDatabaseOnlyOncePerMinute() {
        assertTrue(cache.isActive(EMAIL, database));
        now = now.plusSeconds(59);
        assertTrue(cache.isActive(EMAIL, database));
        assertEquals(1, lookups.size());

        now = now.plusSeconds(1);
        assertTrue(cache.isActive(EMAIL, database));
        assertEquals(2, lookups.size());
    }

    @Test
    void endsTheSessionAtTheNextCheckAfterTheMinute() {
        cache.isActive(EMAIL, database);
        active = false;

        now = now.plusSeconds(60);
        assertFalse(cache.isActive(EMAIL, database));
        assertFalse(cache.isActive(EMAIL, database));
        assertEquals(3, lookups.size(), "an inactive account is never remembered");
    }

    @Test
    void forgettingAnAccountAppliesItsDeactivationAtOnce() {
        cache.isActive(EMAIL, database);
        active = false;

        cache.forget(EMAIL.toUpperCase());

        assertFalse(cache.isActive(EMAIL, database));
    }

    @Test
    void clearingAppliesAnyDeactivationAtOnce() {
        cache.isActive(EMAIL, database);
        active = false;

        cache.clear();

        assertFalse(cache.isActive(EMAIL, database));
    }

    @Test
    void anAccountJustSignedInIsNotReadAgainAndTheEmailCaseDoesNotMatter() {
        cache.remember(" Ana@UTEZ.edu.mx ");

        assertTrue(cache.isActive(EMAIL, database));
        assertEquals(List.of(), lookups);
    }

    @Test
    void theDisabledCacheAlwaysAsks() {
        ActiveAccountCache disabled = ActiveAccountCache.disabled();
        disabled.remember(EMAIL);

        disabled.isActive(EMAIL, database);
        disabled.isActive(EMAIL, database);

        assertEquals(List.of(EMAIL, EMAIL), lookups);
    }
}
