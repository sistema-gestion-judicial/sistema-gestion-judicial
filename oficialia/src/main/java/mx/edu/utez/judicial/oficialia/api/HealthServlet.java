package mx.edu.utez.judicial.oficialia.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.common.api.ApiJson;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;

/**
 * Lets the team and the other systems check that this application is deployed and reaches its database.
 * Public: it needs no credentials and returns no data.
 */
@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String database = OficialiaDaos.database()
            .map(db -> db.isReachable() ? "UP" : "DOWN")
            .orElse("NOT_CONFIGURED");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("system", "oficialia");
        body.put("status", "DOWN".equals(database) ? "DEGRADED" : "UP");
        body.put("database", database);

        response.setStatus("DOWN".equals(database) ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
            : HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(ApiJson.write(body));
    }
}
