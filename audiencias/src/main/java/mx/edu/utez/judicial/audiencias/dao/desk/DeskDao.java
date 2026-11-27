package mx.edu.utez.judicial.audiencias.dao.desk;

import mx.edu.utez.judicial.audiencias.model.Desk;

import java.util.List;
import java.util.Optional;

public interface DeskDao {
    Optional<Desk> findById(int id);
    List<Desk> findAll();
    List<Desk> findActive();
    List<Desk> findActiveByCourtAndMatter(String courtId, String matter);
    Optional<Desk> findByUserId(int userId);
    int insert(Desk desk);
    void assignUser(int deskId, Integer userId);
    void setActive(int deskId, boolean active);
    void update(Desk desk);
}

