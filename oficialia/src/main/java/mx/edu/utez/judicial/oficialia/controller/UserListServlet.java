package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.RegisteredUser;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Lists the users of the administrator's court (DFR 1.5), filtered by text and status and split in pages. A
 * registered or corrected user shows up here.
 */
@WebServlet("/users")
public class UserListServlet extends HttpServlet {

    /** Session key of the user just registered; shown once on this page, with the temporary password. */
    public static final String FLASH_REGISTERED_USER = "flashRegisteredUser";

    private static final String VIEW = "/WEB-INF/views/users/list.jsp";

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        String query = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
        String status = request.getParameter("status");
        Boolean active = "active".equals(status) ? Boolean.TRUE : "inactive".equals(status) ? Boolean.FALSE : null;
        try {
            request.setAttribute("page", userService.search(user, query, active, parsePage(request)));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        request.setAttribute("query", query);
        request.setAttribute("selectedStatus", active == null ? "" : status);
        request.setAttribute("filtered", !query.isEmpty() || active != null);
        moveRegisteredUser(request);
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

    /** The temporary password is shown only once: it is removed from the session as soon as it is read. */
    private static void moveRegisteredUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        RegisteredUser registered = (RegisteredUser) session.getAttribute(FLASH_REGISTERED_USER);
        if (registered != null) {
            request.setAttribute("registeredUser", registered);
            session.removeAttribute(FLASH_REGISTERED_USER);
        }
    }
}
