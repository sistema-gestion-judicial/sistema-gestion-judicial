package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.service.DemandRequestService;

/**
 * Lists the demand requests of the user's court with optional filters (RF 3.2).
 */
@WebServlet("/demand-requests")
public class DemandRequestListServlet extends HttpServlet {

    public static final String FLASH_MESSAGE = "flashMessage";

    private final DemandRequestService demandRequestService = new DemandRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        RequestStatus status = parseStatus(request.getParameter("status"));

        List<DemandRequest> demandRequests = demandRequestService.search(query, status);

        request.setAttribute("demandRequests", demandRequests);
        request.setAttribute("totalCount", demandRequestService.countAll());
        request.setAttribute("statuses", RequestStatus.values());
        request.setAttribute("query", query == null ? "" : query);
        request.setAttribute("selectedStatus", status == null ? "" : status.name());
        moveFlashMessage(request);

        request.getRequestDispatcher("/WEB-INF/views/demand-requests/list.jsp").forward(request, response);
    }

    private static RequestStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RequestStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** A message saved before a redirect is shown once and then removed. */
    private static void moveFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(FLASH_MESSAGE) != null) {
            request.setAttribute(FLASH_MESSAGE, session.getAttribute(FLASH_MESSAGE));
            session.removeAttribute(FLASH_MESSAGE);
        }
    }
}
