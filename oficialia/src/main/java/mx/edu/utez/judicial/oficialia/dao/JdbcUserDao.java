package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Reads and creates the accounts of the users table (V005).
 */
public class JdbcUserDao implements UserDao {

    private final Database database;

    public JdbcUserDao(Database database) {
        this.database = database;
    }

    /**
     * An account counts as active only when the user and their court are both active: the users of a
     * deactivated court cannot sign in (DFR 2.12).
     */
    @Override
    public Optional<UserAccount> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String sql = "SELECT u.id, u.email, u.first_name, u.last_name, u.role, u.password_hash, u.active, "
            + "c.name AS court_name, c.active AS court_active FROM users u "
            + "LEFT JOIN courts c ON c.code = u.court_code WHERE u.email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, EmailValidator.normalize(email));
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                String courtName = rows.getString("court_name");
                boolean courtActive = courtName == null || rows.getBoolean("court_active");
                User user = new User(rows.getLong("id"), rows.getString("email"),
                    rows.getString("first_name") + " " + rows.getString("last_name"),
                    UserRole.valueOf(rows.getString("role")), courtName);
                return Optional.of(new UserAccount(user, rows.getString("password_hash"),
                    rows.getBoolean("active") && courtActive));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the user account", e);
        }
    }

    public int count() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count users", e);
        }
    }

    /** Stores the e-mail normalized, the same way sign in looks it up. */
    public void insert(NewUserAccount account) {
        String sql = "INSERT INTO users (first_name, last_name, email, role, court_code, password_hash, "
            + "must_change_password, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.firstName());
            statement.setString(2, account.lastName());
            statement.setString(3, EmailValidator.normalize(account.email()));
            statement.setString(4, account.role().name());
            if (account.courtCode() == null) {
                statement.setNull(5, Types.CHAR);
            } else {
                statement.setString(5, account.courtCode());
            }
            statement.setString(6, account.passwordHash());
            statement.setBoolean(7, account.mustChangePassword());
            statement.setBoolean(8, account.active());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the user account " + account.email(), e);
        }
    }
}
