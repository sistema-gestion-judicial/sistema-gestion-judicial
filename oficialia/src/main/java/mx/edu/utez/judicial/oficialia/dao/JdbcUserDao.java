package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import mx.edu.utez.judicial.oficialia.model.UserUpdate;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Reads, creates and corrects the accounts of the users table (V005).
 */
public class JdbcUserDao implements UserDao {

    /** ER_DUP_ENTRY: a UNIQUE key (here the e-mail) already has the value. */
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

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
        String sql = "SELECT u.email, u.first_name, u.last_name, u.role, u.password_hash, u.active, "
            + "u.must_change_password, c.name AS court_name, c.active AS court_active FROM users u "
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
                User user = new User(rows.getString("email"),
                    rows.getString("first_name") + " " + rows.getString("last_name"),
                    UserRole.valueOf(rows.getString("role")), courtName, rows.getBoolean("must_change_password"));
                return Optional.of(new UserAccount(user, rows.getString("password_hash"),
                    rows.getBoolean("active") && courtActive));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the user account", e);
        }
    }

    @Override
    public List<UserSummary> findByCourt(String courtName) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.phone, u.role, u.active FROM users u "
            + "JOIN courts c ON c.code = u.court_code WHERE c.name = ? ORDER BY u.first_name, u.last_name";
        List<UserSummary> users = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courtName);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    users.add(new UserSummary(rows.getLong("id"), rows.getString("first_name") + " " + rows.getString("last_name"),
                        rows.getString("email"), rows.getString("phone"), UserRole.valueOf(rows.getString("role")),
                        rows.getBoolean("active")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the users of the court", e);
        }
        return users;
    }

    /**
     * Replaces the password of an account. The user just chose it, so it no longer has to be changed at the next
     * sign in. Does nothing when the e-mail is unknown.
     */
    @Override
    public void updatePasswordHash(String email, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ?, must_change_password = FALSE WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, EmailValidator.normalize(email));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the password", e);
        }
    }

    @Override
    public Optional<UserDetails> findById(long id) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.phone, u.role, u.active, c.name AS court_name "
            + "FROM users u LEFT JOIN courts c ON c.code = u.court_code WHERE u.id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserDetails(rows.getLong("id"), rows.getString("first_name"),
                    rows.getString("last_name"), rows.getString("email"), rows.getString("phone"),
                    UserRole.valueOf(rows.getString("role")), rows.getString("court_name"), rows.getBoolean("active")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read user " + id, e);
        }
    }

    /**
     * Locks the row, writes the audit record with the values before and after, and then the new values, all in
     * one transaction (DFR 3.3.9, business rule 5). The record goes first because its author is looked up by
     * e-mail, which may be the one this same update changes. Nothing is written when no value changed.
     */
    @Override
    public void update(UserUpdate update, User actor) {
        String email = EmailValidator.normalize(update.email());
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Map<String, Object> before;
                String courtCode;
                try (PreparedStatement statement = connection.prepareStatement("SELECT first_name, last_name, email, "
                    + "phone, role, court_code FROM users WHERE id = ? FOR UPDATE")) {
                    statement.setLong(1, update.id());
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) {
                            connection.rollback();
                            return;
                        }
                        before = values(rows.getString("first_name"), rows.getString("last_name"),
                            rows.getString("email"), rows.getString("phone"), rows.getString("role"));
                        courtCode = rows.getString("court_code");
                    }
                }
                Map<String, Object> changes = changes(before, values(update.firstName(), update.lastName(), email,
                    update.phone(), update.role().name()));
                if (changes.isEmpty()) {
                    connection.rollback();
                    return;
                }
                AuditLog.record(connection, actor, courtCode, "USER_UPDATED", "USER", String.valueOf(update.id()),
                    null, changes);
                try (PreparedStatement statement = connection.prepareStatement("UPDATE users SET first_name = ?, "
                    + "last_name = ?, email = ?, phone = ?, role = ? WHERE id = ?")) {
                    statement.setString(1, update.firstName());
                    statement.setString(2, update.lastName());
                    statement.setString(3, email);
                    if (update.phone() == null) {
                        statement.setNull(4, Types.VARCHAR);
                    } else {
                        statement.setString(4, update.phone());
                    }
                    statement.setString(5, update.role().name());
                    statement.setLong(6, update.id());
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Another account took the e-mail after the service checked it; the UNIQUE key decides
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new DuplicateEmailException(email, e);
            }
            throw new DataAccessException("Could not update user " + update.id(), e);
        } catch (SQLException e) {
            throw new DataAccessException("Could not update user " + update.id(), e);
        }
    }

    /**
     * Locks the row, writes the audit record and then the new status, in one transaction (DFR 3.3.9, business
     * rule 5). Signed-in sessions of a deactivated user end on their next request, because AuthFilter checks the
     * status every time.
     */
    @Override
    public boolean updateStatus(long id, boolean active, String reason, User actor) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String courtCode;
                try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT active, court_code FROM users WHERE id = ? FOR UPDATE")) {
                    statement.setLong(1, id);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next() || rows.getBoolean("active") == active) {
                            connection.rollback();
                            return false;
                        }
                        courtCode = rows.getString("court_code");
                    }
                }
                AuditLog.record(connection, actor, courtCode, active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "USER",
                    String.valueOf(id), reason, Map.of("active", active));
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET active = ? WHERE id = ?")) {
                    statement.setBoolean(1, active);
                    statement.setLong(2, id);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not change the status of user " + id, e);
        }
    }

    private static Map<String, Object> values(String firstName, String lastName, String email, String phone,
                                              String role) {
        // LinkedHashMap because the phone may be null, which Map.of refuses
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("role", role);
        return values;
    }

    /** Each changed field with its value before and after, for the audit record. */
    private static Map<String, Object> changes(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> changes = new LinkedHashMap<>();
        before.forEach((field, oldValue) -> {
            Object newValue = after.get(field);
            if (!Objects.equals(oldValue, newValue)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("from", oldValue);
                change.put("to", newValue);
                changes.put(field, change);
            }
        });
        return changes;
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

    /**
     * Stores the e-mail normalized, the same way sign in looks it up.
     *
     * @throws DuplicateEmailException when another account already has that e-mail
     */
    @Override
    public void insert(NewUserAccount account) {
        String sql = "INSERT INTO users (first_name, last_name, email, phone, role, court_code, password_hash, "
            + "must_change_password, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.firstName());
            statement.setString(2, account.lastName());
            statement.setString(3, EmailValidator.normalize(account.email()));
            if (account.phone() == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, account.phone());
            }
            statement.setString(5, account.role().name());
            if (account.courtCode() == null) {
                statement.setNull(6, Types.CHAR);
            } else {
                statement.setString(6, account.courtCode());
            }
            statement.setString(7, account.passwordHash());
            statement.setBoolean(8, account.mustChangePassword());
            statement.setBoolean(9, account.active());
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            // Two registrations of the same e-mail can pass the existence check together; the UNIQUE key decides
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new DuplicateEmailException(account.email(), e);
            }
            throw new DataAccessException("Could not create the user account " + account.email(), e);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the user account " + account.email(), e);
        }
    }
}
