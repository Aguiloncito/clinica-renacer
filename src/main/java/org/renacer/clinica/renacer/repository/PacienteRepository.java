package main.java.org.renacer.clinica.renacer.repository;

import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacienteRepository {

    public void guardar(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO pacientes (nombres, apellidos, fecha_nacimiento, telefono, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(4, paciente.getTelefono());
            ps.setString(5, paciente.getDireccion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    paciente.setIdPaciente(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Paciente paciente) throws SQLException {
        String sql = "UPDATE pacientes SET nombres = ?, apellidos = ?, fecha_nacimiento = ?, telefono = ?, direccion = ? WHERE id_paciente = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(4, paciente.getTelefono());
            ps.setString(5, paciente.getDireccion());
            ps.setInt(6, paciente.getIdPaciente());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPaciente) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE id_paciente = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPaciente);
            ps.executeUpdate();
        }
    }

    public List<Paciente> obtenerTodos() throws SQLException {
        String sql = "SELECT id_paciente, nombres, apellidos, fecha_nacimiento, telefono, direccion FROM pacientes ORDER BY apellidos, nombres";
        List<Paciente> lista = new ArrayList<>();

        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToPaciente(rs));
            }
        }
        return lista;
    }

    public List<Paciente> buscarPorCriterio(String criterio) throws SQLException {
        String sql = "SELECT id_paciente, nombres, apellidos, fecha_nacimiento, telefono, direccion " +
                     "FROM pacientes " +
                     "WHERE nombres LIKE ? OR apellidos LIKE ? OR telefono LIKE ? " +
                     "ORDER BY apellidos, nombres";
        List<Paciente> lista = new ArrayList<>();
        String patron = "%" + criterio + "%";

        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPaciente(rs));
                }
            }
        }
        return lista;
    }

    public Paciente obtenerPorId(int idPaciente) throws SQLException {
        String sql = "SELECT id_paciente, nombres, apellidos, fecha_nacimiento, telefono, direccion FROM pacientes WHERE id_paciente = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaciente(rs);
                }
            }
        }
        return null;
    }

    /**
     * Consulta el historial de recetas asignadas al paciente uniendo las tablas de citas, médicos, consultas y recetas.
     */
    public List<Map<String, String>> obtenerRecetasPorPaciente(int idPaciente) throws SQLException {
        String sql = "SELECT c.fecha_hora, CONCAT(m.nombres, ' ', m.apellidos) AS medico, " +
                     "co.diagnostico, r.medicamentos, r.indicaciones " +
                     "FROM citas c " +
                     "JOIN medicos m ON m.id_medico = c.id_medico " +
                     "JOIN consultas co ON co.id_cita = c.id_cita " +
                     "JOIN recetas r ON r.id_consulta = co.id_consulta " +
                     "WHERE c.id_paciente = ? " +
                     "ORDER BY c.fecha_hora DESC";

        List<Map<String, String>> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("fechaHora", String.valueOf(rs.getTimestamp("fecha_hora")));
                    map.put("medico", rs.getString("medico"));
                    map.put("diagnostico", rs.getString("diagnostico"));
                    map.put("medicamentos", rs.getString("medicamentos"));
                    map.put("indicaciones", rs.getString("indicaciones"));
                    lista.add(map);
                }
            }
        }
        return lista;
    }

    private Paciente mapResultSetToPaciente(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id_paciente"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getString("telefono"),
                rs.getString("direccion")
        );
    }
}