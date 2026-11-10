package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * Starting point after signing in. The judicial body administrator has no court, so that role
 * gets a page of its own; everyone else goes to the demand requests of their court.
 */
@WebServlet(HomeServlet.PATH)
public class HomeServlet extends HttpServlet {

    public static final String PATH = "/home";

    private static final String VIEW = "/WEB-INF/views/home/home.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        if (user.getRole() == UserRole.JUDICIAL_BODY_ADMIN) {
            request.getRequestDispatcher(VIEW).forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/demand-requests");
        }
    }
}
