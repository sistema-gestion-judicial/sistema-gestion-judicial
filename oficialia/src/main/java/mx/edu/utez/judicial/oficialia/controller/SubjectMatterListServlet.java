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
import mx.edu.utez.judicial.oficialia.service.SubjectMatterService;

/**
 * Catalog of subject matters (DFR 2.6), filtered by text and status and split in pages. From here the
 * administrator registers, updates, activates or deactivates them.
 */
@WebServlet("/subject-matters")
public class SubjectMatterListServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/subject-matters/list.jsp";

    private final SubjectMatterService matterService = new SubjectMatterService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        String query = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
        String status = request.getParameter("status");
        Boolean active = "active".equals(status) ? Boolean.TRUE : "inactive".equals(status) ? Boolean.FALSE : null;
        try {
            request.setAttribute("page", matterService.search(user, query, active, parsePage(request)));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        request.setAttribute("query", query);
        request.setAttribute("selectedStatus", active == null ? "" : status);
        request.setAttribute("filtered", !query.isEmpty() || active != null);
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /** A missing or invalid page number shows the first page. */
    private static int parsePage(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter("page"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
