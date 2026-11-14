package mx.edu.utez.judicial.audiencias.DAO;

import java.util.Optional;
import mx.edu.utez.judicial.audiencias.DAO.usuario.JdbcUserDao;
import mx.edu.utez.judicial.audiencias.DAO.usuario.UserDao;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.DatabaseConfig;

/**
 * Provides centralized access to the Audiences database and its DAOs.
 * Reads the environment variables AUDIENCIAS_DB_URL, AUDIENCIAS_DB_USER, and AUDIENCIAS_DB_PASSWORD.
 */
public final class HearingsDaos {

    private static final Optional<Database> DATABASE =
        DatabaseConfig.fromEnvironment("AUDIENCIAS").map(Database::new);

    private static final UserDao USUARIO_DAO = DATABASE
        .<UserDao>map(JdbcUserDao::new)
        .orElse(null);

    private HearingsDaos() {
    }

    /**Returns the database instance when AUDIENCIAS_DB_URL is configured. */
    public static Optional<Database> database() {
        return DATABASE;
    }

    /**Returns the user DAO connected to MySQL. */
    public static UserDao usuarioDao() {
        if (USUARIO_DAO == null) {
            throw new IllegalStateException("AUDIENCIAS_DB_URL no está configurada.");
        }
        return USUARIO_DAO;
    }
}
