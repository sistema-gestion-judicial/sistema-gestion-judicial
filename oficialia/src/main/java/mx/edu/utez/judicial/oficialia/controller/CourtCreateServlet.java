package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.CourtRegistrationForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CourtRegistrationService;

/**
 * Registers a court and its administrator (DFR 2.9).
 */
@WebServlet("/courts/new")
public class CourtCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/courts/new.jsp";

    private final CourtRegistrationService courtService = new CourtRegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // The filter already refuses other profiles; this keeps the rule in the service as well
        try {
            courtService.requireAccess(currentUser(request));
            show(request, response, new CourtRegistrationForm());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CourtRegistrationForm form = new CourtRegistrationForm();
        form.setCode(request.getParameter("code"));
        form.setNumber(request.getParameter("number"));
        form.setName(request.getParameter("name"));
        form.setDistrictCode(request.getParameter("districtCode"));
        form.setStreet(request.getParameter("street"));
        form.setNeighborhood(request.getParameter("neighborhood"));
        form.setPostalCode(request.getParameter("postalCode"));
        form.setMunicipality(request.getParameter("municipality"));
        form.setPhone(request.getParameter("phone"));
        form.setSubjectMatterCodes(request.getParameterValues("subjectMatterCodes"));
        form.setAdminFirstName(request.getParameter("adminFirstName"));
        form.setAdminLastName(request.getParameter("adminLastName"));
        form.setAdminEmail(request.getParameter("adminEmail"));
        form.setAdminPassword(request.getParameter("adminPassword"));

        try {
            CourtRegistrationService.Registration registration =
                courtService.register(currentUser(request), form);
            if (registration.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "El juzgado «" + registration.court().getName() + "» (clave " + registration.court().getCode()
                        + ") quedó registrado como activo. " + registration.administratorName()
                        + " es su administrador y debe cambiar la contraseña temporal al iniciar sesión.");
                response.sendRedirect(request.getContextPath() + "/courts");
                return;
            }
            request.setAttribute("errors", registration.errors());
            show(request, response, form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, CourtRegistrationForm form)
            throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("form", form);
        request.setAttribute("districts", courtService.activeDistricts(user));
        request.setAttribute("matters", courtService.activeSubjectMatters(user));
        request.setAttribute("codeDigits", CourtRegistrationForm.CODE_DIGITS);
        request.setAttribute("maxNumber", CourtRegistrationForm.MAX_NUMBER);
        request.setAttribute("maxNameLength", CourtRegistrationForm.MAX_NAME_LENGTH);
        request.setAttribute("maxStreetLength", CourtRegistrationForm.MAX_STREET_LENGTH);
        request.setAttribute("maxNeighborhoodLength", CourtRegistrationForm.MAX_NEIGHBORHOOD_LENGTH);
        request.setAttribute("maxMunicipalityLength", CourtRegistrationForm.MAX_MUNICIPALITY_LENGTH);
        request.setAttribute("maxPersonNameLength", CourtRegistrationForm.MAX_PERSON_NAME_LENGTH);
        request.setAttribute("maxEmailLength", CourtRegistrationForm.MAX_EMAIL_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
