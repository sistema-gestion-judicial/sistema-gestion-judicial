package mx.edu.utez.judicial.oficialia.model;

/**
 * How a demand was presented (DFR 3.1): by one person or by a group.
 */
public enum PresentationType {
    INDIVIDUAL("Individual"),
    COLLECTIVE("Colectiva");

    private final String label;

    PresentationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
