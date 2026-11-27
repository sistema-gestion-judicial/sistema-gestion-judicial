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
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
import mx.edu.utez.judicial.oficialia.service.UserService;

/**
 * Corrects the name, e-mail, phone and profile of a user of the administrator's court (RF 1.6). The form has
 * no password field: the password is changed only by its owner.
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
            UserDetails user = userService.getForEdit(currentUser(request), request.getParameter("id"));
            show(request, response, user, UserEditForm.of(user));
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
        String id = request.getParameter("id");
        UserEditForm form = new UserEditForm();
        form.setFirstName(request.getParameter("firstName"));
        form.setLastName(request.getParameter("lastName"));
        form.setEmail(request.getParameter("email"));
        form.setPhone(request.getParameter("phone"));
        form.setRole(request.getParameter("role"));

        try {
            UserService.Correction correction = userService.update(admin, id, form);
            if (correction.isSaved()) {
                UserDetails saved = correction.user();
                if (correction.self()) {
                    // The session keeps the name and e-mail shown in the header and used to sign the audit records
                    request.getSession().setAttribute(AuthFilter.SESSION_USER, new User(saved.getEmail(),
                        saved.getFullName(), saved.getRole(), admin.getCourtName(), admin.isMustChangePassword()));
                }
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "Los datos de " + saved.getFullName() + " se actualizaron.");
                response.sendRedirect(request.getContextPath() + "/users");
                return;
            }
            request.setAttribute("errors", correction.errors());
            show(request, response, userService.getForEdit(admin, id), form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TITLE, e.getMessage());
        }
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, UserDetails user,
            UserEditForm form) throws ServletException, IOException {
        request.setAttribute("editedUser", user);
        request.setAttribute("form", form);
        request.setAttribute("roles", UserEditForm.COURT_ROLES);
        request.setAttribute("isSelf", user.getEmail().equalsIgnoreCase(currentUser(request).getEmail()));
        request.setAttribute("maxNameLength", NewUserForm.MAX_NAME_LENGTH);
        request.setAttribute("maxEmailLength", NewUserForm.MAX_EMAIL_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
