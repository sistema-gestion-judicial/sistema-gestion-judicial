package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

public class CaseFile {
    private final Integer id;
    private final Integer caseId;
    private final String url;
    private final String description;
    private final LocalDateTime uploadedAt;

    /** New document, without an id yet: assigned by the database upon insertion. */
    public CaseFile(Integer caseId, String url, String description) {
        this(null, caseId, url, description, null);
    }

    public CaseFile(Integer id, Integer caseId, String url, String description, LocalDateTime uploadedAt) {
        this.id = id;
        this.caseId = caseId;
        this.url = url;
        this.description = description;
        this.uploadedAt = uploadedAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
