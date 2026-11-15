package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Accounts kept in memory when no database is configured: the demo accounts of {@link SampleUsers}.
 * With a database, {@link JdbcUserDao} is used instead.
 */
public class InMemoryUserDao implements UserDao {

    private static final InMemoryUserDao INSTANCE = new InMemoryUserDao(SampleUsers.asUserAccounts());

    private final ConcurrentMap<String, UserAccount> accountsByEmail;

    public InMemoryUserDao(List<UserAccount> accounts) {
        accountsByEmail = accounts.stream()
            .collect(Collectors.toConcurrentMap(account -> EmailValidator.normalize(account.user().getEmail()),
                Function.identity()));
    }

    public static InMemoryUserDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return Optional.ofNullable(accountsByEmail.get(EmailValidator.normalize(email)));
    }

    @Override
    public void updatePasswordHash(String email, String passwordHash) {
        accountsByEmail.computeIfPresent(EmailValidator.normalize(email),
            (key, account) -> new UserAccount(account.user(), passwordHash, account.active()));
    }
}
