package mx.edu.utez.judicial.oficialia.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ApiCredentialsTest {

    private final ApiCredentials credentials = new ApiCredentials("audiencias=key-a; tocas=key-t");

    @Test
    void recognizesEachSystemByItsKey() {
        assertEquals(Optional.of("audiencias"), credentials.systemFor("key-a"));
        assertEquals(Optional.of("tocas"), credentials.systemFor(" key-t "));
    }

    @Test
    void rejectsUnknownOrMissingKeys() {
        assertTrue(credentials.systemFor("key-x").isEmpty());
        assertTrue(credentials.systemFor(null).isEmpty());
        assertTrue(new ApiCredentials(null).systemFor("key-a").isEmpty());
    }
}
