package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.service.CaseFileService;

/**
 * One case file of the user's court (DFR 3.6). It shows only what Oficialía knows: hearings and resolutions
 * belong to the other systems and are not offered here.
 */
@WebServlet("/case-files/detail")
public class CaseFileDetailServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/case-files/detail.jsp";

    private final CaseFileService caseFileService = new CaseFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        try {
            request.setAttribute("caseFile", caseFileService.get(user, request.getParameter("number")));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Expediente no encontrado",
                e.getMessage());
            return;
        }
        request.setAttribute("isClerk", user.getRole() == UserRole.CLERK);
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
