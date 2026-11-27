package mx.edu.utez.judicial.oficialia.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DuplicateEmailException;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.RegisteredUser;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
import mx.edu.utez.judicial.oficialia.model.UserProfile;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Registers and lists the users of a court (RF 1.4). Only the Administrador de Juzgado does it, and only
 * for their own court: the court is never taken from the form.
 */
public class UserService {

    static final String EMAIL_TAKEN = "Este correo ya está registrado.";
    static final String OWN_ROLE = "No puede cambiar su propio perfil.";
    public static final int PAGE_SIZE = 10;

    private final UserDao userDao;
    private final CatalogDao catalogDao;

    public UserService() {
        this(OficialiaDaos.users(), OficialiaDaos.catalogs());
    }

    public UserService(UserDao userDao, CatalogDao catalogDao) {
        this.userDao = userDao;
        this.catalogDao = catalogDao;
    }

    /**
     * The users of the administrator's court.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     */
    public List<UserSummary> listForCourt(User admin) {
        requireCourtAdmin(admin);
        return userDao.findByCourt(admin.getCourtName());
    }

    /**
     * Field errors of the form, including an e-mail that is already registered. Empty when it can be saved.
     */
    public Map<String, String> validate(NewUserForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        if (!errors.containsKey("email") && userDao.findByEmail(form.getEmail()).isPresent()) {
            errors.put("email", EMAIL_TAKEN);
        }
        return errors;
    }

    /**
     * Saves the user as an active clerk of the administrator's court, with a temporary password they must
     * replace the first time they sign in. Nothing is saved when the form has errors.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws BusinessRuleException when the administrator's court does not exist
     */
    public Registration register(User admin, NewUserForm form) {
        requireCourtAdmin(admin);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        Court court = catalogDao.findCourts().stream()
            .filter(candidate -> candidate.getName().equals(admin.getCourtName()))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("No se encontró el juzgado de su cuenta."));

        String temporaryPassword = TemporaryPassword.generate();
        try {
            userDao.insert(new NewUserAccount(form.getFirstName(), form.getLastName(), form.getEmail(),
                form.getPhoneDigits(), UserRole.CLERK, court.getCode(), PasswordHasher.hash(temporaryPassword),
                true, true));
        } catch (DuplicateEmailException e) {
            // Another registration took the e-mail between the check and the insert
            return Registration.rejected(Map.of("email", EMAIL_TAKEN));
        }
        return Registration.saved(new RegisteredUser(form.getFirstName() + " " + form.getLastName(),
            form.getEmail(), temporaryPassword));
    }

    /** Outcome of a registration: the saved user, or the field errors that stopped it. */
    public record Registration(RegisteredUser user, Map<String, String> errors) {

        static Registration saved(RegisteredUser user) {
            return new Registration(user, Map.of());
        }

        static Registration rejected(Map<String, String> errors) {
            return new Registration(null, errors);
        }

        public boolean isSaved() {
            return user != null;
        }
    }

    /**
     * The page of the court's users that matches the filters (DFR 1.5). The text is looked for in the name and
     * the e-mail, ignoring case and accents; a null status shows active and inactive users. Users of other
     * courts never show up, even when their e-mail is typed.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     */
    public ResultPage<UserSummary> search(User admin, String text, Boolean active, int page) {
        String key = CatalogNames.key(text);
        List<UserSummary> rows = listForCourt(admin).stream()
            .filter(user -> active == null || user.isActive() == active)
            .filter(user -> key.isEmpty() || CatalogNames.key(user.getFullName()).contains(key)
                || CatalogNames.key(user.getEmail()).contains(key))
            .toList();
        return ResultPage.of(rows, page, PAGE_SIZE);
    }

    /**
     * A user of the administrator's court, to fill the edit form. A user of another court is reported as not
     * found, so the page does not reveal that the e-mail exists.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws NotFoundException when the court has no user with that e-mail
     */
    public UserProfile get(User admin, String email) {
        requireCourtAdmin(admin);
        return userDao.findProfile(email)
            .filter(profile -> admin.getCourtName().equals(profile.getCourtName()))
            .orElseThrow(() -> new NotFoundException("No existe un usuario de su juzgado con ese correo."));
    }

    /**
     * Corrects the name, e-mail, phone and profile of a user of the court (DFR 1.6) and writes it to the
     * audit log. The password, the court and the status never change here. Administrators cannot change their
     * own profile, so a court is never left without one by mistake.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws NotFoundException when the court has no user with that e-mail
     */
    public Update update(User admin, String email, UserEditForm form) {
        UserProfile current = get(admin, email);
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        if (!errors.containsKey("email") && !form.getEmail().equals(current.getEmail())
                && userDao.findByEmail(form.getEmail()).isPresent()) {
            errors.put("email", EMAIL_TAKEN);
        }
        boolean self = current.getEmail().equals(admin.getEmail());
        if (!errors.containsKey("role") && self && form.getRole() != current.getRole()) {
            errors.put("role", OWN_ROLE);
        }
        if (!errors.isEmpty()) {
            return Update.rejected(errors);
        }
        UserProfile changes = new UserProfile(form.getFirstName(), form.getLastName(), form.getEmail(),
            form.getPhoneDigits(), form.getRole(), current.getCourtName(), current.isActive());
        try {
            userDao.update(current.getEmail(), changes, admin);
        } catch (DuplicateEmailException e) {
            return Update.rejected(Map.of("email", EMAIL_TAKEN));
        }
        return Update.saved(changes);
    }

    /**
     * Activates or deactivates a user of the court (DFR 1.7) and writes it to the audit log. The change takes
     * effect on the user's next request. What they captured keeps showing their name.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws NotFoundException when the court has no user with that e-mail
     * @throws BusinessRuleException when administrators try to deactivate themselves
     */
    public UserProfile changeStatus(User admin, String email, boolean active) {
        UserProfile current = get(admin, email);
        if (!active && current.getEmail().equals(admin.getEmail())) {
            throw new BusinessRuleException("No puede desactivar su propia cuenta.");
        }
        if (current.isActive() != active) {
            userDao.updateStatus(current.getEmail(), active, admin);
        }
        return new UserProfile(current.getFirstName(), current.getLastName(), current.getEmail(),
            current.getPhone(), current.getRole(), current.getCourtName(), active);
    }

    /** Outcome of correcting a user: the saved data, or the field errors that stopped it. */
    public record Update(UserProfile user, Map<String, String> errors) {

        static Update saved(UserProfile user) {
            return new Update(user, Map.of());
        }

        static Update rejected(Map<String, String> errors) {
            return new Update(null, errors);
        }

        public boolean isSaved() {
            return user != null;
        }
    }

    private static void requireCourtAdmin(User user) {
        if (user == null || !user.isCourtAdmin() || user.getCourtName() == null) {
            throw new AccessDeniedException("Solo el Administrador de Juzgado puede administrar los usuarios.");
        }
    }
}
