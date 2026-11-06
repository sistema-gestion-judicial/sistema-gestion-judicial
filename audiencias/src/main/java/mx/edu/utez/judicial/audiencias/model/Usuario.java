package mx.edu.utez.judicial.audiencias.model;

public class Usuario {


    private Long id;
    private String rol;
    private int mesa;
    private String juzgado;

    public Usuario(String juzgado, int mesa, String rol, Long id) {
        this.juzgado = juzgado;
        this.mesa = mesa;
        this.rol = rol;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getMesa() {
        return mesa;
    }

    public void setMesa(int mesa) {
        this.mesa = mesa;
    }

    public String getJuzgado() {
        return juzgado;
    }

    public void setJuzgado(String juzgado) {
        this.juzgado = juzgado;
    }


}
