package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.CaseFileStatus;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CaseFileService;

/**
 * The case files of the user's court and their status (DFR 3.6), filtered by text and status and split in pages.
 */
@WebServlet("/case-files")
public class CaseFileListServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/case-files/list.jsp";

    private final CaseFileService caseFileService = new CaseFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        String query = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
        CaseFileStatus status = Arrays.stream(CaseFileStatus.values())
            .filter(candidate -> candidate.name().equals(request.getParameter("status")))
            .findFirst().orElse(null);
        try {
            request.setAttribute("page", caseFileService.listForCourt(user, query, status, parsePage(request)));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        request.setAttribute("statuses", CaseFileStatus.values());
        request.setAttribute("query", query);
        request.setAttribute("selectedStatus", status == null ? "" : status.name());
        request.setAttribute("filtered", !query.isEmpty() || status != null);
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
