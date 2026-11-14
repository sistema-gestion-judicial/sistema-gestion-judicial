package mx.edu.utez.judicial.audiencias.model.hearing;

import java.time.LocalDateTime;

public class Hearing {

    private Integer id;
    private LocalDateTime hearingDate;
    private HearingStatus status;
    private Integer deskId;
    private Integer caseId;
    private Integer lawyerId;
    private String courtroom;
    private String notes;

    public Hearing(Integer deskId, Integer caseId, Integer lawyerId) {
        this(null, null, HearingStatus.PENDING, deskId, caseId, lawyerId, null, null);
    }

    public Hearing(Integer id, LocalDateTime hearingDate, HearingStatus status, Integer deskId, Integer caseId,
                   Integer lawyerId, String courtroom, String notes) {
        this.id = id;
        this.hearingDate = hearingDate;
        this.status = status == null ? HearingStatus.PENDING : status;
        this.deskId = deskId;
        this.caseId = caseId;
        this.lawyerId = lawyerId;
        this.courtroom = courtroom;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getHearingDate() {
        return hearingDate;
    }

    public HearingStatus getStatus() {
        return status;
    }

    public Integer getDeskId() {
        return deskId;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public Integer getLawyerId() {
        return lawyerId;
    }

    public String getCourtroom() {
        return courtroom;
    }

    public String getNotes() {
        return notes;
    }
}
