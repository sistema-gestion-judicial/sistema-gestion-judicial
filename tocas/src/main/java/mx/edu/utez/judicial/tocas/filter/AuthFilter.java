package mx.edu.utez.judicial.tocas.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Only logged-in users can reach the application (except login, the health check and static files). */
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        boolean isStatic = path.startsWith("/assets/") || path.startsWith("/webjars/");
        if (isStatic) {
            chain.doFilter(req, res);
            return;
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("user") != null;
        // The health check must answer without a session (deploy scripts and other systems call it)
        boolean isPublic = path.equals("/login") || path.equals("/api/v1/health");
        if (loggedIn || isPublic) {
            chain.doFilter(req, res);
            return;
        }
        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":false,\"message\":\"Su sesión expiró. Inicie sesión de nuevo.\"}");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
