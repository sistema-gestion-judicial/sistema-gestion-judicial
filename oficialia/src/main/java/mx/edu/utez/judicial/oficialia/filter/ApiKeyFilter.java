package mx.edu.utez.judicial.oficialia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import mx.edu.utez.judicial.common.api.ApiError;
import mx.edu.utez.judicial.common.api.ApiJson;
import mx.edu.utez.judicial.oficialia.api.ApiCredentials;

/**
 * Every API call except the health check must send its system's key in the X-Api-Key header.
 */
@WebFilter("/api/v1/*")
public class ApiKeyFilter extends HttpFilter {

    public static final String API_CLIENT = "apiClient";

    private static final Logger LOG = Logger.getLogger(ApiKeyFilter.class.getName());

    private ApiCredentials credentials;

    @Override
    public void init() {
        credentials = ApiCredentials.fromEnvironment();
        if (credentials.isEmpty()) {
            LOG.warning("OFICIALIA_API_KEYS is not set: every API call except /api/v1/health is rejected");
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.equals("/api/v1/health")) {
            chain.doFilter(request, response);
            return;
        }
        Optional<String> system = credentials.systemFor(request.getHeader("X-Api-Key"));
        if (system.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("WWW-Authenticate", "ApiKey header=\"X-Api-Key\"");
            response.getWriter().write(ApiJson.write(ApiError.of("INVALID_CREDENTIALS",
                "Missing or invalid X-Api-Key header.")));
            return;
        }
        request.setAttribute(API_CLIENT, system.get());
        chain.doFilter(request, response);
    }
}
