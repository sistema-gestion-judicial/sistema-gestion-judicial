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
import mx.edu.utez.judicial.oficialia.model.UserProfile;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Activates or deactivates a user of the court (DFR 1.7). The list asks for confirmation with the user's name
 * before posting here; the record code is the user's e-mail.
 */
@WebServlet("/users/status")
public class UserStatusServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        boolean active = "true".equals(request.getParameter("active"));
        String message;
        String flashKey = DemandRequestListServlet.FLASH_MESSAGE;
        try {
            UserProfile user = userService.changeStatus(admin, request.getParameter("code"), active);
            message = active ? "La cuenta de " + user.getFullName() + " se activó y ya puede entrar al sistema."
                : "La cuenta de " + user.getFullName() + " se desactivó. Lo que capturó sigue mostrando su nombre.";
        } catch (BusinessRuleException e) {
            message = e.getMessage();
            flashKey = DemandRequestListServlet.FLASH_WARNING;
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado",
                e.getMessage());
            return;
        }
        request.getSession().setAttribute(flashKey, message);
        response.sendRedirect(request.getContextPath() + "/users");
    }
}
