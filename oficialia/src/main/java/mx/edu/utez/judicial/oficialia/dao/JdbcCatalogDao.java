package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Reads the catalogs from the sgj_oficialia database.
 */
public class JdbcCatalogDao implements CatalogDao {

    /** ER_DUP_ENTRY: a UNIQUE key already has the value. */
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private static final String SELECT_DISTRICTS = "SELECT code, name, seat_municipality, active FROM judicial_districts";

    private static final String SELECT_COURTS = "SELECT c.code, c.number, c.name, c.active, c.street, "
        + "c.neighborhood, c.postal_code, c.municipality, c.phone, d.code AS district_code, "
        + "d.name AS district_name FROM courts c JOIN judicial_districts d ON d.code = c.district_code";

    private final Database database;

    public JdbcCatalogDao(Database database) {
        this.database = database;
    }

    @Override
    public List<JudicialDistrict> findJudicialDistricts() {
        return queryDistricts(SELECT_DISTRICTS + " ORDER BY name", null);
    }

    @Override
    public Optional<JudicialDistrict> findJudicialDistrict(String code) {
        return queryDistricts(SELECT_DISTRICTS + " WHERE code = ?", code).stream().findFirst();
    }

    /** The primary key is the code and the other UNIQUE key the name, so the message tells which one clashed. */
    @Override
    public void insertJudicialDistrict(JudicialDistrict district) {
        String sql = "INSERT INTO judicial_districts (code, name, seat_municipality, active) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, district.getCode());
            statement.setString(2, district.getName());
            statement.setString(3, district.getSeatMunicipality());
            statement.setBoolean(4, district.isActive());
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                boolean codeClashed = e.getMessage() != null && e.getMessage().contains("PRIMARY");
                throw codeClashed ? new DuplicateCodeException(district.getCode(), e)
                    : new DuplicateNameException(district.getName(), e);
            }
            throw new DataAccessException("Could not save the judicial district " + district.getCode(), e);
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the judicial district " + district.getCode(), e);
        }
    }

    @Override
    public List<Court> findCourts() {
        return queryCourts(SELECT_COURTS + " ORDER BY c.name", null);
    }

    @Override
    public Optional<Court> findCourt(String code) {
        return queryCourts(SELECT_COURTS + " WHERE c.code = ?", code).stream().findFirst();
    }

    @Override
    public List<SubjectMatter> findSubjectMatters() {
        List<SubjectMatter> matters = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, code, name, description, active FROM subject_matters ORDER BY name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                matters.add(new SubjectMatter(rows.getInt("id"), rows.getString("code"), rows.getString("name"),
                    rows.getString("description"), rows.getBoolean("active")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read subject matters", e);
        }
        return matters;
    }

    private List<JudicialDistrict> queryDistricts(String sql, String code) {
        List<JudicialDistrict> districts = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (code != null) {
                statement.setString(1, code);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    districts.add(new JudicialDistrict(rows.getString("code"), rows.getString("name"),
                        rows.getString("seat_municipality"), rows.getBoolean("active")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read judicial districts", e);
        }
        return districts;
    }

    private List<Court> queryCourts(String sql, String code) {
        List<Court> courts = new ArrayList<>();
        try (Connection connection = database.getConnection()) {
            Map<String, List<String>> mattersByCourt = subjectMatterCodesByCourt(connection);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (code != null) {
                    statement.setString(1, code);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        String courtCode = rows.getString("code");
                        CourtAddress address = new CourtAddress(rows.getString("street"),
                            rows.getString("neighborhood"), rows.getString("postal_code"),
                            rows.getString("municipality"), rows.getString("phone"));
                        courts.add(new Court(courtCode, rows.getInt("number"), rows.getString("name"),
                            rows.getString("district_code"), rows.getString("district_name"), address,
                            mattersByCourt.getOrDefault(courtCode, List.of()), rows.getBoolean("active")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read courts", e);
        }
        return courts;
    }

    /** One query for every court, so listing the catalog does not cost one query per court. */
    private static Map<String, List<String>> subjectMatterCodesByCourt(Connection connection) throws SQLException {
        Map<String, List<String>> codes = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                 "SELECT cm.court_code, m.code FROM court_subject_matters cm "
                     + "JOIN subject_matters m ON m.id = cm.matter_id ORDER BY m.code");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                codes.computeIfAbsent(rows.getString("court_code"), key -> new ArrayList<>()).add(rows.getString("code"));
            }
        }
        return codes;
    }
}
