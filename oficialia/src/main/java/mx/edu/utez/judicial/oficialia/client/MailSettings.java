package mx.edu.utez.judicial.oficialia.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Function;

/**
 * SMTP parameters. Each value is read from its OFICIALIA_MAIL_* environment variable, then from
 * mail.properties on the classpath (not versioned, see mail.properties.example) and finally from a default.
 */
public final class MailSettings {

    private static final String FILE = "mail.properties";

    private final Properties properties;
    private final Function<String, String> environment;

    public MailSettings(Properties properties, Function<String, String> environment) {
        this.properties = properties;
        this.environment = environment;
    }

    /** Reads mail.properties when it exists and the real environment variables. */
    public static MailSettings load() {
        Properties properties = new Properties();
        try (InputStream in = MailSettings.class.getClassLoader().getResourceAsStream(FILE)) {
            if (in != null) {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
        return new MailSettings(properties, System::getenv);
    }

    /** Without a host there is no mail server to talk to. */
    public boolean isConfigured() {
        return !getHost().isBlank();
    }

    /** Development only: write the messages in the log instead of sending them. */
    public boolean isLogOnly() {
        return getBoolean("OFICIALIA_MAIL_LOG_ONLY", "mail.log.only", false);
    }

    public String getHost() {
        return get("OFICIALIA_MAIL_HOST", "mail.smtp.host", "").trim();
    }

    public String getPort() {
        return get("OFICIALIA_MAIL_PORT", "mail.smtp.port", "587").trim();
    }

    public String getUsername() {
        return get("OFICIALIA_MAIL_USER", "mail.smtp.user", "");
    }

    public String getPassword() {
        return get("OFICIALIA_MAIL_PASSWORD", "mail.smtp.password", "");
    }

    /** Address the messages come from; the user name when none is set. */
    public String getFrom() {
        return get("OFICIALIA_MAIL_FROM", "mail.from", getUsername());
    }

    public String getFromName() {
        return get("OFICIALIA_MAIL_FROM_NAME", "mail.from.name", "SCD-2026 Oficialía de Partes");
    }

    public boolean isAuth() {
        return getBoolean("OFICIALIA_MAIL_AUTH", "mail.smtp.auth", true);
    }

    public boolean isStartTls() {
        return getBoolean("OFICIALIA_MAIL_STARTTLS", "mail.smtp.starttls.enable", true);
    }

    public int getConnectionTimeoutMillis() {
        return getInt("OFICIALIA_MAIL_CONNECTION_TIMEOUT_MS", "mail.smtp.connectiontimeout", 10_000);
    }

    public int getReadTimeoutMillis() {
        return getInt("OFICIALIA_MAIL_READ_TIMEOUT_MS", "mail.smtp.timeout", 10_000);
    }

    public int getWriteTimeoutMillis() {
        return getInt("OFICIALIA_MAIL_WRITE_TIMEOUT_MS", "mail.smtp.writetimeout", 10_000);
    }

    private String get(String environmentKey, String propertyKey, String fallback) {
        String fromEnvironment = environment.apply(environmentKey);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return properties.getProperty(propertyKey, fallback);
    }

    private boolean getBoolean(String environmentKey, String propertyKey, boolean fallback) {
        return Boolean.parseBoolean(get(environmentKey, propertyKey, String.valueOf(fallback)).trim());
    }

    private int getInt(String environmentKey, String propertyKey, int fallback) {
        try {
            return Integer.parseInt(get(environmentKey, propertyKey, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
