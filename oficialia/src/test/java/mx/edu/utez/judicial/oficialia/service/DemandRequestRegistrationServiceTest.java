package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.FilingType;
import mx.edu.utez.judicial.oficialia.model.RequestDetailsForm;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class DemandRequestRegistrationServiceTest {

    private final User clerk = new User("l@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");
    private final InMemoryDemandRequestDao requests =
        new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());
    private final DemandRequestRegistrationService service = new DemandRequestRegistrationService(requests,
        new InMemoryCatalogDao(), new DuplicityService(List.of(
            new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "ROGA800101AB1", null))),
        () -> LocalDateTime.parse("2026-11-27T09:30"));

    private static DuplicityCheckForm parties(String plaintiffId) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter("Familiar");
        form.setPlaintiffType("INDIVIDUAL");
        form.setPlaintiffName("Juan López");
        form.setPlaintiffId(plaintiffId);
        form.setDefendantUnknown(true);
        return form;
    }

    private static RequestDetailsForm details() {
        RequestDetailsForm details = new RequestDetailsForm();
        details.setPlaintiffAddress("Av. Reforma 10, Centro, Cuautla, 62740");
        details.setPlaintiffPhone("735 112 4598");
        details.setPlaintiffEmail("Juan@Ejemplo.mx");
        details.setFilingType("IN_PERSON");
        details.addAnnex(new RequestDetailsForm.AnnexUpload("Escrito inicial.pdf", 1_500_000, "application/pdf"));
        return details;
    }

    @Test
    void registersThePendingRequestWithTheNextFolioOfTheYear() {
        DemandRequestRegistrationService.Registration registration =
            service.register(clerk, parties("LOJU900512KL3"), details(), false);

        assertTrue(registration.isSaved());
        assertEquals("S-000128/2026", registration.folio());
        DemandRequest saved = requests.findByFolio("S-000128/2026").orElseThrow();
        assertEquals(RequestStatus.PENDING, saved.getStatus());
        assertEquals("12", saved.getCourtCode());
        assertEquals("Laura Méndez", saved.getCapturedBy());
        assertEquals("7351124598", saved.getPlaintiff().getPhone());
        assertEquals("juan@ejemplo.mx", saved.getPlaintiff().getEmail());
        assertEquals("Av. Reforma 10, Centro, Cuautla, 62740", saved.getPlaintiff().getAddress());
        assertFalse(saved.getDefendant().isIdentified());
        assertEquals(FilingType.IN_PERSON, saved.getFilingType());
        assertEquals(1465, saved.getAnnexes().get(0).getSizeKb());
        assertNull(saved.getDuplicityWarning());
    }

    @Test
    void aMatchStopsTheSaveUntilTheClerkContinuesAndThenTheWarningIsKept() {
        DemandRequestRegistrationService.Registration warned =
            service.register(clerk, parties("ROGA800101AB1"), details(), false);

        assertNotNull(warned.warning());
        assertFalse(warned.isSaved());

        String folio = service.register(clerk, parties("ROGA800101AB1"), details(), true).folio();
        assertEquals("Juzgado de Jojutla", requests.findByFolio(folio).orElseThrow().getDuplicityWarning().getCourtName());
    }

    @Test
    void refusesMissingDetailsAndRequestsWithoutAnnexes() {
        RequestDetailsForm empty = new RequestDetailsForm();

        DemandRequestRegistrationService.Registration rejected =
            service.register(clerk, parties("LOJU900512KL3"), empty, false);

        assertEquals(List.of("plaintiffAddress", "plaintiffPhone", "filingType", "annexes"),
            List.copyOf(rejected.errors().keySet()));
        assertTrue(requests.findByFolio("S-000128/2026").isEmpty());
    }

    @Test
    void acceptsOnlyPdfAnnexesOfUpToTenMegabytes() {
        RequestDetailsForm word = details();
        word.getAnnexes().clear();
        word.addAnnex(new RequestDetailsForm.AnnexUpload("demanda.docx", 1000, "application/msword"));
        RequestDetailsForm heavy = details();
        heavy.addAnnex(new RequestDetailsForm.AnnexUpload("pruebas.pdf", 11L * 1024 * 1024, "application/pdf"));

        assertTrue(service.register(clerk, parties("LOJU900512KL3"), word, false).errors().containsKey("annexes"));
        assertTrue(service.register(clerk, parties("LOJU900512KL3"), heavy, false).errors().containsKey("annexes"));
    }

    @Test
    void checksStepOneAgainBeforeSaving() {
        DuplicityCheckForm invalid = parties("NO-ES-RFC");

        assertTrue(service.register(clerk, invalid, details(), false).errors().containsKey("plaintiffId"));
    }

    @Test
    void needsTheCourtOfTheClerk() {
        User unknownCourt = new User("x@utez.edu.mx", "Rosa Vega", UserRole.CLERK, "Juzgado que no existe");

        assertThrows(AccessDeniedException.class,
            () -> service.register(unknownCourt, parties("LOJU900512KL3"), details(), false));
    }
}
