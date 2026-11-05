package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.AuthService;

/**
 * Shows the login form and starts the session (RF 1.1).
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/auth/login.jsp";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/demand-requests");
            return;
        }
        if (request.getParameter("logout") != null) {
            request.setAttribute("infoMessage", "Cerró sesión correctamente.");
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError(request, response, email, "Escriba su correo electrónico y su contraseña.");
            return;
        }

        Optional<User> user = authService.authenticate(email, password);
        if (user.isEmpty()) {
            // Same message for unknown email or wrong password, so the form does not reveal which accounts exist
            showError(request, response, email, "El correo o la contraseña no son correctos.");
            return;
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthFilter.SESSION_USER, user.get());
        response.sendRedirect(request.getContextPath() + "/demand-requests");
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String email,
            String message) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("email", email);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
