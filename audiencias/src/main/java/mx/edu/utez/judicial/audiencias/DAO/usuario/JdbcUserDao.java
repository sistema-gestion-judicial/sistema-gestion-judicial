package mx.edu.utez.judicial.audiencias.DAO.usuario;

import mx.edu.utez.judicial.audiencias.DAO.DataAccessException;
import mx.edu.utez.judicial.audiencias.model.usuario.Role;
import mx.edu.utez.judicial.audiencias.model.usuario.User;

import mx.edu.utez.judicial.common.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private final Database database;

    public JdbcUserDao(Database database) {
        this.database = database;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id,email, full_name, role, status FROM users WHERE email = ?";
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
        String sql = "UPDATE users SET professional_license = ?, role = ?, status = ?, " +
            "email = ?, full_name = ?, phone = ? WHERE id = ?";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (user.getProfessionalLicense() != null) {
                ps.setString(1, user.getProfessionalLicense());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, user.getRole().name());
            ps.setBoolean(3, Boolean.TRUE.equals(user.getStatus()));
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getFullName());
            if (user.getPhone() != null) {
                ps.setString(6, user.getPhone());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.setInt(7, user.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("User not found with id " + user.getId(), null);
            }
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user", e);
        }
    }

    @Override
    public List<User> findLawyers() {
        String sql = "SELECT u.id,u.full_name, u.professional_license, u.status, d.matter " +
            "FROM users u " +
            "LEFT JOIN desks d ON u.id = d.user_id " +
            "WHERE u.role = 'LAWYER'";
        List<User> lawyers = new ArrayList<>();
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lawyers.add(new User(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("professional_license"),
                    rs.getBoolean("status"),
                    rs.getString("matter")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query", e);
        }
        return lawyers;
    }

    @Override
    public int insert(User user, String passwordHash) {
        String sql = "INSERT INTO users (professional_license, role, status, email, password, full_name, phone) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (user.getProfessionalLicense() != null) {
                ps.setString(1, user.getProfessionalLicense());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, user.getRole().name());
            ps.setBoolean(3, Boolean.TRUE.equals(user.getStatus()));
            ps.setString(4, user.getEmail());
            ps.setString(5, passwordHash);
            ps.setString(6, user.getFullName());
            if (user.getPhone() != null) {
                ps.setString(7, user.getPhone());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user", e);
        }
    }
}
