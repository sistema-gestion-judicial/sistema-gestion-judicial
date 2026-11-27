package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.model.UserUpdate;
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

    private static final User ADMIN = new User("jorge@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN,
        "Juzgado de Cuautla");

    private long insertAdminAndClerk() {
        dao.insert(new NewUserAccount("Jorge", "Salas", ADMIN.getEmail(), UserRole.COURT_ADMIN, "12", HASH, false,
            true));
        dao.insert(new NewUserAccount("Ana", "Ruiz", "ana@utez.edu.mx", "7771234567", UserRole.CLERK, "12", HASH,
            true, true));
        return dao.findByCourt("Juzgado de Cuautla").stream().filter(user -> user.getEmail().equals("ana@utez.edu.mx"))
            .findFirst().orElseThrow().getId();
    }

    @Test
    void findsAUserByIdWithTheNameOfTheirCourt() {
        long id = insertAdminAndClerk();

        UserDetails ana = dao.findById(id).orElseThrow();

        assertEquals("Ana", ana.getFirstName());
        assertEquals("Ruiz", ana.getLastName());
        assertEquals("7771234567", ana.getPhone());
        assertEquals(UserRole.CLERK, ana.getRole());
        assertEquals("Juzgado de Cuautla", ana.getCourtName());
        assertTrue(dao.findById(id + 100).isEmpty());
    }

    @Test
    void correctsAUserKeepingThePasswordAndRecordsTheChangeInTheAuditLog() throws Exception {
        long id = insertAdminAndClerk();

        dao.update(new UserUpdate(id, "Ana María", "Ruiz", "Ana.Maria@utez.edu.mx", null, UserRole.COURT_ADMIN), ADMIN);

        UserAccount account = dao.findByEmail("ana.maria@utez.edu.mx").orElseThrow();
        assertEquals("Ana María Ruiz", account.user().getFullName());
        assertEquals(UserRole.COURT_ADMIN, account.user().getRole());
        assertEquals(HASH, account.passwordHash());
        assertTrue(account.user().isMustChangePassword());
        assertTrue(dao.findByEmail("ana@utez.edu.mx").isEmpty());
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT a.action, a.entity_type, a.entity_id, a.court_code, "
                 + "a.occurred_at, u.email, a.details FROM audit_log a JOIN users u ON u.id = a.actor_user_id")) {
            assertTrue(rows.next());
            assertEquals("USER_UPDATED", rows.getString("action"));
            assertEquals("USER", rows.getString("entity_type"));
            assertEquals(String.valueOf(id), rows.getString("entity_id"));
            assertEquals("12", rows.getString("court_code"));
            assertNotNull(rows.getTimestamp("occurred_at"));
            assertEquals(ADMIN.getEmail(), rows.getString("email"));
            String details = rows.getString("details");
            assertTrue(details.contains("ana.maria@utez.edu.mx"), details);
            assertTrue(details.contains("7771234567"), details);
            assertFalse(details.contains("lastName"), details);
            assertFalse(rows.next());
        }
    }

    @Test
    void anAdministratorCanChangeTheirOwnEmailAndStillSignTheAuditRecord() throws Exception {
        insertAdminAndClerk();
        long adminId = dao.findByCourt("Juzgado de Cuautla").stream()
            .filter(user -> user.getEmail().equals(ADMIN.getEmail())).findFirst().orElseThrow().getId();

        dao.update(new UserUpdate(adminId, "Jorge", "Salas", "jorge.salas@utez.edu.mx", null, UserRole.COURT_ADMIN),
            ADMIN);

        assertTrue(dao.findByEmail("jorge.salas@utez.edu.mx").isPresent());
        assertEquals(1, auditRows());
    }

    @Test
    void writesNothingWhenNoValueChanged() throws Exception {
        long id = insertAdminAndClerk();

        dao.update(new UserUpdate(id, "Ana", "Ruiz", "ana@utez.edu.mx", "7771234567", UserRole.CLERK), ADMIN);

        assertEquals(0, auditRows());
    }

    @Test
    void reportsTheEmailOfAnotherUserAsADuplicateAndLogsNothing() throws Exception {
        long id = insertAdminAndClerk();

        assertThrows(DuplicateEmailException.class, () -> dao.update(
            new UserUpdate(id, "Ana", "Ruiz", "JORGE@utez.edu.mx", null, UserRole.CLERK), ADMIN));
        assertEquals("ana@utez.edu.mx", dao.findById(id).orElseThrow().getEmail());
        assertEquals(0, auditRows());
    }

    @Test
    void deactivatesAUserAndRecordsWhoWhenAndWhyInTheAuditLog() throws Exception {
        long id = insertAdminAndClerk();

        assertTrue(dao.updateStatus(id, false, "Cambió de área", ADMIN));

        assertFalse(dao.findByEmail("ana@utez.edu.mx").orElseThrow().active());
        assertFalse(dao.findById(id).orElseThrow().isActive());
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT a.action, a.entity_id, a.reason, a.occurred_at, u.email "
                 + "FROM audit_log a JOIN users u ON u.id = a.actor_user_id")) {
            assertTrue(rows.next());
            assertEquals("USER_DEACTIVATED", rows.getString("action"));
            assertEquals(String.valueOf(id), rows.getString("entity_id"));
            assertEquals("Cambió de área", rows.getString("reason"));
            assertNotNull(rows.getTimestamp("occurred_at"));
            assertEquals(ADMIN.getEmail(), rows.getString("email"));
            assertFalse(rows.next());
        }
    }

    @Test
    void activatesAUserAgainAndSkipsAChangeToTheSameStatus() throws Exception {
        long id = insertAdminAndClerk();

        assertFalse(dao.updateStatus(id, true, null, ADMIN));
        assertEquals(0, auditRows());
        assertTrue(dao.updateStatus(id, false, null, ADMIN));
        assertTrue(dao.updateStatus(id, true, null, ADMIN));

        assertTrue(dao.findByEmail("ana@utez.edu.mx").orElseThrow().active());
        assertEquals(2, auditRows());
        assertFalse(dao.updateStatus(id + 100, false, null, ADMIN));
    }

    private int auditRows() throws Exception {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM audit_log")) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
