package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.PasswordChangeForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.AuthService;

/**
 * The screen where a newly registered user replaces the temporary password before using the system
 * (RF 1.4). {@link mx.edu.utez.judicial.oficialia.filter.AuthFilter} sends them here until they do.
 */
@WebServlet(ChangePasswordServlet.PATH)
public class ChangePasswordServlet extends HttpServlet {

    public static final String PATH = "/password/change";

    private static final String VIEW = "/WEB-INF/views/auth/change-password.jsp";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!passwordIsTemporary(request, response)) {
            return;
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!passwordIsTemporary(request, response)) {
            return;
        }
        PasswordChangeForm form = new PasswordChangeForm();
        form.setNewPassword(request.getParameter("newPassword"));
        form.setConfirmation(request.getParameter("confirmation"));

        User user = currentUser(request);
        Map<String, String> errors = authService.changeTemporaryPassword(user, form);
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }
        // The filter reads the session user, so it must stop seeing the temporary password
        request.getSession().setAttribute(AuthFilter.SESSION_USER, user.withPasswordChanged());
        request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
            "Su contraseña se actualizó. Ya puede usar el sistema.");
        response.sendRedirect(request.getContextPath() + HomeServlet.PATH);
    }

    /** Users with a password of their own have nothing to do here: they go to the home page. */
    private static boolean passwordIsTemporary(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (currentUser(request).isMustChangePassword()) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + HomeServlet.PATH);
        return false;
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
