package mx.edu.utez.judicial.oficialia.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Accounts kept in memory when no database is configured: the demo accounts of {@link SampleUsers} plus
 * the ones registered while the server runs (they are lost on restart).
 * With a database, {@link JdbcUserDao} is used instead.
 */
public class InMemoryUserDao implements UserDao {

    private static final InMemoryUserDao INSTANCE = new InMemoryUserDao(SampleUsers.asUserAccounts());

    private final ConcurrentMap<String, UserAccount> accountsByEmail = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> phonesByEmail = new ConcurrentHashMap<>();
    private final Function<String, String> courtNameByCode;

    public InMemoryUserDao(List<UserAccount> accounts) {
        this(accounts, InMemoryUserDao::sampleCourtName);
    }

    /** The court name lookup lets {@link #insert} fill the court name from the code, like the JOIN of MySQL does. */
    public InMemoryUserDao(List<UserAccount> accounts, Function<String, String> courtNameByCode) {
        this.courtNameByCode = courtNameByCode;
        accounts.forEach(account -> accountsByEmail.put(EmailValidator.normalize(account.user().getEmail()), account));
    }

    public static InMemoryUserDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return Optional.ofNullable(accountsByEmail.get(EmailValidator.normalize(email)));
    }

    @Override
    public List<UserSummary> findByCourt(String courtName) {
        return accountsByEmail.values().stream()
            .filter(account -> courtName != null && courtName.equals(account.user().getCourtName()))
            .map(account -> new UserSummary(account.user().getFullName(), account.user().getEmail(),
                phonesByEmail.get(EmailValidator.normalize(account.user().getEmail())), account.user().getRole(),
                account.active()))
            .sorted(Comparator.comparing(UserSummary::getFullName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    public void insert(NewUserAccount account) {
        String email = EmailValidator.normalize(account.email());
        String courtName = account.courtCode() == null ? null : courtNameByCode.apply(account.courtCode());
        User user = new User(email, account.firstName() + " " + account.lastName(), account.role(), courtName,
            account.mustChangePassword());
        if (accountsByEmail.putIfAbsent(email, new UserAccount(user, account.passwordHash(), account.active())) != null) {
            throw new DuplicateEmailException(email, null);
        }
        if (account.phone() != null) {
            phonesByEmail.put(email, account.phone());
        }
    }

    /** The user just chose the password, so the account stops being flagged to change it. */
    @Override
    public void updatePasswordHash(String email, String passwordHash) {
        accountsByEmail.computeIfPresent(EmailValidator.normalize(email),
            (key, account) -> new UserAccount(account.user().withPasswordChanged(), passwordHash, account.active()));
    }

    private static String sampleCourtName(String code) {
        return new InMemoryCatalogDao().findCourt(code).map(Court::getName).orElse(code);
    }
}
