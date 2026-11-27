package mx.edu.utez.judicial.audiencias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.audiencias.dao.DataAccessException;
import mx.edu.utez.judicial.audiencias.dao.HearingsDaos;
import mx.edu.utez.judicial.audiencias.dao.desk.JdbcDeskDao;
import mx.edu.utez.judicial.audiencias.model.Desk;
import mx.edu.utez.judicial.audiencias.model.Usuario;
import mx.edu.utez.judicial.audiencias.security.AuthSession;
import mx.edu.utez.judicial.audiencias.security.SessionUser;
import mx.edu.utez.judicial.audiencias.service.DeskService;
import mx.edu.utez.judicial.audiencias.service.DeskService.LawyerOption;
import mx.edu.utez.judicial.common.api.ApiJson;

/**
 * Desk administration. GET /mesas shows the page; GET ?action=list|lawyers|courts and POST
 * ?action=create|update|toggle answer JSON for assets/js/mesas.js.
 *
 * Every request needs an administrator session. POSTs also need the CSRF token that the page
 * carries in its meta tag.
 */
@WebServlet("/mesas")
public class DeskController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DeskController.class.getName());

    private static final String VIEW = "/WEB-INF/views/auth/admin-mesas.jsp";
    private static final String LOGIN_PATH = "/index.jsp";

    /** Session attribute that LoginServlet fills. Change it here when the login moves to User/Role. */
    private static final String SESSION_USER = "user";
    private static final String ADMIN_ROLE = "ADMIN";

    private static final String CSRF_SESSION_KEY = "csrfToken";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    /** Null when the database is not configured; the JSON actions then answer 503. */
    private DeskService deskService;

    /** Development switch (AUDIENCIAS_DISABLE_AUTH=true). Never set it in production. */


    @Override
    public void init() {
        deskService = HearingsDaos.database()
            .map(database -> new DeskService(new JdbcDeskDao(database), HearingsDaos.userDao()))
            .orElse(null);
        if (deskService == null) {
            LOG.warning("AUDIENCIAS_DB_URL is not set: the desk actions answer 503");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) {
            showPage(req, resp);
            return;
        }
        if (!authorize(req, resp, true)) {
            return;
        }
        try {
            switch (action) {
                case "list" -> listDesks(req, resp);
                case "lawyers" -> listLawyers(resp);
                case "courts" -> proxyCourts(resp);
                default -> sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Acción no válida.");
            }
        } catch (RuntimeException e) {
            handleFailure(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        if (!authorize(req, resp, true)) {
            return;
        }
        if (!hasValidCsrfToken(req)) {
            sendError(resp, HttpServletResponse.SC_FORBIDDEN,
                "La sesión de la página caducó. Recarga la página e inténtalo de nuevo.");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Acción no especificada.");
            return;
        }

        try {
            switch (action) {
                case "create" -> handleCreate(req, resp);
                case "update" -> handleUpdate(req, resp);
                case "toggle" -> handleToggle(req, resp);
                default -> sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Acción no válida.");
            }
        } catch (RuntimeException e) {
            handleFailure(resp, e);
        }
    }

    // ---- Page ----

    private void showPage(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        if (!authorize(req, resp, false)) {
            return;
        }
        req.setAttribute("csrfToken", csrfToken(req));
        resp.setHeader("Cache-Control", "no-store");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    // ---- JSON actions ----

    private void listDesks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DeskService service = service(resp);
        if (service == null) {
            return;
        }
        List<Map<String, Object>> desks = service
            .findAll(req.getParameter("search"), req.getParameter("matter"))
            .stream().map(DeskController::toJson).toList();
        sendJson(resp, HttpServletResponse.SC_OK, desks);
    }

    private void listLawyers(HttpServletResponse resp) throws IOException {
        DeskService service = service(resp);
        if (service == null) {
            return;
        }
        List<Map<String, Object>> lawyers = service.findLawyerOptions().stream()
            .map(DeskController::toJson).toList();
        sendJson(resp, HttpServletResponse.SC_OK, lawyers);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DeskService service = service(resp);
        if (service == null) {
            return;
        }
        int newId = service.createDesk(
            req.getParameter("deskName"),
            req.getParameter("matter"),
            requiredBoolean(req, "active", "El estado"),
            optionalInt(req, "userId", "El abogado"),
            req.getParameter("courtId"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("id", newId);
        body.put("message", "Mesa registrada correctamente.");
        sendJson(resp, HttpServletResponse.SC_CREATED, body);
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DeskService service = service(resp);
        if (service == null) {
            return;
        }
        service.updateDesk(
            requiredInt(req, "id", "La mesa"),
            req.getParameter("deskName"),
            req.getParameter("matter"),
            requiredBoolean(req, "active", "El estado"),
            optionalInt(req, "userId", "El abogado"),
            req.getParameter("courtId"));
        sendOk(resp, "Mesa actualizada correctamente.");
    }

    private void handleToggle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DeskService service = service(resp);
        if (service == null) {
            return;
        }
        service.setActive(requiredInt(req, "id", "La mesa"), requiredBoolean(req, "active", "El estado"));
        sendOk(resp, "Estado actualizado correctamente.");
    }

    // ---- Session, role and CSRF ----

    /**
     * Lets the request through when an administrator is logged in. Anything the page fetches
     * (json = true) gets a JSON 401 or 403; only the page itself is redirected to the login.
     */
    private boolean authorize(HttpServletRequest req, HttpServletResponse resp, boolean json)
        throws IOException {

        Optional<SessionUser> user = AuthSession.current(req);

        if (user.isEmpty()) {
            if (json) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Sesión no iniciada.");
            } else {
                resp.sendRedirect(req.getContextPath() + LOGIN_PATH);
            }
            return false;
        }
        if (!user.get().isAdmin()) {
            if (json) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para administrar mesas.");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return false;
        }
        return true;
    }

    private static boolean isAdmin(Object user) {
        return user instanceof Usuario usuario && ADMIN_ROLE.equalsIgnoreCase(usuario.getRol());
    }

    private static String csrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CSRF_SESSION_KEY, token);
        }
        return token;
    }

    private static boolean hasValidCsrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(CSRF_SESSION_KEY);
        String given = req.getHeader(CSRF_HEADER);
        return expected != null && given != null
            && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
            given.getBytes(StandardCharsets.UTF_8));
    }

    // ---- Parameters ----

    private static int requiredInt(HttpServletRequest req, String name, String label) {
        Integer value = optionalInt(req, name, label);
        if (value == null) {
            throw new IllegalArgumentException(label + " es obligatorio.");
        }
        return value;
    }

    private static Integer optionalInt(HttpServletRequest req, String name, String label) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " no es válido.");
        }
    }

    private static boolean requiredBoolean(HttpServletRequest req, String name, String label) {
        String raw = req.getParameter(name);
        if ("true".equals(raw)) {
            return true;
        }
        if ("false".equals(raw)) {
            return false;
        }
        throw new IllegalArgumentException(label + " no es válido.");
    }

    // ---- Responses ----

    private DeskService service(HttpServletResponse resp) throws IOException {
        if (deskService == null) {
            sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "La base de datos de Audiencias no está disponible.");
        }
        return deskService;
    }

    /** IllegalArgumentException carries a message written for the user; anything else stays in the log. */
    private void handleFailure(HttpServletResponse resp, RuntimeException e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof DataAccessException) {
            LOG.log(Level.SEVERE, "Database error in /mesas", e);
            sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "No se pudo acceder a la base de datos. Inténtalo más tarde.");
        } else {
            LOG.log(Level.SEVERE, "Unexpected error in /mesas", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error interno al procesar la mesa.");
        }
    }

    private static void sendOk(HttpServletResponse resp, String message) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, Map.of("ok", true, "message", message));
    }

    private static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        sendJson(resp, status, Map.of("ok", false, "message", message));
    }

    private static void sendJson(HttpServletResponse resp, int status, Object body) throws IOException {
        writeJson(resp, status, ApiJson.write(body));
    }

    private static void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(json);
    }

    private static Map<String, Object> toJson(Desk desk) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", desk.getId());
        json.put("deskName", desk.getDeskName());
        json.put("matter", desk.getMatter());
        json.put("active", desk.isActive());
        json.put("userId", desk.getUserId());
        json.put("courtId", desk.getCourtId());
        return json;
    }

    private static Map<String, Object> toJson(LawyerOption lawyer) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", lawyer.id());
        json.put("fullName", lawyer.fullName());
        json.put("deskId", lawyer.deskId());
        return json;
    }

    // ---- Court catalog (Oficialía) ----

    private static String oficialiaBaseUrl() {
        String url = System.getenv("OFICIALIA_API_URL");
        return (url != null && !url.isBlank())
            ? url.trim().replaceAll("/+$", "")
            : "http://localhost:8080/oficialia_war_exploded";
    }

    /** The key comes only from the environment: there is no default value in the code. */
    private static String oficialiaApiKey() {
        String key = System.getenv("OFICIALIA_API_KEY");
        return (key != null && !key.isBlank()) ? key.trim() : null;
    }

    private void proxyCourts(HttpServletResponse resp) throws IOException {
        String key = oficialiaApiKey();
        if (key == null) {
            LOG.severe("OFICIALIA_API_KEY is not set: the court catalog cannot be loaded");
            sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "El catálogo de juzgados no está configurado.");
            return;
        }

        String body = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oficialiaBaseUrl() + "/api/v1/courts"))
                .header("X-Api-Key", key)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> apiResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (apiResponse.statusCode() == HttpServletResponse.SC_OK) {
                body = apiResponse.body();
            } else {
                LOG.warning("Oficialía answered " + apiResponse.statusCode() + " to GET /api/v1/courts");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warning("The court catalog request was interrupted");
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Could not reach Oficialía", e);
        }

        if (body == null) {
            sendError(resp, HttpServletResponse.SC_BAD_GATEWAY,
                "No se pudo conectar con el catálogo de juzgados de Oficialía.");
            return;
        }
        writeJson(resp, HttpServletResponse.SC_OK, body);
    }
}
