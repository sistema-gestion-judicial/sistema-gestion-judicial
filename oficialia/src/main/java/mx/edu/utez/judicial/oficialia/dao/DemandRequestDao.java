package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.CancelledAttempt;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

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

    void update(DemandRequest demandRequest);

    /** Keeps a registration cancelled after a duplicity warning; it has no folio because nothing was saved. */
    void recordCancelledAttempt(CancelledAttempt attempt);
}
