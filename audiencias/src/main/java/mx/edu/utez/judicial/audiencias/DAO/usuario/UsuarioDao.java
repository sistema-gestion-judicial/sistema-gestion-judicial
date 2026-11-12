package mx.edu.utez.judicial.audiencias.DAO.usuario;

import mx.edu.utez.judicial.audiencias.model.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {

    Optional<Usuario> findByEmail(String email);

    Usuario update(Usuario usuario,int id);

    List<Usuario> findAbogados();

    int insert(Usuario usuario,String pass);

}
