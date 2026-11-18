package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Starting point after signing in: every role goes to the demand requests of their court.
 *
 * <p>DEMO (temporary): the judicial body administrator used to get home.jsp ("coming soon"). While
 * the clerk workflow is shown, that role works as the Oficial de Partes of Cuautla. Revert this commit
 * to restore it.
 */
@WebServlet(HomeServlet.PATH)
public class HomeServlet extends HttpServlet {

    public static final String PATH = "/home";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // DEMO (temporary): the judicial body administrator also starts at the demand requests
        response.sendRedirect(request.getContextPath() + "/demand-requests");
    }
}
