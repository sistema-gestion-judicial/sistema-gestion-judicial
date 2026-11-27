package mx.edu.utez.judicial.audiencias.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.audiencias.dao.DataAccessException;
import mx.edu.utez.judicial.audiencias.dao.HearingsDaos;
import mx.edu.utez.judicial.audiencias.security.AuthSession;
import mx.edu.utez.judicial.audiencias.service.ServiceAuth;
import mx.edu.utez.judicial.audiencias.service.ServiceAuth.AuthResult;

/** GET /login shows the form; POST /login checks the credentials and starts the session. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/auth/login.jsp";

    /** Null when the database is not configured. One instance, so the attempt limiter is shared. */
    private ServiceAuth serviceAuth;

    @Override
    public void init() {
        serviceAuth = HearingsDaos.database()
            .map(database -> new ServiceAuth(HearingsDaos.userDao()))
            .orElse(null);
        if (serviceAuth == null) {
            LOG.warning("AUDIENCIAS_DB_URL is not set: nobody can log in");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        var current = AuthSession.current(req);
        if (current.isPresent()) {
            resp.sendRedirect(req.getContextPath() + AuthSession.homePath(current.get().getRole()));
            return;
        }
        showForm(req, resp, HttpServletResponse.SC_OK, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            showForm(req, resp, HttpServletResponse.SC_BAD_REQUEST, email,
                "Introduzca un correo y una contraseña para iniciar sesión.");
            return;
        }
        if (serviceAuth == null) {
            showForm(req, resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, email,
                "El servicio no está disponible. Inténtelo más tarde.");
            return;
        }

        AuthResult result;
        try {
            result = serviceAuth.authenticate(email, password);
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Database error while logging in", e);
            showForm(req, resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, email,
                "El servicio no está disponible. Inténtelo más tarde.");
            return;
        }

        switch (result.outcome()) {
            case SUCCESS -> {
                // A new session id after login stops session fixation
                HttpSession old = req.getSession(false);
                if (old != null) {
                    old.invalidate();
                }
                AuthSession.start(req, result.user());
                resp.sendRedirect(req.getContextPath() + AuthSession.homePath(result.user().getRole()));
            }
            case LOCKED -> {
                resp.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
                long minutes = (result.retryAfterSeconds() + 59) / 60;
                showForm(req, resp, 429, email, "Demasiados intentos fallidos. Inténtelo de nuevo en "
                    + minutes + (minutes == 1 ? " minuto." : " minutos."));
            }
            case INVALID_CREDENTIALS -> showForm(req, resp, HttpServletResponse.SC_UNAUTHORIZED, email,
                "Correo o contraseña incorrectos.");
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, int status, String email,
                          String errorMessage) throws ServletException, IOException {
        resp.setStatus(status);
        resp.setHeader("Cache-Control", "no-store");
        req.setAttribute("errorMessage", errorMessage);
        req.setAttribute("email", email);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
