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
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CourtService;

/**
 * Activates or deactivates a court (DFR 2.12). The list asks for confirmation with the court and how many
 * users it has before posting here.
 */
@WebServlet("/courts/status")
public class CourtStatusServlet extends HttpServlet {

    private final CourtService courtService = new CourtService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        boolean active = "true".equals(request.getParameter("active"));
        try {
            Court court = courtService.changeStatus(user, request.getParameter("code"), active);
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, active
                ? "El juzgado «" + court.getName() + "» se activó; sus usuarios ya pueden entrar."
                : "El juzgado «" + court.getName() + "» se desactivó. Sus usuarios ya no pueden entrar y las "
                    + "solicitudes y expedientes lo siguen mostrando.");
            response.sendRedirect(request.getContextPath() + "/courts");
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Juzgado no encontrado",
                e.getMessage());
        }
    }
}
