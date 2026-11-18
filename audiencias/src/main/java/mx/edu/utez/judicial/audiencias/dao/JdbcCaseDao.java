package mx.edu.utez.judicial.audiencias.dao;

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
import mx.edu.utez.judicial.audiencias.model.Case;
import mx.edu.utez.judicial.common.db.Database;

public class JdbcCaseDao implements CaseDao {

    private static final String SELECT = "SELECT id, case_number, court_id, matter, defendant, "
        + "plaintiff, status, created_at, closed_at FROM cases";

    private final Database database;

    public JdbcCaseDao(Database database) {
        this.database = database;
    }

    @Override
    public Optional<Case> findById(int id) {
        return find(SELECT + " WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public Optional<Case> findByCaseNumber(String caseNumber) {
        if (caseNumber == null) {
            return Optional.empty();
        }
        return find(SELECT + " WHERE case_number = ?", caseNumber.trim()).stream().findFirst();
    }

    @Override
    public List<Case> findAll() {
        return find(SELECT + " ORDER BY created_at DESC", null);
    }

    @Override
    public List<Case> findActive() {
        return find(SELECT + " WHERE status = 1 ORDER BY created_at DESC", null);
    }

    @Override
    public List<Case> findByCourtAndMatter(String courtId, String matter) {
        String sql = SELECT + " WHERE court_id = ? AND matter = ? ORDER BY created_at DESC";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courtId);
            statement.setString(2, matter);
            try (ResultSet rows = statement.executeQuery()) {
                return readAll(rows);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the cases of court " + courtId, e);
        }
    }

    @Override
    public int insert(Case caseEntity) {
        String sql = "INSERT INTO cases (case_number, court_id, matter, defendant, plaintiff, status) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, caseEntity.getCaseNumber());
            statement.setString(2, caseEntity.getCourtId());
            statement.setString(3, caseEntity.getMatter());
            setNullableString(statement, 4, caseEntity.getDefendant());
            setNullableString(statement, 5, caseEntity.getPlaintiff());
            statement.setBoolean(6, caseEntity.isStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the case " + caseEntity.getCaseNumber(), e);
        }
    }

    @Override
    public void close(Case caseEntity, LocalDateTime closedAt) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement("UPDATE cases SET status = 0, closed_at = ? WHERE id = ?")) {
            statement.setTimestamp(1, Timestamp.valueOf(closedAt));
            statement.setInt(2, caseEntity.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not close the case " + caseEntity.getId(), e);
        }
    }

    private List<Case> find(String sql, Object parameter) {
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
            throw new DataAccessException("Could not read the cases", e);
        }
    }

    private static List<Case> readAll(ResultSet rows) throws SQLException {
        List<Case> cases = new ArrayList<>();
        while (rows.next()) {
            cases.add(toCase(rows));
        }
        return cases;
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static Case toCase(ResultSet rows) throws SQLException {
        Timestamp closedAt = rows.getTimestamp("closed_at");
        return new Case(rows.getInt("id"), rows.getString("case_number"), rows.getString("court_id"),
            rows.getString("matter"), rows.getString("defendant"), rows.getString("plaintiff"),
            rows.getBoolean("status"), rows.getTimestamp("created_at").toLocalDateTime(),
            closedAt == null ? null : closedAt.toLocalDateTime());
    }
}
