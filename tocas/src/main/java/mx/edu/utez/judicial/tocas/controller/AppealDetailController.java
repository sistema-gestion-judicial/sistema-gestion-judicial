package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.model.*;
import mx.edu.utez.judicial.tocas.service.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Appeal detail with its five tabs (resumen, expediente, ponencias, resolucion, bitacora). */
@WebServlet("/tocas/detail")
public class AppealDetailController extends BaseController {
    private static final Set<String> TABS = Set.of("resumen", "expediente", "ponencias", "resolucion", "bitacora");

    private final AppealService appealService = new AppealService();
    private final HearingService hearingService = new HearingService();
    private final ResolutionService resolutionService = new ResolutionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String tab = stringParam(request, "tab");
        if (!TABS.contains(tab)) tab = "resumen";
        try {
            Appeal appeal = appealService.getOwned(user, intParam(request, "id", 0));
            List<Hearing> hearings = hearingService.findByAppeal(user, appeal.getId());
            Resolution resolution = resolutionService.find(user, appeal.getId());
            request.setAttribute("appeal", appeal);
            request.setAttribute("tab", tab);
            request.setAttribute("hearings", hearings);
            request.setAttribute("resolution", resolution);

            switch (tab) {
                case "resumen" -> {
                    List<CaseDocument> docs = appealService.documents(appeal);
                    request.setAttribute("totalDocs", docs.size());
                    request.setAttribute("recentDocs", docs.subList(Math.max(0, docs.size() - 3), docs.size()).reversed());
                    request.setAttribute("nextHearing", hearingService.nextUpcoming(hearings));
                    request.setAttribute("timeline", appealService.summaryTimeline(appeal, hearings, resolution));
                    scheduleData(request, user, appeal);
                }
                case "expediente" -> loadFile(request, appeal);
                case "ponencias" -> {
                    int selectedId = intParam(request, "hearing", 0);
                    Hearing selected = null;
                    for (Hearing h : hearings) {
                        if (h.getId() == selectedId) selected = h;
                    }
                    if (selected == null) selected = hearingService.nextUpcoming(hearings);
                    if (selected == null && !hearings.isEmpty()) selected = hearings.get(0);
                    request.setAttribute("selectedHearing", selected);
                    scheduleData(request, user, appeal);
                }
                case "resolucion" -> request.setAttribute("timeline", appealService.resolutionTimeline(appeal, resolution));
                default -> request.setAttribute("audit", appealService.audit(appeal));
            }
            render(request, response, "appeals/" + tab);
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void scheduleData(HttpServletRequest request, User user, Appeal appeal) {
        if (!appeal.isConcluded()) {
            request.setAttribute("proposal", hearingService.propose(user, appeal.getId()));
            request.setAttribute("minDate", AppClock.today().toString());
        }
    }

    private void loadFile(HttpServletRequest request, Appeal appeal) {
        List<CaseDocument> docs = appealService.documents(appeal);
        int docId = intParam(request, "doc", 0);
        CaseDocument selected = null;
        for (CaseDocument d : docs) {
            if (d.getId() == docId) selected = d;
            if (selected == null && d.getName().equals("Sentencia recurrida")) {
                request.setAttribute("defaultDoc", d);
            }
        }
        if (selected == null) selected = (CaseDocument) request.getAttribute("defaultDoc");
        if (selected == null && !docs.isEmpty()) selected = docs.get(0);
        int totalPages = 0;
        for (CaseDocument d : docs) totalPages += d.getPages();
        request.setAttribute("docs", docs);
        request.setAttribute("selectedDoc", selected);
        request.setAttribute("totalPages", totalPages);
    }
}
