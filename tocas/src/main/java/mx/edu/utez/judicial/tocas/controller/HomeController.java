package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.common.controller.BaseController;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.*;

import java.io.IOException;

/** Home panel: metrics, next hearings and recent notices. */
@WebServlet(urlPatterns = {"", "/home"})
public class HomeController extends BaseController {
    private final AppealService appealService = new AppealService();
    private final HearingService hearingService = new HearingService();
    private final NotificationService notificationService = new NotificationService();
    private final ResolutionService resolutionService = new ResolutionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("greeting", FormatUtils.greeting());
        request.setAttribute("firstName", FormatUtils.firstName(user.getFullName()));
        request.setAttribute("assignedCount", appealService.countAssigned(user));
        request.setAttribute("hearingsIn15Days", hearingService.countWithinDays(user, 15));
        request.setAttribute("draftCount", resolutionService.countDrafts(user));
        request.setAttribute("unreadCount", notificationService.countUnread(user));
        request.setAttribute("upcoming", hearingService.upcoming(user, 4));
        request.setAttribute("notices", notificationService.recent(user, 5));
        render(request, response, "home");
    }
}
