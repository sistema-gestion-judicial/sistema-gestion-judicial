package mx.edu.utez.judicial.common.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Hearing {
    private int id;
    private int appealId;
    private int judgeId;
    private LocalDate date;
    private LocalTime time;
    private String type;
    private String status;
    private String judgeName;
    private String roomName;
    private boolean autoAssigned;
    private Appeal appeal;

    public Hearing() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }
    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public boolean isAutoAssigned() { return autoAssigned; }
    public void setAutoAssigned(boolean autoAssigned) { this.autoAssigned = autoAssigned; }
    public Appeal getAppeal() { return appeal; }
    public void setAppeal(Appeal appeal) { this.appeal = appeal; }

    public boolean isUpcoming() { return "UPCOMING".equals(status); }
    public boolean isDone() { return "DONE".equals(status); }
}
