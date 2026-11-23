package mx.edu.utez.judicial.audiencias.dao.cases;

import mx.edu.utez.judicial.audiencias.model.Case;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaseDao {
    Optional<Case> findById(int id);
    Optional<Case> findByCaseNumber(String caseNumber);
    List<Case> findAll();
    List<Case> findActive();
    List<Case> findByCourtAndMatter(String courtId, String matter);
    int insert(Case caseEntity);
    void close(Case caseToBeClosed, LocalDateTime closedAt);
}
