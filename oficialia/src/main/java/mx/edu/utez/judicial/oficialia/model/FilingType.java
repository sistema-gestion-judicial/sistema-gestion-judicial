package mx.edu.utez.judicial.oficialia.model;

/**
 * How the lawsuit reached the Oficialía de Partes (DFR 3.1).
 */
public enum FilingType {

    IN_PERSON("En ventanilla"),
    MAIL("Por correo certificado"),
    ONLINE("En línea");

    private final String label;

    FilingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
