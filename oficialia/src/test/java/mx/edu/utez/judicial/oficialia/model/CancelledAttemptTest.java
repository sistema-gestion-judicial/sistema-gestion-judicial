package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CancelledAttemptTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2026-11-17T12:30");

    @Test
    void keepsTheDemandThatWasFoundWhoCancelledAndWhen() {
        DuplicityMatch match = new DuplicityMatch(new ActiveDemand("Juzgado de Jojutla", "Familiar",
            "Primera instancia", "TOSA850312ML4", null), PartyRole.PLAINTIFF);

        CancelledAttempt attempt = CancelledAttempt.of("12", match, "Laura Méndez", NOW);

        assertEquals("12", attempt.courtCode());
        assertEquals("Juzgado de Jojutla", attempt.matchedCourt());
        assertEquals("Familiar", attempt.matchedMatter());
        assertFalse(attempt.matchedInSecondInstance());
        assertEquals("Laura Méndez", attempt.cancelledBy());
        assertEquals(NOW, attempt.cancelledAt());
    }

    @Test
    void knowsWhenTheDemandFoundIsInSecondInstance() {
        DuplicityMatch match = new DuplicityMatch(new ActiveDemand("Juzgado de Yautepec", "Civil",
            "Segunda instancia", null, "MSU010203KL9"), PartyRole.DEFENDANT);

        assertTrue(CancelledAttempt.of("12", match, "Laura Méndez", NOW).matchedInSecondInstance());
    }
}
