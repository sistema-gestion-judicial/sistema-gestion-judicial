package mx.edu.utez.judicial.oficialia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailContentBuilderTest {

    @Test
    void recoveryEmailShowsTheCodeAndHowLongItLasts() {
        String html = EmailContentBuilder.buildPasswordRecoveryEmail("Laura Méndez", "048213", 15);

        assertTrue(html.contains("Estimado(a) Laura Méndez:"));
        assertTrue(html.contains("048213"));
        assertTrue(html.contains("vence en 15 minutos"));
    }

    @Test
    void textTypedByPeopleCannotInjectHtml() {
        String html = EmailContentBuilder.buildPasswordRecoveryEmail("<script>alert(1)</script>", "1\"2", 15);

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("1&quot;2"));
    }

    @Test
    void aMissingNameFallsBackToAGenericGreeting() {
        assertTrue(EmailContentBuilder.buildPasswordRecoveryEmail(" ", "111111", 15)
            .contains("Estimado(a) usuario(a):"));
        assertEquals("", EmailContentBuilder.escapeHtml(null));
    }
}
