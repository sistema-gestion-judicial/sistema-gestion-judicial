package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CaseFileService;

/**
 * Records that a case file goes to second instance and the chamber that takes it (DFR 3.7). On success it
 * opens the receipt; a case file that cannot go is sent back to its detail with the reason.
 */
@WebServlet("/case-files/second-instance")
public class CaseFileSecondInstanceServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/case-files/second-instance.jsp";

    private final CaseFileService caseFileService = new CaseFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String number = request.getParameter("number");
        try {
            request.setAttribute("options", caseFileService.secondInstanceOptions(currentUser(request), number));
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (BusinessRuleException e) {
            backToDetail(request, response, number, e.getMessage());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String number = request.getParameter("number");
        try {
            CaseFile moved = caseFileService.moveToSecondInstance(currentUser(request), number,
                request.getParameter("chamber"));
            request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE, moved.isChamberPending()
                ? "El pase a segunda instancia quedó registrado. La sala queda pendiente porque el sistema de "
                    + "tocas no respondió."
                : "El expediente pasó a segunda instancia con la " + moved.getChamberName() + ".");
            response.sendRedirect(request.getContextPath() + "/case-files/receipt?number="
                + URLEncoder.encode(number, StandardCharsets.UTF_8));
        } catch (BusinessRuleException e) {
            backToDetail(request, response, number, e.getMessage());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    private static void backToDetail(HttpServletRequest request, HttpServletResponse response, String number,
                                     String reason) throws IOException {
        request.getSession().setAttribute(DemandRequestListServlet.FLASH_WARNING, reason);
        response.sendRedirect(request.getContextPath() + "/case-files/detail?number="
            + URLEncoder.encode(number, StandardCharsets.UTF_8));
    }

    private static void notFound(HttpServletRequest request, HttpServletResponse response, NotFoundException e)
            throws ServletException, IOException {
        ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Expediente no encontrado",
            e.getMessage());
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
