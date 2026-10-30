package mx.edu.utez.judicial.oficialia.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Checks credentials. Uses demo accounts until the users table exists; then the
 * passwords will be stored as hashes and read through a DAO.
 */
public class AuthService {

    private static final Map<String, DemoAccount> DEMO_ACCOUNTS = Map.of(
            "lmendez@utez.edu.mx", new DemoAccount("Oficialia2026",
                    new User("lmendez@utez.edu.mx", "Laura Méndez", "Oficial de Partes", "Juzgado de Cuautla")),
            "admin.cuautla@utez.edu.mx", new DemoAccount("Oficialia2026",
                    new User("admin.cuautla@utez.edu.mx", "Jorge Salas", "Administrador de Juzgado",
                            "Juzgado de Cuautla")));

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        DemoAccount account = DEMO_ACCOUNTS.get(email.trim().toLowerCase(Locale.ROOT));
        if (account == null || !account.password().equals(password)) {
            return Optional.empty();
        }
        return Optional.of(account.user());
    }

    private record DemoAccount(String password, User user) {
    }
}
