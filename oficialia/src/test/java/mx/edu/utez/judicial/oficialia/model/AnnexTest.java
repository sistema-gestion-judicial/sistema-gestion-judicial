package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AnnexTest {

    @Test
    void roundsTheSizeUpToWholeKilobytes() {
        assertEquals(1, Annex.received("nota.txt", "text/plain", 10).getSizeKb());
        assertEquals(1, Annex.received("vacio.txt", "text/plain", 0).getSizeKb());
        assertEquals(2, Annex.received("foto.jpg", "image/jpeg", 1025).getSizeKb());
        assertEquals("1.5 MB", Annex.received("audio.mp3", "audio/mpeg", 1536 * 1024).getSizeText());
    }

    @Test
    void keepsTheTypeOfTheFile() {
        assertEquals("application/pdf", Annex.received("acta.pdf", "application/pdf", 2048).getContentType());
        assertNull(new Annex("acta.pdf", 120).getContentType());
    }
}
