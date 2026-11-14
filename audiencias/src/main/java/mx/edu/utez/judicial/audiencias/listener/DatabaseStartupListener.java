package mx.edu.utez.judicial.audiencias.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.audiencias.DAO.HearingsDaos;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.SqlMigrator;

/**
 * Apply the Audiences database migration scripts when the application starts.
 */
@WebListener
public class DatabaseStartupListener implements ServletContextListener {

    static final List<String> MIGRATIONS = List.of(
        "V001__create_audiencias_tables.sql"
    );

    private static final Logger LOG = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        HearingsDaos.database().ifPresentOrElse(this::prepare,
            () -> LOG.info("AUDIENCIAS_DB_URL is not configured: migrations skipped"));
    }

    private void prepare(Database database) {
        try {
            new SqlMigrator(database, getClass().getClassLoader()).migrate("db/migration", MIGRATIONS);
            LOG.info("Audiences database prepared and migrations applied correctly.");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "The Audiences database could not be prepared: " + rootCause(e).getMessage(), e);
            throw new IllegalStateException("The Audiences database could not be prepared.", e);
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
