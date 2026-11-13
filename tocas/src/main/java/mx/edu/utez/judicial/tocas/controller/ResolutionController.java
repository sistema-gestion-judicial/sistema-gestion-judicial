package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mx.edu.utez.judicial.tocas.model.Appeal;
import mx.edu.utez.judicial.tocas.model.Resolution;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.AppealService;
import mx.edu.utez.judicial.tocas.service.BusinessException;
import mx.edu.utez.judicial.tocas.service.NotFoundException;
import mx.edu.utez.judicial.tocas.service.ResolutionService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resolution screens and actions.
 * GET: /edit, /preview, /signed   POST: /create, /save, /finalize, /upload, /conclude
 */
@WebServlet("/tocas/resolution/*")
@MultipartConfig(maxFileSize = 21L * 1024 * 1024, maxRequestSize = 22L * 1024 * 1024)
public class ResolutionController extends BaseController {
    private final AppealService appealService = new AppealService();
    private final ResolutionService resolutionService = new ResolutionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        int id = intParam(request, "id", 0);
        String action = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            Appeal appeal = appealService.getOwned(user, id);
            Resolution resolution = resolutionService.find(user, id);
            switch (action) {
                case "/edit" -> {
                    if (resolution == null || !resolution.isDraft() || appeal.isConcluded()) {
                        flashError(request, "La resolución no se puede editar.");
                        redirect(request, response, "/tocas/detail?id=" + id + "&tab=resolucion");
                        return;
                    }
                    request.setAttribute("appeal", appeal);
                    request.setAttribute("resolution", resolution);
                    request.setAttribute("user", user);
                    render(request, response, "resolution/edit");
                }
                case "/preview" -> {
                    if (resolution == null) {
                        flashError(request, "Aún no se ha iniciado la resolución.");
                        redirect(request, response, "/tocas/detail?id=" + id + "&tab=resolucion");
                        return;
                    }
                    request.setAttribute("appeal", appeal);
                    request.setAttribute("resolution", resolution);
                    request.setAttribute("autoPrint", "1".equals(request.getParameter("print")));
                    render(request, response, "resolution/preview");
                }
                case "/signed" -> {
                    byte[] bytes = resolutionService.signedBytes(user, id);
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "inline; filename=\"resolucion-firmada.pdf\"");
                    response.setContentLength(bytes.length);
                    response.getOutputStream().write(bytes);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        int id = intParam(request, "id", 0);
        String action = request.getPathInfo() == null ? "" : request.getPathInfo();
        String backToResolution = "/tocas/detail?id=" + id + "&tab=resolucion";
        String backToEditor = "/tocas/resolution/edit?id=" + id;
        try {
            switch (action) {
                case "/create" -> {
                    resolutionService.create(user, id);
                    redirect(request, response, "/tocas/resolution/edit?id=" + id);
                }
                case "/save" -> {
                    try {
                        Resolution r = resolutionService.saveDraft(user, id, request.getParameter("resultando"),
                                request.getParameter("considerando"), request.getParameter("resuelve"), false);
                        flashSuccess(request, "Borrador guardado (versión " + r.getVersion() + ").");
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                    }
                    redirect(request, response, backToEditor);
                }
                case "/finalize" -> {
                    try {
                        resolutionService.finalizeResolution(user, id, request.getParameter("resultando"),
                                request.getParameter("considerando"), request.getParameter("resuelve"));
                        flashSuccess(request, "Resolución finalizada. Ya no puede editarse.");
                        redirect(request, response, backToResolution);
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                        redirect(request, response, backToEditor);
                    }
                }
                case "/upload" -> {
                    try {
                        Part part = request.getPart("file");
                        if (part == null || part.getSize() == 0) throw new BusinessException("Seleccione un archivo PDF.");
                        try (InputStream in = part.getInputStream()) {
                            resolutionService.uploadSigned(user, id, part.getSubmittedFileName(), in);
                        }
                        flashSuccess(request, "Resolución firmada cargada correctamente.");
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                    } catch (IllegalStateException e) {
                        flashError(request, "El archivo excede el máximo de 20 MB.");
                    }
                    redirect(request, response, backToResolution);
                }
                case "/conclude" -> {
                    try {
                        appealService.conclude(user, id);
                        flashSuccess(request, "La toca fue concluida.");
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                    }
                    redirect(request, response, backToResolution);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            flashError(request, e.getMessage());
            redirect(request, response, backToResolution);
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
