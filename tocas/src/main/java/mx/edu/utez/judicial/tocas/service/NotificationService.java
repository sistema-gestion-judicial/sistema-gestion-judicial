package mx.edu.utez.judicial.tocas.service;

import mx.edu.utez.judicial.tocas.dao.NotificationDao;
import mx.edu.utez.judicial.tocas.model.Notification;
import mx.edu.utez.judicial.tocas.model.User;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final NotificationDao dao = new NotificationDao();

    /** filter: "all", "unread" or "read". */
    public List<Notification> list(User user, String filter) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : dao.findByUser(user.getId())) {
            if ("unread".equals(filter) && n.isRead()) continue;
            if ("read".equals(filter) && !n.isRead()) continue;
            result.add(n);
        }
        return result;
    }

    public List<Notification> recent(User user, int limit) {
        List<Notification> all = dao.findByUser(user.getId());
        return new ArrayList<>(all.subList(0, Math.min(limit, all.size())));
    }

    public int countUnread(User user) {
        return list(user, "unread").size();
    }

    public int countAll(User user) {
        return dao.findByUser(user.getId()).size();
    }

    public void markAllRead(User user) {
        for (Notification n : dao.findByUser(user.getId())) {
            n.setRead(true);
        }
    }

    /** Marks one notification as read and returns it (null if it is not the user's). */
    public Notification open(User user, int id) {
        Notification n = dao.findById(id);
        if (n == null || n.getUserId() != user.getId()) return null;
        n.setRead(true);
        return n;
    }

    public void add(int userId, int appealId, String type, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setAppealId(appealId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setCreatedAt(AppClock.now());
        n.setRead(false);
        dao.add(n);
    }
}
