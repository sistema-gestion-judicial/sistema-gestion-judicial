package mx.edu.utez.judicial.oficialia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Reads every request as UTF-8 and sends anyone without a session to the login page.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    public static final String SESSION_USER = "currentUser";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");

        boolean isPublic = RouteRules.isPublic(resolvedPath(request));
        HttpSession session = request.getSession(false);
        boolean isSignedIn = session != null && session.getAttribute(SESSION_USER) != null;

        if (isPublic || isSignedIn) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
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
