package mx.edu.utez.judicial.audiencias.dao.user;

import mx.edu.utez.judicial.audiencias.model.user.Role;
import mx.edu.utez.judicial.audiencias.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    User update(User usuario);

    List<User> findLawyers(Role role);

    int insert(User user,String pass);

    String authenticate(String email);

    List<User> findActiveLawyers();

    Optional<User> findById(int id);
}
