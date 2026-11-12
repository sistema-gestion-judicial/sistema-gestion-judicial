package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtChanges;
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

    /**
     * Changes the name, district, address and subject matters of a court; its code, number and status stay as
     * they are. Demand requests already registered keep the subject matter they were registered with. Does
     * nothing when the code is unknown.
     *
     * @throws DuplicateNameException when another court already has the name
     */
    void updateCourt(String code, CourtChanges changes);

    List<SubjectMatter> findSubjectMatters();
}
