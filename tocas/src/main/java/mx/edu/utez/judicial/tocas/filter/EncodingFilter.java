package mx.edu.utez.judicial.tocas.filter;

import jakarta.servlet.*;

import java.io.IOException;

/** Forces UTF-8 on every request and response. */
public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
