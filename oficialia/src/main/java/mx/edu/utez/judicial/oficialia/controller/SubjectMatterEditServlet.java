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
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.SubjectMatterService;

/**
 * Updates the name and the description of a subject matter; its code is shown locked (DFR 2.7).
 */
@WebServlet("/subject-matters/edit")
public class SubjectMatterEditServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/subject-matters/edit.jsp";
    private static final String NOT_FOUND_TITLE = "Materia no encontrada";

    private final SubjectMatterService matterService = new SubjectMatterService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            SubjectMatter matter = matterService.get(currentUser(request), request.getParameter("code"));
            show(request, response, matter.getCode(), SubjectMatterForm.of(matter));
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TITLE, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        // The code only says which subject matter to update: the form has no field to change it
        SubjectMatterForm form = new SubjectMatterForm();
        form.setCode(code);
        form.setName(request.getParameter("name"));
        form.setDescription(request.getParameter("description"));

        try {
            SubjectMatterService.Saving saving = matterService.update(currentUser(request), code, form);
            if (saving.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "La materia «" + saving.matter().getName() + "» se actualizó. Las solicitudes registradas "
                        + "con ella la siguen mostrando.");
                response.sendRedirect(request.getContextPath() + "/subject-matters");
                return;
            }
            request.setAttribute("errors", saving.errors());
            show(request, response, code, form);
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, NOT_FOUND_TITLE, e.getMessage());
        }
    }

    private static void show(HttpServletRequest request, HttpServletResponse response, String code,
            SubjectMatterForm form) throws ServletException, IOException {
        request.setAttribute("code", code);
        request.setAttribute("form", form);
        request.setAttribute("maxNameLength", SubjectMatterForm.MAX_NAME_LENGTH);
        request.setAttribute("maxDescriptionLength", SubjectMatterForm.MAX_DESCRIPTION_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
