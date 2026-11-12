package mx.edu.utez.judicial.oficialia.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Removes the session cookie from the browser. Invalidating a session does not do it by itself.
 */
public final class SessionCookie {

    private static final String DEFAULT_NAME = "JSESSIONID";

    private SessionCookie() {
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
        String configuredName = request.getServletContext().getSessionCookieConfig().getName();
        Cookie cookie = new Cookie(configuredName == null ? DEFAULT_NAME : configuredName, "");
        cookie.setPath(pathFor(request.getContextPath()));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /** The browser only replaces a cookie with the same path, and an application at the root has an empty context. */
    static String pathFor(String contextPath) {
        return contextPath.isEmpty() ? "/" : contextPath;
    }
}
