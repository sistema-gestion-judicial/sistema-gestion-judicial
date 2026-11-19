package mx.edu.utez.judicial.tocas.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.model.Resolution;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.BusinessException;
import mx.edu.utez.judicial.tocas.service.FormatUtils;
import mx.edu.utez.judicial.tocas.service.NotFoundException;
import mx.edu.utez.judicial.tocas.service.ResolutionService;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** JSON API. POST /api/v1/appeals/{id}/resolution/autosave (fields: resultando, considerando, resuelve). */
@WebServlet("/api/v1/*")
public class ResolutionApiServlet extends HttpServlet {
    private static final Pattern AUTOSAVE = Pattern.compile("^/appeals/(\\d+)/resolution/autosave$");
    private final ResolutionService resolutionService = new ResolutionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo() == null ? "" : request.getPathInfo();
        Matcher m = AUTOSAVE.matcher(path);
        if (!m.matches()) {
            write(response, HttpServletResponse.SC_NOT_FOUND, "{\"ok\":false,\"message\":\"Ruta no encontrada.\"}");
            return;
        }
        User user = (User) request.getSession().getAttribute("user");
        try {
            Resolution r = resolutionService.saveDraft(user, Integer.parseInt(m.group(1)), request.getParameter("resultando"),
                    request.getParameter("considerando"), request.getParameter("resuelve"), true);
            write(response, HttpServletResponse.SC_OK, "{\"ok\":true,\"savedAt\":\""
                    + json(FormatUtils.time12(r.getLastModified().toLocalTime())) + "\",\"pages\":" + r.getPages() + "}");
        } catch (BusinessException e) {
            write(response, HttpServletResponse.SC_CONFLICT, "{\"ok\":false,\"message\":\"" + json(e.getMessage()) + "\"}");
        } catch (NotFoundException e) {
            write(response, HttpServletResponse.SC_NOT_FOUND, "{\"ok\":false,\"message\":\"" + json(e.getMessage()) + "\"}");
        }
    }

    private void write(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.getWriter().write(body);
    }

    private String json(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
