package mx.edu.utez.judicial.oficialia.model;

/**
 * Result shown in the duplicity warning: where the active lawsuit is and which party matched.
 */
public class DuplicityMatch {

    private final ActiveDemand activeDemand;
    private final PartyRole matchedRole;

    public DuplicityMatch(ActiveDemand activeDemand, PartyRole matchedRole) {
        this.activeDemand = activeDemand;
        this.matchedRole = matchedRole;
    }

    public String getCourtName() {
        return activeDemand.getCourtName();
    }

    public String getMatter() {
        return activeDemand.getMatter();
    }

    public String getInstance() {
        return activeDemand.getInstance();
    }

    public PartyRole getMatchedRole() {
        return matchedRole;
    }

    public String getMatchText() {
        return "RFC o CURP del " + matchedRole.getLabel();
    }
}
