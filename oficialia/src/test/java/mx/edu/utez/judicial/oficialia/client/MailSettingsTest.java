package mx.edu.utez.judicial.oficialia.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class MailSettingsTest {

    private static MailSettings settings(Map<String, String> file, Map<String, String> environment) {
        Properties properties = new Properties();
        properties.putAll(file);
        return new MailSettings(properties, environment::get);
    }

    @Test
    void withoutAnythingConfiguredThereIsNoMailServerAndSafeDefaults() {
        MailSettings settings = settings(Map.of(), Map.of());

        assertFalse(settings.isConfigured());
        assertFalse(settings.isLogOnly());
        assertEquals("587", settings.getPort());
        assertTrue(settings.isAuth());
        assertTrue(settings.isStartTls());
        assertEquals(10_000, settings.getConnectionTimeoutMillis());
        assertEquals("SCD-2026 Oficialía de Partes", settings.getFromName());
    }

    @Test
    void valuesComeFromTheFileAndTheEnvironmentWinsOverIt() {
        MailSettings settings = settings(
            Map.of("mail.smtp.host", "smtp.file.test", "mail.smtp.user", "file@test.mx"),
            Map.of("OFICIALIA_MAIL_HOST", "smtp.env.test"));

        assertTrue(settings.isConfigured());
        assertEquals("smtp.env.test", settings.getHost());
        assertEquals("file@test.mx", settings.getUsername());
    }

    @Test
    void aBlankEnvironmentVariableDoesNotHideTheFile() {
        // Docker Compose passes OFICIALIA_MAIL_HOST as an empty text when .env does not define it
        MailSettings settings = settings(Map.of("mail.smtp.host", "smtp.file.test"),
            Map.of("OFICIALIA_MAIL_HOST", ""));

        assertEquals("smtp.file.test", settings.getHost());
    }

    @Test
    void theSenderDefaultsToTheUserName() {
        assertEquals("file@test.mx", settings(Map.of("mail.smtp.user", "file@test.mx"), Map.of()).getFrom());
        assertEquals("from@test.mx", settings(Map.of("mail.smtp.user", "file@test.mx"),
            Map.of("OFICIALIA_MAIL_FROM", "from@test.mx")).getFrom());
    }

    @Test
    void logOnlyModeMustBeAskedForExplicitly() {
        assertTrue(settings(Map.of("mail.log.only", "true"), Map.of()).isLogOnly());
        assertTrue(settings(Map.of(), Map.of("OFICIALIA_MAIL_LOG_ONLY", "true")).isLogOnly());
        assertFalse(settings(Map.of("mail.log.only", "no"), Map.of()).isLogOnly());
    }

    @Test
    void invalidNumbersFallBackToTheDefault() {
        MailSettings settings = settings(Map.of("mail.smtp.timeout", "abc"), Map.of());

        assertEquals(10_000, settings.getReadTimeoutMillis());
    }
}
