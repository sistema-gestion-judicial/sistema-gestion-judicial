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
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.DemandRequestService;

/**
 * Shows every detail of a demand request of the user's court: parties, annexes and follow-up (RF 3.3).
 */
@WebServlet("/demand-requests/detail")
public class DemandRequestDetailServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/detail.jsp";

    private final DemandRequestService demandRequestService = new DemandRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        try {
            request.setAttribute("demandRequest",
                    demandRequestService.getForCourt(request.getParameter("folio"), user.getCourtName()));
        } catch (NotFoundException e) {
            ErrorView.notFound(request, response, e.getMessage());
            return;
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
