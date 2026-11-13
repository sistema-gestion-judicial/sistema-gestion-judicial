package mx.edu.utez.judicial.audiencias.model.mesa;


public class Mesa {

    private final Integer id;
    private final String nombreMesa;
    private final String materia;
    private final boolean activo;
    private final Integer idUsuario;
    private final String idJuzgado;

    public Mesa(String nombreMesa, String materia, boolean activo, Integer idUsuario, String idJuzgado) {
        this(null, nombreMesa, materia, activo, idUsuario, idJuzgado);
    }

    public Mesa(Integer id, String nombreMesa, String materia, boolean activo, Integer idUsuario,
                String idJuzgado) {
        this.id = id;
        this.nombreMesa = nombreMesa;
        this.materia = materia;
        this.activo = activo;
        this.idUsuario = idUsuario;
        this.idJuzgado = idJuzgado;
    }

    public Integer getId() {
        return id;
    }

    public String getNombreMesa() {
        return nombreMesa;
    }

    public String getMateria() {
        return materia;
    }

    public boolean isActivo() {
        return activo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getIdJuzgado() {
        return idJuzgado;
    }

    public boolean tieneAbogadoAsignado() {
        return idUsuario != null;
    }
}
