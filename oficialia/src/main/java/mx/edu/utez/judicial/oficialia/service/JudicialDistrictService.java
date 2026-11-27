package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateCodeException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNameException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictForm;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictRow;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Manages the catalog of judicial districts (DFR 2.1 to 2.4). Only the Administrador del órgano judicial
 * can do it.
 */
public class JudicialDistrictService {

    static final String CODE_TAKEN = "Esta clave ya existe.";
    static final String NAME_TAKEN = "Este nombre ya pertenece a otro distrito judicial.";
    public static final int PAGE_SIZE = 10;

    private final CatalogDao catalogDao;

    public JudicialDistrictService() {
        this(OficialiaDaos.catalogs());
    }

    public JudicialDistrictService(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /**
     * Every district of the catalog.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public List<JudicialDistrict> list(User user) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findJudicialDistricts();
    }

    /**
     * The page of the catalog that matches the filters (DFR 2.2), ordered by name, with the number of courts
     * of each district. The text is looked for in the code, the name and the seat, ignoring case and accents;
     * a null status shows active and inactive districts.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public ResultPage<JudicialDistrictRow> search(User user, String text, Boolean active, int page) {
        requireJudicialBodyAdmin(user);
        String key = CatalogNames.key(text);
        Map<String, Long> courtsByDistrict = catalogDao.findCourts().stream()
            .collect(Collectors.groupingBy(Court::getDistrictCode, Collectors.counting()));
        List<JudicialDistrictRow> rows = catalogDao.findJudicialDistricts().stream()
            .filter(district -> active == null || district.isActive() == active)
            .filter(district -> key.isEmpty()
                || Stream.of(district.getCode(), district.getName(), district.getSeatMunicipality())
                    .anyMatch(value -> CatalogNames.key(value).contains(key)))
            .map(district -> new JudicialDistrictRow(district,
                courtsByDistrict.getOrDefault(district.getCode(), 0L).intValue()))
            .toList();
        return ResultPage.of(rows, page, PAGE_SIZE);
    }

    /** Field errors of the form, including a code or a name that already exist. Empty when it can be saved. */
    public Map<String, String> validate(JudicialDistrictForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        List<JudicialDistrict> existing = catalogDao.findJudicialDistricts();
        if (!errors.containsKey("code")
                && existing.stream().anyMatch(district -> district.getCode().equalsIgnoreCase(form.getCode()))) {
            errors.put("code", CODE_TAKEN);
        }
        if (!errors.containsKey("name")
                && existing.stream().anyMatch(district -> CatalogNames.sameName(district.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        return errors;
    }

    /**
     * Saves the district as active. Nothing is saved when the form has errors or the code or the name
     * already exist.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public Saving register(User user, JudicialDistrictForm form) {
        requireJudicialBodyAdmin(user);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            return Saving.rejected(errors);
        }
        JudicialDistrict district =
            new JudicialDistrict(form.getCode(), form.getName(), form.getSeatMunicipality(), true);
        try {
            catalogDao.insertJudicialDistrict(district);
        } catch (DuplicateCodeException e) {
            // Another registration took it between the check and the insert
            return Saving.rejected(Map.of("code", CODE_TAKEN));
        } catch (DuplicateNameException e) {
            return Saving.rejected(Map.of("name", NAME_TAKEN));
        }
        return Saving.saved(district);
    }

    /** Outcome of saving a district: the saved district, or the field errors that stopped it. */
    public record Saving(JudicialDistrict district, Map<String, String> errors) {

        static Saving saved(JudicialDistrict district) {
            return new Saving(district, Map.of());
        }

        static Saving rejected(Map<String, String> errors) {
            return new Saving(null, errors);
        }

        public boolean isSaved() {
            return district != null;
        }
    }

    /**
     * The district with that code, to show it in the edit form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no district has the code
     */
    public JudicialDistrict get(User user, String code) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findJudicialDistrict(code)
            .orElseThrow(() -> new NotFoundException("No existe un distrito judicial con la clave indicada."));
    }

    /**
     * Updates the name and the seat of a district. The code is never changed, whatever the form carries.
     * Nothing is saved when the form has errors or the name already belongs to another district.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no district has the code
     */
    public Saving update(User user, String code, JudicialDistrictForm form) {
        JudicialDistrict current = get(user, code);
        Map<String, String> errors = new LinkedHashMap<>(form.validateWithoutCode());
        if (!errors.containsKey("name") && catalogDao.findJudicialDistricts().stream()
                .anyMatch(other -> !other.getCode().equals(code)
                    && CatalogNames.sameName(other.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        if (!errors.isEmpty()) {
            return Saving.rejected(errors);
        }
        try {
            catalogDao.updateJudicialDistrict(code, form.getName(), form.getSeatMunicipality());
        } catch (DuplicateNameException e) {
            // Another update took the name between the check and the write
            return Saving.rejected(Map.of("name", NAME_TAKEN));
        }
        return Saving.saved(new JudicialDistrict(code, form.getName(), form.getSeatMunicipality(),
            current.isActive()));
    }

    /**
     * Activates or deactivates a district (DFR 2.4). A district with active courts is not deactivated: the
     * message says how many there are. Once inactive, it is no longer offered in the court form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no district has the code
     * @throws BusinessRuleException when the district still has active courts
     */
    public JudicialDistrict changeStatus(User user, String code, boolean active) {
        JudicialDistrict current = get(user, code);
        if (!active) {
            long activeCourts = catalogDao.findCourts().stream()
                .filter(court -> court.getDistrictCode().equals(code) && court.isActive())
                .count();
            if (activeCourts > 0) {
                throw new BusinessRuleException("El distrito «" + current.getName() + "» tiene " + activeCourts
                    + (activeCourts == 1 ? " juzgado activo" : " juzgados activos")
                    + ". Desactívelos o cámbielos de distrito antes de desactivarlo.");
            }
        }
        if (current.isActive() != active) {
            catalogDao.updateJudicialDistrictStatus(code, active);
        }
        return new JudicialDistrict(code, current.getName(), current.getSeatMunicipality(), active);
    }

    /**
     * Checks that the user can manage the districts, so a page can refuse them before showing a form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public void requireAccess(User user) {
        requireJudicialBodyAdmin(user);
    }

    private static void requireJudicialBodyAdmin(User user) {
        if (user == null || user.getRole() != UserRole.JUDICIAL_BODY_ADMIN) {
            throw new AccessDeniedException(
                "Solo el Administrador del órgano judicial puede administrar los distritos judiciales.");
        }
    }
}
