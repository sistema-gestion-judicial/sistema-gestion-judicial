package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.model.Appeal;
import mx.edu.utez.judicial.tocas.model.Hearing;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.AppealService;
import mx.edu.utez.judicial.tocas.service.BusinessException;
import mx.edu.utez.judicial.tocas.service.HearingService;
import mx.edu.utez.judicial.tocas.service.NotFoundException;

import java.io.IOException;

/** Hearing detail with its record: GET /tocas/hearing?id=, POST /tocas/hearing/entry and /tocas/hearing/finish. */
@WebServlet("/tocas/hearing/*")
public class HearingDetailController extends BaseController {
    private final HearingService hearingService = new HearingService();
    private final AppealService appealService = new AppealService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        try {
            Hearing hearing = hearingService.getOwned(user, intParam(request, "id", 0));
            Appeal appeal = appealService.getOwned(user, hearing.getAppealId());
            request.setAttribute("hearing", hearing);
            request.setAttribute("appeal", appeal);
            request.setAttribute("entries", hearing.getEntries());
            request.setAttribute("entryTypes", HearingService.ENTRY_TYPES);
            request.setAttribute("attendees", hearingService.attendees(appeal));
            request.setAttribute("location", HearingService.location(hearing.getRoomName()));
            request.setAttribute("durationText", hearingService.durationText(hearing));
            render(request, response, "hearings/detail");
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        int id = intParam(request, "id", 0);
        String action = request.getPathInfo() == null ? "" : request.getPathInfo();
        String back = "/tocas/hearing?id=" + id;
        try {
            switch (action) {
                case "/entry" -> {
                    try {
                        hearingService.addEntry(user, id, stringParam(request, "type"), request.getParameter("text"));
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                    }
                    redirect(request, response, back);
                }
                case "/finish" -> {
                    try {
                        Hearing h = hearingService.finish(user, id);
                        flashSuccess(request, "Ponencia finalizada. El registro ya no puede modificarse.");
                    } catch (BusinessException e) {
                        flashError(request, e.getMessage());
                    }
                    redirect(request, response, back);
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
