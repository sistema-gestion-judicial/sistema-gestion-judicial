package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import org.junit.jupiter.api.Test;

class DuplicityServiceTest {

    private final DuplicityService service = new DuplicityService(List.of(
            new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "TOSA850312ML4",
                    "PEGL800101HD1")));

    @Test
    void findsMatchWhenPlaintiffAndMatterAreTheSame() {
        Optional<DuplicityMatch> match = service.findMatch(form("Familiar", "TOSA850312ML4", "TOML820704HX9"));

        assertTrue(match.isPresent());
        assertEquals(PartyRole.PLAINTIFF, match.get().getMatchedRole());
        assertEquals("Juzgado de Jojutla", match.get().getCourtName());
    }

    @Test
    void anAdmissibleRequestBecomesAnActiveLawsuit() {
        DemandRequest request = new DemandRequest("S-000001/2026", LocalDate.parse("2026-11-05"),
                "Juzgado de Cuautla", "12", "Familiar",
                new Party(PartyRole.PLAINTIFF, "Ana Torres", "Persona física", "TOAA900512KL3", null, null),
                Party.unidentified(PartyRole.DEFENDANT), null, "Laura Méndez", List.of(), null);
        InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(List.of(request));
        DuplicityService fromRequests = new DuplicityService(dao);
        DuplicityCheckForm sameParty = form("Familiar", "TOAA900512KL3", "TOML820704HX9");

        assertTrue(fromRequests.findMatch(sameParty).isEmpty());

        request.recordAdmissible("2026/00432/12", "Mesa 2", "Laura Méndez", LocalDateTime.now());

        assertEquals("Juzgado de Cuautla", fromRequests.findMatch(sameParty).get().getCourtName());
    }

    @Test
    void ignoresMatchInAnotherMatter() {
        assertTrue(service.findMatch(form("Civil", "TOSA850312ML4", "TOML820704HX9")).isEmpty());
    }

    @Test
    void ignoresCounterclaimWithRolesSwapped() {
        // The plaintiff of the active lawsuit appears here as the defendant: not a duplicate
        assertTrue(service.findMatch(form("Familiar", "PEGL800101HD1", "TOSA850312ML4")).isEmpty());
    }

    private static DuplicityCheckForm form(String matter, String plaintiffId, String defendantId) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(matter);
        form.setPlaintiffName("Plaintiff");
        form.setPlaintiffId(plaintiffId);
        form.setDefendantName("Defendant");
        form.setDefendantId(defendantId);
        return form;
    }
}
