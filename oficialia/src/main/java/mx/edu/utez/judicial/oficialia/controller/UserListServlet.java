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
import mx.edu.utez.judicial.oficialia.model.UserStatusForm;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Lists the users of the administrator's court (RF 1.4 to 1.7): a registered or corrected user
 * shows up here, and each row can be activated or deactivated after a confirmation.
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
        try {
            request.setAttribute("users", userService.listForCourt(user));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        moveRegisteredUser(request);
        DemandRequestListServlet.moveFlashMessage(request);
        request.setAttribute("maxReasonLength", UserStatusForm.MAX_REASON_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
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
