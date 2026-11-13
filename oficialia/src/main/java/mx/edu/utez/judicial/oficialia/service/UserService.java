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

/**
 * Registers and lists the users of a court (RF 1.4). Only the Administrador de Juzgado does it, and only
 * for their own court: the court is never taken from the form.
 */
public class UserService {

    static final String EMAIL_TAKEN = "Este correo ya está registrado.";

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

    private static void requireCourtAdmin(User user) {
        if (user == null || !user.isCourtAdmin() || user.getCourtName() == null) {
            throw new AccessDeniedException("Solo el Administrador de Juzgado puede registrar usuarios.");
        }
    }
}
