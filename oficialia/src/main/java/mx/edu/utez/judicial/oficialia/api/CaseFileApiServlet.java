package mx.edu.utez.judicial.oficialia.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.service.CaseFileService;

/**
 * Case files for Audiencias, Tocas and the other systems (#29, contract in contracts/oficialia.yaml).
 * GET /api/v1/case-files lists them; GET /api/v1/case-files/{year}/{sequence}/{court} returns one.
 */
@WebServlet("/api/v1/case-files/*")
public class CaseFileApiServlet extends HttpServlet {

    private final CaseFileService caseFileService = new CaseFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            list(request, response);
        } else {
            findOne(path.substring(1), response);
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LocalDate admittedSince;
        try {
            String since = blankToNull(request.getParameter("admittedSince"));
            admittedSince = since == null ? null : LocalDate.parse(since);
        } catch (DateTimeParseException e) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PARAMETER",
                "admittedSince must be a date in ISO 8601 format, for example 2026-11-01.");
            return;
        }
        List<Map<String, Object>> caseFiles = caseFileService.search(
                blankToNull(request.getParameter("courtCode")),
                blankToNull(request.getParameter("subjectMatter")), admittedSince)
            .stream().map(CaseFileApiServlet::toJson).toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", caseFiles);
        body.put("count", caseFiles.size());
        ApiResponses.ok(response, body);
    }

    private void findOne(String number, HttpServletResponse response) throws IOException {
        if (!CaseFileService.CASE_FILE_NUMBER.matcher(number).matches()) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_CASE_FILE_NUMBER",
                "The case file number must look like 2026/00432/12.");
            return;
        }
        Optional<DemandRequest> caseFile = caseFileService.findByNumber(number);
        if (caseFile.isEmpty()) {
            ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "CASE_FILE_NOT_FOUND",
                "No case file has the number " + number + ".");
            return;
        }
        ApiResponses.ok(response, Map.of("data", toJson(caseFile.get())));
    }

    static Map<String, Object> toJson(DemandRequest request) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("caseFileNumber", request.getCaseFileNumber());
        json.put("requestFolio", request.getFolio());
        Map<String, Object> court = new LinkedHashMap<>();
        court.put("code", request.getCourtCode());
        court.put("name", request.getCourtName());
        json.put("court", court);
        json.put("subjectMatter", request.getMatter());
        json.put("receivedDate", request.getReceivedDate());
        json.put("admittedAt", request.getRuledAt());
        json.put("hearingDesk", request.getDeskName());
        json.put("hearingDeskPending", request.isDeskPending());
        json.put("plaintiff", party(request.getPlaintiff()));
        json.put("defendant", party(request.getDefendant()));
        json.put("legalRepresentative", request.getLegalRepresentative());
        json.put("annexCount", request.getAnnexes().size());
        return json;
    }

    /** Name and type only: contact data and RFC/CURP stay in Oficialía. */
    private static Map<String, Object> party(Party party) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("identified", party.isIdentified());
        json.put("name", party.getName());
        json.put("personType", personType(party.getPersonType()));
        return json;
    }

    private static String personType(String label) {
        if (label == null) {
            return null;
        }
        return label.equals("Persona moral") ? "COMPANY" : "INDIVIDUAL";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
