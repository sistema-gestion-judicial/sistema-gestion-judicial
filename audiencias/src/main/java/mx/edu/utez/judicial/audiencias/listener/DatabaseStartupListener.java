package mx.edu.utez.judicial.audiencias.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.audiencias.api.DAO.AudienciasDaos;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.SqlMigrator;

/**
 * Aplica los scripts de migracion de la base de datos de Audiencias al arrancar la aplicación.
 */
@WebListener
public class DatabaseStartupListener implements ServletContextListener {

    static final List<String> MIGRATIONS = List.of(
        "V001__create_audiencias_tables.sql"
    );

    private static final Logger LOG = Logger.getLogger(DatabaseStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        AudienciasDaos.database().ifPresentOrElse(this::prepare,
            () -> LOG.info("AUDIENCIAS_DB_URL no está configurada: migraciones omitidas"));
    }

    private void prepare(Database database) {
        try {
            new SqlMigrator(database, getClass().getClassLoader()).migrate("db/migration", MIGRATIONS);
            LOG.info("Base de datos de Audiencias preparada y migraciones aplicadas correctamente.");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "No se pudo preparar la base de datos de Audiencias: " + rootCause(e).getMessage(), e);
            throw new IllegalStateException("No se pudo preparar la base de datos de Audiencias", e);
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
