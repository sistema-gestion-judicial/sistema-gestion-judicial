package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcDemandRequestDaoTest {

    private Database database;
    private JdbcDemandRequestDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        database = TestDatabase.migrated();
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

    @Test
    void keepsTheCaseFilesTheWarningDecisionsAndTheStatusLogWithEachSave() throws Exception {
        List<DemandRequest> samples = InMemoryDemandRequestDao.sampleRequests();
        long caseFiles = samples.stream().filter(request -> request.getCaseFileNumber() != null).count();
        long ruled = samples.stream().filter(request -> request.getStatus() != RequestStatus.PENDING).count();
        assertEquals(caseFiles, count("SELECT COUNT(*) FROM case_files"));
        assertEquals(samples.stream().filter(request -> request.getDuplicityWarning() != null).count(),
            count("SELECT COUNT(*) FROM duplicity_warning_decisions WHERE decision = 'CONTINUE'"));
        assertEquals(samples.size() + ruled, count("SELECT COUNT(*) FROM request_status_history"));

        DemandRequest pending = dao.findByFolio("S-000127/2026").orElseThrow();
        pending.recordAdmissible("2026/00432/12", null, "Laura Méndez", LocalDateTime.parse("2026-11-08T12:30"));
        dao.update(pending);
        dao.update(pending);

        assertEquals(1, count("SELECT COUNT(*) FROM case_files WHERE number = '2026/00432/12' "
            + "AND year = 2026 AND sequence = 432 AND desk_name IS NULL AND folio = 'S-000127/2026'"));
        assertEquals(1, count("SELECT COUNT(*) FROM request_status_history WHERE folio = 'S-000127/2026' "
            + "AND from_status = 'PENDING' AND to_status = 'ADMISSIBLE' AND changed_by = 'Laura Méndez'"));
        assertEquals(caseFiles + 1, count("SELECT COUNT(*) FROM case_files"));
        assertEquals(samples.size() + ruled + 1, count("SELECT COUNT(*) FROM request_status_history"));
    }

    private int count(String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
