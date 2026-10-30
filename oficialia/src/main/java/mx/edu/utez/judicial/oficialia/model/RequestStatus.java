package mx.edu.utez.judicial.oficialia.model;

/**
 * Qualification status of a demand request. Labels are shown to the user in Spanish.
 */
public enum RequestStatus {

    PENDING("Sin calificar", "status-pending"),
    ADMISSIBLE("Procedente", "status-admissible"),
    INADMISSIBLE("No procedente", "status-inadmissible");

    private final String label;
    private final String cssClass;

    RequestStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getCode() {
        return name();
    }
}
