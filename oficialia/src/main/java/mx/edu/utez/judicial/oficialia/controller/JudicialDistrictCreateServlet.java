package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.JudicialDistrictService;

/**
 * Registers a judicial district (DFR 2.1).
 */
@WebServlet("/judicial-districts/new")
public class JudicialDistrictCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/judicial-districts/new.jsp";

    private final JudicialDistrictService districtService = new JudicialDistrictService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // The filter already refuses other profiles; this keeps the rule in the service as well
        try {
            districtService.requireAccess(currentUser(request));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        show(request, response, new JudicialDistrictForm());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JudicialDistrictForm form = new JudicialDistrictForm();
        form.setCode(request.getParameter("code"));
        form.setName(request.getParameter("name"));
        form.setSeatMunicipality(request.getParameter("seatMunicipality"));

        try {
            JudicialDistrictService.Saving saving =
                districtService.register(currentUser(request), form);
            if (saving.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "El distrito judicial «" + saving.district().getName() + "» (clave "
                        + saving.district().getCode() + ") quedó registrado como activo.");
                response.sendRedirect(request.getContextPath() + "/judicial-districts");
                return;
            }
            request.setAttribute("errors", saving.errors());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        show(request, response, form);
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, JudicialDistrictForm form)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("maxCodeLength", JudicialDistrictForm.MAX_CODE_LENGTH);
        request.setAttribute("maxNameLength", JudicialDistrictForm.MAX_NAME_LENGTH);
        request.setAttribute("maxSeatLength", JudicialDistrictForm.MAX_SEAT_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
