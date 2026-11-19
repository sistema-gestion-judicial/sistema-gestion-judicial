package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.listener.DatabaseStartupListener;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import org.junit.jupiter.api.Test;

/**
 * Upgrades a database the way production will be upgraded: built by V001 and V002, already holding the demo
 * requests, and then brought up to date with the newer scripts. Runs only against {@link TestDatabase}.
 */
class MigrationUpgradeTest {

    @Test
    void upgradesADatabaseThatAlreadyHasRequestsWithoutLosingThem() throws Exception {
        Database database = TestDatabase.empty();
        TestDatabase.migrate(database, List.of("V001__create_catalogs.sql", "V002__create_demand_requests.sql"));
        InMemoryDemandRequestDao.sampleRequests().forEach(request -> LegacyRequests.insert(database, request));
        JdbcDemandRequestDao requests = new JdbcDemandRequestDao(database);

        TestDatabase.migrate(database, DatabaseStartupListener.MIGRATIONS);

        assertEquals(10, requests.count());
        assertEquals("2026/00431/12", requests.findByFolio("S-000124/2026").orElseThrow().getCaseFileNumber());
        Court cuautla = new JdbcCatalogDao(database).findCourt("12").orElseThrow();
        assertEquals("CUA", cuautla.getDistrictCode());
        assertEquals("Distrito judicial de Cuautla", cuautla.getJudicialDistrict());
        assertEquals(12, cuautla.getNumber());
        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), cuautla.getSubjectMatterCodes());
        assertEquals(DatabaseStartupListener.MIGRATIONS.size(), count(database, "SELECT COUNT(*) FROM schema_history"));
        assertEquals(0, count(database, "SELECT COUNT(*) FROM users"));
    }

    @Test
    void fillsTheCaseFilesTheWarningDecisionsAndTheStatusLogFromTheStoredRequests() throws Exception {
        Database database = TestDatabase.empty();
        TestDatabase.migrate(database, DatabaseStartupListener.MIGRATIONS.subList(0, 5));
        List<DemandRequest> samples = InMemoryDemandRequestDao.sampleRequests();
        samples.forEach(request -> LegacyRequests.insert(database, request));

        TestDatabase.migrate(database, DatabaseStartupListener.MIGRATIONS);

        long ruled = samples.stream().filter(request -> request.getStatus() != RequestStatus.PENDING).count();
        assertEquals(3, count(database, "SELECT COUNT(*) FROM roles"));
        assertEquals(samples.stream().filter(request -> request.getCaseFileNumber() != null).count(),
            count(database, "SELECT COUNT(*) FROM case_files"));
        assertEquals("2026|431|Mesa 2|FIRST", text(database, "SELECT CONCAT_WS('|', year, sequence, desk_name, "
            + "instance) FROM case_files WHERE number = '2026/00431/12'"));
        assertEquals(samples.stream().filter(request -> request.getDuplicityWarning() != null).count(),
            count(database, "SELECT COUNT(*) FROM duplicity_warning_decisions WHERE decision = 'CONTINUE'"));
        assertEquals(samples.size() + ruled, count(database, "SELECT COUNT(*) FROM request_status_history"));
        assertEquals("La demanda no trae la firma autógrafa de la promovente.", text(database,
            "SELECT reason FROM request_status_history WHERE folio = 'S-000123/2026' AND to_status = 'INADMISSIBLE'"));
        assertEquals(0, count(database, "SELECT COUNT(*) FROM second_instance_transfers"));
        // V007: the next case file of a court continues after the ones it already had
        assertEquals(431, count(database,
            "SELECT last_sequence FROM case_file_sequences WHERE court_code = '12' AND year = 2026"));
        assertEquals(0, count(database, "SELECT COUNT(*) FROM audit_log"));
    }

    @Test
    void refusesAChamberForACaseFileStillInFirstInstanceAndACancelledWarningWithARequest() throws Exception {
        Database database = TestDatabase.migrated();
        new JdbcDemandRequestDao(database).insert(InMemoryDemandRequestDao.sampleRequests().get(3));

        assertThrows(SQLException.class, () -> execute(database,
            "UPDATE case_files SET chamber_code = 'SALA-1' WHERE number = '2026/00431/12'"));
        execute(database, "UPDATE case_files SET instance = 'SECOND', chamber_code = 'SALA-1' "
            + "WHERE number = '2026/00431/12'");
        assertThrows(SQLException.class, () -> execute(database, "INSERT INTO duplicity_warning_decisions "
            + "(folio, court_code, matched_court, matched_matter, decision, decided_by) "
            + "VALUES ('S-000124/2026', '12', 'Juzgado de Yautepec', 'Penal', 'CANCEL', 'Laura Méndez')"));
        execute(database, "INSERT INTO duplicity_warning_decisions "
            + "(court_code, matched_court, matched_matter, decision, decided_by) "
            + "VALUES ('12', 'Juzgado de Yautepec', 'Penal', 'CANCEL', 'Laura Méndez')");
        assertThrows(SQLException.class, () -> execute(database, "INSERT INTO users (first_name, last_name, "
            + "email, role, court_code, password_hash) VALUES ('A', 'B', 'x@utez.edu.mx', 'JUDGE', '12', 'h')"));
    }

    private static void execute(Database database, String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static String text(Database database, String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getString(1);
        }
    }

    private static int count(Database database, String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
