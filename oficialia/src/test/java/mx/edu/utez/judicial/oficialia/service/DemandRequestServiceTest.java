package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import org.junit.jupiter.api.Test;

class DemandRequestServiceTest {

    private static final String CUAUTLA = "Juzgado de Cuautla";

    private final DemandRequestService service = new DemandRequestService(new InMemoryDemandRequestDao(List.of(
            request("S-000002/2026", CUAUTLA, "Ana Torres"),
            request("S-000001/2026", CUAUTLA, "Rosa León"),
            request("S-000009/2026", "Juzgado de Jojutla", "Teresa Ortiz"))));

    @Test
    void listsOnlyTheRequestsOfTheUsersCourt() {
        List<DemandRequest> result = service.search(CUAUTLA, "", null);

        assertEquals(List.of("S-000002/2026", "S-000001/2026"),
                result.stream().map(DemandRequest::getFolio).toList());
        assertEquals(2, service.countByCourt(CUAUTLA));
    }

    @Test
    void filtersByPartyNameAndStatus() {
        assertEquals(1, service.search(CUAUTLA, "rosa", RequestStatus.PENDING).size());
        assertTrue(service.search(CUAUTLA, "rosa", RequestStatus.ADMISSIBLE).isEmpty());
    }

    @Test
    void deniesTheDetailOfAnotherCourt() {
        assertThrows(AccessDeniedException.class, () -> service.getForCourt("S-000009/2026", CUAUTLA));
    }

    @Test
    void reportsAnUnknownFolio() {
        assertThrows(NotFoundException.class, () -> service.getForCourt("S-999999/2026", CUAUTLA));
    }

    private static DemandRequest request(String folio, String court, String plaintiff) {
        return new DemandRequest(folio, LocalDate.parse("2026-10-20"), court, "12", "Civil",
                new Party(PartyRole.PLAINTIFF, plaintiff, "Persona física", "TOAA900512KL3", null, null),
                Party.unidentified(PartyRole.DEFENDANT), null, "Laura Méndez", List.of(), null);
    }
}
