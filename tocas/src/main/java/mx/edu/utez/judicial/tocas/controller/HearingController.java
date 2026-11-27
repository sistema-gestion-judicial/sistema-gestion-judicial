package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.service.BusinessException;
import mx.edu.utez.judicial.tocas.service.FormatUtils;
import mx.edu.utez.judicial.tocas.service.HearingService;
import mx.edu.utez.judicial.tocas.service.NotFoundException;

import java.io.IOException;

/** Handles the "Agendar ponencia" form. */
@WebServlet("/tocas/hearings")
public class HearingController extends BaseController {
    private final HearingService hearingService = new HearingService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int appealId = intParam(request, "appealId", 0);
        String returnTab = stringParam(request, "returnTab").equals("resumen") ? "resumen" : "ponencias";
        try {
            var hearing = hearingService.schedule(currentUser(request), appealId, dateParam(request, "date"),
                    timeParam(request, "time"), stringParam(request, "type"));
            flashSuccess(request, FormatUtils.hearingType(hearing.getType()) + " agendada para el " + FormatUtils.dateNum(hearing.getDate()) + ".");
            redirect(request, response, "/tocas/detail?id=" + appealId + "&tab=" + returnTab + "&hearing=" + hearing.getId());
        } catch (BusinessException e) {
            flashError(request, e.getMessage());
            redirect(request, response, "/tocas/detail?id=" + appealId + "&tab=" + returnTab);
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
