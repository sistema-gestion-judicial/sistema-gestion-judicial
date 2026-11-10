package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.RulingForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.RulingService;

/**
 * Rules a demand request as admissible or inadmissible after the clerk confirms (RF 3.5).
 */
@WebServlet("/demand-requests/rule")
public class DemandRequestRuleServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/rule.jsp";

    private final RulingService rulingService = new RulingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String folio = request.getParameter("folio");
        DemandRequest demandRequest = loadRulable(request, response, folio);
        if (demandRequest != null) {
            show(request, response, demandRequest, new RulingForm());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String folio = request.getParameter("folio");
        DemandRequest demandRequest = loadRulable(request, response, folio);
        if (demandRequest == null) {
            return;
        }

        RulingForm form = new RulingForm();
        form.setDecision(request.getParameter("decision"));
        form.setReason(request.getParameter("reason"));
        Map<String, String> errors = form.validate();
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            show(request, response, demandRequest, form);
            return;
        }
        if (!"true".equals(request.getParameter("confirmed"))) {
            // Valid, but the ruling cannot be undone: ask before applying it
            request.setAttribute("askConfirmation", true);
            show(request, response, demandRequest, form);
            return;
        }

        try {
            DemandRequest ruled = rulingService.rule(folio, currentUser(request), form);
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, successMessage(ruled));
        } catch (BusinessRuleException e) {
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_WARNING, e.getMessage());
        }
        redirectToDetail(request, response, folio);
    }

    /** Returns the request, or answers the error itself and returns null. */
    private DemandRequest loadRulable(HttpServletRequest request, HttpServletResponse response, String folio)
            throws ServletException, IOException {
        try {
            return rulingService.getRulable(folio, currentUser(request));
        } catch (NotFoundException e) {
            ErrorView.notFound(request, response, e.getMessage());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (BusinessRuleException e) {
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_WARNING, e.getMessage());
            redirectToDetail(request, response, folio);
        }
        return null;
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, DemandRequest demandRequest,
            RulingForm form) throws ServletException, IOException {
        request.setAttribute("demandRequest", demandRequest);
        request.setAttribute("form", form);
        request.setAttribute("maxReasonLength", RulingForm.MAX_REASON_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String successMessage(DemandRequest ruled) {
        if (ruled.getStatus() == RequestStatus.INADMISSIBLE) {
            return "La solicitud quedó como no procedente. El motivo quedó registrado para el acuse del demandante.";
        }
        if (ruled.isDeskPending()) {
            return "La solicitud quedó como procedente con el expediente " + ruled.getCaseFileNumber()
                    + ". La mesa queda pendiente porque el servicio de Audiencias no respondió.";
        }
        return "La solicitud quedó como procedente con el expediente " + ruled.getCaseFileNumber()
                + " y la " + ruled.getDeskName() + " para la primera audiencia.";
    }

    private static void redirectToDetail(HttpServletRequest request, HttpServletResponse response, String folio)
            throws IOException {
        String encoded = URLEncoder.encode(folio == null ? "" : folio, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/demand-requests/detail?folio=" + encoded);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
