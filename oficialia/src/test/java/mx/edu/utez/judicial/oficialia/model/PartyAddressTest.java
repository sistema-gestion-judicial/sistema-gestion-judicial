package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PartyAddressTest {

    @Test
    void showsTheAddressInOneLine() {
        PartyAddress address = new PartyAddress("Calle Hidalgo 12", "Centro", "62740", "Cuautla");

        assertEquals("Calle Hidalgo 12, Centro, C.P. 62740, Cuautla", address.getFullText());
    }
}
