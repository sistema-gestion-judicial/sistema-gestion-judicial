package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcDemandRequestDaoTest {

    private JdbcDemandRequestDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        dao = new JdbcDemandRequestDao(TestDatabase.migrated());
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
