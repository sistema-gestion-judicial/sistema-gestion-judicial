package mx.edu.utez.judicial.oficialia.dao;

import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.DatabaseConfig;

/**
 * Chooses where Oficialía keeps its data: MySQL when OFICIALIA_DB_URL is set, memory otherwise.
 * Every servlet and service gets its DAOs from here, so they all share the same data.
 */
public final class OficialiaDaos {

    private static final Optional<Database> DATABASE =
        DatabaseConfig.fromEnvironment("OFICIALIA").map(Database::new);

    private static final DemandRequestDao DEMAND_REQUESTS = DATABASE
        .<DemandRequestDao>map(JdbcDemandRequestDao::new)
        .orElseGet(InMemoryDemandRequestDao::getInstance);

    private static final CatalogDao CATALOGS = DATABASE
        .<CatalogDao>map(JdbcCatalogDao::new)
        .orElseGet(InMemoryCatalogDao::new);

    private static final UserDao USERS = DATABASE
        .<UserDao>map(JdbcUserDao::new)
        .orElseGet(InMemoryUserDao::getInstance);

    private OficialiaDaos() {
    }

    /** The database, when one is configured. */
    public static Optional<Database> database() {
        return DATABASE;
    }

    public static DemandRequestDao demandRequests() {
        return DEMAND_REQUESTS;
    }

    public static CatalogDao catalogs() {
        return CATALOGS;
    }

    public static UserDao users() {
        return USERS;
    }
}
