package mx.edu.utez.judicial.oficialia.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.CancelledAttempt;
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

    /** The highest folio number of that year (S-000127/2026 is 127), or 0 when the year has none yet. */
    int lastFolioNumber(int year);

    /**
     * Saves a new request with its parties, annexes, warning decision and first status.
     *
     * @throws DuplicateCodeException when another request already has the folio
     */
    void insert(DemandRequest demandRequest);

    /**
     * Saves a request the clerk just registered, like {@link #insert}, and logs the registration with the
     * clerk and the moment, and the decision to continue when it came with a duplicity warning (DFR 3.3.9).
     *
     * @throws DuplicateCodeException when another request already has the folio
     */
    void register(DemandRequest demandRequest, User clerk);

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

    /** Keeps a registration cancelled after a duplicity warning; it has no folio because nothing was saved. */
    void recordCancelledAttempt(CancelledAttempt attempt);
}
