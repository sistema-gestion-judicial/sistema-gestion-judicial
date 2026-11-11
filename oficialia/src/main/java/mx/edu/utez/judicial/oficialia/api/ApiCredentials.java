package mx.edu.utez.judicial.oficialia.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One key per system allowed to call the API, read from OFICIALIA_API_KEYS with the format
 * "audiencias=key1;tocas=key2;peritos=key3". Without that variable no call is accepted.
 */
public final class ApiCredentials {

    private final Map<String, byte[]> keysBySystem = new LinkedHashMap<>();

    public ApiCredentials(String definition) {
        if (definition == null) {
            return;
        }
        for (String entry : definition.split(";")) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                keysBySystem.put(parts[0].trim(), parts[1].trim().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public static ApiCredentials fromEnvironment() {
        String value = System.getenv("OFICIALIA_API_KEYS");
        return new ApiCredentials(value != null ? value : System.getProperty("OFICIALIA_API_KEYS"));
    }

    /** The system that owns the key, if the key is valid. Compared in constant time. */
    public Optional<String> systemFor(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        byte[] given = key.trim().getBytes(StandardCharsets.UTF_8);
        String match = null;
        for (Map.Entry<String, byte[]> entry : keysBySystem.entrySet()) {
            if (MessageDigest.isEqual(entry.getValue(), given)) {
                match = entry.getKey();
            }
        }
        return Optional.ofNullable(match);
    }

    public boolean isEmpty() {
        return keysBySystem.isEmpty();
    }
}
