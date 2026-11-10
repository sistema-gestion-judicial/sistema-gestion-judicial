package mx.edu.utez.judicial.oficialia.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import mx.edu.utez.judicial.common.api.ApiError;
import mx.edu.utez.judicial.common.api.ApiJson;

final class ApiResponses {

    private ApiResponses() {
    }

    static void ok(HttpServletResponse response, Object body) throws IOException {
        write(response, HttpServletResponse.SC_OK, ApiJson.write(body));
    }

    static void error(HttpServletResponse response, int status, String code, String message) throws IOException {
        write(response, status, ApiJson.write(new ApiError(code, message, List.of())));
    }

    private static void write(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(json);
    }
}
