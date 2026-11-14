package mx.edu.utez.judicial.audiencias.DAO.usuario;

import mx.edu.utez.judicial.audiencias.model.usuario.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    User update(User usuario,int id);

    List<User> findLawyers();

    int insert(User user,String pass);

}
