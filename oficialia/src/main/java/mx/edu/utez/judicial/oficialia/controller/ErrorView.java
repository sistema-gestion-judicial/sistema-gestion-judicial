package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Shows a message page inside the application layout, with the HTTP status it belongs to.
 */
public final class ErrorView {

    private static final String VIEW = "/WEB-INF/views/error/message.jsp";

    private ErrorView() {
    }

    static void show(HttpServletRequest request, HttpServletResponse response, int status, String title,
            String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("errorTitle", title);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    static void notFound(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        show(request, response, HttpServletResponse.SC_NOT_FOUND, "Solicitud no encontrada", message);
    }

    public static void forbidden(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        show(request, response, HttpServletResponse.SC_FORBIDDEN, "Permiso insuficiente", message);
    }
}
