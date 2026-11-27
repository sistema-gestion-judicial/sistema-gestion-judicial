package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.CancelledAttempt;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyAddress;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.PresentationType;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcDemandRequestDaoTest {

    private static final LocalDateTime RULED_AT = LocalDateTime.parse("2026-11-08T12:30");
    private static final String CLERK_EMAIL = "lmendez@utez.edu.mx";

    private Database database;
    private JdbcDemandRequestDao dao;
    private User clerk;

    @BeforeEach
    void freshSchema() throws Exception {
        database = TestDatabase.migrated();
        dao = new JdbcDemandRequestDao(database);
        InMemoryDemandRequestDao.sampleRequests().forEach(dao::insert);
        JdbcUserDao users = new JdbcUserDao(database);
        users.insert(new NewUserAccount("Laura", "Méndez", CLERK_EMAIL, UserRole.CLERK, "12",
            "600000:c2FsdA==:aGFzaA==", false, true));
        clerk = users.findByEmail(CLERK_EMAIL).orElseThrow().user();
    }

    @Test
    void readsBackWhatWasSavedWithPartiesAnnexesAndRuling() {
        DemandRequest penal = dao.findByFolio("S-000124/2026").orElseThrow();

        assertEquals("Juzgado de Cuautla", penal.getCourtName());
        assertEquals("Juan Ruiz Hernández", penal.getPlaintiff().getName());
        assertFalse(penal.getDefendant().isIdentified());
        assertEquals(RequestStatus.ADMISSIBLE, penal.getStatus());
        assertEquals("2026/00431/12", penal.getCaseFileNumber());
        assertEquals(1, penal.getAnnexes().size());
        assertEquals("Juzgado de Cuernavaca",
            dao.findByFolio("S-000125/2026").orElseThrow().getDuplicityWarning().getCourtName());
        assertEquals(9, dao.findByCourt("Juzgado de Cuautla").size());
        assertEquals(10, dao.count());
    }

    @Test
    void readsBackTheAddressOfEachParty() {
        Party plaintiff = new Party(PartyRole.PLAINTIFF, "Rosa Díaz Luna", "INDIVIDUAL", "DILR900101AB1",
            "7351234567", "rosa@ejemplo.mx", new PartyAddress("Calle Hidalgo 12", "Centro", "62740", "Cuautla"));
        dao.insert(new DemandRequest("S-000200/2026", LocalDate.parse("2026-11-17"), "Juzgado de Cuautla", "12",
            "Civil", plaintiff, Party.unidentified(PartyRole.DEFENDANT), "Lic. Marcos Rivera Peña", "Laura Méndez",
            List.of(new Annex("Demanda.pdf", 120)), null));

        DemandRequest saved = dao.findByFolio("S-000200/2026").orElseThrow();
        assertEquals("Calle Hidalgo 12, Centro, C.P. 62740, Cuautla", saved.getPlaintiff().getAddress().getFullText());
        assertNull(saved.getDefendant().getAddress());
        assertNull(dao.findByFolio("S-000127/2026").orElseThrow().getPlaintiff().getAddress());
    }

    @Test
    void readsBackThePresentationTypeAndTheObservations() {
        dao.insert(new DemandRequest("S-000201/2026", LocalDate.parse("2026-11-17"), "Juzgado de Cuautla", "12",
            "Civil", Party.unidentified(PartyRole.PLAINTIFF), Party.unidentified(PartyRole.DEFENDANT),
            "Colectivo Vecinos Unidos", "Laura Méndez", List.of(new Annex("Demanda.pdf", 120)), null,
            PresentationType.COLLECTIVE, "Entregó los anexos en papel"));

        DemandRequest saved = dao.findByFolio("S-000201/2026").orElseThrow();
        assertEquals(PresentationType.COLLECTIVE, saved.getPresentationType());
        assertEquals("Entregó los anexos en papel", saved.getObservations());
        assertEquals(PresentationType.INDIVIDUAL, dao.findByFolio("S-000127/2026").orElseThrow().getPresentationType());
    }

    @Test
    void readsBackTheNameSizeAndTypeOfEachAnnex() {
        dao.insert(new DemandRequest("S-000202/2026", LocalDate.parse("2026-11-17"), "Juzgado de Cuautla", "12",
            "Civil", Party.unidentified(PartyRole.PLAINTIFF), Party.unidentified(PartyRole.DEFENDANT),
            "Lic. Marcos Rivera Peña", "Laura Méndez",
            List.of(Annex.received("Demanda.pdf", "application/pdf", 2048)), null));

        Annex saved = dao.findByFolio("S-000202/2026").orElseThrow().getAnnexes().get(0);
        assertEquals("Demanda.pdf", saved.getFileName());
        assertEquals(2, saved.getSizeKb());
        assertEquals("application/pdf", saved.getContentType());
    }

    @Test
    void findsTheLastFolioNumberOfEachYear() {
        assertEquals(127, dao.lastFolioNumber(2026));
        assertEquals(0, dao.lastFolioNumber(2027));
    }

    @Test
    void refusesAFolioThatIsAlreadyTaken() {
        DemandRequest taken = InMemoryDemandRequestDao.sampleRequests().get(0);

        assertThrows(DuplicateCodeException.class, () -> dao.insert(taken));
        assertEquals(10, dao.count());
    }

    @Test
    void keepsACancelledAttemptWithoutFolio() throws Exception {
        dao.recordCancelledAttempt(new CancelledAttempt("12", "Juzgado de Jojutla", "Familiar", true, "Laura Méndez",
            LocalDateTime.parse("2026-11-17T12:30")));

        assertEquals(1, count("SELECT COUNT(*) FROM duplicity_warning_decisions WHERE decision = 'CANCEL' "
            + "AND folio IS NULL AND court_code = '12' AND matched_court = 'Juzgado de Jojutla' "
            + "AND matched_instance = 'SECOND' AND decided_by = 'Laura Méndez'"));
        assertEquals(10, dao.count());
    }

    @Test
    void logsTheRegistrationAndTheDecisionToContinueWithTheClerk() throws Exception {
        dao.register(new DemandRequest("S-000203/2026", LocalDate.parse("2026-11-27"), "Juzgado de Cuautla", "12",
            "Civil", Party.unidentified(PartyRole.PLAINTIFF), Party.unidentified(PartyRole.DEFENDANT),
            "Lic. Marcos Rivera Peña", "Laura Méndez", List.of(new Annex("Demanda.pdf", 120)),
            new DuplicityWarning("Juzgado de Jojutla", "Civil", "Continuó con el registro")), clerk);

        assertEquals(1, count("SELECT COUNT(*) FROM audit_log a JOIN users u ON u.id = a.actor_user_id "
            + "WHERE a.action = 'REQUEST_REGISTERED' AND a.entity_id = 'S-000203/2026' AND u.email = '" + CLERK_EMAIL
            + "'"));
        assertEquals(1, count("SELECT COUNT(*) FROM audit_log WHERE action = 'DUPLICITY_WARNING_CONTINUED' "
            + "AND entity_id = 'S-000203/2026'"));
    }

    @Test
    void savesNothingWhenTheClerkHasNoAccount() {
        User stranger = new User("nadie@utez.edu.mx", "Nadie", UserRole.CLERK, "Juzgado de Cuautla");

        assertThrows(RuntimeException.class, () -> dao.register(new DemandRequest("S-000204/2026",
            LocalDate.parse("2026-11-27"), "Juzgado de Cuautla", "12", "Civil", Party.unidentified(PartyRole.PLAINTIFF),
            Party.unidentified(PartyRole.DEFENDANT), "Lic. Marcos Rivera Peña", "Nadie",
            List.of(new Annex("Demanda.pdf", 120)), null), stranger));
        assertTrue(dao.findByFolio("S-000204/2026").isEmpty());
    }

    @Test
    void keepsTheCaseFilesTheWarningDecisionsAndTheStatusLogOfTheSavedRequests() throws Exception {
        List<DemandRequest> samples = InMemoryDemandRequestDao.sampleRequests();
        long caseFiles = samples.stream().filter(request -> request.getCaseFileNumber() != null).count();
        long ruled = samples.stream().filter(request -> request.getStatus() != RequestStatus.PENDING).count();

        assertEquals(caseFiles, count("SELECT COUNT(*) FROM case_files"));
        assertEquals(samples.stream().filter(request -> request.getDuplicityWarning() != null).count(),
            count("SELECT COUNT(*) FROM duplicity_warning_decisions WHERE decision = 'CONTINUE'"));
        assertEquals(samples.size() + ruled, count("SELECT COUNT(*) FROM request_status_history"));
        // The sample numbers moved the sequence of their court past them
        assertEquals(431, count("SELECT last_sequence FROM case_file_sequences WHERE court_code = '12' AND year = 2026"));
    }

    @Test
    void admitsWithTheNextNumberOfTheCourtAndLogsTheRuling() throws Exception {
        String number = dao.admit(pending("S-000127/2026"), clerk, RULED_AT);

        assertEquals("2026/00432/12", number);
        DemandRequest saved = dao.findByFolio("S-000127/2026").orElseThrow();
        assertEquals(RequestStatus.ADMISSIBLE, saved.getStatus());
        assertEquals(number, saved.getCaseFileNumber());
        assertTrue(saved.isDeskPending());
        assertEquals("Laura Méndez", saved.getRuledBy());
        assertEquals(1, count("SELECT COUNT(*) FROM case_files WHERE number = '2026/00432/12' "
            + "AND year = 2026 AND sequence = 432 AND desk_name IS NULL AND folio = 'S-000127/2026'"));
        assertEquals(1, count("SELECT COUNT(*) FROM request_status_history WHERE folio = 'S-000127/2026' "
            + "AND from_status = 'PENDING' AND to_status = 'ADMISSIBLE' AND changed_by = 'Laura Méndez'"));
        assertEquals(List.of("USER|" + CLERK_EMAIL + "|REQUEST_ADMITTED|S-000127/2026"), auditEntries());
    }

    @Test
    void startsTheNumbersAgainEveryYear() {
        assertEquals("2027/00001/12",
            dao.admit(pending("S-000127/2026"), clerk, LocalDateTime.parse("2027-01-04T09:00")));
    }

    @Test
    void assignsTheDeskLaterAsTheSystemAndOnlyOnce() throws Exception {
        String number = dao.admit(pending("S-000127/2026"), clerk, RULED_AT);

        dao.assignHearingDesk(number, "Mesa 1", RULED_AT.plusMinutes(1));
        dao.assignHearingDesk(number, "Mesa 3", RULED_AT.plusMinutes(2));

        DemandRequest saved = dao.findByFolio("S-000127/2026").orElseThrow();
        assertEquals("Mesa 1", saved.getDeskName());
        assertFalse(saved.isDeskPending());
        assertEquals(1, count("SELECT COUNT(*) FROM case_files WHERE number = '" + number + "' AND desk_name = 'Mesa 1'"));
        assertEquals(List.of("USER|" + CLERK_EMAIL + "|REQUEST_ADMITTED|S-000127/2026",
            "SYSTEM|null|HEARING_DESK_ASSIGNED|" + number), auditEntries());
    }

    @Test
    void rejectsWithTheReasonAndOpensNoCaseFile() throws Exception {
        long caseFiles = count("SELECT COUNT(*) FROM case_files");

        dao.reject(pending("S-000126/2026"), "Falta la firma del promovente.", clerk, RULED_AT);

        DemandRequest saved = dao.findByFolio("S-000126/2026").orElseThrow();
        assertEquals(RequestStatus.INADMISSIBLE, saved.getStatus());
        assertEquals("Falta la firma del promovente.", saved.getRejectionReason());
        assertNull(saved.getCaseFileNumber());
        assertEquals(caseFiles, count("SELECT COUNT(*) FROM case_files"));
        assertEquals(1, count("SELECT COUNT(*) FROM request_status_history WHERE folio = 'S-000126/2026' "
            + "AND to_status = 'INADMISSIBLE' AND reason = 'Falta la firma del promovente.'"));
        assertEquals(List.of("USER|" + CLERK_EMAIL + "|REQUEST_REJECTED|S-000126/2026"), auditEntries());
    }

    @Test
    void neverRulesTheSameRequestTwiceAndLeavesNothingHalfSaved() throws Exception {
        DemandRequest request = pending("S-000127/2026");
        dao.admit(request, clerk, RULED_AT);

        assertThrows(IllegalStateException.class, () -> dao.admit(request, clerk, RULED_AT));
        assertThrows(IllegalStateException.class, () -> dao.reject(request, "Otro motivo", clerk, RULED_AT));

        assertEquals("2026/00432/12", dao.findByFolio("S-000127/2026").orElseThrow().getCaseFileNumber());
        assertEquals(1, auditEntries().size());
        // The failed attempts rolled back, so the next number is not skipped
        assertEquals("2026/00433/12", dao.admit(pending("S-000126/2026"), clerk, RULED_AT));
    }

    @Test
    void aRulingBySomeoneWithoutAnAccountIsRolledBack() throws Exception {
        User stranger = new User("nadie@utez.edu.mx", "Sin Cuenta", UserRole.CLERK, "Juzgado de Cuautla");

        assertThrows(DataAccessException.class, () -> dao.admit(pending("S-000127/2026"), stranger, RULED_AT));

        assertTrue(dao.findByFolio("S-000127/2026").orElseThrow().isPending());
        assertEquals(0, count("SELECT COUNT(*) FROM case_files WHERE folio = 'S-000127/2026'"));
        assertEquals(431, count("SELECT last_sequence FROM case_file_sequences WHERE court_code = '12' AND year = 2026"));
    }

    @Test
    void givesDifferentNumbersToTwoClerksRulingAtTheSameTime() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = pool.submit(() -> {
                start.await();
                return dao.admit(pending("S-000127/2026"), clerk, RULED_AT);
            });
            Future<String> second = pool.submit(() -> {
                start.await();
                return dao.admit(pending("S-000126/2026"), clerk, RULED_AT);
            });
            start.countDown();

            assertEquals(Set.of("2026/00432/12", "2026/00433/12"), Set.of(first.get(), second.get()));
        } finally {
            pool.shutdownNow();
        }
    }

    private DemandRequest pending(String folio) {
        DemandRequest request = dao.findByFolio(folio).orElseThrow();
        assertTrue(request.isPending(), folio + " must be pending in the sample data");
        return request;
    }

    /** actor_type|author e-mail|action|entity_id of every audit entry, oldest first. */
    private List<String> auditEntries() throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT a.actor_type, u.email, a.action, a.entity_id "
                 + "FROM audit_log a LEFT JOIN users u ON u.id = a.actor_user_id ORDER BY a.id")) {
            List<String> entries = new ArrayList<>();
            while (rows.next()) {
                entries.add(rows.getString(1) + "|" + rows.getString(2) + "|" + rows.getString(3) + "|"
                    + rows.getString(4));
            }
            return entries;
        }
    }

    private int count(String sql) throws Exception {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
