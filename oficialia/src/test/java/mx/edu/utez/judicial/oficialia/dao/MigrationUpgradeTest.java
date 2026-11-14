package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.listener.DatabaseStartupListener;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import org.junit.jupiter.api.Test;

/**
 * Upgrades a database the way production is upgraded: built by V001 and V002, holding the demo requests
 * written with the columns those scripts had, and then brought up to date with every newer script.
 * Runs only against {@link TestDatabase}.
 */
class MigrationUpgradeTest {

    @Test
    void upgradesADatabaseThatAlreadyHasRequestsWithoutLosingThem() throws Exception {
        Database database = TestDatabase.empty();
        TestDatabase.migrate(database, List.of("V001__create_catalogs.sql", "V002__create_demand_requests.sql"));
        try (Connection connection = database.getConnection()) {
            for (DemandRequest request : InMemoryDemandRequestDao.sampleRequests()) {
                insertAsV002(connection, request);
            }
        }

        TestDatabase.migrate(database, DatabaseStartupListener.MIGRATIONS);

        JdbcDemandRequestDao requests = new JdbcDemandRequestDao(database);
        assertEquals(10, requests.count());
        DemandRequest penal = requests.findByFolio("S-000124/2026").orElseThrow();
        assertEquals("2026/00431/12", penal.getCaseFileNumber());
        assertEquals("Mesa 2", penal.getDeskName());
        assertEquals("Penal", penal.getMatter());
        Court cuautla = new JdbcCatalogDao(database).findCourt("12").orElseThrow();
        assertEquals("CUA", cuautla.getDistrictCode());
        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), cuautla.getSubjectMatterCodes());
        assertEquals(DatabaseStartupListener.MIGRATIONS.size(), count(database, "SELECT COUNT(*) FROM schema_history"));
        assertEquals(0, count(database, "SELECT COUNT(*) FROM users"));
        assertEquals(0, count(database, "SELECT COUNT(*) FROM demand_requests WHERE matter_id IS NULL"));
        assertEquals(4, count(database, "SELECT COUNT(*) FROM case_files"));
        assertEquals(431, count(database,
            "SELECT last_sequence FROM case_file_sequences WHERE court_code = '12' AND year = 2026"));
        long withWarning = InMemoryDemandRequestDao.sampleRequests().stream()
            .filter(request -> request.getDuplicityWarning() != null).count();
        assertEquals(withWarning, count(database, "SELECT COUNT(*) FROM demand_requests WHERE has_duplicity_warning"));
    }

    private static int count(Database database, String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    /** The same rows the application wrote before V003: only the columns of V002. */
    private static void insertAsV002(Connection connection, DemandRequest request) throws Exception {
        DuplicityWarning warning = request.getDuplicityWarning();
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO demand_requests (folio, "
            + "received_date, court_code, matter, legal_representative, captured_by, duplicity_court, "
            + "duplicity_matter, duplicity_decision, status, case_file_number, desk_name, rejection_reason, "
            + "ruled_by, ruled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, request.getFolio());
            statement.setDate(2, Date.valueOf(request.getReceivedDate()));
            statement.setString(3, request.getCourtCode());
            statement.setString(4, request.getMatter());
            statement.setString(5, request.getLegalRepresentative());
            statement.setString(6, request.getCapturedBy());
            statement.setString(7, warning == null ? null : warning.getCourtName());
            statement.setString(8, warning == null ? null : warning.getMatter());
            statement.setString(9, warning == null ? null : warning.getDecision());
            statement.setString(10, request.getStatus().name());
            statement.setString(11, request.getCaseFileNumber());
            statement.setString(12, request.getDeskName());
            statement.setString(13, request.getRejectionReason());
            statement.setString(14, request.getRuledBy());
            statement.setTimestamp(15, request.getRuledAt() == null ? null : Timestamp.valueOf(request.getRuledAt()));
            statement.executeUpdate();
        }
        for (Party party : List.of(request.getPlaintiff(), request.getDefendant())) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO parties (folio, role, name, "
                + "person_type, person_id, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, request.getFolio());
                statement.setString(2, party.getRole().name());
                statement.setString(3, party.getName());
                statement.setString(4, party.getPersonType());
                statement.setString(5, party.getPersonId());
                statement.setString(6, party.getPhone());
                statement.setString(7, party.getEmail());
                statement.executeUpdate();
            }
        }
        for (Annex annex : request.getAnnexes()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO annexes (folio, file_name, size_kb) VALUES (?, ?, ?)")) {
                statement.setString(1, request.getFolio());
                statement.setString(2, annex.getFileName());
                statement.setInt(3, annex.getSizeKb());
                statement.executeUpdate();
            }
        }
    }
}
