package mx.edu.utez.judicial.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Resolution {
    private int appealId;
    private String status;
    private int version;
    private String resultando;
    private String considerando;
    private String resuelve;
    private LocalDate startedDate;
    private LocalDateTime lastModified;
    private LocalDateTime finalizedAt;
    private int pages;
    private String signedFileName;
    private int signedPages;
    private long signedSize;
    private LocalDateTime signedUploadedAt;
    private String signedPath;

    public Resolution() {}

    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getResultando() { return resultando; }
    public void setResultando(String resultando) { this.resultando = resultando; }
    public String getConsiderando() { return considerando; }
    public void setConsiderando(String considerando) { this.considerando = considerando; }
    public String getResuelve() { return resuelve; }
    public void setResuelve(String resuelve) { this.resuelve = resuelve; }
    public LocalDate getStartedDate() { return startedDate; }
    public void setStartedDate(LocalDate startedDate) { this.startedDate = startedDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
    public String getSignedFileName() { return signedFileName; }
    public void setSignedFileName(String signedFileName) { this.signedFileName = signedFileName; }
    public int getSignedPages() { return signedPages; }
    public void setSignedPages(int signedPages) { this.signedPages = signedPages; }
    public long getSignedSize() { return signedSize; }
    public void setSignedSize(long signedSize) { this.signedSize = signedSize; }
    public LocalDateTime getSignedUploadedAt() { return signedUploadedAt; }
    public void setSignedUploadedAt(LocalDateTime signedUploadedAt) { this.signedUploadedAt = signedUploadedAt; }
    public String getSignedPath() { return signedPath; }
    public void setSignedPath(String signedPath) { this.signedPath = signedPath; }

    public boolean isDraft() { return "DRAFT".equals(status); }
    public boolean isFinalized() { return "FINALIZED".equals(status); }
    public boolean isSigned() { return signedFileName != null; }
}
