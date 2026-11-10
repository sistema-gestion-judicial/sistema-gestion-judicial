package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import mx.edu.utez.judicial.oficialia.client.HearingDeskClient;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.RulingForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class RulingServiceTest {

    private static final String CUAUTLA = "Juzgado de Cuautla";
    private static final Clock NOV_6 = Clock.fixed(Instant.parse("2026-11-06T17:00:00Z"), ZoneId.of("UTC"));
    private static final User CLERK = new User("lmendez@utez.edu.mx", "Laura Méndez", UserRole.CLERK, CUAUTLA);

    private final InMemoryDemandRequestDao dao = new InMemoryDemandRequestDao(List.of(
            pending("S-000002/2026"), pending("S-000003/2026"), admitted("S-000001/2026", "2026/00431/12")));

    @Test
    void admissibleGetsTheNextCaseFileNumberAndADesk() {
        RulingService service = new RulingService(dao, (caseFile, matter) -> "Mesa 2", NOV_6);

        DemandRequest ruled = service.rule("S-000002/2026", CLERK, form("ADMISSIBLE", ""));

        assertEquals(RequestStatus.ADMISSIBLE, ruled.getStatus());
        assertEquals("2026/00432/12", ruled.getCaseFileNumber());
        assertEquals("Mesa 2", ruled.getDeskName());
        assertEquals("Laura Méndez", ruled.getRuledBy());
    }

    @Test
    void numbersKeepGrowingWithinTheCourtAndYear() {
        RulingService service = new RulingService(dao, (caseFile, matter) -> "Mesa 1", NOV_6);

        service.rule("S-000002/2026", CLERK, form("ADMISSIBLE", ""));
        DemandRequest second = service.rule("S-000003/2026", CLERK, form("ADMISSIBLE", ""));

        assertEquals("2026/00433/12", second.getCaseFileNumber());
    }

    @Test
    void deskStaysPendingWhenTheHearingsSystemDoesNotAnswer() {
        HearingDeskClient offline = (caseFile, matter) -> {
            throw new ServiceUnavailableException("offline");
        };
        DemandRequest ruled = new RulingService(dao, offline, NOV_6)
                .rule("S-000002/2026", CLERK, form("ADMISSIBLE", ""));

        assertEquals("2026/00432/12", ruled.getCaseFileNumber());
        assertNull(ruled.getDeskName());
        assertTrue(ruled.isDeskPending());
    }

    @Test
    void inadmissibleKeepsTheReasonAndNoCaseFile() {
        DemandRequest ruled = new RulingService(dao, (caseFile, matter) -> "Mesa 1", NOV_6)
                .rule("S-000002/2026", CLERK, form("INADMISSIBLE", "Falta la firma autógrafa del promovente."));

        assertEquals(RequestStatus.INADMISSIBLE, ruled.getStatus());
        assertEquals("Falta la firma autógrafa del promovente.", ruled.getRejectionReason());
        assertNull(ruled.getCaseFileNumber());
    }

    @Test
    void aRuledRequestCannotChange() {
        RulingService service = new RulingService(dao, (caseFile, matter) -> "Mesa 1", NOV_6);

        assertThrows(BusinessRuleException.class,
                () -> service.rule("S-000001/2026", CLERK, form("INADMISSIBLE", "Cambio de opinión del oficial.")));
    }

    @Test
    void onlyTheClerkRules() {
        User admin = new User("admin.cuautla@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, CUAUTLA);
        RulingService service = new RulingService(dao, (caseFile, matter) -> "Mesa 1", NOV_6);

        assertThrows(AccessDeniedException.class, () -> service.getRulable("S-000002/2026", admin));
    }

    @Test
    void inadmissibleNeedsAReason() {
        assertTrue(form("INADMISSIBLE", "corto").validate().containsKey("reason"));
        assertTrue(form("", "").validate().containsKey("decision"));
        assertTrue(form("ADMISSIBLE", "").validate().isEmpty());
    }

    private static RulingForm form(String decision, String reason) {
        RulingForm form = new RulingForm();
        form.setDecision(decision);
        form.setReason(reason);
        return form;
    }

    private static DemandRequest pending(String folio) {
        return new DemandRequest(folio, LocalDate.parse("2026-11-05"), CUAUTLA, "12", "Civil",
                new Party(PartyRole.PLAINTIFF, "Ana Torres", "Persona física", "TOAA900512KL3", null, null),
                Party.unidentified(PartyRole.DEFENDANT), null, "Laura Méndez", List.of(), null);
    }

    private static DemandRequest admitted(String folio, String caseFile) {
        DemandRequest request = pending(folio);
        request.recordAdmissible(caseFile, "Mesa 3", "Laura Méndez", LocalDate.parse("2026-11-01").atStartOfDay());
        return request;
    }
}
