package mx.edu.utez.judicial.audiencias.model.hearing;

public enum HearingStatus {

    PENDING("Pendiente"),
    IN_PROGRESS("En proceso"),
    COMPLETED("Finalizada");

    private final String label;

    HearingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
