package mx.edu.utez.judicial.audiencias.model.audiencia;

public enum EstadoAudiencia {

    PENDIENTE("Pendiente"),
    PROCESO("En proceso"),
    FINALIZADA("Finalizada");

    private final String etiqueta;

    EstadoAudiencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
