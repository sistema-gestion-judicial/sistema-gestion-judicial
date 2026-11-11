package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * Demo accounts for development. Without a database they are the only accounts; with one they are loaded
 * only when OFICIALIA_SAMPLE_USERS=true, so production never gets them. Only salted hashes are kept here;
 * the demo password is shared with the team outside the repository.
 */
public final class SampleUsers {

    private static final String CUAUTLA_CODE = "12";
    private static final String CUAUTLA_NAME = "Juzgado de Cuautla";

    private SampleUsers() {
    }

    public static List<NewUserAccount> accounts() {
        return List.of(
            new NewUserAccount("Laura", "Méndez", "lmendez@utez.edu.mx", UserRole.CLERK, CUAUTLA_CODE,
                "600000:nUd+s0wZ6/YYBDgkb3uOIQ==:pAU208hbwEBg1oCUXi/y0YixRfxWm64w0CZmS589fJI=", false, true),
            new NewUserAccount("Jorge", "Salas", "admin.cuautla@utez.edu.mx", UserRole.COURT_ADMIN, CUAUTLA_CODE,
                "600000:bwb+592evMrGu/AkB1T3fA==:Vl24sjR30O7Qf+le2NbclAxa1PySpCVHQJ+sGHjNB80=", false, true),
            // This role oversees every court, so it has no court of its own
            new NewUserAccount("Elena", "Ríos", "admin.organo@utez.edu.mx", UserRole.JUDICIAL_BODY_ADMIN, null,
                "600000:rPUa/+93FCrD41pn//9nRg==:M7m2y3WRF9aQlyqMc+Jr+/FM9cUI3kUSgHkadSvAUUg=", false, true),
            new NewUserAccount("Mario", "Vega", "inactivo@utez.edu.mx", UserRole.CLERK, CUAUTLA_CODE,
                "600000:Jz6UCXXsn2qWVxXxGrT+Mg==:8jI9nBu8bl4PnRIRgvtwKqyU9C5S4ckzbg5NU32iLIA=", false, false));
    }

    /** The same accounts as {@link InMemoryUserDao} keeps them. */
    static List<UserAccount> asUserAccounts() {
        return accounts().stream()
            .map(account -> new UserAccount(new User(account.email(),
                account.firstName() + " " + account.lastName(), account.role(),
                account.courtCode() == null ? null : CUAUTLA_NAME), account.passwordHash(), account.active()))
            .toList();
    }
}
