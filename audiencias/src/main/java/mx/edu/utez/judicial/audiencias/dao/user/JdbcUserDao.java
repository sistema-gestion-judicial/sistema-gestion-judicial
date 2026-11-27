package mx.edu.utez.judicial.audiencias.dao.user;

import mx.edu.utez.judicial.audiencias.dao.DataAccessException;
import mx.edu.utez.judicial.audiencias.model.user.Role;
import mx.edu.utez.judicial.audiencias.model.user.User;
import mx.edu.utez.judicial.common.db.Database;

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

public class JdbcUserDao implements UserDao {

    private static final String SELECT_WITH_DESK =
        "SELECT u.id, u.full_name, u.email, u.professional_license, u.phone, u.role, u.status, "
            + "u.must_change_password, COALESCE(d.matter, u.matter) AS matter, "
            + "d.id AS desk_id, d.desk_name "
            + "FROM users u LEFT JOIN desks d ON u.id = d.user_id";

    private final Database database;

    public JdbcUserDao(Database database) {
        this.database = database;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, full_name, role, status, must_change_password FROM users WHERE email = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        Role.valueOf(rs.getString("role")),
                        rs.getBoolean("status")
                    );
                    user.setMustChangePassword(rs.getBoolean("must_change_password"));
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    @Override
    public String authenticate(String email) {
        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET professional_license = ?, matter = ?, role = ?, status = ?, "
            + "email = ?, full_name = ?, phone = ? WHERE id = ?";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setNullableString(ps, 1, user.getProfessionalLicense());
            setNullableString(ps, 2, user.getMatter());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, Boolean.TRUE.equals(user.getStatus()));
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getFullName());
            setNullableString(ps, 7, user.getPhone());
            ps.setInt(8, user.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("User not found with id " + user.getId(), null);
            }
            return user;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("El correo electrónico ya está registrado.", e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user", e);
        }
    }

    @Override
    public List<User> findActiveLawyers(Role role) {
        return queryUsers(SELECT_WITH_DESK + " WHERE u.role = ? AND u.status = 1 ORDER BY u.full_name", role);
    }

    @Override
    public List<User> findByRole(Role role) {
        return queryUsers(SELECT_WITH_DESK + " WHERE u.role = ? ORDER BY u.id DESC", role);
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = SELECT_WITH_DESK + " WHERE u.id = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapWithDesk(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
    }

    @Override
    public int insert(User user, String passwordHash) {
        String sql = "INSERT INTO users (professional_license, matter, role, status, email, password, "
            + "must_change_password, full_name, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setNullableString(ps, 1, user.getProfessionalLicense());
            setNullableString(ps, 2, user.getMatter());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, Boolean.TRUE.equals(user.getStatus()));
            ps.setString(5, user.getEmail());
            ps.setString(6, passwordHash);
            ps.setBoolean(7, user.isMustChangePassword());
            ps.setString(8, user.getFullName());
            setNullableString(ps, 9, user.getPhone());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("El correo electrónico ya está registrado.", e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user", e);
        }
    }

    @Override
    public void setStatus(int userId, boolean status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("User not found with id " + userId, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user status", e);
        }
    }

    @Override
    public void updatePassword(int userId, String passwordHash, boolean mustChangePassword) {
        String sql = "UPDATE users SET password = ?, must_change_password = ? WHERE id = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setBoolean(2, mustChangePassword);
            ps.setInt(3, userId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("User not found with id " + userId, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user password", e);
        }
    }

    private List<User> queryUsers(String sql, Role role) {
        List<User> users = new ArrayList<>();
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapWithDesk(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
        return users;
    }

    private static User mapWithDesk(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setProfessionalLicense(rs.getString("professional_license"));
        user.setPhone(rs.getString("phone"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setStatus(rs.getBoolean("status"));
        user.setMustChangePassword(rs.getBoolean("must_change_password"));
        user.setMatter(rs.getString("matter"));
        user.setDeskId(rs.getObject("desk_id", Integer.class));
        user.setDeskName(rs.getString("desk_name"));
        return user;
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
