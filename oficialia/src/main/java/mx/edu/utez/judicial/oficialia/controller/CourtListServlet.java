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
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.service.CourtService;

/**
 * Courts catalog (DFR 2.10), open to every profile. It only shows the courts: no request or case file can be
 * opened from here.
 */
@WebServlet("/courts")
public class CourtListServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/courts/list.jsp";

    private final CourtService courtService = new CourtService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        boolean canSeeInactive = CourtService.canSeeInactive(user);
        String query = trimmed(request.getParameter("q"));
        String district = trimmed(request.getParameter("district"));
        String matter = trimmed(request.getParameter("matter"));
        String status = canSeeInactive ? trimmed(request.getParameter("status")) : "";
        Boolean active = "active".equals(status) ? Boolean.TRUE : "inactive".equals(status) ? Boolean.FALSE : null;
        try {
            request.setAttribute("page", courtService.search(user, query, district, matter, active,
                parsePage(request)));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        request.setAttribute("districts", courtService.districts());
        request.setAttribute("subjectMatters", courtService.subjectMatters());
        request.setAttribute("canSeeInactive", canSeeInactive);
        // Only the judicial body administrator registers courts (DFR 2.9)
        request.setAttribute("canRegister", user != null && user.getRole() == UserRole.JUDICIAL_BODY_ADMIN);
        request.setAttribute("query", query);
        request.setAttribute("selectedDistrict", district);
        request.setAttribute("selectedMatter", matter);
        request.setAttribute("selectedStatus", active == null ? "" : status);
        request.setAttribute("filtered", !query.isEmpty() || !district.isEmpty() || !matter.isEmpty()
            || active != null);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
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
