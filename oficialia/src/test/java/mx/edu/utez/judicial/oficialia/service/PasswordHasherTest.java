package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private static final String PASSWORD = "correct horse";

    @Test
    void acceptsTheOriginalPassword() {
        assertTrue(PasswordHasher.matches(PASSWORD, PasswordHasher.hash(PASSWORD)));
    }

    @Test
    void rejectsAnotherPassword() {
        assertFalse(PasswordHasher.matches("wrong horse", PasswordHasher.hash(PASSWORD)));
    }

    @Test
    void usesANewSaltForEveryHash() {
        assertNotEquals(PasswordHasher.hash(PASSWORD), PasswordHasher.hash(PASSWORD));
    }

    @Test
    void storesTheIterationsSaltAndHash() {
        String[] parts = PasswordHasher.hash(PASSWORD).split(":");

        assertEquals(3, parts.length);
        assertEquals("600000", parts[0]);
    }

    @Test
    void matchesAPublishedPbkdf2HmacSha256Vector() {
        // RFC 7914, section 11: password "passwd", salt "salt", 1 iteration
        String stored = "1:c2FsdA==:VawEblbjCJ/sFpHCJUS2BflBhSFt3gRl5oudV8INrLw=";

        assertTrue(PasswordHasher.matches("passwd", stored));
    }

    @Test
    void rejectsAStoredValueWithoutTheExpectedForm() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.matches(PASSWORD, "plain-text"));
    }
}
