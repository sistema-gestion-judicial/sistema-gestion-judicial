package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.SubjectMatterService;

/**
 * Activates or deactivates a subject matter (DFR 2.8). The list asks for confirmation with the name of the
 * subject matter before posting here.
 */
@WebServlet("/subject-matters/status")
public class SubjectMatterStatusServlet extends HttpServlet {

    private final SubjectMatterService matterService = new SubjectMatterService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        boolean active = "true".equals(request.getParameter("active"));
        try {
            SubjectMatter matter = matterService.changeStatus(user, request.getParameter("code"), active);
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, active
                ? "La materia «" + matter.getName() + "» se activó y ya se ofrece en las solicitudes y juzgados."
                : "La materia «" + matter.getName() + "» se desactivó. Ya no se ofrece en solicitudes ni juzgados "
                    + "nuevos; las solicitudes registradas la siguen mostrando.");
            response.sendRedirect(request.getContextPath() + "/subject-matters");
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Materia no encontrada",
                e.getMessage());
        }
    }
}
