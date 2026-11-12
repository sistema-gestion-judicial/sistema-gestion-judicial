package mx.edu.utez.judicial.oficialia.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.SqlMigrator;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.JdbcDemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.JdbcUserDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * When a database is configured, brings its schema up to date before the first request, creates the
 * first user accounts when there are none ({@link InitialAccounts}) and, if it has no requests yet, fills
 * it with the demo requests (unless OFICIALIA_SAMPLE_DATA=false).
 */
@WebListener
public class DatabaseStartupListener implements ServletContextListener {

    /** Schema scripts in the order they run. Add new ones at the end; never edit one already pushed. */
    public static final List<String> MIGRATIONS = List.of(
        "V001__create_catalogs.sql",
        "V002__create_demand_requests.sql",
        "V003__add_judicial_districts_and_court_details.sql",
        "V004__add_matter_codes_and_rejection_reasons.sql",
        "V005__create_users.sql");

    private static final Logger LOG = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        OficialiaDaos.database().ifPresentOrElse(this::prepare,
            () -> LOG.info("OFICIALIA_DB_URL is not set: using in-memory sample data"));
    }

    private void prepare(Database database) {
        try {
            new SqlMigrator(database, getClass().getClassLoader()).migrate("db/migration", MIGRATIONS);
            new InitialAccounts((JdbcUserDao) OficialiaDaos.users(), System::getenv).createIfEmpty();
            JdbcDemandRequestDao requests = (JdbcDemandRequestDao) OficialiaDaos.demandRequests();
            if (requests.count() == 0 && !"false".equalsIgnoreCase(System.getenv("OFICIALIA_SAMPLE_DATA"))) {
                for (DemandRequest request : InMemoryDemandRequestDao.sampleRequests()) {
                    requests.insert(request);
                }
                LOG.info("Loaded the sample demand requests into the database");
            }
        } catch (Exception e) {
            // Logged here too, so the cause shows up in the server console and not only in Tomcat's files
            LOG.log(Level.SEVERE, "Could not prepare the Oficialía database: " + rootCause(e).getMessage(), e);
            // Fail the deployment: running against a half-built schema would corrupt data
            throw new IllegalStateException("Could not prepare the Oficialía database", e);
        }
    }

    private static Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
