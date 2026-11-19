// model/CaseDocument.java
package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

/**
 * Document attached to a {@link Case} within Audiencias (an agreement, evidence, a scanned
 * notice). Deliberately not named CaseFile: Oficialía de Partes already publishes a CaseFile
 * resource in its contract (the whole admitted judicial case) — this is only a local attachment.
 * {@code sequenceNumber} is the document's position within the case file (page order), not its
 * upload time; {@link JdbcCaseDocumentDao} assigns the next one automatically on insert.
 */
public class CaseDocument {

    private final Integer id;
    private final Integer caseId;
    private final String url;
    private final String description;
    private final LocalDateTime uploadedAt;
    private final Integer sequenceNumber;

    /** New document, without an id or a sequence number yet: both assigned upon insertion. */
    public CaseDocument(Integer caseId, String url, String description) {
        this(null, caseId, url, description, null, null);
    }

    public CaseDocument(Integer id, Integer caseId, String url, String description, LocalDateTime uploadedAt,
                        Integer sequenceNumber) {
        this.id = id;
        this.caseId = caseId;
        this.url = url;
        this.description = description;
        this.uploadedAt = uploadedAt;
        this.sequenceNumber = sequenceNumber;
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

    /** Position of this document within the case file; null only before it has been inserted. */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
