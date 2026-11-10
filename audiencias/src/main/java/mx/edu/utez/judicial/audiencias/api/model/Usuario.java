package mx.edu.utez.judicial.audiencias.api.model;

public class Usuario {

    private String nombre;
    private  String apellido;
    private Roles rol;
    private  boolean activo;
    private  String mesa;

    public Usuario(String nombre, String apellido, boolean activo,Roles rol,String mesa) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo;
        this.rol = rol;
        this.mesa = mesa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Roles getRol() {
        return rol;
    }

    public void setRol(Roles rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }


}
