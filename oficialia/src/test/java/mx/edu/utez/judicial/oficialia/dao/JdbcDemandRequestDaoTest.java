package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.DatabaseConfig;
import mx.edu.utez.judicial.common.db.SqlMigrator;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only when OFICIALIA_TEST_DB_URL (plus _USER and _PASSWORD) points to an empty test database,
 * for example the local Docker MySQL. It drops and recreates the tables.
 */
class JdbcDemandRequestDaoTest {

    private JdbcDemandRequestDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        DatabaseConfig config = DatabaseConfig.fromEnvironment("OFICIALIA_TEST").orElse(null);
        assumeTrue(config != null, "No test database configured");
        Database database = new Database(config);
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            for (String table : List.of("annexes", "parties", "demand_requests", "subject_matters", "courts",
                "schema_history")) {
                statement.execute("DROP TABLE IF EXISTS " + table);
            }
        }
        new SqlMigrator(database, getClass().getClassLoader()).migrate("db/migration", List.of(
            "V001__create_catalogs.sql", "V002__create_demand_requests.sql"));
        dao = new JdbcDemandRequestDao(database);
        InMemoryDemandRequestDao.sampleRequests().forEach(dao::insert);
    }

    @Test
    void readsBackWhatWasSavedWithPartiesAnnexesAndRuling() {
        DemandRequest penal = dao.findByFolio("S-000124/2026").orElseThrow();

        assertEquals("Juzgado de Cuautla", penal.getCourtName());
        assertEquals("Juan Ruiz Hernández", penal.getPlaintiff().getName());
        assertTrue(!penal.getDefendant().isIdentified());
        assertEquals(RequestStatus.ADMISSIBLE, penal.getStatus());
        assertEquals("2026/00431/12", penal.getCaseFileNumber());
        assertEquals(1, penal.getAnnexes().size());
        assertEquals("Juzgado de Cuernavaca",
            dao.findByFolio("S-000125/2026").orElseThrow().getDuplicityWarning().getCourtName());
    }

    @Test
    void filtersByCourtAndSavesARuling() {
        assertEquals(9, dao.findByCourt("Juzgado de Cuautla").size());
        assertEquals(10, dao.count());

        DemandRequest pending = dao.findByFolio("S-000127/2026").orElseThrow();
        pending.recordAdmissible("2026/00432/12", null, "Laura Méndez", LocalDateTime.parse("2026-11-08T12:30"));
        dao.update(pending);

        DemandRequest saved = dao.findByFolio("S-000127/2026").orElseThrow();
        assertEquals("2026/00432/12", saved.getCaseFileNumber());
        assertTrue(saved.isDeskPending());
    }
}
