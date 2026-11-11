package mx.edu.utez.judicial.audiencias.api.DAO.usuario;

import mx.edu.utez.judicial.audiencias.api.DAO.DataAccessException;
import mx.edu.utez.judicial.audiencias.api.model.usuario.Rol;
import mx.edu.utez.judicial.audiencias.api.model.usuario.Usuario;
import mx.edu.utez.judicial.common.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUsuarioDao implements UsuarioDao {

    private final Database database;

    public JdbcUsuarioDao(Database database) {
        this.database = database;
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT correo, nombre_completo, rol, estado FROM usuarios WHERE correo = ?";
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario(
                        rs.getString("correo"),
                        rs.getString("nombre_completo"),
                        Rol.valueOf(rs.getString("rol")),
                        rs.getBoolean("estado")
                    );
                    return Optional.of(usuario);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Consulta no realizada", e);
        }
    }


    public String authenticate(String email) {
        String sql = "SELECT password FROM usuarios WHERE correo = ?";
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
            throw new DataAccessException("Consulta no realizada", e);
        }
    }

    @Override
    public Usuario update(Usuario u,int id) {
        String sql = "UPDATE usuarios SET cedula_profesional = ?, rol = ?, estado = ?, " +
            "correo = ?, nombre_completo = ? WHERE id = ?";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (u.getCedulaProfesional() != null) {
                ps.setString(1, u.getCedulaProfesional());
            } else {
                ps.setNull(1, Types.CHAR);
            }
            ps.setString(2, u.getRol().name());
            ps.setBoolean(3, Boolean.TRUE.equals(u.getEstado()));
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getNombreCompleto());
            ps.setInt(6, id);

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("No existe el usuario con id " + id, null);
            }
            return u;
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public List<Usuario> findAbogados() {
        String sql = "SELECT u.nombre_completo, u.cedula_profesional, u.estado, m.materia " +
            "FROM usuarios u " +
            "LEFT JOIN mesas m ON u.id = m.id_usuario " +
            "WHERE u.rol = 'ABOGADO'";
        List<Usuario> abogados = new ArrayList<>();
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                abogados.add(new Usuario(
                    rs.getString("nombre_completo"),
                    rs.getString("cedula_profesional"),
                    rs.getBoolean("estado"),
                    rs.getString("materia")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Consulta no realizada", e);
        }
        return abogados;
    }

    public int insert(Usuario u, String passwordHash) {
        String sql = "INSERT INTO usuarios (cedula_profesional, rol, estado, correo, password, nombre_completo) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (u.getCedulaProfesional() != null) {
                ps.setString(1, u.getCedulaProfesional());
            } else {
                ps.setNull(1, Types.CHAR);
            }
            ps.setString(2, u.getRol().name());
            ps.setBoolean(3, Boolean.TRUE.equals(u.getEstado()));
            ps.setString(4, u.getCorreo());
            ps.setString(5, passwordHash);
            ps.setString(6, u.getNombreCompleto());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo insertar el usuario", e);
        }
    }
}
