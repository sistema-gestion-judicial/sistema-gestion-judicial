package mx.edu.utez.judicial.audiencias.model;

import java.time.LocalDateTime;

public class Expediente {
    private final Integer id;
    private final Integer idCaso;
    private final String url;
    private final String descripcion;
    private final LocalDateTime fechaSubida;

    /** Documento nuevo, sin id todavía: lo asigna la base de datos al insertarlo. */
    public Expediente(Integer idCaso, String url, String descripcion) {
        this(null, idCaso, url, descripcion, null);
    }

    public Expediente(Integer id, Integer idCaso, String url, String descripcion, LocalDateTime fechaSubida) {
        this.id = id;
        this.idCaso = idCaso;
        this.url = url;
        this.descripcion = descripcion;
        this.fechaSubida = fechaSubida;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdCaso() {
        return idCaso;
    }

    public String getUrl() {
        return url;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

}
