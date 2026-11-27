package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import mx.edu.utez.judicial.oficialia.client.ChamberClient;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCaseFileDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.CaseFileStatus;
import mx.edu.utez.judicial.oficialia.model.Chamber;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
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

    private final InMemoryDemandRequestDao requests =
        new InMemoryDemandRequestDao(InMemoryDemandRequestDao.sampleRequests());
    private final InMemoryCaseFileDao caseFiles = new InMemoryCaseFileDao(requests);
    private final FakeChambers chambers = new FakeChambers();
    private final CaseFileService courtService = new CaseFileService(requests, caseFiles, chambers,
        () -> LocalDateTime.parse("2026-11-20T10:00"));
    private final User clerk = new User("l@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");
    private final User otherClerk = new User("r@utez.edu.mx", "Rosa Vega", UserRole.CLERK, "Juzgado de Jojutla");
    private final User courtAdmin =
        new User("a@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, "Juzgado de Cuautla");

    @Test
    void listsTheCaseFilesOfTheCourtWithTheirStatus() {
        courtService.recordConclusion("2026/00419/12");

        ResultPage<CaseFile> page = courtService.listForCourt(clerk, "", null, 1);

        assertEquals(4, page.getTotalItems());
        assertEquals(List.of("2026/00419/12"),
            courtService.listForCourt(courtAdmin, null, CaseFileStatus.CONCLUDED, 1).getItems().stream()
                .map(CaseFile::getNumber).toList());
        assertTrue(courtService.listForCourt(otherClerk, "", null, 1).getItems().isEmpty());
        assertTrue(courtService.listForCourt(clerk, "nadie se llama así", null, 1).getItems().isEmpty());
    }

    @Test
    void aCaseFileOfAnotherCourtIsNotFound() {
        assertThrows(NotFoundException.class, () -> courtService.get(otherClerk, "2026/00431/12"));
        assertThrows(AccessDeniedException.class, () -> courtService.listForCourt(
            new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null), "", null, 1));
    }

    @Test
    void refusesTheSecondInstanceWithoutAFirstInstanceResolution() {
        BusinessRuleException refused = assertThrows(BusinessRuleException.class,
            () -> courtService.moveToSecondInstance(clerk, "2026/00431/12", "SCU"));

        assertTrue(refused.getMessage().contains("resolución de primera instancia"));
        assertFalse(courtService.get(clerk, "2026/00431/12").isSecondInstance());
    }

    @Test
    void movesToSecondInstanceWithAnOfferedChamberAndDeliversIt() {
        courtService.recordFirstInstanceResolution("2026/00431/12");

        assertEquals(List.of("SCU", "SC1"), courtService.secondInstanceOptions(clerk, "2026/00431/12")
            .chambers().stream().map(Chamber::getCode).toList());
        CaseFile moved = courtService.moveToSecondInstance(clerk, "2026/00431/12", "SCU");

        assertEquals(CaseFileStatus.SECOND_INSTANCE, moved.getStatus());
        assertEquals("Sala Regional de Cuautla", moved.getChamberName());
        assertEquals("second-instance:2026/00431/12", chambers.deliveredKey);
        BusinessRuleException again = assertThrows(BusinessRuleException.class,
            () -> courtService.secondInstanceOptions(clerk, "2026/00431/12"));
        assertTrue(again.getMessage().contains("Sala Regional de Cuautla"));
    }

    @Test
    void refusesAChamberThatWasNotOffered() {
        courtService.recordFirstInstanceResolution("2026/00431/12");

        assertThrows(BusinessRuleException.class, () -> courtService.moveToSecondInstance(clerk, "2026/00431/12", "XXX"));
        assertFalse(courtService.get(clerk, "2026/00431/12").isSecondInstance());
    }

    @Test
    void whenTheTocasSystemDoesNotAnswerTheMoveIsRecordedWithTheChamberPending() {
        courtService.recordFirstInstanceResolution("2026/00431/12");
        chambers.offline = true;

        assertTrue(courtService.secondInstanceOptions(clerk, "2026/00431/12").chambersUnavailable());
        CaseFile moved = courtService.moveToSecondInstance(clerk, "2026/00431/12", null);

        assertTrue(moved.isChamberPending());
        chambers.offline = false;
        assertEquals("SC1", courtService.moveToSecondInstance(clerk, "2026/00431/12", "SC1").getChamberCode());
    }

    @Test
    void onlyTheClerkRecordsTheSecondInstance() {
        courtService.recordFirstInstanceResolution("2026/00431/12");

        assertThrows(AccessDeniedException.class,
            () -> courtService.moveToSecondInstance(courtAdmin, "2026/00431/12", "SCU"));
    }

    @Test
    void aConcludedCaseFileNoLongerCountsForDuplicity() {
        DemandRequest admitted = requests.findAll().stream()
            .filter(request -> "2026/00431/12".equals(request.getCaseFileNumber())).findFirst().orElseThrow();
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(admitted.getMatter());
        form.setPlaintiffId(admitted.getPlaintiff().getPersonId());
        DuplicityService duplicity = new DuplicityService(requests, caseFiles);
        boolean matchedBefore = duplicity.findMatch(form)
            .filter(match -> match.getCourtName().equals(admitted.getCourtName())).isPresent();

        courtService.recordConclusion("2026/00431/12");

        assertTrue(matchedBefore);
        assertFalse(duplicity.findMatch(form)
            .filter(match -> match.getCourtName().equals(admitted.getCourtName())).isPresent());
    }

    /** Two chambers, nearest first, and a switch to play the Tocas system being down. */
    private static final class FakeChambers implements ChamberClient {

        private boolean offline;
        private String deliveredKey;

        @Override
        public List<Chamber> chambersFor(String courtCode) {
            if (offline) {
                throw new ServiceUnavailableException("down");
            }
            return List.of(new Chamber("SCU", "Sala Regional de Cuautla", "Cuautla"),
                new Chamber("SC1", "Primera Sala Civil", "Cuernavaca"));
        }

        @Override
        public void deliver(CaseFile caseFile, Chamber chamber, String operationKey) {
            if (offline) {
                throw new ServiceUnavailableException("down");
            }
            deliveredKey = operationKey;
        }
    }
}
