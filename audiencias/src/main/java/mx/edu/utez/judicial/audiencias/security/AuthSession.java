package mx.edu.utez.judicial.audiencias.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import mx.edu.utez.judicial.audiencias.model.user.Role;
import mx.edu.utez.judicial.audiencias.model.user.User;

/** The only place that knows how the logged-in user is stored in the session. */
public final class AuthSession {

    /** Also readable from JSP as ${sessionScope.authUser}. */
    public static final String USER_ATTRIBUTE = "authUser";

    private static final int IDLE_TIMEOUT_SECONDS = 30 * 60;

    private AuthSession() {
    }

    /** Call after the old session was invalidated, so the new session gets a new id. */
    public static void start(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ATTRIBUTE,
            new SessionUser(user.getId(), user.getFullName(), user.getRole()));
        session.setMaxInactiveInterval(IDLE_TIMEOUT_SECONDS);
    }

    public static Optional<SessionUser> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(USER_ATTRIBUTE) instanceof SessionUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** Where each role lands after logging in, relative to the context path. */
    public static String homePath(Role role) {
        return role == Role.ADMINISTRATOR ? "/mesas" : "/inicioabogados.jsp";
    }
}
