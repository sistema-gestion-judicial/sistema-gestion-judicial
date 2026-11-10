package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import org.junit.jupiter.api.Test;

class LoginAttemptTrackerTest {

    private static final String EMAIL = "ana@utez.edu.mx";

    private Instant now = Instant.parse("2026-11-09T10:00:00Z");
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(() -> now);

    @Test
    void locksOnTheLastAllowedFailure() {
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS - 1);
        assertFalse(tracker.remainingLock(EMAIL).isPresent());

        tracker.recordFailure(EMAIL);

        assertEquals(Duration.ofMinutes(AuthSettings.LOCK_MINUTES), tracker.remainingLock(EMAIL).orElseThrow());
    }

    @Test
    void reportsTheRemainingTime() {
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS);

        advanceMinutes(5);

        assertEquals(Duration.ofMinutes(AuthSettings.LOCK_MINUTES - 5), tracker.remainingLock(EMAIL).orElseThrow());
    }

    @Test
    void unlocksWhenTheLockEnds() {
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS);

        advanceMinutes(AuthSettings.LOCK_MINUTES);

        assertFalse(tracker.remainingLock(EMAIL).isPresent());
    }

    @Test
    void resetClearsTheCounter() {
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS - 1);

        tracker.reset(EMAIL);
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS - 1);

        assertFalse(tracker.remainingLock(EMAIL).isPresent());
    }

    @Test
    void treatsSpellingsOfAnEmailAsTheSameOne() {
        tracker.recordFailure(" ANA@utez.edu.mx ");
        failTimes(AuthSettings.MAX_FAILED_ATTEMPTS - 1);

        assertTrue(tracker.remainingLock("Ana@UTEZ.edu.mx").isPresent());
    }

    @Test
    void forgetsExpiredEntries() {
        tracker.recordFailure("first@utez.edu.mx");
        tracker.recordFailure("second@utez.edu.mx");

        advanceMinutes(AuthSettings.LOCK_MINUTES);
        tracker.recordFailure("third@utez.edu.mx");

        assertEquals(1, tracker.trackedEmailCount());
    }

    @Test
    void countsSimultaneousFailures() {
        IntStream.range(0, 100).parallel().forEach(i -> tracker.recordFailure(EMAIL));

        assertTrue(tracker.remainingLock(EMAIL).isPresent());
        assertEquals(1, tracker.trackedEmailCount());
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            tracker.recordFailure(EMAIL);
        }
    }

    private void advanceMinutes(int minutes) {
        now = now.plus(Duration.ofMinutes(minutes));
    }
}
