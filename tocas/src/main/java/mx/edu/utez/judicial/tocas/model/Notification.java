package mx.edu.utez.judicial.tocas.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int appealId;
    private String type;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
