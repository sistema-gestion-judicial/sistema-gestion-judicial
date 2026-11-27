package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.CaseFileState;
import mx.edu.utez.judicial.oficialia.model.CaseFileStatus;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcCaseFileDaoTest {

    private static final String NUMBER = "2026/00431/12";
    private static final LocalDateTime AT = LocalDateTime.parse("2026-11-20T10:00");

    private Database database;
    private JdbcCaseFileDao dao;
    private User clerk;

    @BeforeEach
    void freshSchema() throws Exception {
        database = TestDatabase.migrated();
        JdbcDemandRequestDao requests = new JdbcDemandRequestDao(database);
        InMemoryDemandRequestDao.sampleRequests().forEach(requests::insert);
        JdbcUserDao users = new JdbcUserDao(database);
        users.insert(new NewUserAccount("Laura", "Méndez", "lmendez@utez.edu.mx", UserRole.CLERK, "12",
            "600000:c2FsdA==:aGFzaA==", false, true));
        clerk = users.findByEmail("lmendez@utez.edu.mx").orElseThrow().user();
        dao = new JdbcCaseFileDao(database);
    }

    @Test
    void readsTheSampleCaseFilesAsActiveInFirstInstance() {
        assertEquals(4, dao.findStates().size());
        CaseFileState state = dao.findState(NUMBER).orElseThrow();
        assertEquals(CaseFileStatus.ACTIVE, state.status());
        assertNull(state.firstInstanceResolvedAt());
        assertTrue(dao.findState("2026/99999/12").isEmpty());
    }

    @Test
    void recordsTheResolutionAndTheConclusionKeepingTheFirstDate() {
        assertTrue(dao.recordFirstInstanceResolution(NUMBER, AT));
        assertTrue(dao.recordFirstInstanceResolution(NUMBER, AT.plusDays(3)));
        assertTrue(dao.recordConclusion(NUMBER, AT.plusDays(5)));

        CaseFileState state = dao.findState(NUMBER).orElseThrow();
        assertEquals(AT, state.firstInstanceResolvedAt());
        assertEquals(AT.plusDays(5), state.concludedAt());
        assertEquals(CaseFileStatus.CONCLUDED, state.status());
        assertFalse(dao.recordConclusion("2026/99999/12", AT));
    }

    @Test
    void movesToSecondInstanceWithAChamberAndWritesTheAuditLog() throws Exception {
        dao.moveToSecondInstance(NUMBER, "SCU", "Sala Regional de Cuautla", clerk, AT);

        CaseFileState state = dao.findState(NUMBER).orElseThrow();
        assertEquals(CaseFileStatus.SECOND_INSTANCE, state.status());
        assertEquals("SCU", state.chamberCode());
        assertEquals(AT, state.secondInstanceAt());
        assertEquals(1, auditCount());
        assertThrows(IllegalStateException.class,
            () -> dao.moveToSecondInstance(NUMBER, "SC1", "Primera Sala Civil", clerk, AT));
    }

    @Test
    void aPendingChamberCanBeFilledInLaterKeepingTheDateOfTheMove() throws Exception {
        dao.moveToSecondInstance(NUMBER, null, null, clerk, AT);
        assertNull(dao.findState(NUMBER).orElseThrow().chamberCode());

        dao.moveToSecondInstance(NUMBER, "SCU", "Sala Regional de Cuautla", clerk, AT.plusDays(1));

        CaseFileState state = dao.findState(NUMBER).orElseThrow();
        assertEquals("SCU", state.chamberCode());
        assertEquals(AT, state.secondInstanceAt());
        assertEquals(2, auditCount());
    }

    private int auditCount() throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                 "SELECT COUNT(*) FROM audit_log WHERE action = 'CASE_FILE_SECOND_INSTANCE'")) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
