package mx.edu.utez.judicial.oficialia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Demand request received at the Oficialía de Partes window.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class DemandRequest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String folio;
    private final LocalDate receivedDate;
    private final String courtName;
    private final String courtCode;
    private final String matter;
    private final Party plaintiff;
    private final Party defendant;
    private final String legalRepresentative;
    private final String capturedBy;
    private final List<Annex> annexes;
    private final DuplicityWarning duplicityWarning;

    private RequestStatus status = RequestStatus.PENDING;
    private String caseFileNumber;
    private String deskName;
    private String rejectionReason;
    private String ruledBy;
    private LocalDateTime ruledAt;

    public DemandRequest(String folio, LocalDate receivedDate, String courtName, String courtCode, String matter,
            Party plaintiff, Party defendant, String legalRepresentative, String capturedBy, List<Annex> annexes,
            DuplicityWarning duplicityWarning) {
        this.folio = folio;
        this.receivedDate = receivedDate;
        this.courtName = courtName;
        this.courtCode = courtCode;
        this.matter = matter;
        this.plaintiff = plaintiff;
        this.defendant = defendant;
        this.legalRepresentative = legalRepresentative;
        this.capturedBy = capturedBy;
        this.annexes = List.copyOf(annexes);
        this.duplicityWarning = duplicityWarning;
    }

    /**
     * Rules the request as admissible. The desk can be null when the hearings service did not answer;
     * it is then shown as pending.
     */
    public void recordAdmissible(String caseFileNumber, String deskName, String ruledBy, LocalDateTime ruledAt) {
        ensurePending();
        this.status = RequestStatus.ADMISSIBLE;
        this.caseFileNumber = caseFileNumber;
        this.deskName = deskName;
        this.ruledBy = ruledBy;
        this.ruledAt = ruledAt;
    }

    /**
     * Records the desk the hearings service assigned after the case file was opened. Only an admissible
     * request whose desk is still pending gets one.
     */
    public void assignDesk(String deskName) {
        if (!isDeskPending()) {
            throw new IllegalStateException("Request " + folio + " has no pending desk");
        }
        this.deskName = deskName;
    }

    /** Rules the request as inadmissible, keeping the reason for the receipt given to the plaintiff. */
    public void recordInadmissible(String rejectionReason, String ruledBy, LocalDateTime ruledAt) {
        ensurePending();
        this.status = RequestStatus.INADMISSIBLE;
        this.rejectionReason = rejectionReason;
        this.ruledBy = ruledBy;
        this.ruledAt = ruledAt;
    }

    private void ensurePending() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Request " + folio + " was already ruled");
        }
    }

    public String getFolio() {
        return folio;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public String getReceivedDateText() {
        return receivedDate.format(DATE_FORMAT);
    }

    public String getCourtName() {
        return courtName;
    }

    public String getCourtCode() {
        return courtCode;
    }

    public String getMatter() {
        return matter;
    }

    public Party getPlaintiff() {
        return plaintiff;
    }

    public Party getDefendant() {
        return defendant;
    }

    public String getPlaintiffName() {
        return plaintiff.getName();
    }

    public String getDefendantName() {
        return defendant.getName();
    }

    /** Both parties as shown in lists; a missing party is shown as anonymous or unknown. */
    public String getPartiesText() {
        return plaintiff.getDisplayName() + " vs " + defendant.getDisplayName();
    }

    public String getLegalRepresentative() {
        return legalRepresentative;
    }

    public String getCapturedBy() {
        return capturedBy;
    }

    public List<Annex> getAnnexes() {
        return annexes;
    }

    public DuplicityWarning getDuplicityWarning() {
        return duplicityWarning;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public String getCaseFileNumber() {
        return caseFileNumber;
    }

    public String getCaseFileText() {
        return caseFileNumber == null ? "Sin asignar" : caseFileNumber;
    }

    public String getDeskName() {
        return deskName;
    }

    /** Admissible, but the hearings service has not assigned the desk yet. */
    public boolean isDeskPending() {
        return status == RequestStatus.ADMISSIBLE && deskName == null;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getRuledBy() {
        return ruledBy;
    }

    public LocalDateTime getRuledAt() {
        return ruledAt;
    }

    public String getRuledAtText() {
        return ruledAt == null ? "" : ruledAt.format(DATE_TIME_FORMAT);
    }
}
