package mx.edu.utez.judicial.oficialia.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Logger;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.SqlMigrator;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.JdbcDemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * When a database is configured, brings its schema up to date before the first request and,
 * if it has no requests yet, fills it with the demo data (unless OFICIALIA_SAMPLE_DATA=false).
 */
@WebListener
public class DatabaseStartupListener implements ServletContextListener {

    /** Schema scripts in the order they run. Add new ones at the end; never edit one already pushed. */
    static final List<String> MIGRATIONS = List.of(
        "V001__create_catalogs.sql",
        "V002__create_demand_requests.sql");

    private static final Logger LOG = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        OficialiaDaos.database().ifPresentOrElse(this::prepare,
            () -> LOG.info("OFICIALIA_DB_URL is not set: using in-memory sample data"));
    }

    private void prepare(Database database) {
        try {
            new SqlMigrator(database, getClass().getClassLoader()).migrate("db/migration", MIGRATIONS);
            JdbcDemandRequestDao requests = (JdbcDemandRequestDao) OficialiaDaos.demandRequests();
            if (requests.count() == 0 && !"false".equalsIgnoreCase(System.getenv("OFICIALIA_SAMPLE_DATA"))) {
                for (DemandRequest request : InMemoryDemandRequestDao.sampleRequests()) {
                    requests.insert(request);
                }
                LOG.info("Loaded the sample demand requests into the database");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not prepare the Oficialía database", e);
        }
    }
}
