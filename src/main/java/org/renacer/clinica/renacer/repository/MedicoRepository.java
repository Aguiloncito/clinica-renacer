/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.repository;


import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.model.Medico;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * CRUD de la tabla medicos.
 *
 * @author AGUILON
 */
public class MedicoRepository {
 
    public List<Medico> listar() throws SQLException {
        String sql = "SELECT id_medico, nombres, apellidos, especialidad, numero_colegiado " +
                     "FROM medicos ORDER BY apellidos, nombres";
 
        List<Medico> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }
 
    public List<Medico> buscar(String filtro) throws SQLException {
        String f = filtro == null ? "" : filtro.trim();
        String like = "%" + f + "%";
 
        String sql = "SELECT id_medico, nombres, apellidos, especialidad, numero_colegiado " +
                     "FROM medicos " +
                     "WHERE ? = '' OR nombres LIKE ? OR apellidos LIKE ? OR especialidad LIKE ? OR numero_colegiado LIKE ? " +
                     "ORDER BY apellidos, nombres";
 
        List<Medico> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, f);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }
 
    public boolean existeColegiado(String colegiado, String idExcluir) throws SQLException {
        String sql = "SELECT 1 FROM medicos WHERE numero_colegiado = ? AND id_medico <> ? LIMIT 1";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, colegiado);
            ps.setInt(2, parseId(idExcluir));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public void insertar(Medico m) throws SQLException {
        String sql = "INSERT INTO medicos (nombres, apellidos, especialidad, numero_colegiado) VALUES (?,?,?,?)";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNombres());
            ps.setString(2, m.getApellidos());
            ps.setString(3, m.getEspecialidad());
            ps.setString(4, m.getNumeroColegiado());
            ps.executeUpdate();
 
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setIdMedico(String.valueOf(rs.getInt(1)));
            }
        }
    }

    /** true si ya existe un usuario con ese nombre de acceso. */
    public boolean existeUsuario(String usuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE usuario = ? LIMIT 1";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int obtenerIdRolMedico(Connection cn) throws SQLException {
        String sql = "SELECT id_rol FROM roles WHERE nombre_rol = 'medico'";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id_rol");
        }
        throw new SQLException("No existe el rol 'medico' en la tabla roles");
    }

    /**
     * Inserta el médico y, en la misma transacción, crea su usuario de acceso
     * (rol 'medico') con la contraseña ya encriptada.
     */
    public void insertarConUsuario(Medico m, String usuario, String passwordHasheada) throws SQLException {
        String sqlMedico = "INSERT INTO medicos (nombres, apellidos, especialidad, numero_colegiado) VALUES (?,?,?,?)";
        String sqlUsuario = "INSERT INTO usuarios (id_rol, id_medico, usuario, password_hash) VALUES (?,?,?,?)";

        Connection cn = null;
        try {
            cn = DataBaseConnection.getConnectionDataBase();
            cn.setAutoCommit(false);

            int idMedicoGenerado;
            try (PreparedStatement psMedico = cn.prepareStatement(sqlMedico, Statement.RETURN_GENERATED_KEYS)) {
                psMedico.setString(1, m.getNombres());
                psMedico.setString(2, m.getApellidos());
                psMedico.setString(3, m.getEspecialidad());
                psMedico.setString(4, m.getNumeroColegiado());
                psMedico.executeUpdate();

                try (ResultSet rs = psMedico.getGeneratedKeys()) {
                    if (rs.next()) {
                        idMedicoGenerado = rs.getInt(1);
                    } else {
                        cn.rollback();
                        throw new SQLException("No se pudo obtener el ID del médico recién creado.");
                    }
                }
            }

            int idRolMedico = obtenerIdRolMedico(cn);
            try (PreparedStatement psUsuario = cn.prepareStatement(sqlUsuario)) {
                psUsuario.setInt(1, idRolMedico);
                psUsuario.setInt(2, idMedicoGenerado);
                psUsuario.setString(3, usuario);
                psUsuario.setString(4, passwordHasheada);
                psUsuario.executeUpdate();
            }

            cn.commit();
            m.setIdMedico(String.valueOf(idMedicoGenerado));

        } catch (SQLException e) {
            if (cn != null) {
                cn.rollback();
            }
            throw e;
        } finally {
            if (cn != null) {
                cn.setAutoCommit(true);
            }
        }
    }
 
    public void actualizar(Medico m) throws SQLException {
        String sql = "UPDATE medicos SET nombres=?, apellidos=?, especialidad=?, numero_colegiado=? WHERE id_medico=?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, m.getNombres());
            ps.setString(2, m.getApellidos());
            ps.setString(3, m.getEspecialidad());
            ps.setString(4, m.getNumeroColegiado());
            ps.setInt(5, parseId(m.getIdMedico()));
            ps.executeUpdate();
        }
    }
 
    public void eliminar(String idMedico) throws SQLException {
        String sql = "DELETE FROM medicos WHERE id_medico = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, parseId(idMedico));
            ps.executeUpdate();
        }
    }
 
    public int contarCitasActivas(String idMedico) throws SQLException {
        String sql = "SELECT COUNT(*) FROM citas WHERE id_medico = ? AND LOWER(estado) <> 'cancelada'";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, parseId(idMedico));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
 
    public static int parseId(String id) {
        try {
            return id == null || id.trim().isEmpty() ? 0 : Integer.parseInt(id.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
 
    private Medico mapear(ResultSet rs) throws SQLException {
        return new Medico(
                String.valueOf(rs.getInt("id_medico")),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("especialidad"),
                rs.getString("numero_colegiado"));
    }
}