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

    void insert(DemandRequest demandRequest);

    void update(DemandRequest demandRequest);
}
