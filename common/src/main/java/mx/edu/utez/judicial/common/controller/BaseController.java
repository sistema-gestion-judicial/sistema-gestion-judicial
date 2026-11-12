package mx.edu.utez.judicial.common.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.judicial.tocas.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Helpers shared by all page controllers. */
public abstract class BaseController extends HttpServlet {

    protected User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    /** Forwards to /WEB-INF/views/{view}.jsp */
    protected void render(HttpServletRequest request, HttpServletResponse response, String view) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void flashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }

    protected void flashSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    protected int intParam(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(request.getParameter(name).trim());
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    protected String stringParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    /** Returns null when the value is missing or not a valid yyyy-MM-dd date. */
    protected LocalDate dateParam(HttpServletRequest request, String name) {
        try {
            return LocalDate.parse(stringParam(request, name));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    protected LocalTime timeParam(HttpServletRequest request, String name) {
        try {
            return LocalTime.parse(stringParam(request, name));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
