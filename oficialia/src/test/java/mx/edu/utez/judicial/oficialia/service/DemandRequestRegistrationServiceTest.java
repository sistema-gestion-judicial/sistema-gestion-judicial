package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.CancelledAttempt;
import mx.edu.utez.judicial.oficialia.model.ComplementaryDataForm;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.PresentationType;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class DemandRequestRegistrationServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T18:30:00Z"), ZONE);
    private static final ActiveDemand IN_JOJUTLA =
        new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "TOSA850312ML4", "PEGL800101HD1");

    private final User clerk = new User("laura@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");
    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao();
    private final InMemoryDemandRequestDao requests =
        new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());
    private final DemandRequestRegistrationService service = new DemandRequestRegistrationService(catalog, requests,
        new DuplicityService(List.of(IN_JOJUTLA)), CLOCK);

    private static DuplicityCheckForm parties(String matter, String plaintiffId) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(matter);
        form.setPlaintiffName("Rosa Díaz Luna");
        form.setPlaintiffId(plaintiffId);
        form.setDefendantUnknown(true);
        return form;
    }

    private static ComplementaryDataForm data() {
        ComplementaryDataForm form = new ComplementaryDataForm();
        form.getPlaintiff().setStreet("Calle Hidalgo 12");
        form.getPlaintiff().setNeighborhood("Centro");
        form.getPlaintiff().setPostalCode("62740");
        form.getPlaintiff().setMunicipality("Cuautla");
        form.getPlaintiff().setPhone("735 112 4598");
        form.getPlaintiff().setEmail("rosa@ejemplo.mx");
        form.setLegalRepresentative("Lic. Marcos Rivera Peña");
        form.setPresentationType("INDIVIDUAL");
        form.setObservations("Entregó el acta en papel");
        form.addAnnex(Annex.received("Demanda.pdf", "application/pdf", 2048));
        return form;
    }

    @Test
    void offersTheActiveSubjectMattersOrderedByName() {
        assertEquals(List.of("Civil", "Familiar", "Mercantil", "Penal"), service.activeMatterNames());
    }

    @Test
    void leavesOutAnInactiveSubjectMatter() {
        InMemoryCatalogDao withInactive = new InMemoryCatalogDao() {
            @Override
            public List<SubjectMatter> findSubjectMatters() {
                return List.of(new SubjectMatter(1, "CIV", "Civil", null, true),
                    new SubjectMatter(2, "AGR", "Agraria", null, false));
            }
        };

        assertEquals(List.of("Civil"),
            new DemandRequestRegistrationService(withInactive, requests, CLOCK).activeMatterNames());
    }

    @Test
    void registersThePendingRequestWithTheNextFolioOfTheYearInTheClerksCourt() {
        var registration = service.register(clerk, parties("Civil", "DILR900101AB1"), data());

        assertTrue(registration.isSaved());
        DemandRequest saved = requests.findByFolio("S-000128/2026").orElseThrow();
        assertEquals(RequestStatus.PENDING, saved.getStatus());
        assertEquals(LocalDate.parse("2026-11-17"), saved.getReceivedDate());
        assertEquals("12", saved.getCourtCode());
        assertEquals("Juzgado de Cuautla", saved.getCourtName());
        assertEquals("Laura Méndez", saved.getCapturedBy());
        assertEquals("Civil", saved.getMatter());
        assertNull(saved.getDuplicityWarning());
    }

    @Test
    void keepsThePartiesAndTheComplementaryData() {
        DemandRequest saved = service.register(clerk, parties("Civil", "DILR900101AB1"), data()).request();

        assertEquals("Rosa Díaz Luna", saved.getPlaintiff().getName());
        assertEquals("Persona física", saved.getPlaintiff().getPersonType());
        assertEquals("DILR900101AB1", saved.getPlaintiff().getPersonId());
        assertEquals("7351124598", saved.getPlaintiff().getPhone());
        assertEquals("rosa@ejemplo.mx", saved.getPlaintiff().getEmail());
        assertEquals("Calle Hidalgo 12, Centro, C.P. 62740, Cuautla", saved.getPlaintiff().getAddress().getFullText());
        assertFalse(saved.getDefendant().isIdentified());
        assertEquals("Lic. Marcos Rivera Peña", saved.getLegalRepresentative());
        assertEquals(PresentationType.INDIVIDUAL, saved.getPresentationType());
        assertEquals("Entregó el acta en papel", saved.getObservations());
        assertEquals(List.of("Demanda.pdf"), saved.getAnnexes().stream().map(Annex::getFileName).toList());
    }

    @Test
    void registersWithTheWarningWhenTheClerkContinuesAfterAMatch() {
        DemandRequest saved = service.register(clerk, parties("Familiar", "TOSA850312ML4"), data()).request();

        assertEquals("Juzgado de Jojutla", saved.getDuplicityWarning().getCourtName());
        assertEquals("Familiar", saved.getDuplicityWarning().getMatter());
        assertEquals("Continuó con el registro", saved.getDuplicityWarning().getDecision());
        assertEquals(RequestStatus.PENDING, saved.getStatus());
    }

    @Test
    void savesNothingWithoutAnyIdentifiedParty() {
        DuplicityCheckForm nobody = parties("Civil", "DILR900101AB1");
        nobody.setPlaintiffAnonymous(true);

        var registration = service.register(clerk, nobody, data());

        assertFalse(registration.isSaved());
        assertTrue(registration.errors().containsKey("parties"));
        assertEquals(10, requests.findAll().size());
    }

    @Test
    void savesNothingWhenAComplementaryDatumOrTheAnnexesAreMissing() {
        ComplementaryDataForm incomplete = new ComplementaryDataForm();
        incomplete.setLegalRepresentative("Lic. Marcos Rivera Peña");
        incomplete.setPresentationType("INDIVIDUAL");

        var registration = service.register(clerk, parties("Civil", "DILR900101AB1"), incomplete);

        assertFalse(registration.isSaved());
        assertTrue(registration.errors().containsKey("plaintiffStreet"));
        assertTrue(registration.errors().containsKey("annexes"));
        assertEquals(10, requests.findAll().size());
    }

    @Test
    void refusesASubjectMatterThatIsNotActive() {
        var registration = service.register(clerk, parties("Agraria", "DILR900101AB1"), data());

        assertEquals("Seleccione una materia activa.", registration.errors().get("matter"));
        assertEquals(10, requests.findAll().size());
    }

    @Test
    void anAnonymousPlaintiffIsRegisteredWithoutAddressNorContact() {
        DuplicityCheckForm anonymous = new DuplicityCheckForm();
        anonymous.setMatter("Civil");
        anonymous.setPlaintiffAnonymous(true);
        anonymous.setDefendantName("Minera del Sur S.A. de C.V.");
        anonymous.setDefendantType("COMPANY");
        anonymous.setDefendantId("MSU010203KL9");
        ComplementaryDataForm data = new ComplementaryDataForm();
        data.setLegalRepresentative("Colectivo Vecinos Unidos");
        data.setPresentationType("COLLECTIVE");
        data.addAnnex(Annex.received("Demanda.pdf", "application/pdf", 10));

        DemandRequest saved = service.register(clerk, anonymous, data).request();

        assertFalse(saved.getPlaintiff().isIdentified());
        assertEquals("Persona moral", saved.getDefendant().getPersonType());
        assertNull(saved.getDefendant().getAddress());
        assertEquals(PresentationType.COLLECTIVE, saved.getPresentationType());
    }

    @Test
    void takesTheNextFolioWhenAnotherClerkTookItFirst() {
        InMemoryDemandRequestDao racing = new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests()) {
            private boolean stale = true;

            @Override
            public int lastFolioNumber(int year) {
                int last = super.lastFolioNumber(year);
                if (stale) {
                    stale = false;
                    return last - 1;
                }
                return last;
            }
        };
        DemandRequestRegistrationService raced = new DemandRequestRegistrationService(catalog, racing, CLOCK);

        assertEquals("S-000128/2026", raced.register(clerk, parties("Civil", "DILR900101AB1"), data())
            .request().getFolio());
    }

    @Test
    void keepsTheAttemptCancelledAfterAWarning() {
        assertTrue(service.cancel(clerk, parties("Familiar", "TOSA850312ML4")));

        assertEquals(List.of(new CancelledAttempt("12", "Juzgado de Jojutla", "Familiar", false, "Laura Méndez",
            LocalDateTime.parse("2026-11-17T12:30"))), requests.findCancelledAttempts());
        assertEquals(10, requests.findAll().size());
    }

    @Test
    void keepsNoAttemptWhenNothingMatches() {
        assertFalse(service.cancel(clerk, parties("Civil", "DILR900101AB1")));
        assertTrue(requests.findCancelledAttempts().isEmpty());
    }

    @Test
    void theDemoAdministratorRegistersInCuautlaLikeTheClerk() {
        User demo = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);

        DemandRequest saved = service.register(demo, parties("Civil", "DILR900101AB1"), data()).request();

        assertEquals("12", saved.getCourtCode());
        assertEquals("Elena Ríos", saved.getCapturedBy());
    }

    @Test
    void onlyAnOficialDePartesRegisters() {
        User courtAdmin = new User("a@utez.edu.mx", "Ana Paz", UserRole.COURT_ADMIN, "Juzgado de Cuautla");

        assertThrows(AccessDeniedException.class,
            () -> service.register(courtAdmin, parties("Civil", "DILR900101AB1"), data()));
        assertThrows(AccessDeniedException.class,
            () -> service.cancel(courtAdmin, parties("Familiar", "TOSA850312ML4")));
        assertThrows(AccessDeniedException.class,
            () -> service.register(null, parties("Civil", "DILR900101AB1"), data()));
        assertEquals(10, requests.findAll().size());
    }
}
