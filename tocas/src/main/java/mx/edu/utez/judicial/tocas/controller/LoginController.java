package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.judicial.common.controller.BaseController;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.AuthService;
import mx.edu.utez.judicial.tocas.service.BusinessException;

import java.io.IOException;

/** Login screen (default, error and focus states are handled by the JSP/CSS). */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginController extends BaseController {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getServletPath().equals("/logout")) {
            logout(request, response);
            return;
        }
        if (currentUser(request) != null) {
            redirect(request, response, "/home");
            return;
        }
        render(request, response, "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getServletPath().equals("/logout")) {
            logout(request, response);
            return;
        }
        String email = stringParam(request, "email");
        try {
            User user = authService.authenticate(email, request.getParameter("password"));
            HttpSession old = request.getSession(false);
            if (old != null) old.invalidate(); // avoid session fixation
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60);
            redirect(request, response, "/home");
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("email", email);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            render(request, response, "login");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        redirect(request, response, "/login");
    }
}
