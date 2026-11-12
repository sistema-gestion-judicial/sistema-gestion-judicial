package mx.edu.utez.judicial.common.dao;

import mx.edu.utez.judicial.common.service.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Plain JDBC connection factory (ready for when the database exists; see sql/schema.sql). */
public final class DbConnection {
    private DbConnection() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                AppConfig.get("db.url", ""), AppConfig.get("db.user", ""), AppConfig.get("db.password", ""));
    }
}
