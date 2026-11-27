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
import mx.edu.utez.judicial.oficialia.model.UserProfile;
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
    // First name and last name apart, as the users table keeps them; the User only has the full name
    private final ConcurrentMap<String, String[]> namesByEmail = new ConcurrentHashMap<>();
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
        namesByEmail.put(email, new String[] {account.firstName(), account.lastName()});
    }

    /** The user just chose the password, so the account stops being flagged to change it. */
    @Override
    public void updatePasswordHash(String email, String passwordHash) {
        accountsByEmail.computeIfPresent(EmailValidator.normalize(email),
            (key, account) -> new UserAccount(account.user().withPasswordChanged(), passwordHash, account.active()));
    }

    @Override
    public Optional<UserProfile> findProfile(String email) {
        String key = EmailValidator.normalize(email);
        UserAccount account = accountsByEmail.get(key);
        if (account == null) {
            return Optional.empty();
        }
        String[] names = names(key, account.user());
        return Optional.of(new UserProfile(names[0], names[1], account.user().getEmail(), phonesByEmail.get(key),
            account.user().getRole(), account.user().getCourtName(), account.active()));
    }

    @Override
    public synchronized void update(String email, UserProfile changes, User actor) {
        String current = EmailValidator.normalize(email);
        String updatedEmail = EmailValidator.normalize(changes.getEmail());
        UserAccount account = accountsByEmail.get(current);
        if (account == null) {
            return;
        }
        if (!updatedEmail.equals(current) && accountsByEmail.containsKey(updatedEmail)) {
            throw new DuplicateEmailException(updatedEmail, null);
        }
        User user = new User(updatedEmail, changes.getFullName(), changes.getRole(), account.user().getCourtName(),
            account.user().isMustChangePassword());
        accountsByEmail.remove(current);
        phonesByEmail.remove(current);
        namesByEmail.remove(current);
        accountsByEmail.put(updatedEmail, new UserAccount(user, account.passwordHash(), account.active()));
        if (changes.getPhone() != null) {
            phonesByEmail.put(updatedEmail, changes.getPhone());
        }
        namesByEmail.put(updatedEmail, new String[] {changes.getFirstName(), changes.getLastName()});
    }

    @Override
    public void updateStatus(String email, boolean active, User actor) {
        accountsByEmail.computeIfPresent(EmailValidator.normalize(email),
            (key, account) -> new UserAccount(account.user(), account.passwordHash(), active));
    }

    /** The sample accounts only have a full name, so it is split at the first space. */
    private String[] names(String email, User user) {
        String[] names = namesByEmail.get(email);
        if (names != null) {
            return names;
        }
        String fullName = user.getFullName();
        int space = fullName.indexOf(' ');
        return space < 0 ? new String[] {fullName, ""}
            : new String[] {fullName.substring(0, space), fullName.substring(space + 1)};
    }

    private static String sampleCourtName(String code) {
        return new InMemoryCatalogDao().findCourt(code).map(Court::getName).orElse(code);
    }
}
