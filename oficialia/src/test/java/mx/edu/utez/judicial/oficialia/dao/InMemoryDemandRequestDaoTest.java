package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.oficialia.model.CancelledAttempt;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import org.junit.jupiter.api.Test;

class InMemoryDemandRequestDaoTest {

    @Test
    void findsTheLastFolioNumberOfEachYear() {
        InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());

        assertEquals(127, dao.lastFolioNumber(2026));
        assertEquals(0, dao.lastFolioNumber(2027));
        assertEquals(0, new InMemoryDemandRequestDao(List.of()).lastFolioNumber(2026));
    }

    @Test
    void refusesAFolioThatIsAlreadyTaken() {
        InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());
        DemandRequest taken = dao.findByFolio("S-000127/2026").orElseThrow();

        assertThrows(DuplicateCodeException.class, () -> dao.insert(taken));
        assertEquals(10, dao.findAll().size());
    }

    @Test
    void keepsTheCancelledAttempts() {
        InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(List.of());
        CancelledAttempt attempt = new CancelledAttempt("12", "Juzgado de Jojutla", "Familiar", false,
            "Laura Méndez", LocalDateTime.parse("2026-11-17T12:30"));

        dao.recordCancelledAttempt(attempt);

        assertEquals(List.of(attempt), dao.findCancelledAttempts());
        assertEquals(0, dao.findAll().size());
    }
}
