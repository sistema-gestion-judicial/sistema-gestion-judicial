package mx.edu.utez.judicial.oficialia.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;

/**
 * Case files for the other systems: every demand request ruled admissible (#29).
 */
public class CaseFileService {

    /** YYYY/NNNNN/CC, the format of every case file number (DFR 2.5). */
    public static final Pattern CASE_FILE_NUMBER = Pattern.compile("\\d{4}/\\d{5}/\\d{2}");

    private final DemandRequestDao demandRequestDao;

    public CaseFileService() {
        this(OficialiaDaos.demandRequests());
    }

    public CaseFileService(DemandRequestDao demandRequestDao) {
        this.demandRequestDao = demandRequestDao;
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
}
