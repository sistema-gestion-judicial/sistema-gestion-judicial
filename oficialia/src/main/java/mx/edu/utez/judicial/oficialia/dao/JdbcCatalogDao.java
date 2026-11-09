package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Reads the catalogs from the sgj_oficialia database.
 */
public class JdbcCatalogDao implements CatalogDao {

    private final Database database;

    public JdbcCatalogDao(Database database) {
        this.database = database;
    }

    @Override
    public List<Court> findCourts() {
        return queryCourts("SELECT code, name, judicial_district, active FROM courts ORDER BY name", null);
    }

    @Override
    public Optional<Court> findCourt(String code) {
        return queryCourts("SELECT code, name, judicial_district, active FROM courts WHERE code = ?", code)
            .stream().findFirst();
    }

    @Override
    public List<SubjectMatter> findSubjectMatters() {
        List<SubjectMatter> matters = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, name, active FROM subject_matters ORDER BY name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                matters.add(new SubjectMatter(rows.getInt("id"), rows.getString("name"), rows.getBoolean("active")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read subject matters", e);
        }
        return matters;
    }

    private List<Court> queryCourts(String sql, String code) {
        List<Court> courts = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (code != null) {
                statement.setString(1, code);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    courts.add(new Court(rows.getString("code"), rows.getString("name"),
                        rows.getString("judicial_district"), rows.getBoolean("active")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read courts", e);
        }
        return courts;
    }
}
