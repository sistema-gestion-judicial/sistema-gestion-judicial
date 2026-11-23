
package mx.edu.utez.judicial.audiencias.dao.hearing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.edu.utez.judicial.audiencias.dao.DataAccessException;
import mx.edu.utez.judicial.audiencias.model.hearing.Hearing;
import mx.edu.utez.judicial.audiencias.model.hearing.HearingStatus;
import mx.edu.utez.judicial.common.db.Database;

public class JdbcHearingDao implements HearingDao {

    private static final String SELECT = "SELECT id, hearing_date, status, desk_id, case_id, lawyer_id, "
        + "courtroom, notes FROM hearings";

    private final Database database;

    public JdbcHearingDao(Database database) {
        this.database = database;
    }

    @Override
    public Optional<Hearing> findById(int id) {
        return find(SELECT + " WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Hearing> findByCaseId(int caseId) {
        return find(SELECT + " WHERE case_id = ? ORDER BY hearing_date", caseId);
    }

    @Override
    public List<Hearing> findByDeskId(int deskId) {
        return find(SELECT + " WHERE desk_id = ? ORDER BY hearing_date", deskId);
    }

    @Override
    public List<Hearing> findByLawyerId(int lawyerId) {
        return find(SELECT + " WHERE lawyer_id = ? ORDER BY hearing_date", lawyerId);
    }

    @Override
    public List<Hearing> findByStatus(HearingStatus status) {
        return find(SELECT + " WHERE status = ? ORDER BY hearing_date", status.name());
    }

    @Override
    public List<Hearing> findScheduledBetween(LocalDateTime from, LocalDateTime to) {
        String sql = SELECT + " WHERE hearing_date BETWEEN ? AND ? ORDER BY hearing_date";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rows = statement.executeQuery()) {
                return readAll(rows);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the agenda", e);
        }
    }

    @Override
    public int insert(Hearing hearing) {
        String sql = "INSERT INTO hearings (hearing_date, status, desk_id, case_id, lawyer_id, courtroom, notes) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableTimestamp(statement, 1, hearing.getHearingDate());
            statement.setString(2, hearing.getStatus().name());
            setNullableInt(statement, 3, hearing.getDeskId());
            setNullableInt(statement, 4, hearing.getCaseId());
            setNullableInt(statement, 5, hearing.getLawyerId());
            setNullableString(statement, 6, hearing.getCourtroom());
            setNullableString(statement, 7, hearing.getNotes());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the hearing", e);
        }
    }

    @Override
    public void schedule(int id, LocalDateTime hearingDate, String courtroom) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE hearings SET hearing_date = ?, courtroom = ?, status = ? WHERE id = ?")) {
            statement.setTimestamp(1, Timestamp.valueOf(hearingDate));
            setNullableString(statement, 2, courtroom);
            statement.setString(3, HearingStatus.IN_PROGRESS.name());
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not schedule hearing " + id, e);
        }
    }

    @Override
    public void changeStatus(int id, HearingStatus status) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement("UPDATE hearings SET status = ? WHERE id = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not change the status of hearing " + id, e);
        }
    }

    @Override
    public void recordNotes(int id, String notes) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement("UPDATE hearings SET notes = ? WHERE id = ?")) {
            setNullableString(statement, 1, notes);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the notes of hearing " + id, e);
        }
    }

    private List<Hearing> find(String sql, Object parameter) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter instanceof Integer value) {
                statement.setInt(1, value);
            } else if (parameter instanceof String value) {
                statement.setString(1, value);
            }
            try (ResultSet rows = statement.executeQuery()) {
                return readAll(rows);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the hearings", e);
        }
    }

    private static List<Hearing> readAll(ResultSet rows) throws SQLException {
        List<Hearing> hearings = new ArrayList<>();
        while (rows.next()) {
            hearings.add(toHearing(rows));
        }
        return hearings;
    }

    private static void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static void setNullableTimestamp(PreparedStatement statement, int index, LocalDateTime value)
        throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private static Hearing toHearing(ResultSet rows) throws SQLException {
        Timestamp hearingDate = rows.getTimestamp("hearing_date");
        return new Hearing(rows.getInt("id"), hearingDate == null ? null : hearingDate.toLocalDateTime(),
            HearingStatus.valueOf(rows.getString("status")), rows.getObject("desk_id", Integer.class),
            rows.getObject("case_id", Integer.class), rows.getObject("lawyer_id", Integer.class),
            rows.getString("courtroom"), rows.getString("notes"));
    }
}
