package mx.edu.utez.judicial.common.model;

import java.time.LocalDateTime;

public class AuditEntry {
    private int id;
    private int appealId;
    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String detail;

    public AuditEntry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
