package mx.edu.utez.judicial.common.db;

import java.util.Optional;

public final class DatabaseConfig {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String user;
    private final String password;
    private final String driverClass;

    public DatabaseConfig(String url, String user, String password, String driverClass) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driverClass = driverClass == null || driverClass.isBlank() ? DEFAULT_DRIVER : driverClass;
    }

    /** Settings for the module whose prefix is given, for example "OFICIALIA". Empty when no URL is set. */
    public static Optional<DatabaseConfig> fromEnvironment(String modulePrefix) {
        String url = read(modulePrefix + "_DB_URL");
        if (url == null) {
            return Optional.empty();
        }
        return Optional.of(new DatabaseConfig(url, read(modulePrefix + "_DB_USER"),
            read(modulePrefix + "_DB_PASSWORD"), read(modulePrefix + "_DB_DRIVER")));
    }

    private static String read(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
