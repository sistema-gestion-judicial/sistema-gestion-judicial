package mx.edu.utez.judicial.audiencias.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mx.edu.utez.judicial.audiencias.security.AuthSession;
import mx.edu.utez.judicial.audiencias.security.SessionUser;

/**
 * Everything that is not public needs a session. Pages under WEB-INF are only reached through a
 * servlet or a forward from one of these pages, so protecting these paths protects them too.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    /** Reachable without a session. */
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/", "/index.jsp", "/login", "/logout", "/recover.jsp", "/verify.jsp", "/newPassword.jsp");

    private static final List<String> PUBLIC_PREFIXES = List.of("/assets/");

    /** These answer for themselves: /mesas answers JSON 401 to fetch(), /api/ uses the API key. */
    private static final Set<String> SELF_PROTECTED_PATHS = Set.of("/mesas");
    private static final List<String> SELF_PROTECTED_PREFIXES = List.of("/api/");

    private static final Set<String> ADMIN_ONLY_PATHS = Set.of("/mesas.jsp", "/abogados.jsp");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String path = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Private pages must not be kept by the browser: "back" after logging out would show them
        response.setHeader("Cache-Control", "no-store");

        if (isSelfProtected(path)) {
            chain.doFilter(request, response);
            return;
        }

        Optional<SessionUser> user = AuthSession.current(request);
        if (user.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (ADMIN_ONLY_PATHS.contains(path) && !user.get().isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path) || PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private static boolean isSelfProtected(String path) {
        return SELF_PROTECTED_PATHS.contains(path)
            || SELF_PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
