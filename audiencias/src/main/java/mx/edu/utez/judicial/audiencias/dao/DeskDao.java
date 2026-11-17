package mx.edu.utez.judicial.audiencias.dao;

import java.util.List;
import java.util.Optional;

public interface DeskDao {
    Optional<DeskDao> findById(int id);
    List<DeskDao> findAll();
    List<DeskDao> findActive();
    List<DeskDao> findActiveByCourtAndMatter(String courtId, String matter);
    Optional<DeskDao> findByUserId(int userId);
    int insert(DeskDao desk);
    void assignUser(int deskId, Integer userId);
    void setActive(int deskId, boolean active);
}

