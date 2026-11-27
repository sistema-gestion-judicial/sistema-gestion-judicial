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

    /**
     * Changes the name and the seat of a district; its code and status stay as they are. The courts of the
     * district show the new name from then on. Does nothing when the code is unknown.
     *
     * @throws DuplicateNameException when another district already has the name
     */
    void updateJudicialDistrict(String code, String name, String seatMunicipality);

    List<Court> findCourts();

    Optional<Court> findCourt(String code);

    /** Every subject matter, active or not, ordered by name. */
    List<SubjectMatter> findSubjectMatters();

    Optional<SubjectMatter> findSubjectMatter(String code);

    /**
     * Adds a subject matter to the catalog. The code is stored as given.
     *
     * @throws DuplicateCodeException when another subject matter already has the code
     * @throws DuplicateNameException when another subject matter already has the name
     */
    void insertSubjectMatter(SubjectMatter matter);

    /**
     * Changes the name and the description of a subject matter; its code and status stay as they are. The
     * demand requests registered with it keep pointing to it. Does nothing when the code is unknown.
     *
     * @throws DuplicateNameException when another subject matter already has the name
     */
    void updateSubjectMatter(String code, String name, String description);

    /** Activates or deactivates a subject matter. Does nothing when the code is unknown. */
    void updateSubjectMatterStatus(String code, boolean active);
}
