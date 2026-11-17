package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.ComplementaryDataForm;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.DemandRequestRegistrationService;
import mx.edu.utez.judicial.oficialia.service.DuplicityService;

/**
 * Registers a demand request (DFR 3.1): step 1 checks duplicity, step 2 sends the complementary data and the
 * annexes. Step 2 sends the data of step 1 again, so they are checked once more before saving.
 */
@WebServlet("/demand-requests/new")
public class DemandRequestCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/new.jsp";

    private static final Set<String> STEP_ONE_FIELDS = Set.of("matter", "parties", "plaintiffName", "plaintiffId",
            "defendantName", "defendantId");

    private final DuplicityService duplicityService = new DuplicityService();
    private final DemandRequestRegistrationService registrationService = new DemandRequestRegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response, new DuplicityCheckForm());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        DuplicityCheckForm form = readForm(request);
        if ("cancel".equals(action)) {
            cancel(request, response, form);
            return;
        }
        if ("register".equals(action)) {
            register(request, response, form);
            return;
        }

        Map<String, String> errors = form.validate();
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            show(request, response, form);
            return;
        }

        Optional<DuplicityMatch> match = duplicityService.findMatch(form);
        if ("continue".equals(action) && match.isPresent()) {
            request.setAttribute("verified", true);
            request.setAttribute("continuedWithWarning", match.get());
        } else if (match.isPresent()) {
            request.setAttribute("duplicityMatch", match.get());
        } else {
            request.setAttribute("verified", true);
        }
        show(request, response, form);
    }

    private void register(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        ComplementaryDataForm data = readData(request);
        try {
            DemandRequestRegistrationService.Registration registration =
                    registrationService.register(currentUser(request), form, data);
            if (registration.isSaved()) {
                DemandRequest saved = registration.request();
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                        "Se registró la solicitud con el folio " + saved.getFolio()
                                + " y quedó pendiente de calificación."
                                + (saved.getDuplicityWarning() == null ? ""
                                        : " Quedó marcada con advertencia de duplicidad."));
                response.sendRedirect(request.getContextPath() + "/demand-requests");
                return;
            }
            boolean stepOneValid = registration.errors().keySet().stream().noneMatch(STEP_ONE_FIELDS::contains);
            request.setAttribute("errors", registration.errors());
            request.setAttribute("data", data);
            if (stepOneValid) {
                request.setAttribute("verified", true);
                duplicityService.findMatch(form)
                        .ifPresent(match -> request.setAttribute("continuedWithWarning", match));
            }
            show(request, response, form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        }
    }

    private static ComplementaryDataForm readData(HttpServletRequest request) {
        ComplementaryDataForm data = new ComplementaryDataForm();
        readContact(request, "plaintiff", data.getPlaintiff());
        readContact(request, "defendant", data.getDefendant());
        data.setLegalRepresentative(request.getParameter("legalRepresentative"));
        data.setPresentationType(request.getParameter("presentationType"));
        data.setObservations(request.getParameter("observations"));
        return data;
    }

    private static void readContact(HttpServletRequest request, String party,
            ComplementaryDataForm.ContactFields fields) {
        fields.setStreet(request.getParameter(party + "Street"));
        fields.setNeighborhood(request.getParameter(party + "Neighborhood"));
        fields.setPostalCode(request.getParameter(party + "PostalCode"));
        fields.setMunicipality(request.getParameter(party + "Municipality"));
        fields.setPhone(request.getParameter(party + "Phone"));
        fields.setEmail(request.getParameter(party + "Email"));
    }

    /** The request is not saved, but the attempt is, with the clerk and the date (DFR 3.1). */
    private void cancel(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        try {
            boolean kept = registrationService.cancel(currentUser(request), form);
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, kept
                    ? "Se canceló el registro. El intento quedó guardado con su usuario y la fecha."
                    : "Se canceló el registro.");
            response.sendRedirect(request.getContextPath() + "/demand-requests");
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        }
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("matters", registrationService.activeMatterNames());
        request.setAttribute("receivedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static DuplicityCheckForm readForm(HttpServletRequest request) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(request.getParameter("matter"));
        form.setPlaintiffType(request.getParameter("plaintiffType"));
        form.setPlaintiffName(request.getParameter("plaintiffName"));
        form.setPlaintiffId(request.getParameter("plaintiffId"));
        form.setPlaintiffAnonymous(request.getParameter("plaintiffAnonymous") != null);
        form.setDefendantType(request.getParameter("defendantType"));
        form.setDefendantName(request.getParameter("defendantName"));
        form.setDefendantId(request.getParameter("defendantId"));
        form.setDefendantUnknown(request.getParameter("defendantUnknown") != null);
        return form;
    }
}
