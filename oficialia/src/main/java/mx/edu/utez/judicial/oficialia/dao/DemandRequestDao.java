package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * Access to the demand requests of every court. The in-memory version is replaced by a JDBC one
 * when the database schema exists (#28); services do not change.
 */
public interface DemandRequestDao {

    List<DemandRequest> findAll();

    List<DemandRequest> findByCourt(String courtName);

    Optional<DemandRequest> findByFolio(String folio);

    /** Saves the changes made to a request. */
    void update(DemandRequest demandRequest);
}
