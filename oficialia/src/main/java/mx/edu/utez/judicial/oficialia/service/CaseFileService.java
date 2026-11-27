package mx.edu.utez.judicial.oficialia.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import mx.edu.utez.judicial.oficialia.client.ChamberClient;
import mx.edu.utez.judicial.oficialia.client.SampleChamberClient;
import mx.edu.utez.judicial.oficialia.dao.CaseFileDao;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCaseFileDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.CaseFileState;
import mx.edu.utez.judicial.oficialia.model.CaseFileStatus;
import mx.edu.utez.judicial.oficialia.model.Chamber;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Case files for the other systems: every demand request ruled admissible (#29).
 */
public class CaseFileService {

    /** YYYY/NNNNN/CC, the format of every case file number (DFR 2.5). */
    public static final Pattern CASE_FILE_NUMBER = Pattern.compile("\\d{4}/\\d{5}/\\d{2}");

    public static final int PAGE_SIZE = 10;

    private static final ChamberClient SHARED_CHAMBER_CLIENT = new SampleChamberClient();

    private final DemandRequestDao demandRequestDao;
    private final CaseFileDao caseFileDao;
    private final ChamberClient chamberClient;
    private final Supplier<LocalDateTime> clock;

    public CaseFileService() {
        this(OficialiaDaos.demandRequests(), OficialiaDaos.caseFiles(), SHARED_CHAMBER_CLIENT, LocalDateTime::now);
    }

    public CaseFileService(DemandRequestDao demandRequestDao) {
        this(demandRequestDao, new InMemoryCaseFileDao(demandRequestDao), SHARED_CHAMBER_CLIENT, LocalDateTime::now);
    }

    public CaseFileService(DemandRequestDao demandRequestDao, CaseFileDao caseFileDao, ChamberClient chamberClient,
                           Supplier<LocalDateTime> clock) {
        this.demandRequestDao = demandRequestDao;
        this.caseFileDao = caseFileDao;
        this.chamberClient = chamberClient;
        this.clock = clock;
    }

    /**
     * Case files that match every given filter; null filters are ignored. Newest first.
     *
     * @param courtCode two-digit court code
     * @param matter subject matter name, case-insensitive
     * @param admittedSince only case files ruled on or after this date
     */
    public List<DemandRequest> search(String courtCode, String matter, LocalDate admittedSince) {
        return demandRequestDao.findAll().stream()
            .filter(request -> request.getStatus() == RequestStatus.ADMISSIBLE)
            .filter(request -> courtCode == null || request.getCourtCode().equals(courtCode))
            .filter(request -> matter == null || request.getMatter().equalsIgnoreCase(matter))
            .filter(request -> admittedSince == null
                || !request.getRuledAt().toLocalDate().isBefore(admittedSince))
            .sorted(Comparator.comparing(DemandRequest::getRuledAt).reversed())
            .toList();
    }

    public Optional<DemandRequest> findByNumber(String caseFileNumber) {
        return demandRequestDao.findAll().stream()
            .filter(request -> request.getStatus() == RequestStatus.ADMISSIBLE)
            .filter(request -> request.getCaseFileNumber().equals(caseFileNumber))
            .findFirst();
    }

    /** The state of a case file for the API, or a just-opened state when the table has none. */
    public CaseFileState stateOf(String caseFileNumber) {
        return caseFileDao.findState(caseFileNumber).orElse(CaseFileState.opened(caseFileNumber));
    }

    /**
     * Records what another system reports about a case file (DFR 3.6): the first instance was resolved, or the
     * case file concluded. A concluded case file no longer counts for duplicity.
     *
     * @return false when no case file has the number
     */
    public boolean recordFirstInstanceResolution(String caseFileNumber) {
        return caseFileDao.recordFirstInstanceResolution(caseFileNumber, clock.get());
    }

    public boolean recordConclusion(String caseFileNumber) {
        return caseFileDao.recordConclusion(caseFileNumber, clock.get());
    }

    /**
     * The page of the case files of the user's court that match the filters (DFR 3.6), newest first. The text
     * is looked for in the number, the parties and the subject matter, ignoring case and accents. Inadmissible
     * requests never show up because they have no case file.
     *
     * @throws AccessDeniedException when the user is neither an Oficial de Partes nor an Administrador de Juzgado
     */
    public ResultPage<CaseFile> listForCourt(User user, String text, CaseFileStatus status, int page) {
        requireCourtStaff(user);
        String key = CatalogNames.key(text);
        List<CaseFile> rows = caseFilesOf(user).stream()
            .filter(caseFile -> status == null || caseFile.getStatus() == status)
            .filter(caseFile -> key.isEmpty() || Stream.of(caseFile.getNumber(), caseFile.getPlaintiffName(),
                    caseFile.getDefendantName(), caseFile.getMatter())
                .anyMatch(value -> CatalogNames.key(value).contains(key)))
            .toList();
        return ResultPage.of(rows, page, PAGE_SIZE);
    }

    /**
     * A case file of the user's court. One of another court is reported as not found.
     *
     * @throws AccessDeniedException when the user is neither an Oficial de Partes nor an Administrador de Juzgado
     * @throws NotFoundException when the court has no case file with that number
     */
    public CaseFile get(User user, String caseFileNumber) {
        requireCourtStaff(user);
        return caseFilesOf(user).stream()
            .filter(caseFile -> caseFile.getNumber().equals(caseFileNumber))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No existe un expediente de su juzgado con ese número."));
    }

    /**
     * Checks that the case file can go to second instance and gets the chambers of the Tocas system, nearest
     * first (DFR 3.7). When the Tocas system does not answer, the chambers are empty and the move can still be
     * recorded with the chamber pending.
     *
     * @throws AccessDeniedException when the user is not an Oficial de Partes
     * @throws NotFoundException when the court has no case file with that number
     * @throws BusinessRuleException when the case file cannot go to second instance
     */
    public SecondInstanceOptions secondInstanceOptions(User clerk, String caseFileNumber) {
        CaseFile caseFile = eligibleForSecondInstance(clerk, caseFileNumber);
        try {
            return new SecondInstanceOptions(caseFile, chamberClient.chambersFor(caseFile.getCourtCode()), false);
        } catch (ServiceUnavailableException e) {
            return new SecondInstanceOptions(caseFile, List.of(), true);
        }
    }

    /**
     * Moves the case file to second instance with the chosen chamber, hands it to the Tocas system and writes
     * it to the audit log (DFR 3.7). No duplicity warning applies: it is the same lawsuit. When the Tocas system
     * does not answer, the move is recorded with the chamber pending.
     *
     * @throws AccessDeniedException when the user is not an Oficial de Partes
     * @throws NotFoundException when the court has no case file with that number
     * @throws BusinessRuleException when the case file cannot go to second instance or the chamber is not offered
     */
    public CaseFile moveToSecondInstance(User clerk, String caseFileNumber, String chamberCode) {
        CaseFile caseFile = eligibleForSecondInstance(clerk, caseFileNumber);
        Chamber chamber = null;
        try {
            List<Chamber> chambers = chamberClient.chambersFor(caseFile.getCourtCode());
            chamber = chambers.stream().filter(candidate -> candidate.getCode().equals(chamberCode)).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Elija una de las salas de la lista."));
            // The same key on a retry, so the Tocas system does not open two appeals for the case file
            chamberClient.deliver(caseFile, chamber, "second-instance:" + caseFileNumber);
        } catch (ServiceUnavailableException e) {
            chamber = null;
        }
        try {
            caseFileDao.moveToSecondInstance(caseFileNumber, chamber == null ? null : chamber.getCode(),
                chamber == null ? null : chamber.getName(), clerk, clock.get());
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("El expediente ya tiene una sala asignada; no se puede elegir otra.");
        }
        return get(clerk, caseFileNumber);
    }

    /** What the second instance form offers: the chambers, or none when the Tocas system did not answer. */
    public record SecondInstanceOptions(CaseFile caseFile, List<Chamber> chambers, boolean chambersUnavailable) {

        public CaseFile getCaseFile() {
            return caseFile;
        }

        public List<Chamber> getChambers() {
            return chambers;
        }

        public boolean isChambersUnavailable() {
            return chambersUnavailable;
        }
    }

    private CaseFile eligibleForSecondInstance(User clerk, String caseFileNumber) {
        if (clerk == null || clerk.getRole() != UserRole.CLERK) {
            throw new AccessDeniedException("Solo el Oficial de Partes registra el pase a segunda instancia.");
        }
        CaseFile caseFile = get(clerk, caseFileNumber);
        if (caseFile.isConcluded()) {
            throw new BusinessRuleException("El expediente " + caseFileNumber + " ya concluyó.");
        }
        if (caseFile.getChamberCode() != null) {
            throw new BusinessRuleException("El expediente ya tiene asignada la " + caseFile.getChamberName()
                + "; no se puede elegir otra sala.");
        }
        if (!caseFile.isFirstInstanceResolved()) {
            throw new BusinessRuleException("El expediente " + caseFileNumber
                + " todavía no tiene resolución de primera instancia.");
        }
        return caseFile;
    }

    private List<CaseFile> caseFilesOf(User user) {
        Map<String, CaseFileState> states = caseFileDao.findStates();
        return demandRequestDao.findAll().stream()
            .filter(request -> request.getStatus() == RequestStatus.ADMISSIBLE)
            .filter(request -> user.getCourtName().equals(request.getCourtName()))
            .sorted(Comparator.comparing(DemandRequest::getRuledAt).reversed())
            .map(request -> new CaseFile(request, states.getOrDefault(request.getCaseFileNumber(),
                CaseFileState.opened(request.getCaseFileNumber()))))
            .toList();
    }

    private static void requireCourtStaff(User user) {
        if (user == null || user.getCourtName() == null
                || (user.getRole() != UserRole.CLERK && user.getRole() != UserRole.COURT_ADMIN)) {
            throw new AccessDeniedException("Solo el personal del juzgado consulta sus expedientes.");
        }
    }
}
