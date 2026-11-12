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
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.JudicialDistrictService;

/**
 * Updates the name and the seat of a judicial district; its code is shown locked (DFR 2.3).
 */
@WebServlet("/judicial-districts/edit")
public class JudicialDistrictEditServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/judicial-districts/edit.jsp";

    private final JudicialDistrictService districtService = new JudicialDistrictService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JudicialDistrict district = districtService.get(currentUser(request), request.getParameter("code"));
            show(request, response, district.getCode(), JudicialDistrictForm.of(district));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Distrito judicial no encontrado",
                e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        // The code only says which district to update: the form has no field to change it
        JudicialDistrictForm form = new JudicialDistrictForm();
        form.setCode(code);
        form.setName(request.getParameter("name"));
        form.setSeatMunicipality(request.getParameter("seatMunicipality"));

        try {
            JudicialDistrictService.Saving saving = districtService.update(currentUser(request), code, form);
            if (saving.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "El distrito judicial «" + saving.district().getName() + "» se actualizó.");
                response.sendRedirect(request.getContextPath() + "/judicial-districts");
                return;
            }
            request.setAttribute("errors", saving.errors());
            show(request, response, code, form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Distrito judicial no encontrado",
                e.getMessage());
        }
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, String code,
            JudicialDistrictForm form) throws ServletException, IOException {
        request.setAttribute("code", code);
        request.setAttribute("form", form);
        request.setAttribute("maxNameLength", JudicialDistrictForm.MAX_NAME_LENGTH);
        request.setAttribute("maxSeatLength", JudicialDistrictForm.MAX_SEAT_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
