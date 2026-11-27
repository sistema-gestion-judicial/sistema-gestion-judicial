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
    void close(int id, LocalDateTime closedAt);
    /** Records where the single merged PDF of the case's documents was saved, and when. */
    void saveCompiledDocument(int id, String compiledUrl, LocalDateTime compiledAt);

    /** Clears the compiled PDF reference; call this whenever the case's documents change. */
    void invalidateCompiledDocument(int id);
}
