package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Judicial districts, courts and subject matters. Oficialía owns these catalogs; the other systems read
 * them through the API.
 */
public interface CatalogDao {

    /** Every district, active or not, ordered by name. */
    List<JudicialDistrict> findJudicialDistricts();

    Optional<JudicialDistrict> findJudicialDistrict(String code);

    /**
     * Adds a district to the catalog. The code is stored as given.
     *
     * @throws DuplicateCodeException when another district already has the code
     * @throws DuplicateNameException when another district already has the name
     */
    void insertJudicialDistrict(JudicialDistrict district);

    List<Court> findCourts();

    Optional<Court> findCourt(String code);

    List<SubjectMatter> findSubjectMatters();
}
