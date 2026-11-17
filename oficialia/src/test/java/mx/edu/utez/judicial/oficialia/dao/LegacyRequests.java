package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * Saves requests the way the application did before V006: only demand_requests, parties and annexes.
 * The upgrade tests use it to fill a database that does not have the newer tables yet.
 */
final class LegacyRequests {

    private LegacyRequests() {
    }

    static void insert(Database database, DemandRequest request) {
        try (Connection connection = database.getConnection()) {
            JdbcDemandRequestDao.insertRequest(connection, request);
            JdbcDemandRequestDao.insertParty(connection, request.getFolio(), request.getPlaintiff());
            JdbcDemandRequestDao.insertParty(connection, request.getFolio(), request.getDefendant());
            for (Annex annex : request.getAnnexes()) {
                JdbcDemandRequestDao.insertAnnex(connection, request.getFolio(), annex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not save " + request.getFolio(), e);
        }
    }
}
