package mx.edu.utez.judicial.audiencias.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.audiencias.DAO.AudienciasDaos;
import mx.edu.utez.judicial.common.api.ApiJson;

/**
 * Permite verificar si la aplicación de Audiencias está desplegada y si alcanza su base de datos.
 */
@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String database = AudienciasDaos.database()
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
