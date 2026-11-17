package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AnnexTest {

    @Test
    void roundsTheSizeUpToWholeKilobytes() {
        assertEquals(1, new Annex("nota.txt", "text/plain", new byte[10]).getSizeKb());
        assertEquals(1, new Annex("vacio.txt", "text/plain", new byte[0]).getSizeKb());
        assertEquals(2, new Annex("foto.jpg", "image/jpeg", new byte[1025]).getSizeKb());
        assertEquals("1.5 MB", new Annex("audio.mp3", "audio/mpeg", new byte[1536 * 1024]).getSizeText());
    }

    @Test
    void keepsItsOwnCopyOfTheBytes() {
        byte[] bytes = {1, 2, 3};
        Annex annex = new Annex("acta.pdf", "application/pdf", bytes);
        bytes[0] = 9;
        annex.getContent()[1] = 9;

        assertArrayEquals(new byte[] {1, 2, 3}, annex.getContent());
    }

    @Test
    void anAnnexReadBackHasNoBytes() {
        assertNull(new Annex("acta.pdf", 120, "application/pdf").getContent());
        assertNull(new Annex("acta.pdf", 120).getContentType());
    }
}
