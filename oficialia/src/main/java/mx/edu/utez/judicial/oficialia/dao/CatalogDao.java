package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
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
     * Adds a court together with the account of its administrator (DFR 2.9). Both are saved or neither is:
     * a court is never left without the administrator responsible for it. The court's code, number, district,
     * address and subject matters are stored as given; {@code administrator} must carry the court's code.
     *
     * @throws DuplicateCodeException when another court already has the code
     * @throws DuplicateNumberException when another court already has the number
     * @throws DuplicateNameException when another court already has the name
     * @throws DuplicateEmailException when another account already has the administrator's e-mail
     */
    void insertCourt(Court court, NewUserAccount administrator);

    List<SubjectMatter> findSubjectMatters();
}
