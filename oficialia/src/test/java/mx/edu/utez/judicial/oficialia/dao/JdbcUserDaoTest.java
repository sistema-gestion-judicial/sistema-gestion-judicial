package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserProfile;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
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
        assertFalse(dao.findByEmail("ana@utez.edu.mx").orElseThrow().courtActive());
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

    @Test
    void reportsARepeatedEmailAsADuplicate() {
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, false, true));

        assertThrows(DuplicateEmailException.class, () -> dao.insert(
            new NewUserAccount("Otra", "Ana", "ANA@utez.edu.mx", UserRole.CLERK, "12", HASH, false, true)));
    }

    @Test
    void keepsThePhoneAndTheTemporaryPasswordFlagOfARegisteredClerk() {
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", "7771234567", UserRole.CLERK, "12", HASH,
            true, true));

        assertTrue(dao.findByEmail("ana@utez.edu.mx").orElseThrow().user().isMustChangePassword());
        assertEquals("7771234567", dao.findByCourt("Juzgado de Cuautla").get(0).getPhone());
    }

    @Test
    void listsOnlyTheUsersOfTheCourtByName() {
        dao.insert(new NewUserAccount("Zoe", "Mar", "zoe@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.COURT_ADMIN, "12", HASH, true, true));
        dao.insert(new NewUserAccount("Luz", "Paz", "luz@utez.edu.mx", UserRole.CLERK, "07", HASH, true, true));
        dao.insert(new NewUserAccount("Elena", "R", "admin@utez.edu.mx", UserRole.JUDICIAL_BODY_ADMIN, null, HASH,
            false, true));

        assertEquals(List.of("Ana Ruiz", "Zoe Mar"),
            dao.findByCourt("Juzgado de Cuautla").stream().map(UserSummary::getFullName).toList());
    }

    @Test
    void correctsAUserAndWritesTheChangesToTheAuditLog() throws Exception {
        dao.insert(new NewUserAccount("Jorge", "Salas", "admin@utez.edu.mx", UserRole.COURT_ADMIN, "12", HASH, false, true));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));
        User admin = dao.findByEmail("admin@utez.edu.mx").orElseThrow().user();

        dao.update("ana@utez.edu.mx", new UserProfile("Ana María", "Ruiz", "Ana.Maria@utez.edu.mx", "7771234567",
            UserRole.COURT_ADMIN, null, true), admin);

        assertTrue(dao.findProfile("ana@utez.edu.mx").isEmpty());
        UserProfile saved = dao.findProfile("ana.maria@utez.edu.mx").orElseThrow();
        assertEquals("Ana María Ruiz", saved.getFullName());
        assertEquals("7771234567", saved.getPhone());
        assertEquals(UserRole.COURT_ADMIN, saved.getRole());
        assertEquals("Juzgado de Cuautla", saved.getCourtName());
        assertEquals(HASH, dao.findByEmail("ana.maria@utez.edu.mx").orElseThrow().passwordHash());
        assertEquals(List.of("admin@utez.edu.mx|USER_UPDATED|12"), auditEntries());
    }

    @Test
    void anAdministratorCanCorrectTheirOwnEmail() throws Exception {
        dao.insert(new NewUserAccount("Jorge", "Salas", "admin@utez.edu.mx", UserRole.COURT_ADMIN, "12", HASH, false, true));
        User admin = dao.findByEmail("admin@utez.edu.mx").orElseThrow().user();

        dao.update("admin@utez.edu.mx", new UserProfile("Jorge", "Salas", "jsalas@utez.edu.mx", null,
            UserRole.COURT_ADMIN, null, true), admin);

        assertTrue(dao.findProfile("jsalas@utez.edu.mx").isPresent());
        assertEquals(List.of("jsalas@utez.edu.mx|USER_UPDATED|12"), auditEntries());
    }

    @Test
    void refusesTheEmailOfAnotherUser() {
        dao.insert(new NewUserAccount("Jorge", "Salas", "admin@utez.edu.mx", UserRole.COURT_ADMIN, "12", HASH, false, true));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));
        User admin = dao.findByEmail("admin@utez.edu.mx").orElseThrow().user();

        assertThrows(DuplicateEmailException.class, () -> dao.update("ana@utez.edu.mx",
            new UserProfile("Ana", "Ruiz", "admin@utez.edu.mx", null, UserRole.CLERK, null, true), admin));
        assertEquals("Ana Ruiz", dao.findProfile("ana@utez.edu.mx").orElseThrow().getFullName());
    }

    @Test
    void deactivatesAUserAndWritesItToTheAuditLog() throws Exception {
        dao.insert(new NewUserAccount("Jorge", "Salas", "admin@utez.edu.mx", UserRole.COURT_ADMIN, "12", HASH, false, true));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));
        User admin = dao.findByEmail("admin@utez.edu.mx").orElseThrow().user();

        dao.updateStatus("ana@utez.edu.mx", false, admin);

        assertFalse(dao.findByEmail("ana@utez.edu.mx").orElseThrow().active());
        assertEquals(List.of("admin@utez.edu.mx|USER_DEACTIVATED|12"), auditEntries());
    }

    /** author e-mail|action|court of every audit entry, oldest first. */
    private List<String> auditEntries() throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT u.email, a.action, a.court_code "
                 + "FROM audit_log a LEFT JOIN users u ON u.id = a.actor_user_id ORDER BY a.id")) {
            List<String> entries = new ArrayList<>();
            while (rows.next()) {
                entries.add(rows.getString(1) + "|" + rows.getString(2) + "|" + rows.getString(3));
            }
            return entries;
        }
    }
}
