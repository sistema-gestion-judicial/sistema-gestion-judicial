package mx.edu.utez.judicial.audiencias.api.DAO;

import mx.edu.utez.judicial.audiencias.api.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {

    Optional<Usuario> findByEmail(String email);

    Usuario update(Usuario usuario);

    List<Usuario> findByRol();

    Usuario insert();

}
