package mx.edu.utez.judicial.oficialia.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Demand request received at the Oficialía de Partes window.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class DemandRequest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String folio;
    private final LocalDate receivedDate;
    private final String matter;
    private final String plaintiffName;
    private final String defendantName;
    private final String caseFileNumber;
    private final RequestStatus status;

    public DemandRequest(String folio, LocalDate receivedDate, String matter, String plaintiffName,
            String defendantName, String caseFileNumber, RequestStatus status) {
        this.folio = folio;
        this.receivedDate = receivedDate;
        this.matter = matter;
        this.plaintiffName = plaintiffName;
        this.defendantName = defendantName;
        this.caseFileNumber = caseFileNumber;
        this.status = status;
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

    public String getMatter() {
        return matter;
    }

    public String getPlaintiffName() {
        return plaintiffName;
    }

    public String getDefendantName() {
        return defendantName;
    }

    /** Both parties as shown in lists; a missing party is shown as unknown or anonymous. */
    public String getPartiesText() {
        String plaintiff = plaintiffName == null ? "Anónimo" : plaintiffName;
        String defendant = defendantName == null ? "Desconocido" : defendantName;
        return plaintiff + " vs " + defendant;
    }

    public String getCaseFileNumber() {
        return caseFileNumber;
    }

    public String getCaseFileText() {
        return caseFileNumber == null ? "Sin asignar" : caseFileNumber;
    }

    public RequestStatus getStatus() {
        return status;
    }
}
