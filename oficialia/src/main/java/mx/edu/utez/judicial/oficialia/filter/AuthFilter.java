package mx.edu.utez.judicial.oficialia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.controller.ErrorView;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.AuthService;
import mx.edu.utez.judicial.oficialia.util.SessionCookie;

/**
 * Reads every request as UTF-8, sends anyone without a session to the login page, ends the
 * session of accounts that are no longer active and stops users whose role cannot open the page.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    public static final String SESSION_USER = "currentUser";

    private final AuthService authService = new AuthService();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");

        String path = resolvedPath(request);
        if (RouteRules.isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        User user = signedInUser(request);
        if (user == null) {
            sendToLogin(request, response);
            return;
        }
        if (!authService.isActive(user.getEmail())) {
            request.getSession().invalidate();
            SessionCookie.clear(request, response);
            response.sendRedirect(request.getContextPath() + "/login?inactive");
            return;
        }
        if (!RouteRules.canAccess(path, user.getRole())) {
            ErrorView.forbidden(request, response, "Su perfil no tiene acceso a esta sección.");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * A session cookie the server no longer recognizes means the session expired. It is removed
     * so the notice appears only once; signing out already removes it, so that case shows no notice.
     */
    private static void sendToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean sessionExpired = request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();
        if (sessionExpired) {
            SessionCookie.clear(request, response);
        }
        response.sendRedirect(request.getContextPath() + (sessionExpired ? "/login?expired" : "/login"));
    }

    /** Returns the user of the current session, or null when nobody is signed in. */
    private static User signedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(SESSION_USER);
    }

    /**
     * The path the server actually resolved. getRequestURI() returns the raw text, so a request
     * for /assets/../demand-requests would look public and still reach the private page.
     */
    private static String resolvedPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return pathInfo == null ? request.getServletPath() : request.getServletPath() + pathInfo;
    }
}
