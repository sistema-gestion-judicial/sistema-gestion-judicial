package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.JudicialDistrictService;

/**
 * Catalog of judicial districts (DFR 2.1): a registered district shows up here.
 */
@WebServlet("/judicial-districts")
public class JudicialDistrictListServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/judicial-districts/list.jsp";

    private final JudicialDistrictService districtService = new JudicialDistrictService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        try {
            request.setAttribute("districts", districtService.list(user));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
