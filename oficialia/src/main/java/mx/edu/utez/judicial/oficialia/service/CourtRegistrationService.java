package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateCodeException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateEmailException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNameException;
import mx.edu.utez.judicial.oficialia.dao.DuplicateNumberException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.CourtRegistrationForm;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Registers a court and its administrator (DFR 2.9). Only the Administrador del órgano judicial does it.
 * The court is always saved as active, and its administrator can sign in at once but must replace the
 * temporary password the first time.
 */
public class CourtRegistrationService {

    static final String CODE_TAKEN = "Esta clave ya pertenece a otro juzgado.";
    static final String NUMBER_TAKEN = "Este número ya pertenece a otro juzgado.";
    static final String NAME_TAKEN = "Este nombre ya pertenece a otro juzgado.";
    static final String EMAIL_TAKEN = "Este correo ya está registrado.";
    static final String DISTRICT_UNAVAILABLE = "Elija un distrito judicial activo.";
    static final String MATTER_UNAVAILABLE = "Elija solo materias activas.";

    private final CatalogDao catalogDao;
    private final UserDao userDao;

    public CourtRegistrationService() {
        this(OficialiaDaos.catalogs(), OficialiaDaos.users());
    }

    public CourtRegistrationService(CatalogDao catalogDao, UserDao userDao) {
        this.catalogDao = catalogDao;
        this.userDao = userDao;
    }

    /**
     * Checks that the user can register courts, so a page can refuse them before showing the form.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public void requireAccess(User user) {
        if (user == null || user.getRole() != UserRole.JUDICIAL_BODY_ADMIN) {
            throw new AccessDeniedException("Solo el Administrador del órgano judicial puede registrar juzgados.");
        }
    }

    /**
     * The districts the form offers: only the active ones, by name.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public List<JudicialDistrict> activeDistricts(User user) {
        requireAccess(user);
        return catalogDao.findJudicialDistricts().stream().filter(JudicialDistrict::isActive).toList();
    }

    /**
     * The subject matters the form offers: only the active ones.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public List<SubjectMatter> activeSubjectMatters(User user) {
        requireAccess(user);
        return catalogDao.findSubjectMatters().stream().filter(SubjectMatter::isActive).toList();
    }

    /**
     * Field errors of the form, including a code, a number, a name or an e-mail that are already taken and a
     * district or subject matter that is not available. Empty when it can be saved.
     */
    public Map<String, String> validate(CourtRegistrationForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        List<Court> courts = catalogDao.findCourts();
        if (!errors.containsKey("code") && courts.stream().anyMatch(court -> court.getCode().equals(form.getCode()))) {
            errors.put("code", CODE_TAKEN);
        }
        if (!errors.containsKey("number")
                && courts.stream().anyMatch(court -> court.getNumber() == form.getNumberValue())) {
            errors.put("number", NUMBER_TAKEN);
        }
        if (!errors.containsKey("name")
                && courts.stream().anyMatch(court -> CatalogNames.sameName(court.getName(), form.getName()))) {
            errors.put("name", NAME_TAKEN);
        }
        if (!errors.containsKey("districtCode") && activeDistrict(form.getDistrictCode()) == null) {
            errors.put("districtCode", DISTRICT_UNAVAILABLE);
        }
        if (!errors.containsKey("subjectMatterCodes")) {
            Set<String> active = catalogDao.findSubjectMatters().stream()
                .filter(SubjectMatter::isActive)
                .map(SubjectMatter::getCode)
                .collect(Collectors.toSet());
            if (!active.containsAll(form.getSubjectMatterCodes())) {
                errors.put("subjectMatterCodes", MATTER_UNAVAILABLE);
            }
        }
        if (!errors.containsKey("adminEmail") && userDao.findByEmail(form.getAdminEmail()).isPresent()) {
            errors.put("adminEmail", EMAIL_TAKEN);
        }
        return errors;
    }

    /**
     * Saves the court as active and creates the account of its administrator with the temporary password
     * typed in the form. Nothing is saved when the form has errors.
     *
     * @throws AccessDeniedException when the user is not the Administrador del órgano judicial
     */
    public Registration register(User user, CourtRegistrationForm form) {
        requireAccess(user);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        JudicialDistrict district = activeDistrict(form.getDistrictCode());
        CourtAddress address = new CourtAddress(form.getStreet(), form.getNeighborhood(), form.getPostalCode(),
            form.getMunicipality(), form.getPhoneDigits());
        Court court = new Court(form.getCode(), form.getNumberValue(), form.getName(), district.getCode(),
            district.getName(), address, form.getSubjectMatterCodes(), true);
        NewUserAccount administrator = new NewUserAccount(form.getAdminFirstName(), form.getAdminLastName(),
            form.getAdminEmail(), UserRole.COURT_ADMIN, court.getCode(), PasswordHasher.hash(form.getAdminPassword()),
            true, true);
        try {
            catalogDao.insertCourt(court, administrator);
        } catch (DuplicateCodeException e) {
            // Another registration took it between the check and the insert
            return Registration.rejected(Map.of("code", CODE_TAKEN));
        } catch (DuplicateNumberException e) {
            return Registration.rejected(Map.of("number", NUMBER_TAKEN));
        } catch (DuplicateNameException e) {
            return Registration.rejected(Map.of("name", NAME_TAKEN));
        } catch (DuplicateEmailException e) {
            return Registration.rejected(Map.of("adminEmail", EMAIL_TAKEN));
        }
        return Registration.saved(court, form.getAdminFirstName() + " " + form.getAdminLastName(),
            form.getAdminEmail());
    }

    private JudicialDistrict activeDistrict(String code) {
        return catalogDao.findJudicialDistrict(code).filter(JudicialDistrict::isActive).orElse(null);
    }

    /** Outcome of a registration: the saved court and its administrator, or the field errors that stopped it. */
    public record Registration(Court court, String administratorName, String administratorEmail,
                               Map<String, String> errors) {

        static Registration saved(Court court, String administratorName, String administratorEmail) {
            return new Registration(court, administratorName, administratorEmail, Map.of());
        }

        static Registration rejected(Map<String, String> errors) {
            return new Registration(null, null, null, errors);
        }

        public boolean isSaved() {
            return court != null;
        }
    }
}
