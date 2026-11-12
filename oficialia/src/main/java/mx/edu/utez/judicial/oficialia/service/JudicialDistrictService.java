package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateCodeException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNameException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictForm;
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
    public Registration register(User user, JudicialDistrictForm form) {
        requireJudicialBodyAdmin(user);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        JudicialDistrict district =
            new JudicialDistrict(form.getCode(), form.getName(), form.getSeatMunicipality(), true);
        try {
            catalogDao.insertJudicialDistrict(district);
        } catch (DuplicateCodeException e) {
            // Another registration took it between the check and the insert
            return Registration.rejected(Map.of("code", CODE_TAKEN));
        } catch (DuplicateNameException e) {
            return Registration.rejected(Map.of("name", NAME_TAKEN));
        }
        return Registration.saved(district);
    }

    /** Outcome of a registration: the saved district, or the field errors that stopped it. */
    public record Registration(JudicialDistrict district, Map<String, String> errors) {

        static Registration saved(JudicialDistrict district) {
            return new Registration(district, Map.of());
        }

        static Registration rejected(Map<String, String> errors) {
            return new Registration(null, errors);
        }

        public boolean isSaved() {
            return district != null;
        }
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
