package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import org.junit.jupiter.api.Test;

class CaseFileServiceTest {

    private final CaseFileService service =
        new CaseFileService(new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests()));

    @Test
    void listsOnlyAdmissibleRequestsNewestFirst() {
        List<String> numbers = service.search(null, null, null).stream()
            .map(DemandRequest::getCaseFileNumber).toList();

        assertEquals(List.of("2026/00431/12", "2026/00428/12", "2026/00425/12", "2026/00419/12"), numbers);
    }

    @Test
    void filtersByMatterAndAdmissionDate() {
        assertEquals(1, service.search("12", "civil", LocalDate.parse("2026-10-21")).size());
        assertTrue(service.search("07", null, null).isEmpty());
    }

    @Test
    void findsACaseFileByItsNumber() {
        assertEquals("S-000124/2026", service.findByNumber("2026/00431/12").orElseThrow().getFolio());
        assertTrue(service.findByNumber("2026/99999/12").isEmpty());
    }
}
