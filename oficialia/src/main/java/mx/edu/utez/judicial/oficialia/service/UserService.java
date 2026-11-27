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
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.RegisteredUser;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Registers, lists and corrects the users of a court (RF 1.4 to 1.6). Only the Administrador de Juzgado does
 * it, and only for their own court: the court is never taken from the form.
 */
public class UserService {

    static final String EMAIL_TAKEN = "Este correo ya está registrado.";
    static final String EMAIL_OF_ANOTHER_USER = "Este correo ya pertenece a otro usuario.";
    static final String OWN_ROLE = "No puede cambiar su propio perfil.";
    static final String USER_NOT_FOUND = "No se encontró el usuario en su juzgado.";

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

    /**
     * A user of the administrator's court, to open the edit form with the current data (RF 1.6). A user of
     * another court is reported as not found, so the administrator cannot tell whether it exists.
     *
     * @param id the id as it came in the request
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws NotFoundException when the id is not one of a user of the administrator's court
     */
    public UserDetails getForEdit(User admin, String id) {
        requireCourtAdmin(admin);
        long parsed;
        try {
            parsed = Long.parseLong(id == null ? "" : id.trim());
        } catch (NumberFormatException e) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        return userDao.findById(parsed)
            .filter(user -> admin.getCourtName().equals(user.getCourtName()))
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    /**
     * Field errors of the corrected data, including an e-mail that belongs to another user. Empty when it can
     * be saved. An administrator cannot change their own profile: they would lose the access they are using.
     */
    public Map<String, String> validateUpdate(User admin, UserDetails current, UserEditForm form) {
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        boolean sameEmail = EmailValidator.normalize(current.getEmail()).equals(form.getEmail());
        if (!errors.containsKey("email") && !sameEmail && userDao.findByEmail(form.getEmail()).isPresent()) {
            errors.put("email", EMAIL_OF_ANOTHER_USER);
        }
        if (!errors.containsKey("role") && isSelf(admin, current) && form.getRoleValue() != current.getRole()) {
            errors.put("role", OWN_ROLE);
        }
        return errors;
    }

    /**
     * Saves the corrected name, e-mail, phone and profile of a user of the administrator's court; the password,
     * the court and the status are kept. The change is written to the audit trail with the administrator and the
     * date. Nothing is saved when the form has errors.
     *
     * @throws AccessDeniedException when the user is not an Administrador de Juzgado
     * @throws NotFoundException when the id is not one of a user of the administrator's court
     */
    public Correction update(User admin, String id, UserEditForm form) {
        UserDetails current = getForEdit(admin, id);
        Map<String, String> errors = validateUpdate(admin, current, form);
        if (!errors.isEmpty()) {
            return Correction.rejected(errors);
        }
        try {
            userDao.update(form.toUpdate(current.getId()), admin);
        } catch (DuplicateEmailException e) {
            // Another account took the e-mail between the check and the write
            return Correction.rejected(Map.of("email", EMAIL_OF_ANOTHER_USER));
        }
        UserDetails saved = userDao.findById(current.getId()).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return Correction.saved(saved, isSelf(admin, current));
    }

    private static boolean isSelf(User admin, UserDetails user) {
        return EmailValidator.normalize(admin.getEmail()).equals(EmailValidator.normalize(user.getEmail()));
    }

    /**
     * Outcome of a correction: the user as stored afterwards, or the field errors that stopped it. When the
     * administrator corrected their own data, {@code self} tells the caller to refresh the signed-in user.
     */
    public record Correction(UserDetails user, boolean self, Map<String, String> errors) {

        static Correction saved(UserDetails user, boolean self) {
            return new Correction(user, self, Map.of());
        }

        static Correction rejected(Map<String, String> errors) {
            return new Correction(null, false, errors);
        }

        public boolean isSaved() {
            return user != null;
        }
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

    private static void requireCourtAdmin(User user) {
        if (user == null || !user.isCourtAdmin() || user.getCourtName() == null) {
            throw new AccessDeniedException("Solo el Administrador de Juzgado puede administrar los usuarios.");
        }
    }
}
