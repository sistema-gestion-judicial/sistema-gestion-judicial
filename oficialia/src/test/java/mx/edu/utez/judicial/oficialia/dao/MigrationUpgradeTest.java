package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.listener.DatabaseStartupListener;
import mx.edu.utez.judicial.oficialia.model.Court;
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
        JdbcDemandRequestDao requests = new JdbcDemandRequestDao(database);
        InMemoryDemandRequestDao.sampleRequests().forEach(requests::insert);

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

    private static int count(Database database, String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
