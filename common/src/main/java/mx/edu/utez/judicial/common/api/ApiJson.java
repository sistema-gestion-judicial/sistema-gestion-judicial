package mx.edu.utez.judicial.common.api;

import tools.jackson.databind.json.JsonMapper;

/**
 * Shared JSON mapper for the APIs between systems; dates are written in ISO 8601.
 */
public final class ApiJson {

    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    private ApiJson() {
    }
}
