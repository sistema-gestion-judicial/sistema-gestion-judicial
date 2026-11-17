package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryDemandRequestDaoTest {

    @Test
    void findsTheLastFolioNumberOfEachYear() {
        InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());

        assertEquals(127, dao.lastFolioNumber(2026));
        assertEquals(0, dao.lastFolioNumber(2027));
        assertEquals(0, new InMemoryDemandRequestDao(List.of()).lastFolioNumber(2026));
    }
}
