package mx.edu.utez.judicial.oficialia.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.JdbcUserDao;
import mx.edu.utez.judicial.oficialia.dao.TestDatabase;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.service.PasswordHasher;
import org.junit.jupiter.api.Test;

class InitialAccountsTest {

    @Test
    void acceptsOnlyAValidEmailAndAPasswordWithLettersAndNumbers() {
        assertTrue(InitialAccounts.problemWith("admin@utez.edu.mx", "Segura2026").isEmpty());
        assertTrue(InitialAccounts.problemWith("admin", "Segura2026").isPresent());
        assertTrue(InitialAccounts.problemWith(null, "Segura2026").isPresent());
        assertTrue(InitialAccounts.problemWith("admin@utez.edu.mx", "Corta1").isPresent());
        assertTrue(InitialAccounts.problemWith("admin@utez.edu.mx", "SoloLetras").isPresent());
        assertTrue(InitialAccounts.problemWith("admin@utez.edu.mx", "12345678").isPresent());
    }

    // The tests below run only against the test database described in TestDatabase

    @Test
    void createsTheAdministratorFromTheEnvironmentOnlyOnce() throws Exception {
        JdbcUserDao users = new JdbcUserDao(TestDatabase.migrated());
        Map<String, String> environment = Map.of("OFICIALIA_ADMIN_EMAIL", "Admin@Utez.edu.mx",
            "OFICIALIA_ADMIN_PASSWORD", "Segura2026");

        new InitialAccounts(users, environment::get).createIfEmpty();
        new InitialAccounts(users, Map.of("OFICIALIA_ADMIN_EMAIL", "otro@utez.edu.mx",
            "OFICIALIA_ADMIN_PASSWORD", "Distinta2026")::get).createIfEmpty();

        assertEquals(1, users.count());
        UserAccount admin = users.findByEmail("admin@utez.edu.mx").orElseThrow();
        assertEquals(UserRole.JUDICIAL_BODY_ADMIN, admin.user().getRole());
        assertEquals("Administrador del órgano judicial", admin.user().getFullName());
        assertTrue(PasswordHasher.matches("Segura2026", admin.passwordHash()));
    }

    @Test
    void createsNobodyWithAWeakPasswordOrWithoutVariables() throws Exception {
        JdbcUserDao users = new JdbcUserDao(TestDatabase.migrated());

        new InitialAccounts(users, Map.of("OFICIALIA_ADMIN_EMAIL", "admin@utez.edu.mx",
            "OFICIALIA_ADMIN_PASSWORD", "corta")::get).createIfEmpty();
        new InitialAccounts(users, Map.<String, String>of()::get).createIfEmpty();

        assertEquals(0, users.count());
    }

    @Test
    void loadsTheDemoAccountsOnlyWhenAskedTo() throws Exception {
        JdbcUserDao users = new JdbcUserDao(TestDatabase.migrated());

        new InitialAccounts(users, Map.of("OFICIALIA_SAMPLE_USERS", "true")::get).createIfEmpty();

        assertEquals(4, users.count());
        assertEquals("Juzgado de Cuautla", users.findByEmail("lmendez@utez.edu.mx").orElseThrow().user().getCourtName());
        assertTrue(!users.findByEmail("inactivo@utez.edu.mx").orElseThrow().active());
    }
}
