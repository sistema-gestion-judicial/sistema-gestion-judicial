package mx.edu.utez.judicial.audiencias.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.audiencias.DAO.HearingsDaos;
import mx.edu.utez.judicial.common.api.ApiJson;

/**
 * Allows you to verify whether the Audiences application is deployed and whether it can reach its database.
 */
@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String database = HearingsDaos.database()
            .map(db -> db.isReachable() ? "UP" : "DOWN")
            .orElse("NOT_CONFIGURED");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("system", "audiencias");
        body.put("status", "DOWN".equals(database) ? "DEGRADED" : "UP");
        body.put("database", database);

        response.setStatus("DOWN".equals(database) ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
            : HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(ApiJson.write(body));
    }
}
