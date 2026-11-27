package mx.edu.utez.judicial.audiencias.dao.user;

import mx.edu.utez.judicial.audiencias.model.user.Role;
import mx.edu.utez.judicial.audiencias.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    User update(User usuario);

    int insert(User user,String pass);

    String authenticate(String email);

    List<User> findActiveLawyers(Role role);

    List<User> findByRole(Role role);

    Optional<User> findById(int id);

    void setStatus(int userId, boolean status);

    void updatePassword(int userId, String passwordHash, boolean mustChangePassword);
}
