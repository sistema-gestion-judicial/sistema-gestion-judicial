package mx.edu.utez.judicial.common.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Applies numbered SQL scripts once, in order, and records them in a schema_history table.
 * A script that already ran is never run again, so a change to the schema always goes in a new script.
 */
public class SqlMigrator {

    private static final Logger LOG = Logger.getLogger(SqlMigrator.class.getName());

    private final Database database;
    private final ClassLoader classLoader;

    public SqlMigrator(Database database, ClassLoader classLoader) {
        this.database = database;
        this.classLoader = classLoader;
    }

    /**
     * Runs the scripts that have not run yet.
     *
     * @param folder classpath folder with the scripts, for example "db/migration"
     * @param scripts file names in the order they must run, for example "V001__create_catalogs.sql"
     */
    public void migrate(String folder, List<String> scripts) throws SQLException, IOException {
        try (Connection connection = database.getConnection()) {
            createHistoryTable(connection);
            Set<String> applied = appliedScripts(connection);
            for (String script : scripts) {
                if (applied.contains(script)) {
                    continue;
                }
                LOG.info("Applying " + script);
                runScript(connection, read(folder + "/" + script));
                try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO schema_history (script) VALUES (?)")) {
                    insert.setString(1, script);
                    insert.executeUpdate();
                }
            }
        }
    }

    private static void createHistoryTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS schema_history ("
                + "script VARCHAR(150) PRIMARY KEY, "
                + "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private static Set<String> appliedScripts(Connection connection) throws SQLException {
        Set<String> applied = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT script FROM schema_history")) {
            while (rows.next()) {
                applied.add(rows.getString(1));
            }
        }
        return applied;
    }

    private String read(String path) throws IOException {
        try (InputStream input = classLoader.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Migration script not found on the classpath: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void runScript(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String command : split(sql)) {
                statement.execute(command);
            }
        }
    }

    static List<String> split(String sql) {
        List<String> commands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String command = current.toString().trim();
                commands.add(command.substring(0, command.length() - 1));
                current.setLength(0);
            }
        }
        if (!current.toString().isBlank()) {
            commands.add(current.toString().trim());
        }
        return commands;
    }
}
