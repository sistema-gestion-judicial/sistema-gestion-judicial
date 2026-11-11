package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class RecoveryRequestLimiterTest {

    private static final String EMAIL = "ana@utez.edu.mx";

    private Instant now = Instant.parse("2026-11-10T18:00:00Z");
    private final RecoveryRequestLimiter limiter = new RecoveryRequestLimiter(() -> now);

    private void advanceSeconds(long seconds) {
        now = now.plusSeconds(seconds);
    }

    @Test
    void theFirstRequestIsAllowed() {
        assertTrue(limiter.tryAcquire(EMAIL));
    }

    @Test
    void aSecondRequestInsideTheWaitIsRefused() {
        assertTrue(limiter.tryAcquire(EMAIL));

        advanceSeconds(59);

        assertFalse(limiter.tryAcquire(EMAIL));
    }

    @Test
    void afterTheWaitAnotherRequestIsAllowed() {
        assertTrue(limiter.tryAcquire(EMAIL));

        advanceSeconds(60);

        assertTrue(limiter.tryAcquire(EMAIL));
    }

    @Test
    void aRefusedRequestDoesNotMakeTheWaitLonger() {
        assertTrue(limiter.tryAcquire(EMAIL));
        advanceSeconds(30);
        assertFalse(limiter.tryAcquire(EMAIL));

        advanceSeconds(30);

        assertTrue(limiter.tryAcquire(EMAIL));
    }

    @Test
    void atMostFiveRequestsPerHourAndThenTheWindowSlides() {
        Instant first = now;
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire(EMAIL), "request " + (i + 1));
            advanceSeconds(61);
        }
        assertFalse(limiter.tryAcquire(EMAIL), "the sixth one inside the hour");

        now = first.plusSeconds(3599);
        assertFalse(limiter.tryAcquire(EMAIL));

        now = first.plusSeconds(3601);
        assertTrue(limiter.tryAcquire(EMAIL), "the first request left the hour");
    }

    @Test
    void eachEmailHasItsOwnCounterIgnoringCaseAndSpaces() {
        assertTrue(limiter.tryAcquire(EMAIL));

        assertTrue(limiter.tryAcquire("beto@utez.edu.mx"));
        assertFalse(limiter.tryAcquire("  ANA@utez.edu.mx "));
    }

    @Test
    void emailsThatAreNotTriedAgainAreForgottenAfterAnHour() {
        limiter.tryAcquire("a@utez.edu.mx");
        limiter.tryAcquire("b@utez.edu.mx");
        assertEquals(2, limiter.trackedEmailCount());

        advanceSeconds(3601);
        limiter.tryAcquire("c@utez.edu.mx");

        assertEquals(1, limiter.trackedEmailCount());
    }
}
