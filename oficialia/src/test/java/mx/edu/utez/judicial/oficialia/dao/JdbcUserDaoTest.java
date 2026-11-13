package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcUserDaoTest {

    private static final String HASH = "600000:c2FsdA==:aGFzaA==";

    private Database database;
    private JdbcUserDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        database = TestDatabase.migrated();
        dao = new JdbcUserDao(database);
    }

    @Test
    void readsBackAClerkWithTheNameOfTheirCourt() {
        dao.insert(new NewUserAccount("Ana", "Ruiz", "Ana.Ruiz@Utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));

        UserAccount account = dao.findByEmail("  ana.ruiz@utez.edu.mx ").orElseThrow();

        assertEquals("Ana Ruiz", account.user().getFullName());
        assertEquals("ana.ruiz@utez.edu.mx", account.user().getEmail());
        assertEquals(UserRole.CLERK, account.user().getRole());
        assertEquals("Juzgado de Cuautla", account.user().getCourtName());
        assertEquals(HASH, account.passwordHash());
        assertTrue(account.active());
        assertEquals(1, dao.count());
    }

    @Test
    void keepsTheJudicialBodyAdministratorWithoutCourt() {
        dao.insert(new NewUserAccount("Elena", "Ríos", "admin@utez.edu.mx", UserRole.JUDICIAL_BODY_ADMIN, null, HASH,
            false, true));

        // DEMO (temporary): the judicial body administrator works as the Oficial de Partes of Cuautla
        assertEquals("Juzgado de Cuautla", dao.findByEmail("admin@utez.edu.mx").orElseThrow().user().getCourtName());
        assertTrue(dao.findByEmail("nadie@utez.edu.mx").isEmpty());
    }

    @Test
    void treatsTheUsersOfAnInactiveCourtAsInactive() throws Exception {
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, false, true));
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE courts SET active = FALSE WHERE code = '12'");
        }

        assertFalse(dao.findByEmail("ana@utez.edu.mx").orElseThrow().active());
    }

    @Test
    void replacesThePasswordHashAndNoLongerAsksToChangeIt() throws Exception {
        String newHash = "600000:bmV3c2FsdA==:bmV3aGFzaA==";
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));

        dao.updatePasswordHash("  ANA@utez.edu.mx ", newHash);

        assertEquals(newHash, dao.findByEmail("ana@utez.edu.mx").orElseThrow().passwordHash());
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                 "SELECT must_change_password FROM users WHERE email = 'ana@utez.edu.mx'")) {
            assertTrue(rows.next());
            assertFalse(rows.getBoolean(1));
        }
    }

    @Test
    void updatingThePasswordOfAnUnknownEmailDoesNothing() {
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));

        dao.updatePasswordHash("nadie@utez.edu.mx", "600000:bmV3c2FsdA==:bmV3aGFzaA==");

        assertEquals(HASH, dao.findByEmail("ana@utez.edu.mx").orElseThrow().passwordHash());
        assertEquals(1, dao.count());
    }

    @Test
    void rejectsAClerkWithoutCourtAndARepeatedEmail() {
        assertThrows(DataAccessException.class, () -> dao.insert(
            new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, null, HASH, false, true)));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, false, true));
        assertThrows(DataAccessException.class, () -> dao.insert(
            new NewUserAccount("Otra", "Ana", "ANA@utez.edu.mx", UserRole.CLERK, "12", HASH, false, true)));
    }
}
