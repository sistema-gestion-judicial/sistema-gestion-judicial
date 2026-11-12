package mx.edu.utez.judicial.audiencias.api.model.mesa;

public class Mesa {

    private Integer id;
    private String nombreMesa;
    private String materia;
    private Boolean activo;
    private Integer idUsuario;
    private String idJuzgado;
    private String nombreAbogado;

    public Mesa() {
    }

    public Mesa(String nombreMesa, String materia, Boolean activo, Integer idUsuario, String idJuzgado) {
        this.nombreMesa = nombreMesa;
        this.materia = materia;
        this.activo = activo;
        this.idUsuario = idUsuario;
        this.idJuzgado = idJuzgado;
    }

    public Mesa(Integer id, String nombreMesa, String materia, Boolean activo, Integer idUsuario, String idJuzgado) {
        this.id = id;
        this.nombreMesa = nombreMesa;
        this.materia = materia;
        this.activo = activo;
        this.idUsuario = idUsuario;
        this.idJuzgado = idJuzgado;
    }

    public Mesa(Integer id, String nombreMesa, String materia, Boolean activo, Integer idUsuario, String idJuzgado, String nombreAbogado) {
        this(id, nombreMesa, materia, activo, idUsuario, idJuzgado);
        this.nombreAbogado = nombreAbogado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreMesa() {
        return nombreMesa;
    }

    public void setNombreMesa(String nombreMesa) {
        this.nombreMesa = nombreMesa;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdJuzgado() {
        return idJuzgado;
    }

    public void setIdJuzgado(String idJuzgado) {
        this.idJuzgado = idJuzgado;
    }

    public String getNombreAbogado() {
        return nombreAbogado;
    }

    public void setNombreAbogado(String nombreAbogado) {
        this.nombreAbogado = nombreAbogado;
    }
}
