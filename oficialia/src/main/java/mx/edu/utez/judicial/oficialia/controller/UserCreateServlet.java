package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Registers a clerk in the administrator's court (RF 1.4).
 */
@WebServlet("/users/new")
public class UserCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/users/new.jsp";

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isCourtAdmin(request, response)) {
            return;
        }
        show(request, response, new NewUserForm());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isCourtAdmin(request, response)) {
            return;
        }
        NewUserForm form = new NewUserForm();
        form.setFirstName(request.getParameter("firstName"));
        form.setLastName(request.getParameter("lastName"));
        form.setEmail(request.getParameter("email"));
        form.setPhone(request.getParameter("phone"));

        try {
            UserService.Registration registration = userService.register(currentUser(request), form);
            if (registration.isSaved()) {
                request.getSession().setAttribute(UserListServlet.FLASH_REGISTERED_USER, registration.user());
                response.sendRedirect(request.getContextPath() + "/users");
                return;
            }
            request.setAttribute("errors", registration.errors());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (BusinessRuleException e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        show(request, response, form);
    }

    private boolean isCourtAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user.isCourtAdmin()) {
            return true;
        }
        ErrorView.forbidden(request, response, "Solo el Administrador de Juzgado puede registrar usuarios.");
        return false;
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, NewUserForm form)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("maxNameLength", NewUserForm.MAX_NAME_LENGTH);
        request.setAttribute("maxEmailLength", NewUserForm.MAX_EMAIL_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
