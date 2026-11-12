package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CourtService;

/**
 * Catalog of courts (DFR 2.9, 2.11): an updated court shows up here with its new data.
 */
@WebServlet("/courts")
public class CourtListServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/courts/list.jsp";

    private final CourtService courtService = new CourtService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        try {
            request.setAttribute("courts", courtService.list(user));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
