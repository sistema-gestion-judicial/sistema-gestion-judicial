package mx.edu.utez.judicial.common.api;

import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON for the APIs between systems, with Jackson. Dates come out in ISO 8601 ("2026-10-24T10:15:00").
 */
public final class ApiJson {

    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    private ApiJson() {
    }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Error writing JSON", e);
        }
    }

    /** The standard error body: {"error": {"code": ..., "message": ..., "details": [...]}}. */
    public static String write(ApiError error) {
        return write(Map.of("error", error));
    }
}
