package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
import mx.edu.utez.judicial.oficialia.model.UserProfile;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Corrects the name, e-mail, phone and profile of a user of the court (DFR 1.6). The form has no password.
 */
@WebServlet("/users/edit")
public class UserEditServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/users/edit.jsp";
    private static final String NOT_FOUND_TITLE = "Usuario no encontrado";

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserProfile profile = userService.get(currentUser(request), request.getParameter("email"));
            show(request, response, profile.getEmail(), UserEditForm.of(profile));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TITLE, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = currentUser(request);
        // The current e-mail says which user to correct; the "email" field carries the new one
        String currentEmail = request.getParameter("currentEmail");
        UserEditForm form = new UserEditForm();
        form.setFirstName(request.getParameter("firstName"));
        form.setLastName(request.getParameter("lastName"));
        form.setEmail(request.getParameter("email"));
        form.setPhone(request.getParameter("phone"));
        form.setRole(request.getParameter("role"));

        try {
            UserService.Update update = userService.update(admin, currentEmail, form);
            if (update.isSaved()) {
                UserProfile saved = update.user();
                if (admin.getEmail().equalsIgnoreCase(currentEmail)) {
                    // The session keeps the e-mail it checks on every request, so it follows the change
                    request.getSession().setAttribute(AuthFilter.SESSION_USER, new User(saved.getEmail(),
                        saved.getFullName(), saved.getRole(), saved.getCourtName(), admin.isMustChangePassword()));
                }
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "Los datos de " + saved.getFullName() + " se actualizaron.");
                response.sendRedirect(request.getContextPath() + "/users");
                return;
            }
            request.setAttribute("errors", update.errors());
            show(request, response, currentEmail, form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TITLE, e.getMessage());
        }
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, String currentEmail,
            UserEditForm form) throws ServletException, IOException {
        request.setAttribute("currentEmail", currentEmail);
        request.setAttribute("form", form);
        request.setAttribute("roles", UserEditForm.COURT_ROLES);
        request.setAttribute("maxNameLength", NewUserForm.MAX_NAME_LENGTH);
        request.setAttribute("maxEmailLength", NewUserForm.MAX_EMAIL_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
