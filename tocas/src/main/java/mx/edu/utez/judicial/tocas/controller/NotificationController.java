package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.model.Notification;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.NotificationService;

import java.io.IOException;

/** Notifications list. GET /notifications, GET /notifications/open?id=, POST /notifications/read-all */
@WebServlet("/notifications/*")
public class NotificationController extends BaseController {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        if ("/open".equals(request.getPathInfo())) {
            Notification n = notificationService.open(user, intParam(request, "id", 0));
            if (n != null && n.getAppealId() > 0) {
                String tab = n.getType().startsWith("HEARING") ? "ponencias" : "resumen";
                redirect(request, response, "/tocas/detail?id=" + n.getAppealId() + "&tab=" + tab);
            } else {
                redirect(request, response, "/notifications");
            }
            return;
        }
        String filter = stringParam(request, "filter");
        if (!filter.equals("unread") && !filter.equals("read")) filter = "all";
        int unread = notificationService.countUnread(user);
        int total = notificationService.countAll(user);
        request.setAttribute("filter", filter);
        request.setAttribute("notifications", notificationService.list(user, filter));
        request.setAttribute("countAll", total);
        request.setAttribute("countUnread", unread);
        request.setAttribute("countRead", total - unread);
        render(request, response, "notifications");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/read-all".equals(request.getPathInfo())) {
            notificationService.markAllRead(currentUser(request));
        }
        redirect(request, response, "/notifications");
    }
}
