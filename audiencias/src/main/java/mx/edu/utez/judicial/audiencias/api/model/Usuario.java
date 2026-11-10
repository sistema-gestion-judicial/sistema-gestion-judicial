package mx.edu.utez.judicial.audiencias.api.model;

public class Usuario {

    private String cedulaProfesional;
    private Rol rol;
    private Boolean activo;
    private String correo;
    private String nombreCompleto;
    private String materia;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con parámetros
    public Usuario(String cedulaProfesional, Rol rol, Boolean activo, String correo, String nombreCompleto) {
        this.cedulaProfesional = cedulaProfesional;
        this.rol = rol;
        this.activo = activo;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
    }
    public Usuario(String correo, String nombreCompleto,Rol rol,Boolean activo) {
        this.rol = rol;
        this.activo = activo;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
    }

    public Usuario(String nombreCompleto,String cedulaProfesional,boolean estado,String materia) {
        this.cedulaProfesional = cedulaProfesional;
        this.nombreCompleto = nombreCompleto;
        this.activo = activo;
        this.materia = materia;


    }
    // Getters y Setters
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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
