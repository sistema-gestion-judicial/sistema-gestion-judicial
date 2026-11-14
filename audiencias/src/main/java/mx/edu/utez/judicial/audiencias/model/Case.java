package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

public class Case {
    private final Integer id;
    private final String caseNumber;
    private final String courtId;
    private final String matter;
    private final String defendant;
    private final String plaintiff;
    private final boolean status;
    private final LocalDateTime createdAt;
    private final LocalDateTime closedAt;

    /** New active case, recently created from an admitted case file. */
    public Case(String caseNumber, String courtId, String matter, String defendant, String plaintiff) {
        this(null, caseNumber, courtId, matter, defendant, plaintiff, true, null, null);
    }

    public Case(Integer id, String caseNumber, String courtId, String matter, String defendant,
                String plaintiff, boolean status, LocalDateTime createdAt, LocalDateTime closedAt) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.courtId = courtId;
        this.matter = matter;
        this.defendant = defendant;
        this.plaintiff = plaintiff;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public Integer getId() {
        return id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public String getCourtId() {
        return courtId;
    }

    public String getMatter() {
        return matter;
    }

    public String getDefendant() {
        return defendant;
    }

    public String getPlaintiff() {
        return plaintiff;
    }

    public boolean isStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public boolean isClosed() {
        return closedAt != null;
    }
}
