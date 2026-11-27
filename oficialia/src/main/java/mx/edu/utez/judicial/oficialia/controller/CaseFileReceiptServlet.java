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
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.CaseFileService;

/**
 * The printable receipt (acuse) of the move to second instance (DFR 3.7). It is built from what was saved,
 * so it can be printed again later.
 */
@WebServlet("/case-files/receipt")
public class CaseFileReceiptServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/case-files/receipt.jsp";

    private final CaseFileService caseFileService = new CaseFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
        try {
            CaseFile caseFile = caseFileService.get(user, request.getParameter("number"));
            if (!caseFile.isSecondInstance()) {
                throw new NotFoundException("El expediente no tiene pase a segunda instancia.");
            }
            request.setAttribute("caseFile", caseFile);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Acuse no disponible",
                e.getMessage());
            return;
        }
        DemandRequestListServlet.moveFlashMessage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
