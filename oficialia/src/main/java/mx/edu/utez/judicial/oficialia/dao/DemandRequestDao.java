package mx.edu.utez.judicial.oficialia.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Access to the demand requests of every court. {@link JdbcDemandRequestDao} reads MySQL;
 * {@link InMemoryDemandRequestDao} keeps sample data when no database is configured.
 */
public interface DemandRequestDao {

    List<DemandRequest> findAll();

    List<DemandRequest> findByCourt(String courtName);

    Optional<DemandRequest> findByFolio(String folio);

    void insert(DemandRequest demandRequest);

    /**
     * Registers a new request (DFR 3.1) with the next folio of the year (S-NNNNNN/AAAA), pending to be ruled.
     * The parties, annexes, duplicity decision and audit record are saved together or not at all, and the
     * folio is never given twice. The folio of the draft is ignored.
     *
     * @return the folio given to the request
     */
    String register(DemandRequest draft, User clerk, LocalDateTime registeredAt);

    /**
     * Keeps a registration the clerk cancelled after a duplicity warning: no request is saved, only the
     * attempt with the court found, the user and the date (DFR 3.1).
     */
    void recordCancelledAttempt(String courtCode, String matchedCourt, String matchedMatter, User clerk,
                                LocalDateTime at);

    /**
     * Rules a pending request as admissible and opens its case file with the next number of its court
     * for that year (AAAA/NNNNN/JJ). Everything is saved together or not at all (DFR 3.3.2), the number
     * is never given twice, and the desk stays pending until {@link #assignHearingDesk} runs.
     *
     * @return the case file number
     * @throws IllegalStateException when the request was ruled in the meantime
     */
    String admit(DemandRequest request, User clerk, LocalDateTime ruledAt);

    /** Records the desk the hearings service assigned to an open case file whose desk is still pending. */
    void assignHearingDesk(String caseFileNumber, String deskName, LocalDateTime assignedAt);

    /**
     * Rules a pending request as inadmissible with the reason given to the plaintiff.
     *
     * @throws IllegalStateException when the request was ruled in the meantime
     */
    void reject(DemandRequest request, String reason, User clerk, LocalDateTime ruledAt);
}
