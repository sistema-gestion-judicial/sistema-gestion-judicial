package mx.edu.utez.judicial.audiencias.model.audiencia;

import java.time.LocalDateTime;

public class Audiencia {

    private final Integer id;
    private final LocalDateTime fecha;
    private final EstadoAudiencia estado;
    private final Integer idMesa;
    private final Integer idCaso;
    private final Integer idAbogado;
    private final String sala;
    private final String observaciones;

    public Audiencia(Integer idMesa, Integer idCaso, Integer idAbogado) {
        this(null, null, EstadoAudiencia.PENDIENTE, idMesa, idCaso, idAbogado, null, null);
    }

    public Audiencia(Integer id, LocalDateTime fecha, EstadoAudiencia estado, Integer idMesa, Integer idCaso,
                     Integer idAbogado, String sala, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado == null ? EstadoAudiencia.PENDIENTE : estado;
        this.idMesa = idMesa;
        this.idCaso = idCaso;
        this.idAbogado = idAbogado;
        this.sala = sala;
        this.observaciones = observaciones;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoAudiencia getEstado() {
        return estado;
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public Integer getIdCaso() {
        return idCaso;
    }

    public Integer getIdAbogado() {
        return idAbogado;
    }

    public String getSala() {
        return sala;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
