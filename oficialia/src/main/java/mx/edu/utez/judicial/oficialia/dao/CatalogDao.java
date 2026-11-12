package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Courts and subject matters. Oficialía owns these catalogs; the other systems read them through the API.
 */
public interface CatalogDao {

    List<Court> findCourts();

    Optional<Court> findCourt(String code);

    List<SubjectMatter> findSubjectMatters();
}
