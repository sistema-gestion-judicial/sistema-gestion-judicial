package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.common.db.DatabaseConfig;
import mx.edu.utez.judicial.common.db.SqlMigrator;
import mx.edu.utez.judicial.oficialia.listener.DatabaseStartupListener;

/**
 * The test database for the JDBC tests. They run only when OFICIALIA_TEST_DB_URL (plus _USER and _PASSWORD)
 * points to an EMPTY database used only for tests, for example one created in the local Docker MySQL:
 * every test drops all of its tables.
 */
public final class TestDatabase {

    private TestDatabase() {
    }

    /** A database with no tables at all; the test is skipped when none is configured. */
    public static Database empty() throws Exception {
        DatabaseConfig config = DatabaseConfig.fromEnvironment("OFICIALIA_TEST").orElse(null);
        assumeTrue(config != null, "No test database configured");
        Database database = new Database(config);
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            List<String> tables = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                while (rows.next()) {
                    tables.add(rows.getString(1));
                }
            }
            // The tables reference each other, so the order would matter without this
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : tables) {
                statement.execute("DROP TABLE `" + table + "`");
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        return database;
    }

    /** An empty database with every script the application runs at startup. */
    public static Database migrated() throws Exception {
        Database database = empty();
        migrate(database, DatabaseStartupListener.MIGRATIONS);
        return database;
    }

    public static void migrate(Database database, List<String> scripts) throws Exception {
        new SqlMigrator(database, TestDatabase.class.getClassLoader()).migrate("db/migration", scripts);
    }
}
