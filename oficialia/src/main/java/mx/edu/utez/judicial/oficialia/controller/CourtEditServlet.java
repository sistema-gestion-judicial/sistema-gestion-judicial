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
import mx.edu.utez.judicial.oficialia.model.CourtForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CourtService;

/**
 * Updates a court; its code and number are shown locked (DFR 2.11).
 */
@WebServlet("/courts/edit")
public class CourtEditServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/courts/edit.jsp";

    private final CourtService courtService = new CourtService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Court court = courtService.get(currentUser(request), request.getParameter("code"));
            show(request, response, court, CourtForm.of(court));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        // The code only says which court to update: the form has no field to change it or the number
        CourtForm form = new CourtForm();
        form.setName(request.getParameter("name"));
        form.setDistrictCode(request.getParameter("districtCode"));
        form.setStreet(request.getParameter("street"));
        form.setNeighborhood(request.getParameter("neighborhood"));
        form.setPostalCode(request.getParameter("postalCode"));
        form.setMunicipality(request.getParameter("municipality"));
        form.setPhone(request.getParameter("phone"));
        form.setSubjectMatterCodes(request.getParameterValues("subjectMatterCodes"));

        try {
            User user = currentUser(request);
            CourtService.Saving saving = courtService.update(user, code, form);
            if (saving.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "El juzgado «" + saving.name() + "» se actualizó.");
                response.sendRedirect(request.getContextPath() + "/courts");
                return;
            }
            request.setAttribute("errors", saving.errors());
            show(request, response, courtService.get(user, code), form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, Court court, CourtForm form)
            throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("court", court);
        request.setAttribute("form", form);
        request.setAttribute("districts", courtService.districts(user));
        request.setAttribute("subjectMatters", courtService.subjectMatters(user));
        request.setAttribute("maxNameLength", CourtForm.MAX_NAME_LENGTH);
        request.setAttribute("maxStreetLength", CourtForm.MAX_STREET_LENGTH);
        request.setAttribute("maxNeighborhoodLength", CourtForm.MAX_NEIGHBORHOOD_LENGTH);
        request.setAttribute("maxMunicipalityLength", CourtForm.MAX_MUNICIPALITY_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static void notFound(HttpServletRequest request, HttpServletResponse response, NotFoundException e)
            throws ServletException, IOException {
        ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Juzgado no encontrado", e.getMessage());
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
