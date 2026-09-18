package main.java.org.renacer.clinica.renacer.repository;

import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.model.Paciente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PacienteRepository {

    public void guardar(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO pacientes (nombres, apellidos, fecha_nacimiento, telefono, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            ps.setString(4, paciente.getTelefono());
            ps.setString(5, paciente.getDireccion());
            ps.executeUpdate();
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
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPaciente(rs));
            }
        }
        return lista;
    }

    public List<Paciente> buscarPorCriterio(String filtro) throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE LOWER(nombres) LIKE ? OR LOWER(apellidos) LIKE ? OR CAST(id_paciente AS CHAR) LIKE ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String parametro = "%" + filtro.toLowerCase() + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPaciente(rs));
                }
            }
        }
        return lista;
    }

    private Paciente mapearPaciente(ResultSet rs) throws SQLException {
        Date fecha = rs.getDate("fecha_nacimiento");
        return new Paciente(
                rs.getInt("id_paciente"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                fecha != null ? fecha.toLocalDate() : null,
                rs.getString("telefono"),
                rs.getString("direccion")
        );
    }
}
