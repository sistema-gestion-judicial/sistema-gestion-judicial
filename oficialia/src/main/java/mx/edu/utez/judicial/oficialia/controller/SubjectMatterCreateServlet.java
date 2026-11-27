package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.SubjectMatterService;

/**
 * Registers a subject matter (DFR 2.5).
 */
@WebServlet("/subject-matters/new")
public class SubjectMatterCreateServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/subject-matters/new.jsp";

    private final SubjectMatterService matterService = new SubjectMatterService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // The filter already refuses other profiles; this keeps the rule in the service as well
        try {
            matterService.requireAccess(currentUser(request));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        show(request, response, new SubjectMatterForm());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SubjectMatterForm form = new SubjectMatterForm();
        form.setCode(request.getParameter("code"));
        form.setName(request.getParameter("name"));
        form.setDescription(request.getParameter("description"));

        try {
            SubjectMatterService.Saving saving = matterService.register(currentUser(request), form);
            if (saving.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "La materia «" + saving.matter().getName() + "» (clave " + saving.matter().getCode()
                        + ") quedó registrada como activa.");
                response.sendRedirect(request.getContextPath() + "/subject-matters");
                return;
            }
            request.setAttribute("errors", saving.errors());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
            return;
        }
        show(request, response, form);
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, SubjectMatterForm form)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("maxCodeLength", SubjectMatterForm.MAX_CODE_LENGTH);
        request.setAttribute("maxNameLength", SubjectMatterForm.MAX_NAME_LENGTH);
        request.setAttribute("maxDescriptionLength", SubjectMatterForm.MAX_DESCRIPTION_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
