package mx.edu.utez.judicial.oficialia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Reads every request as UTF-8 and sends anyone without a session to the login page.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    public static final String SESSION_USER = "currentUser";

    private static final List<String> PUBLIC_PATHS =
            List.of("/login", "/assets/", "/webjars/", "/api/v1/health");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        HttpSession session = request.getSession(false);
        boolean isSignedIn = session != null && session.getAttribute(SESSION_USER) != null;

        if (isPublic || isSignedIn) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
