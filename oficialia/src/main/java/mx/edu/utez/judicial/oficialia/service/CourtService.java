package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNameException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.CourtChanges;
import mx.edu.utez.judicial.oficialia.model.CourtForm;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Manages the catalog of courts (DFR 2.9 to 2.12). Only the Administrador del órgano judicial can do it.
 */
public class CourtService {

    static final String NAME_TAKEN = "Este nombre ya pertenece a otro juzgado.";
    static final String UNKNOWN_DISTRICT = "Elija un distrito judicial de la lista.";
    static final String UNKNOWN_MATTER = "Elija solo materias de la lista.";

    private final CatalogDao catalogDao;

    public CourtService() {
        this(OficialiaDaos.catalogs());
    }

    public CourtService(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /**
     * Every court of the catalog.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public List<Court> list(User user) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findCourts();
    }

    /**
     * The court with that code, to show it in the edit form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no court has the code
     */
    public Court get(User user, String code) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findCourt(code)
            .orElseThrow(() -> new NotFoundException("No existe un juzgado con la clave indicada."));
    }

    /** The districts a court can belong to, for the select of the form. */
    public List<JudicialDistrict> districts(User user) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findJudicialDistricts();
    }

    /** The subject matters a court can handle, for the checkboxes of the form. */
    public List<SubjectMatter> subjectMatters(User user) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findSubjectMatters();
    }

    /**
     * Updates the name, district, address and subject matters of a court. The code and the number never
     * change, whatever the form carries. Nothing is saved when the form has errors or the name already
     * belongs to another court.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no court has the code
     */
    public Saving update(User user, String code, CourtForm form) {
        Court current = get(user, code);
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        if (!errors.containsKey("name") && catalogDao.findCourts().stream()
                .anyMatch(other -> !other.getCode().equals(code)
                    && CatalogNames.sameName(other.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        if (!errors.containsKey("districtCode") && catalogDao.findJudicialDistrict(form.getDistrictCode()).isEmpty()) {
            errors.put("districtCode", UNKNOWN_DISTRICT);
        }
        if (!errors.containsKey("subjectMatterCodes") && !knownMatterCodes().containsAll(form.getSubjectMatterCodes())) {
            errors.put("subjectMatterCodes", UNKNOWN_MATTER);
        }
        if (!errors.isEmpty()) {
            return Saving.rejected(errors);
        }
        CourtChanges changes = new CourtChanges(form.getName(), form.getDistrictCode(),
            new CourtAddress(form.getStreet(), form.getNeighborhood(), form.getPostalCode(), form.getMunicipality(),
                form.getPhoneDigits()),
            form.getSubjectMatterCodes());
        try {
            catalogDao.updateCourt(code, changes);
        } catch (DuplicateNameException e) {
            // Another update took the name between the check and the write
            return Saving.rejected(Map.of("name", NAME_TAKEN));
        }
        return Saving.saved(current.getCode(), form.getName());
    }

    private List<String> knownMatterCodes() {
        return catalogDao.findSubjectMatters().stream().map(SubjectMatter::getCode).toList();
    }

    /** Outcome of saving a court: the saved court, or the field errors that stopped it. */
    public record Saving(String code, String name, Map<String, String> errors) {

        static Saving saved(String code, String name) {
            return new Saving(code, name, Map.of());
        }

        static Saving rejected(Map<String, String> errors) {
            return new Saving(null, null, errors);
        }

        public boolean isSaved() {
            return code != null;
        }
    }

    private static void requireJudicialBodyAdmin(User user) {
        if (user == null || user.getRole() != UserRole.JUDICIAL_BODY_ADMIN) {
            throw new AccessDeniedException(
                "Solo el Administrador del órgano judicial puede administrar los juzgados.");
        }
    }
}
