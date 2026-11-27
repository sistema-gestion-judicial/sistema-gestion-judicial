package mx.edu.utez.judicial.oficialia.model;

/**
 * Where a case file stands (DFR 3.6): in first instance, in second instance before the Tocas system, or
 * concluded as reported by another system.
 */
public enum CaseFileStatus {

    ACTIVE("Activo", "status-admissible"),
    SECOND_INSTANCE("Segunda instancia", "status-pending"),
    CONCLUDED("Concluido", "status-inadmissible");

    private final String label;
    private final String cssClass;

    CaseFileStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }
}
