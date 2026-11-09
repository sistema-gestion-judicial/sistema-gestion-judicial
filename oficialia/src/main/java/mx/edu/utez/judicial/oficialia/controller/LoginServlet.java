package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.SignInResult;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.AuthService;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Shows the login form and starts the session (RF 1.1).
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/auth/login.jsp";
    private static final String INACTIVE_ACCOUNT_MESSAGE =
            "Su cuenta está inactiva. Contacte al administrador del sistema.";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + HomeServlet.PATH);
            return;
        }
        if (request.getParameter("logout") != null) {
            request.setAttribute("infoMessage", "Cerró sesión correctamente.");
        }
        if (request.getParameter("expired") != null) {
            request.setAttribute("expiredMessage", "Su sesión se cerró por inactividad. Vuelva a iniciar sesión.");
        }
        if (request.getParameter("inactive") != null) {
            request.setAttribute("errorMessage", INACTIVE_ACCOUNT_MESSAGE);
        }
        showLoginForm(request, response);
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
        if (!EmailValidator.isValid(email)) {
            request.setAttribute("emailError", "Escriba un correo con formato válido.");
            request.setAttribute("email", email);
            showLoginForm(request, response);
            return;
        }

        SignInResult result = authService.signIn(email, password);
        switch (result.status()) {
            case SIGNED_IN -> startSession(request, response, result.user());
            // Same message for unknown email or wrong password, so the form does not reveal which accounts exist
            case INVALID_CREDENTIALS ->
                    showError(request, response, email, "El correo o la contraseña no son correctos.");
            case INACTIVE_ACCOUNT -> showError(request, response, email, INACTIVE_ACCOUNT_MESSAGE);
            case LOCKED -> showError(request, response, email, lockedMessage(result.lockedMinutes()));
        }
    }

    private static String lockedMessage(long minutes) {
        return "Demasiados intentos fallidos. Intente de nuevo en " + minutes
                + (minutes == 1 ? " minuto." : " minutos.");
    }

    private void startSession(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval((int) Duration.ofMinutes(AuthSettings.SESSION_TIMEOUT_MINUTES).toSeconds());
        session.setAttribute(AuthFilter.SESSION_USER, user);
        response.sendRedirect(request.getContextPath() + HomeServlet.PATH);
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String email,
            String message) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("email", email);
        showLoginForm(request, response);
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Without this, the back button could bring back the form with the password still typed in it
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
