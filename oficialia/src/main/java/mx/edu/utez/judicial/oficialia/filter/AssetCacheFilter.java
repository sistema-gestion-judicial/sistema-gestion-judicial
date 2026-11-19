package mx.edu.utez.judicial.oficialia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Makes the browser ask again for the module's styles, scripts and images on every page load.
 * Their URLs never change, so without this a browser could keep the old styles after a deploy.
 * Tomcat answers with 304 when the file did not change, so the check is cheap.
 */
@WebFilter("/assets/*")
public class AssetCacheFilter extends HttpFilter {

    public static final String CACHE_CONTROL = "no-cache";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        response.setHeader("Cache-Control", CACHE_CONTROL);
        chain.doFilter(request, response);
    }
}
