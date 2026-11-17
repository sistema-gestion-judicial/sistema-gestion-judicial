package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequestFolioTest {

    @Test
    void writesSixDigitsAndTheYear() {
        assertEquals("S-000128/2026", RequestFolio.of(128, 2026));
        assertEquals("S-000001/2027", RequestFolio.of(1, 2027));
    }

    @Test
    void readsTheNumberOnlyFromAFolioOfThatYear() {
        assertEquals(127, RequestFolio.number("S-000127/2026", 2026).getAsInt());
        assertTrue(RequestFolio.number("S-000127/2025", 2026).isEmpty());
        assertTrue(RequestFolio.number("X-000127/2026", 2026).isEmpty());
        assertTrue(RequestFolio.number(null, 2026).isEmpty());
    }
}
