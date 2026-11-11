package mx.edu.utez.judicial.audiencias.api.model.usuario;

public class Usuario {

    private String cedulaProfesional;
    private Rol rol;
    private Boolean estado;
    private String correo;
    private String nombreCompleto;
    private String materia;

    public Usuario() {
    }

    public Usuario(String cedulaProfesional, Rol rol, Boolean estado, String correo, String nombreCompleto) {
        this.cedulaProfesional = cedulaProfesional;
        this.rol = rol;
        this.estado = estado;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
    }
    public Usuario(String correo, String nombreCompleto,Rol rol,Boolean estado) {
        this.rol = rol;
        this.estado = estado;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
    }

    public Usuario(String nombreCompleto,String cedulaProfesional,boolean estado,String materia) {
        this.cedulaProfesional = cedulaProfesional;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.materia = materia;


    }

    public String getCedulaProfesional() {
        return cedulaProfesional;
    }

    public void setCedulaProfesional(String cedulaProfesional) {
        this.cedulaProfesional = cedulaProfesional;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}
