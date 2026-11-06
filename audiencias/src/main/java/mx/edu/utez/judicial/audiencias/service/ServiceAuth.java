package mx.edu.utez.judicial.audiencias.service;

import mx.edu.utez.judicial.audiencias.model.Usuario;
import mx.edu.utez.judicial.audiencias.repository.RepositoryUsuario;

public class ServiceAuth {

    private final RepositoryUsuario repositoryUsuario;

    public ServiceAuth(RepositoryUsuario repositoryUsuario) {
        this.repositoryUsuario = repositoryUsuario;
    }


    public Usuario autenticar(String correo, String contra) {
        Usuario usuario = new Usuario("1",1,"ADMIN",1L);

        if(correo.equals("Ale")&&contra.equals("1234")){
            return usuario;
        }
        else {
            return null;
        }
    }
}
