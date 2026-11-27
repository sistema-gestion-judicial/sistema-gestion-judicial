package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserStatusForm;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Activates or deactivates a user of the administrator's court (RF 1.7). The users list asks for confirmation,
 * naming the user, before it posts here; the outcome is shown back in the list.
 */
@WebServlet("/users/status")
public class UserStatusServlet extends HttpServlet {

    private final UserService userService = new UserService();

    /** The change is made only by the form of the confirmation, never by opening a link. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/users");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        UserStatusForm form = new UserStatusForm();
        form.setStatus(request.getParameter("status"));
        form.setReason(request.getParameter("reason"));

        try {
            UserService.StatusChange change = userService.changeStatus(admin, request.getParameter("id"), form);
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, change.message());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado",
                e.getMessage());
            return;
        } catch (BusinessRuleException e) {
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_WARNING, e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }
}
