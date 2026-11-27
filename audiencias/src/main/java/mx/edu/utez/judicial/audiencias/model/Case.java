package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

public class Case {

    private final Integer id;
    private final String caseNumber;
    private final String courtId;
    private final String matter;
    private final String defendant;
    private final String plaintiff;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime closedAt;
    private final String compiledUrl;
    private final LocalDateTime compiledAt;

    /** New, active case, just created from an admitted case file. No compiled file yet. */
    public Case(String caseNumber, String courtId, String matter, String defendant, String plaintiff) {
        this(null, caseNumber, courtId, matter, defendant, plaintiff, true, null, null, null, null);
    }

    public Case(Integer id, String caseNumber, String courtId, String matter, String defendant,
                String plaintiff, boolean active, LocalDateTime createdAt, LocalDateTime closedAt,
                String compiledUrl, LocalDateTime compiledAt) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.courtId = courtId;
        this.matter = matter;
        this.defendant = defendant;
        this.plaintiff = plaintiff;
        this.active = active;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.compiledUrl = compiledUrl;
        this.compiledAt = compiledAt;
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

    public boolean isActive() {
        return active;
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

    /** URL of the single merged PDF of this case's documents; null until it has been compiled. */
    public String getCompiledUrl() {
        return compiledUrl;
    }

    public LocalDateTime getCompiledAt() {
        return compiledAt;
    }

    public boolean hasCompiledDocument() {
        return compiledUrl != null;
    }
}
