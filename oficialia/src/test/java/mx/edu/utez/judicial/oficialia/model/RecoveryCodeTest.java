package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RecoveryCodeTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2026-11-10T10:00:00");

    private final RecoveryCode code = new RecoveryCode("ana@utez.edu.mx", "048213", NOW.plusMinutes(15));

    @Test
    void expiresExactlyAtTheEndOfItsLife() {
        assertFalse(code.isExpired(NOW.plusMinutes(14)));
        assertTrue(code.isExpired(NOW.plusMinutes(15)));
    }

    @Test
    void onlyTheSameDigitsMatch() {
        assertTrue(code.matches("048213"));
        assertFalse(code.matches("048214"));
        assertFalse(code.matches("48213"));
        assertFalse(code.matches(null));
    }

    @Test
    void countsWrongGuesses() {
        assertTrue(code.registerFailedAttempt() == 1);
        assertTrue(code.registerFailedAttempt() == 2);
    }

    @Test
    void toStringDoesNotExposeTheCode() {
        String text = code.toString();

        assertFalse(text.contains("048213"));
        assertTrue(text.contains("ana@utez.edu.mx"));
    }
}
