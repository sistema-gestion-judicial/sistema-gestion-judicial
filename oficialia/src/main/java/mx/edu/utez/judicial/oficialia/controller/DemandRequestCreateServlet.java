package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.FilingType;
import mx.edu.utez.judicial.oficialia.model.RequestDetailsForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.DemandRequestRegistrationService;
import mx.edu.utez.judicial.oficialia.service.DuplicityService;
import mx.edu.utez.judicial.oficialia.service.SubjectMatterService;

/**
 * Registers a demand request (DFR 3.1): step 1 checks duplicity before the full capture, step 2 saves the
 * details and annexes with a new folio.
 */
@WebServlet("/demand-requests/new")
// Each annex can weigh up to 10 MB; the request carries at most 10 of them plus the form fields
@MultipartConfig(maxFileSize = RequestDetailsForm.MAX_ANNEX_BYTES, maxRequestSize = 110L * 1024 * 1024,
    fileSizeThreshold = 1024 * 1024)
public class DemandRequestCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/new.jsp";
    private final DuplicityService duplicityService = new DuplicityService();
    private final SubjectMatterService subjectMatterService = new SubjectMatterService();
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
            try {
                registrationService.cancel(currentUser(request), form);
            } catch (AccessDeniedException e) {
                ErrorView.forbidden(request, response, e.getMessage());
                return;
            }
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "Se canceló el registro. El intento quedó guardado con su usuario y la fecha.");
            response.sendRedirect(request.getContextPath() + "/demand-requests");
            return;
        }
        if ("register".equals(action)) {
            register(request, response, form);
            return;
        }

        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        // A deactivated subject matter is no longer offered for new requests (DFR 2.8)
        if (!errors.containsKey("matter") && !subjectMatterService.activeNames().contains(form.getMatter())) {
            errors.put("matter", "Seleccione una materia activa del catálogo.");
        }
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

    /** Step 2: saves the request with the details and annexes, or shows what is missing. */
    private void register(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        boolean warningAccepted = "true".equals(request.getParameter("warningAccepted"));
        RequestDetailsForm details = readDetails(request);
        DemandRequestRegistrationService.Registration registration;
        try {
            registration = details == null ? null
                : registrationService.register(currentUser(request), form, details, warningAccepted);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        if (registration != null && registration.isSaved()) {
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "La solicitud quedó registrada con el folio " + registration.folio()
                        + " y está pendiente de calificación.");
            response.sendRedirect(request.getContextPath() + "/demand-requests/detail?folio="
                + URLEncoder.encode(registration.folio(), StandardCharsets.UTF_8));
            return;
        }
        if (registration != null && registration.warning() != null) {
            request.setAttribute("duplicityMatch", registration.warning());
            show(request, response, form);
            return;
        }
        // Back to step 2 with what was typed; the files have to be chosen again
        request.setAttribute("verified", true);
        if (warningAccepted) {
            duplicityService.findMatch(form).ifPresent(match -> request.setAttribute("continuedWithWarning", match));
        }
        request.setAttribute("details", details == null ? new RequestDetailsForm() : details);
        request.setAttribute("detailErrors", registration == null
            ? Map.of("annexes", "Cada anexo puede pesar como máximo 10 MB.") : registration.errors());
        show(request, response, form);
    }

    /** The step 2 fields and the chosen files; null when a file is larger than the container allows. */
    private static RequestDetailsForm readDetails(HttpServletRequest request) throws IOException, ServletException {
        RequestDetailsForm details = new RequestDetailsForm();
        details.setPlaintiffAddress(request.getParameter("plaintiffAddress"));
        details.setPlaintiffPhone(request.getParameter("plaintiffPhone"));
        details.setPlaintiffEmail(request.getParameter("plaintiffEmail"));
        details.setDefendantAddress(request.getParameter("defendantAddress"));
        details.setDefendantPhone(request.getParameter("defendantPhone"));
        details.setDefendantEmail(request.getParameter("defendantEmail"));
        details.setLegalRepresentative(request.getParameter("legalRepresentative"));
        details.setFilingType(request.getParameter("filingType"));
        try {
            for (Part part : request.getParts()) {
                if ("annexes".equals(part.getName()) && part.getSize() > 0 && part.getSubmittedFileName() != null) {
                    details.addAnnex(new RequestDetailsForm.AnnexUpload(
                        Paths.get(part.getSubmittedFileName()).getFileName().toString(), part.getSize(),
                        part.getContentType()));
                }
            }
        } catch (IllegalStateException e) {
            return null;
        }
        return details;
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        request.setAttribute("filingTypes", FilingType.values());
        request.setAttribute("maxAnnexes", RequestDetailsForm.MAX_ANNEXES);
        request.setAttribute("form", form);
        request.setAttribute("matters", subjectMatterService.activeNames());
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
