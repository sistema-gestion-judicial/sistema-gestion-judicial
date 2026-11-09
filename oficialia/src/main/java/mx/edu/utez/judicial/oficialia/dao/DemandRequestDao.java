package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * Access to the demand requests of every court. {@link JdbcDemandRequestDao} reads MySQL;
 * {@link InMemoryDemandRequestDao} keeps sample data when no database is configured.
 */
public interface DemandRequestDao {

    List<DemandRequest> findAll();

    List<DemandRequest> findByCourt(String courtName);

    Optional<DemandRequest> findByFolio(String folio);

    /** Saves a new request with its parties and annexes. */
    void insert(DemandRequest demandRequest);

    /** Saves the changes made to a request. */
    void update(DemandRequest demandRequest);
}
