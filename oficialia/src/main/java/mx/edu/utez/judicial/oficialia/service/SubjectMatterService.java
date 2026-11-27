package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateCodeException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNameException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterForm;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterRow;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Manages the catalog of subject matters (DFR 2.5 to 2.8). Only the Administrador del órgano judicial can
 * change it; the code never changes because the other systems use it as the key.
 */
public class SubjectMatterService {

    static final String CODE_TAKEN = "Esta clave ya existe.";
    static final String NAME_TAKEN = "Este nombre ya pertenece a otra materia.";
    public static final int PAGE_SIZE = 10;

    private final CatalogDao catalogDao;

    public SubjectMatterService() {
        this(OficialiaDaos.catalogs());
    }

    public SubjectMatterService(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /**
     * The page of the catalog that matches the filters (DFR 2.6), ordered by name, with the number of courts
     * that handle each subject matter. The text is looked for in the code, the name and the description,
     * ignoring case and accents; a null status shows active and inactive ones.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public ResultPage<SubjectMatterRow> search(User user, String text, Boolean active, int page) {
        requireJudicialBodyAdmin(user);
        String key = CatalogNames.key(text);
        List<Court> courts = catalogDao.findCourts();
        List<SubjectMatterRow> rows = catalogDao.findSubjectMatters().stream()
            .filter(matter -> active == null || matter.isActive() == active)
            .filter(matter -> key.isEmpty()
                || Stream.of(matter.getCode(), matter.getName(), matter.getDescription())
                    .anyMatch(value -> CatalogNames.key(value).contains(key)))
            .map(matter -> new SubjectMatterRow(matter, (int) courts.stream()
                .filter(court -> court.getSubjectMatterCodes().contains(matter.getCode())).count()))
            .toList();
        return ResultPage.of(rows, page, PAGE_SIZE);
    }

    /**
     * The names of the active subject matters, ordered by name, for the forms that register new things
     * (DFR 2.8): an inactive one is no longer offered, but what was registered with it keeps showing it.
     */
    public List<String> activeNames() {
        return catalogDao.findSubjectMatters().stream()
            .filter(SubjectMatter::isActive)
            .map(SubjectMatter::getName)
            .toList();
    }

    /** Field errors of the form, including a code or a name that already exist. Empty when it can be saved. */
    public Map<String, String> validate(SubjectMatterForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        List<SubjectMatter> existing = catalogDao.findSubjectMatters();
        if (!errors.containsKey("code")
                && existing.stream().anyMatch(matter -> matter.getCode().equalsIgnoreCase(form.getCode()))) {
            errors.put("code", CODE_TAKEN);
        }
        if (!errors.containsKey("name")
                && existing.stream().anyMatch(matter -> CatalogNames.sameName(matter.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        return errors;
    }

    /**
     * Saves the subject matter as active. Nothing is saved when the form has errors or the code or the name
     * already exist.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public Saving register(User user, SubjectMatterForm form) {
        requireJudicialBodyAdmin(user);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            return Saving.rejected(errors);
        }
        SubjectMatter matter = new SubjectMatter(0, form.getCode(), form.getName(), form.getDescriptionOrNull(), true);
        try {
            catalogDao.insertSubjectMatter(matter);
        } catch (DuplicateCodeException e) {
            // Another registration took it between the check and the insert
            return Saving.rejected(Map.of("code", CODE_TAKEN));
        } catch (DuplicateNameException e) {
            return Saving.rejected(Map.of("name", NAME_TAKEN));
        }
        return Saving.saved(matter);
    }

    /**
     * The subject matter with that code.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no subject matter has the code
     */
    public SubjectMatter get(User user, String code) {
        requireJudicialBodyAdmin(user);
        return catalogDao.findSubjectMatter(code)
            .orElseThrow(() -> new NotFoundException("No existe una materia con la clave indicada."));
    }

    /**
     * Updates the name and the description. The code is never changed, whatever the form carries, so the
     * demand requests stay linked to the same subject matter. Nothing is saved when the form has errors or
     * the name already belongs to another subject matter.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no subject matter has the code
     */
    public Saving update(User user, String code, SubjectMatterForm form) {
        SubjectMatter current = get(user, code);
        Map<String, String> errors = new LinkedHashMap<>(form.validateWithoutCode());
        if (!errors.containsKey("name") && catalogDao.findSubjectMatters().stream()
                .anyMatch(other -> !other.getCode().equals(code)
                    && CatalogNames.sameName(other.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        if (!errors.isEmpty()) {
            return Saving.rejected(errors);
        }
        try {
            catalogDao.updateSubjectMatter(code, form.getName(), form.getDescriptionOrNull());
        } catch (DuplicateNameException e) {
            return Saving.rejected(Map.of("name", NAME_TAKEN));
        }
        return Saving.saved(new SubjectMatter(current.getId(), code, form.getName(), form.getDescriptionOrNull(),
            current.isActive()));
    }

    /**
     * Activates or deactivates a subject matter (DFR 2.8). Deactivating it only stops offering it in new
     * registrations; the requests and courts that already have it keep it.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     * @throws NotFoundException when no subject matter has the code
     */
    public SubjectMatter changeStatus(User user, String code, boolean active) {
        SubjectMatter current = get(user, code);
        if (current.isActive() != active) {
            catalogDao.updateSubjectMatterStatus(code, active);
        }
        return new SubjectMatter(current.getId(), code, current.getName(), current.getDescription(), active);
    }

    /**
     * Checks that the user can manage the subject matters, so a page can refuse them before showing a form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public void requireAccess(User user) {
        requireJudicialBodyAdmin(user);
    }

    /** Outcome of saving a subject matter: the saved one, or the field errors that stopped it. */
    public record Saving(SubjectMatter matter, Map<String, String> errors) {

        static Saving saved(SubjectMatter matter) {
            return new Saving(matter, Map.of());
        }

        static Saving rejected(Map<String, String> errors) {
            return new Saving(null, errors);
        }

        public boolean isSaved() {
            return matter != null;
        }
    }

    private static void requireJudicialBodyAdmin(User user) {
        if (user == null || user.getRole() != UserRole.JUDICIAL_BODY_ADMIN) {
            throw new AccessDeniedException(
                "Solo el Administrador del órgano judicial puede administrar las materias.");
        }
    }
}
