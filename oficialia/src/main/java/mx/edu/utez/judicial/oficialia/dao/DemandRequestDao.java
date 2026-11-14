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
     * Rules a pending request as admissible and opens its case file with the next number of its court
     * for that year (AAAA/NNNNN/JJ). Everything is saved together or not at all (DFR 3.3.2), the number
     * is never given twice, and the desk stays pending until {@link #assignHearingDesk} runs.
     *
     * @return the case file number
     * @throws IllegalStateException when the request was ruled in the meantime
     */
    String admit(DemandRequest request, User clerk, LocalDateTime ruledAt);

    /** Records the desk the hearings service assigned to an open case file. */
    void assignHearingDesk(String caseFileNumber, String deskName, LocalDateTime assignedAt);

    /**
     * Rules a pending request as inadmissible with the reason given to the plaintiff.
     *
     * @throws IllegalStateException when the request was ruled in the meantime
     */
    void reject(DemandRequest request, String reason, User clerk, LocalDateTime ruledAt);
}
