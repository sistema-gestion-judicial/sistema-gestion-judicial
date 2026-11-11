package mx.edu.utez.judicial.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    private final DatabaseConfig config;

    public Database(DatabaseConfig config) {
        this.config = config;
        try {
            // Inside Tomcat the driver in WEB-INF/lib is not registered automatically
            Class.forName(config.getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver not found: " + config.getDriverClass(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    public boolean isReachable() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
