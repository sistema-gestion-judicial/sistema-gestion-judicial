package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

public class Caso {
    private final Integer id;
    private final String numeroExpediente;
    private final String idJuzgado;
    private final String materia;
    private final String demandado;
    private final String demandante;
    private final boolean activo;
    private final LocalDateTime creacion;
    private final LocalDateTime cierre;

    /** Caso nuevo y activo, recién creado a partir de un expediente admitido. */
    public Caso(String numeroExpediente, String idJuzgado, String materia, String demandado, String demandante) {
        this(null, numeroExpediente, idJuzgado, materia, demandado, demandante, true, null, null);
    }

    public Caso(Integer id, String numeroExpediente, String idJuzgado, String materia, String demandado,
                String demandante, boolean activo, LocalDateTime creacion, LocalDateTime cierre) {
        this.id = id;
        this.numeroExpediente = numeroExpediente;
        this.idJuzgado = idJuzgado;
        this.materia = materia;
        this.demandado = demandado;
        this.demandante = demandante;
        this.activo = activo;
        this.creacion = creacion;
        this.cierre = cierre;
    }

    public Integer getId() {
        return id;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getIdJuzgado() {
        return idJuzgado;
    }

    public String getMateria() {
        return materia;
    }

    public String getDemandado() {
        return demandado;
    }

    public String getDemandante() {
        return demandante;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getCreacion() {
        return creacion;
    }

    public LocalDateTime getCierre() {
        return cierre;
    }

    public boolean estaCerrado() {
        return cierre != null;
    }
}
