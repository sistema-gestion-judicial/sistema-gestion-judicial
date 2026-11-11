package mx.edu.utez.judicial.oficialia.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;

/**
 * Subject matter catalog: GET /api/v1/subject-matters.
 */
@WebServlet("/api/v1/subject-matters")
public class SubjectMatterApiServlet extends HttpServlet {

    private final CatalogDao catalogDao = OficialiaDaos.catalogs();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> matters = catalogDao.findSubjectMatters().stream().map(matter -> {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("id", matter.getId());
            json.put("code", matter.getCode());
            json.put("name", matter.getName());
            json.put("description", matter.getDescription());
            json.put("active", matter.isActive());
            return json;
        }).toList();
        ApiResponses.ok(response, Map.of("data", matters));
    }
}
