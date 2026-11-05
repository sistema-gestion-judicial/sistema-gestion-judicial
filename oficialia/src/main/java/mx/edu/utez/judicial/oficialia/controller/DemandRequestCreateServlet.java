package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.service.DuplicityService;

/**
 * Step 1 of registering a demand request: checks duplicity before the full capture (RF 3.1).
 */
@WebServlet("/demand-requests/new")
public class DemandRequestCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/new.jsp";
    private static final List<String> MATTERS = List.of("Civil", "Familiar", "Mercantil", "Penal");

    private final DuplicityService duplicityService = new DuplicityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response, new DuplicityCheckForm());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("cancel".equals(action)) {
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "Se canceló el registro. El intento quedó guardado con su usuario y la fecha.");
            response.sendRedirect(request.getContextPath() + "/demand-requests");
            return;
        }

        DuplicityCheckForm form = readForm(request);
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

    private void show(HttpServletRequest request, HttpServletResponse response, DuplicityCheckForm form)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("matters", MATTERS);
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
