package mx.edu.utez.judicial.oficialia.model;

/**
 * Duplicity warning shown while a request was registered, and what the clerk decided about it.
 */
public class DuplicityWarning {

    private final String courtName;
    private final String matter;
    private final String decision;

    public DuplicityWarning(String courtName, String matter, String decision) {
        this.courtName = courtName;
        this.matter = matter;
        this.decision = decision;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getMatter() {
        return matter;
    }

    public String getDecision() {
        return decision;
    }
}
