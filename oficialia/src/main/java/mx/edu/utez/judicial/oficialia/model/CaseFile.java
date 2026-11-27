package mx.edu.utez.judicial.oficialia.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A case file as the clerks see it (DFR 3.6): the admissible demand request it came from plus its state.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class CaseFile {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DemandRequest request;
    private final CaseFileState state;

    public CaseFile(DemandRequest request, CaseFileState state) {
        this.request = request;
        this.state = state;
    }

    public String getNumber() {
        return request.getCaseFileNumber();
    }

    public String getFolio() {
        return request.getFolio();
    }

    public String getCourtName() {
        return request.getCourtName();
    }

    public String getCourtCode() {
        return request.getCourtCode();
    }

    public String getMatter() {
        return request.getMatter();
    }

    public String getPlaintiffName() {
        return request.getPlaintiffName();
    }

    public String getDefendantName() {
        return request.getDefendantName();
    }

    public String getPartiesText() {
        return request.getPartiesText();
    }

    /** Null while the Audiencias system has not assigned it. */
    public String getDeskName() {
        return request.getDeskName();
    }

    public LocalDateTime getOpenedAt() {
        return request.getRuledAt();
    }

    public String getOpenedAtText() {
        return format(request.getRuledAt());
    }

    public CaseFileStatus getStatus() {
        return state.status();
    }

    public boolean isFirstInstanceResolved() {
        return state.firstInstanceResolvedAt() != null;
    }

    public String getFirstInstanceResolvedAtText() {
        return format(state.firstInstanceResolvedAt());
    }

    public boolean isSecondInstance() {
        return state.secondInstance();
    }

    public String getChamberCode() {
        return state.chamberCode();
    }

    public String getChamberName() {
        return state.chamberName();
    }

    /** In second instance while the Tocas system has not given a chamber. */
    public boolean isChamberPending() {
        return state.secondInstance() && state.chamberCode() == null;
    }

    public String getSecondInstanceAtText() {
        return format(state.secondInstanceAt());
    }

    public boolean isConcluded() {
        return state.concludedAt() != null;
    }

    public String getConcludedAtText() {
        return format(state.concludedAt());
    }

    public DemandRequest getRequest() {
        return request;
    }

    public CaseFileState getState() {
        return state;
    }

    private static String format(LocalDateTime value) {
        return value == null ? "" : value.format(DATE);
    }
}
