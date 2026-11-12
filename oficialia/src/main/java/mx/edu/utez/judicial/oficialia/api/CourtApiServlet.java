package mx.edu.utez.judicial.oficialia.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;

/**
 * Court catalog. GET /api/v1/courts lists them; GET /api/v1/courts/{code} returns one.
 */
@WebServlet("/api/v1/courts/*")
public class CourtApiServlet extends HttpServlet {

    private final CatalogDao catalogDao = OficialiaDaos.catalogs();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            List<Map<String, Object>> courts = catalogDao.findCourts().stream().map(CourtApiServlet::toJson).toList();
            ApiResponses.ok(response, Map.of("data", courts));
            return;
        }
        String code = path.substring(1);
        Optional<Court> court = catalogDao.findCourt(code);
        if (court.isEmpty()) {
            ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "COURT_NOT_FOUND",
                "No court has the code " + code + ".");
            return;
        }
        ApiResponses.ok(response, Map.of("data", toJson(court.get())));
    }

    private static Map<String, Object> toJson(Court court) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("code", court.getCode());
        json.put("number", court.getNumber());
        json.put("name", court.getName());
        json.put("judicialDistrict", court.getJudicialDistrict());
        json.put("judicialDistrictCode", court.getDistrictCode());
        CourtAddress address = court.getAddress();
        Map<String, Object> addressJson = new LinkedHashMap<>();
        addressJson.put("street", address.street());
        addressJson.put("neighborhood", address.neighborhood());
        addressJson.put("postalCode", address.postalCode());
        addressJson.put("municipality", address.municipality());
        addressJson.put("phone", address.phone());
        json.put("address", addressJson);
        json.put("subjectMatters", court.getSubjectMatterCodes());
        json.put("active", court.isActive());
        return json;
    }
}
