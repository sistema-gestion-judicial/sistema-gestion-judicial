package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.JudicialDistrictService;

/**
 * Activates or deactivates a judicial district (DFR 2.4). The list asks for confirmation with the name of the
 * district before posting here.
 */
@WebServlet("/judicial-districts/status")
public class JudicialDistrictStatusServlet extends HttpServlet {

    private final JudicialDistrictService districtService = new JudicialDistrictService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        boolean active = "true".equals(request.getParameter("active"));
        String flashKey = DemandRequestListServlet.FLASH_MESSAGE;
        String message;
        try {
            JudicialDistrict district = districtService.changeStatus(user, request.getParameter("code"), active);
            message = active ? "El distrito «" + district.getName() + "» se activó."
                : "El distrito «" + district.getName() + "» se desactivó y ya no se ofrece al registrar juzgados.";
        } catch (BusinessRuleException e) {
            flashKey = DemandRequestListServlet.FLASH_WARNING;
            message = e.getMessage();
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Distrito judicial no encontrado",
                e.getMessage());
            return;
        }
        request.getSession().setAttribute(flashKey, message);
        response.sendRedirect(request.getContextPath() + "/judicial-districts");
    }
}
