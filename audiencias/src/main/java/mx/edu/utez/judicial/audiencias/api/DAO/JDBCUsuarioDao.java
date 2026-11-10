package mx.edu.utez.judicial.audiencias.api.DAO;

import mx.edu.utez.judicial.audiencias.api.model.Rol;
import mx.edu.utez.judicial.audiencias.api.model.Usuario;
import mx.edu.utez.judicial.common.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCUsuarioDao implements UsuarioDAO {

    private final Database database;

    public JDBCUsuarioDao(Database database) {
        this.database = database;
    }


    public Optional<Usuario> findByEmail(String email){
        String sql ="Select correo,nombre_completo,rol , estado , password FROM usuarios WHERE coreeo = ?";
        try(Connection con = database.getConnection()) {
            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1,email);
                try(ResultSet rs = ps.executeQuery()){
                    if (rs.next()) {
                        Usuario usuario = new Usuario(
                            rs.getString("email"),
                            rs.getString("nombre_completo"),
                            Rol.valueOf(rs.getString("rol")),
                            rs.getBoolean("activo")
                        );
                        return Optional.of(usuario);
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccesException("Consulta no realizada",e);
        }
    }

    public String authenticate(String email){
        String sql ="Select password FROM usuarios WHERE correo = ?";
        try(Connection con = database.getConnection()) {
            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1,email);
                try(ResultSet rs = ps.executeQuery()){
                    if (rs.next()) {
                        return rs.getString("password");
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccesException("Consulta no realizada",e);
        }
    }

    @Override
    public Usuario update(Usuario usuario) {
        return null;
    }


    @Override
    public List<Usuario> findByRol() {
        String sql = "SELECT u.nombre_completo, u.cedula_profesional,u.estado, m.materia " +
            "FROM usuarios u " +
            "LEFT JOIN mesas m ON u.id = m.id_usuario " +
            "WHERE u.rol = 'ABOGADO'";
        List<Usuario> abogados = new ArrayList<>();
        try (Connection con = database.getConnection()){
            try (PreparedStatement ps = con.prepareStatement(sql)){
                try (ResultSet rs = ps.executeQuery()){
                    while (rs.next()){
                        abogados.add(Optional.of(new Usuario(
                            rs.getString("nombre_completo"),
                            rs.getString("cedula_profesional"),
                            rs.getBoolean("estado"),
                            rs.getString("materia")
                        )));
                    }
                }
            }
        }catch (SQLException e){
            throw new DataAccesException("Consulta no realizada",e);
        }
        return abogados;

    }

    @Override
    public Usuario insert() {
        return null;
    }


}
