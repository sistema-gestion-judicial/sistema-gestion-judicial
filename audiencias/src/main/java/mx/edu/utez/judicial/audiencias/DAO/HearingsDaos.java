package mx.edu.utez.judicial.audiencias.DAO;

import java.util.Optional;
import mx.edu.utez.judicial.audiencias.DAO.usuario.JdbcUserDao;
import mx.edu.utez.judicial.audiencias.DAO.usuario.UserDao;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.DatabaseConfig;

/**
 * Provee acceso centralizado a la base de datos de Audiencias y a sus Daos.
 * Lee las variables de entorno AUDIENCIAS_DB_URL, AUDIENCIAS_DB_USER, AUDIENCIAS_DB_PASSWORD.
 */
public final class HearingsDaos {

    private static final Optional<Database> DATABASE =
        DatabaseConfig.fromEnvironment("AUDIENCIAS").map(Database::new);

    private static final UserDao USUARIO_DAO = DATABASE
        .<UserDao>map(JdbcUserDao::new)
        .orElse(null);

    private HearingsDaos() {
    }

    /** Retorna la instancia de base de datos cuando AUDIENCIAS_DB_URL está configurada. */
    public static Optional<Database> database() {
        return DATABASE;
    }

    /** Retorna el DAO de usuarios conectado a MySQL. */
    public static UserDao usuarioDao() {
        if (USUARIO_DAO == null) {
            throw new IllegalStateException("AUDIENCIAS_DB_URL no está configurada.");
        }
        return USUARIO_DAO;
    }
}
