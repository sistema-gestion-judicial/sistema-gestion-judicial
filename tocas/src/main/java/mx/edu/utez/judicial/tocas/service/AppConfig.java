package mx.edu.utez.judicial.tocas.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Reads app.properties from the classpath. */
public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read app.properties", e);
        }
    }

    private AppConfig() {}

    public static String get(String key, String defaultValue) {
        String value = PROPS.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /** Folder where signed PDFs are stored. */
    public static Path uploadDir() {
        String configured = get("upload.dir", "");
        if (!configured.isEmpty()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "tocas-uploads");
    }
}
