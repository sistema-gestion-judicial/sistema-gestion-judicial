package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class DemandRequestCorrectionServiceTest {

    private static final String FOLIO = "S-000127/2026";

    private final User clerk = new User("l@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");
    private final InMemoryDemandRequestDao requests =
        new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());
    // Someone with this RFC already has an active family lawsuit in Jojutla
    private final DuplicityService duplicity = new DuplicityService(List.of(
        new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "ROGA800101AB1", null)));
    private final DemandRequestCorrectionService service = new DemandRequestCorrectionService(requests,
        new InMemoryCatalogDao(), duplicity, () -> LocalDateTime.parse("2026-11-20T10:00"));

    /** The captured data of the sample request, with the plaintiff's name typed again. */
    private static DuplicityCheckForm form(String matter, String plaintiffId) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(matter);
        form.setPlaintiffType("INDIVIDUAL");
        form.setPlaintiffName("Ana Torres Aguilar");
        form.setPlaintiffId(plaintiffId);
        form.setDefendantType("INDIVIDUAL");
        form.setDefendantName("Luis Torres López");
        form.setDefendantId("TOLL880203H45");
        return form;
    }

    @Test
    void correctsThePartiesMatterAndRepresentativeKeepingFolioCourtDateAndAnnexes() {
        DemandRequest before = requests.findByFolio(FOLIO).orElseThrow();

        DemandRequestCorrectionService.Correction correction =
            service.correct(clerk, FOLIO, form("Civil", "TOAA900512KL3"), "Lic. Rosa Campos", false);

        assertTrue(correction.isSaved());
        DemandRequest after = requests.findByFolio(FOLIO).orElseThrow();
        assertEquals("Civil", after.getMatter());
        assertEquals("Lic. Rosa Campos", after.getLegalRepresentative());
        assertEquals(before.getReceivedDate(), after.getReceivedDate());
        assertEquals(before.getCourtName(), after.getCourtName());
        assertEquals(before.getAnnexes().size(), after.getAnnexes().size());
        // Same person, so the phone captured before is kept
        assertEquals(before.getPlaintiff().getPhone(), after.getPlaintiff().getPhone());
    }

    @Test
    void checksDuplicityAgainWhenAPartyChangesAndSavesOnlyAfterTheClerkContinues() {
        DemandRequestCorrectionService.Correction warned =
            service.correct(clerk, FOLIO, form("Familiar", "ROGA800101AB1"), null, false);

        assertNotNull(warned.warning());
        assertFalse(warned.isSaved());
        assertEquals("TOAA900512KL3", requests.findByFolio(FOLIO).orElseThrow().getPlaintiff().getPersonId());

        assertTrue(service.correct(clerk, FOLIO, form("Familiar", "ROGA800101AB1"), null, true).isSaved());
        DemandRequest saved = requests.findByFolio(FOLIO).orElseThrow();
        assertEquals("ROGA800101AB1", saved.getPlaintiff().getPersonId());
        assertEquals("Juzgado de Jojutla", saved.getDuplicityWarning().getCourtName());
    }

    @Test
    void refusesARequestAlreadyRuled() {
        DemandRequest ruled = requests.findAll().stream().filter(request -> !request.isPending()
            && request.getCourtName().equals("Juzgado de Cuautla")).findFirst().orElseThrow();

        BusinessRuleException refused = assertThrows(BusinessRuleException.class,
            () -> service.getForCorrection(clerk, ruled.getFolio()));
        assertTrue(refused.getMessage().contains("ya fue calificada"));
    }

    @Test
    void validatesTheFieldsAndSavesNothing() {
        DemandRequestCorrectionService.Correction rejected =
            service.correct(clerk, FOLIO, form("Familiar", "NO-ES-RFC"), "x".repeat(151), false);

        assertTrue(rejected.errors().containsKey("plaintiffId"));
        assertTrue(rejected.errors().containsKey("legalRepresentative"));
        assertEquals("TOAA900512KL3", requests.findByFolio(FOLIO).orElseThrow().getPlaintiff().getPersonId());
    }

    @Test
    void onlyTheClerkOfTheCourtCorrects() {
        User otherClerk = new User("r@utez.edu.mx", "Rosa Vega", UserRole.CLERK, "Juzgado de Jojutla");
        User courtAdmin = new User("a@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, "Juzgado de Cuautla");

        assertThrows(NotFoundException.class, () -> service.getForCorrection(otherClerk, FOLIO));
        assertThrows(AccessDeniedException.class, () -> service.getForCorrection(courtAdmin, FOLIO));
    }
}
