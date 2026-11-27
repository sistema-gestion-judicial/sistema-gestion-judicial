package mx.edu.utez.judicial.oficialia.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.model.UserUpdate;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Accounts kept in memory when no database is configured: the demo accounts of {@link SampleUsers} plus
 * the ones registered while the server runs (they are lost on restart). There is no audit trail here: it
 * lives in the audit_log table, so it is written only by {@link JdbcUserDao}.
 * With a database, {@link JdbcUserDao} is used instead.
 */
public class InMemoryUserDao implements UserDao {

    private static final InMemoryUserDao INSTANCE = new InMemoryUserDao(SampleUsers.asUserAccounts());

    /** What the users table keeps besides the account: the id, the name split in two and the phone. */
    private record Entry(long id, String firstName, String lastName, String phone, UserAccount account) {

        Entry withAccount(UserAccount newAccount) {
            return new Entry(id, firstName, lastName, phone, newAccount);
        }
    }

    private final ConcurrentMap<String, Entry> entriesByEmail = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Function<String, String> courtNameByCode;

    public InMemoryUserDao(List<UserAccount> accounts) {
        this(accounts, InMemoryUserDao::sampleCourtName);
    }

    /** The court name lookup lets {@link #insert} fill the court name from the code, like the JOIN of MySQL does. */
    public InMemoryUserDao(List<UserAccount> accounts, Function<String, String> courtNameByCode) {
        this.courtNameByCode = courtNameByCode;
        for (UserAccount account : accounts) {
            // Only the full name is known here: the first word is taken as the first name
            String[] names = account.user().getFullName().trim().split("\\s+", 2);
            entriesByEmail.put(EmailValidator.normalize(account.user().getEmail()), new Entry(nextId.getAndIncrement(),
                names[0], names.length > 1 ? names[1] : "", null, account));
        }
    }

    public static InMemoryUserDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return Optional.ofNullable(entriesByEmail.get(EmailValidator.normalize(email))).map(Entry::account);
    }

    @Override
    public List<UserSummary> findByCourt(String courtName) {
        return entriesByEmail.values().stream()
            .filter(entry -> courtName != null && courtName.equals(entry.account().user().getCourtName()))
            .map(entry -> new UserSummary(entry.id(), entry.account().user().getFullName(),
                entry.account().user().getEmail(), entry.phone(), entry.account().user().getRole(),
                entry.account().active()))
            .sorted(Comparator.comparing(UserSummary::getFullName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    public synchronized void insert(NewUserAccount account) {
        String email = EmailValidator.normalize(account.email());
        if (entriesByEmail.containsKey(email)) {
            throw new DuplicateEmailException(email, null);
        }
        String courtName = account.courtCode() == null ? null : courtNameByCode.apply(account.courtCode());
        User user = new User(email, account.firstName() + " " + account.lastName(), account.role(), courtName,
            account.mustChangePassword());
        entriesByEmail.put(email, new Entry(nextId.getAndIncrement(), account.firstName(), account.lastName(),
            account.phone(), new UserAccount(user, account.passwordHash(), account.active())));
    }

    /** The user just chose the password, so the account stops being flagged to change it. */
    @Override
    public synchronized void updatePasswordHash(String email, String passwordHash) {
        entriesByEmail.computeIfPresent(EmailValidator.normalize(email), (key, entry) -> entry.withAccount(
            new UserAccount(entry.account().user().withPasswordChanged(), passwordHash, entry.account().active())));
    }

    @Override
    public Optional<UserDetails> findById(long id) {
        return findEntry(id).map(entry -> new UserDetails(entry.id(), entry.firstName(), entry.lastName(),
            entry.account().user().getEmail(), entry.phone(), entry.account().user().getRole(),
            entry.account().user().getCourtName(), entry.account().active()));
    }

    /** Keeps the password, the court and the status; the account is moved to its new e-mail when it changes. */
    @Override
    public synchronized void update(UserUpdate update, User actor) {
        Entry current = findEntry(update.id()).orElse(null);
        if (current == null) {
            return;
        }
        String oldEmail = EmailValidator.normalize(current.account().user().getEmail());
        String newEmail = EmailValidator.normalize(update.email());
        if (!newEmail.equals(oldEmail) && entriesByEmail.containsKey(newEmail)) {
            throw new DuplicateEmailException(newEmail, null);
        }
        User old = current.account().user();
        User user = new User(newEmail, update.firstName() + " " + update.lastName(), update.role(),
            old.getCourtName(), old.isMustChangePassword());
        entriesByEmail.remove(oldEmail);
        entriesByEmail.put(newEmail, new Entry(current.id(), update.firstName(), update.lastName(), update.phone(),
            new UserAccount(user, current.account().passwordHash(), current.account().active())));
    }

    @Override
    public synchronized boolean updateStatus(long id, boolean active, String reason, User actor) {
        Entry current = findEntry(id).orElse(null);
        if (current == null || current.account().active() == active) {
            return false;
        }
        UserAccount account = current.account();
        entriesByEmail.put(EmailValidator.normalize(account.user().getEmail()),
            current.withAccount(new UserAccount(account.user(), account.passwordHash(), active)));
        return true;
    }

    private Optional<Entry> findEntry(long id) {
        return entriesByEmail.values().stream().filter(entry -> entry.id() == id).findFirst();
    }

    private static String sampleCourtName(String code) {
        return new InMemoryCatalogDao().findCourt(code).map(Court::getName).orElse(code);
    }
}
