package mx.edu.utez.judicial.common.model;

import java.time.LocalDate;

public class Appeal {
    private int id;
    private String number;
    private String plaintiff;
    private String defendant;
    private String originCourt;
    private LocalDate receivedDate;
    private String appealType;
    private String originFile;
    private String roomName;
    private String status;
    private int ownerId;
    private LocalDate concludedDate;
    private Hearing nextHearing;

    public Appeal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getPlaintiff() { return plaintiff; }
    public void setPlaintiff(String plaintiff) { this.plaintiff = plaintiff; }
    public String getDefendant() { return defendant; }
    public void setDefendant(String defendant) { this.defendant = defendant; }
    public String getOriginCourt() { return originCourt; }
    public void setOriginCourt(String originCourt) { this.originCourt = originCourt; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public String getAppealType() { return appealType; }
    public void setAppealType(String appealType) { this.appealType = appealType; }
    public String getOriginFile() { return originFile; }
    public void setOriginFile(String originFile) { this.originFile = originFile; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public LocalDate getConcludedDate() { return concludedDate; }
    public void setConcludedDate(LocalDate concludedDate) { this.concludedDate = concludedDate; }
    public Hearing getNextHearing() { return nextHearing; }
    public void setNextHearing(Hearing nextHearing) { this.nextHearing = nextHearing; }

    /** Parties as shown in lists, e.g. "Paloma vs Alejandro". */
    public String getParties() { return plaintiff + " vs " + defendant; }
    public boolean isConcluded() { return "CONCLUDED".equals(status); }
}
