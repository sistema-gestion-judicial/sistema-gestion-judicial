package mx.edu.utez.judicial.oficialia.model;

/**
 * Role of a party in a lawsuit. Duplicity is always compared role by role.
 */
public enum PartyRole {

    PLAINTIFF("demandante"),
    DEFENDANT("demandado");

    private final String label;

    PartyRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
