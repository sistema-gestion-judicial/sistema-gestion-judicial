package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * TEMPORARY demo accounts, kept until the users table exists (#28). Only salted hashes are
 * stored here; the demo password is shared with the team outside the repository.
 */
public class InMemoryUserDao implements UserDao {

    private static final InMemoryUserDao INSTANCE = new InMemoryUserDao(sampleAccounts());

    private final Map<String, UserAccount> accountsByEmail;

    public InMemoryUserDao(List<UserAccount> accounts) {
        accountsByEmail = accounts.stream()
                .collect(Collectors.toUnmodifiableMap(account -> normalize(account.user().getEmail()),
                        Function.identity()));
    }

    public static InMemoryUserDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return Optional.ofNullable(accountsByEmail.get(normalize(email)));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static List<UserAccount> sampleAccounts() {
        String cuautla = "Juzgado de Cuautla";
        return List.of(
                new UserAccount(new User("lmendez@utez.edu.mx", "Laura Méndez", UserRole.CLERK, cuautla),
                        "600000:nUd+s0wZ6/YYBDgkb3uOIQ==:pAU208hbwEBg1oCUXi/y0YixRfxWm64w0CZmS589fJI="),
                new UserAccount(new User("admin.cuautla@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, cuautla),
                        "600000:bwb+592evMrGu/AkB1T3fA==:Vl24sjR30O7Qf+le2NbclAxa1PySpCVHQJ+sGHjNB80="),
                // This role oversees every court, so it has no court of its own
                new UserAccount(new User("admin.organo@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null),
                        "600000:rPUa/+93FCrD41pn//9nRg==:M7m2y3WRF9aQlyqMc+Jr+/FM9cUI3kUSgHkadSvAUUg="));
    }
}
