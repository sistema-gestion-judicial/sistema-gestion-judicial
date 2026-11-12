package mx.edu.utez.judicial.oficialia.listener;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import mx.edu.utez.judicial.oficialia.dao.JdbcUserDao;
import mx.edu.utez.judicial.oficialia.dao.SampleUsers;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.service.PasswordHasher;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Creates the first accounts when the users table is empty, so someone can sign in after the first deploy:
 *
 * <ul>
 *   <li>The judicial body administrator from OFICIALIA_ADMIN_EMAIL and OFICIALIA_ADMIN_PASSWORD (and,
 *       optionally, OFICIALIA_ADMIN_FIRST_NAME and OFICIALIA_ADMIN_LAST_NAME). That administrator then
 *       registers the courts and their administrators.</li>
 *   <li>The demo accounts of {@link SampleUsers}, only with OFICIALIA_SAMPLE_USERS=true. For development:
 *       production must not set it.</li>
 * </ul>
 *
 * Nothing happens once the table has accounts, so changing these variables later changes no one's password.
 */
final class InitialAccounts {

    private static final Logger LOG = Logger.getLogger(InitialAccounts.class.getName());
    private static final int PASSWORD_MIN_LENGTH = 8;

    private final JdbcUserDao users;
    private final Function<String, String> environment;

    InitialAccounts(JdbcUserDao users, Function<String, String> environment) {
        this.users = users;
        this.environment = environment;
    }

    void createIfEmpty() {
        if (users.count() > 0) {
            return;
        }
        boolean created = createAdministrator();
        if ("true".equalsIgnoreCase(read("OFICIALIA_SAMPLE_USERS"))) {
            SampleUsers.accounts().stream()
                .filter(account -> users.findByEmail(account.email()).isEmpty())
                .forEach(users::insert);
            LOG.info("Loaded the demo user accounts (OFICIALIA_SAMPLE_USERS=true)");
            created = true;
        }
        if (!created) {
            LOG.warning("There are no user accounts and nobody can sign in: set OFICIALIA_ADMIN_EMAIL and "
                + "OFICIALIA_ADMIN_PASSWORD and restart to create the judicial body administrator");
        }
    }

    private boolean createAdministrator() {
        String email = read("OFICIALIA_ADMIN_EMAIL");
        String password = read("OFICIALIA_ADMIN_PASSWORD");
        if (email == null && password == null) {
            return false;
        }
        Optional<String> problem = problemWith(email, password);
        if (problem.isPresent()) {
            LOG.warning("The judicial body administrator was not created: " + problem.get());
            return false;
        }
        String firstName = Optional.ofNullable(read("OFICIALIA_ADMIN_FIRST_NAME")).orElse("Administrador");
        String lastName = Optional.ofNullable(read("OFICIALIA_ADMIN_LAST_NAME")).orElse("del órgano judicial");
        users.insert(new NewUserAccount(firstName, lastName, email, UserRole.JUDICIAL_BODY_ADMIN, null,
            PasswordHasher.hash(password), false, true));
        LOG.info("Created the judicial body administrator " + EmailValidator.normalize(email));
        return true;
    }

    /** The same rules as any password in the system: 8 characters or more, with letters and numbers (DFR 1.3). */
    static Optional<String> problemWith(String email, String password) {
        if (email == null || !EmailValidator.isValid(email)) {
            return Optional.of("OFICIALIA_ADMIN_EMAIL is missing or is not a valid e-mail");
        }
        if (password == null || password.length() < PASSWORD_MIN_LENGTH
            || password.chars().noneMatch(Character::isLetter) || password.chars().noneMatch(Character::isDigit)) {
            return Optional.of("OFICIALIA_ADMIN_PASSWORD must have at least " + PASSWORD_MIN_LENGTH
                + " characters, with letters and numbers");
        }
        return Optional.empty();
    }

    private String read(String name) {
        String value = environment.apply(name);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
