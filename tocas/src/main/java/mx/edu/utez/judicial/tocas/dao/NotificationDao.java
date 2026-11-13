package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Notification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotificationDao {
    private final MemoryStore store = MemoryStore.get();

    /** Newest first. */
    public List<Notification> findByUser(int userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : store.notifications) {
            if (n.getUserId() == userId) result.add(n);
        }
        result.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        return result;
    }

    public Notification findById(int id) {
        for (Notification n : store.notifications) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    public Notification add(Notification notification) {
        notification.setId(store.notificationSeq.incrementAndGet());
        store.notifications.add(notification);
        return notification;
    }
}
