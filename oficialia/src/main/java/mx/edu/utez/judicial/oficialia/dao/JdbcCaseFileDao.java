package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.CaseFileState;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Reads and changes the state columns of case_files (V006, V009).
 */
public class JdbcCaseFileDao implements CaseFileDao {

    private static final String SELECT_STATES = "SELECT number, first_instance_resolved_at, instance, chamber_code, "
        + "chamber_name, second_instance_at, concluded_at FROM case_files";

    private final Database database;

    public JdbcCaseFileDao(Database database) {
        this.database = database;
    }

    @Override
    public Map<String, CaseFileState> findStates() {
        Map<String, CaseFileState> states = new HashMap<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STATES);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                CaseFileState state = toState(rows);
                states.put(state.number(), state);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the case files", e);
        }
        return states;
    }

    @Override
    public Optional<CaseFileState> findState(String number) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STATES + " WHERE number = ?")) {
            statement.setString(1, number);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toState(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the case file " + number, e);
        }
    }

    @Override
    public boolean recordFirstInstanceResolution(String number, LocalDateTime resolvedAt) {
        return recordDate("first_instance_resolved_at", number, resolvedAt);
    }

    @Override
    public boolean recordConclusion(String number, LocalDateTime concludedAt) {
        return recordDate("concluded_at", number, concludedAt);
    }

    /** COALESCE keeps the first date when the other system reports the same event again. */
    private boolean recordDate(String column, String number, LocalDateTime at) {
        String sql = "UPDATE case_files SET " + column + " = COALESCE(" + column + ", ?) WHERE number = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(at));
            statement.setString(2, number);
            // Connector/J reports the matched rows (useAffectedRows=false is the default), so a repeat still counts
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the case file " + number, e);
        }
    }

    /** The case file row is locked, so two clerks cannot give it two chambers. */
    @Override
    public void moveToSecondInstance(String number, String chamberCode, String chamberName, User clerk,
                                     LocalDateTime at) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String courtCode;
                try (PreparedStatement statement = connection.prepareStatement(
                         "SELECT court_code, chamber_code FROM case_files WHERE number = ? FOR UPDATE")) {
                    statement.setString(1, number);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) {
                            throw new IllegalStateException("No case file has the number " + number);
                        }
                        if (rows.getString("chamber_code") != null) {
                            throw new IllegalStateException("The case file " + number + " already has a chamber");
                        }
                        courtCode = rows.getString("court_code");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                         "UPDATE case_files SET instance = 'SECOND', chamber_code = ?, chamber_name = ?, "
                             + "second_instance_at = COALESCE(second_instance_at, ?) WHERE number = ?")) {
                    statement.setString(1, chamberCode);
                    statement.setString(2, chamberName);
                    statement.setTimestamp(3, Timestamp.valueOf(at));
                    statement.setString(4, number);
                    statement.executeUpdate();
                }
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("chamberCode", chamberCode);
                details.put("chamberName", chamberName);
                AuditLog.record(connection, clerk, courtCode, "CASE_FILE_SECOND_INSTANCE", "CASE_FILE", number,
                    chamberCode == null ? "Sala pendiente: el sistema de tocas no respondió" : null, details);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not move the case file " + number + " to second instance", e);
        }
    }

    private static CaseFileState toState(ResultSet rows) throws SQLException {
        return new CaseFileState(rows.getString("number"), dateTime(rows, "first_instance_resolved_at"),
            "SECOND".equals(rows.getString("instance")), rows.getString("chamber_code"),
            rows.getString("chamber_name"), dateTime(rows, "second_instance_at"), dateTime(rows, "concluded_at"));
    }

    private static LocalDateTime dateTime(ResultSet rows, String column) throws SQLException {
        Timestamp value = rows.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
