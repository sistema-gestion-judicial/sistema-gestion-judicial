// JdbcDeskDao.java  — nota el catch de SQLIntegrityConstraintViolationException (el UNIQUE user_id)
package mx.edu.utez.judicial.audiencias.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.audiencias.model.Desk;
import mx.edu.utez.judicial.common.db.Database;

public class JdbcDeskDao implements DeskDao {

    private static final String SELECT =
        "SELECT id, desk_name, matter, active, user_id, court_id FROM desks";

    private final Database database;

    public JdbcDeskDao(Database database) {
        this.database = database;
    }

    @Override
    public Optional<Desk> findById(int id) {
        return find(SELECT + " WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Desk> findAll() {
        return find(SELECT + " ORDER BY desk_name", null);
    }

    @Override
    public List<Desk> findActive() {
        return find(SELECT + " WHERE active = 1 ORDER BY desk_name", null);
    }

    @Override
    public List<Desk> findActiveByCourtAndMatter(String courtId, String matter) {
        String sql = SELECT + " WHERE active = 1 AND court_id = ? AND matter = ? ORDER BY desk_name";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courtId);
            statement.setString(2, matter);
            try (ResultSet rows = statement.executeQuery()) {
                return readAll(rows);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the desks of court " + courtId, e);
        }
    }

    @Override
    public Optional<Desk> findByUserId(int userId) {
        return find(SELECT + " WHERE user_id = ?", userId).stream().findFirst();
    }

    @Override
    public int insert(Desk desk) {
        String sql = "INSERT INTO desks (desk_name, matter, active, user_id, court_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, desk.getDeskName());
            statement.setString(2, desk.getMatter());
            statement.setBoolean(3, desk.isActive());
            setNullableInt(statement, 4, desk.getUserId());
            setNullableString(statement, 5, desk.getCourtId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("That lawyer already heads another desk", e);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the desk " + desk.getDeskName(), e);
        }
    }

    @Override
    public void assignUser(int deskId, Integer userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement("UPDATE desks SET user_id = ? WHERE id = ?")) {
            setNullableInt(statement, 1, userId);
            statement.setInt(2, deskId);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("That lawyer already heads another desk", e);
        } catch (SQLException e) {
            throw new DataAccessException("Could not assign the lawyer to desk " + deskId, e);
        }
    }

    @Override
    public void setActive(int deskId, boolean active) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement("UPDATE desks SET active = ? WHERE id = ?")) {
            statement.setBoolean(1, active);
            statement.setInt(2, deskId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not change the status of desk " + deskId, e);
        }
    }

    private List<Desk> find(String sql, Object parameter) {
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
            throw new DataAccessException("Could not read the desks", e);
        }
    }

    private static List<Desk> readAll(ResultSet rows) throws SQLException {
        List<Desk> desks = new ArrayList<>();
        while (rows.next()) {
            desks.add(toDesk(rows));
        }
        return desks;
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

    private static Desk toDesk(ResultSet rows) throws SQLException {
        return new Desk(rows.getInt("id"), rows.getString("desk_name"), rows.getString("matter"),
            rows.getBoolean("active"), rows.getObject("user_id", Integer.class), rows.getString("court_id"));
    }
}
