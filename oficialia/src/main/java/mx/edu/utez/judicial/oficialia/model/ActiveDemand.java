package mx.edu.utez.judicial.oficialia.model;

/**
 * Active lawsuit registered in any court of the state, used to detect duplicates.
 */
public class ActiveDemand {

    private final String courtName;
    private final String matter;
    private final String instance;
    private final String plaintiffId;
    private final String defendantId;

    public ActiveDemand(String courtName, String matter, String instance, String plaintiffId,
            String defendantId) {
        this.courtName = courtName;
        this.matter = matter;
        this.instance = instance;
        this.plaintiffId = plaintiffId;
        this.defendantId = defendantId;
    }

    public String getCourtName() {
        return courtName;
    }

    public String getMatter() {
        return matter;
    }

    public String getInstance() {
        return instance;
    }

    public String getPlaintiffId() {
        return plaintiffId;
    }

    public String getDefendantId() {
        return defendantId;
    }
}
