/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.repository;


import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.dto.response.CitaAgendaResponse;
import main.java.org.renacer.clinica.renacer.util.EstadoCita;
 
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
 * Acceso a la tabla citas. Se apoya en los JOIN con medicos, pacientes y
 * consultas para devolver directamente lo que el dashboard necesita mostrar.
 *
 * @author AGUILON
 */
public class CitaRepository {
 
    private static final String SELECT_AGENDA =
            "SELECT c.id_cita, c.id_medico, c.id_paciente, c.fecha_hora, c.estado, " +
            "       CONCAT(m.nombres,' ',m.apellidos) AS medico, m.especialidad, " +
            "       CONCAT(p.nombres,' ',p.apellidos) AS paciente, " +
            "       co.id_consulta " +
            "FROM citas c " +
            "JOIN medicos m        ON m.id_medico   = c.id_medico " +
            "JOIN pacientes p      ON p.id_paciente = c.id_paciente " +
            "LEFT JOIN consultas co ON co.id_cita   = c.id_cita ";
 
    /** Citas de un día. Si idMedico es 0 devuelve las de todos los médicos. */
    public List<CitaAgendaResponse> agendaPorDia(LocalDate dia, int idMedico) throws SQLException {
        String sql = SELECT_AGENDA +
                "WHERE DATE(c.fecha_hora) = ? AND (? = 0 OR c.id_medico = ?) " +
                "ORDER BY c.fecha_hora, m.apellidos";
 
        List<CitaAgendaResponse> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
 
            ps.setDate(1, Date.valueOf(dia));
            ps.setInt(2, idMedico);
            ps.setInt(3, idMedico);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }
 
    /** Citas pendientes (programadas o en curso) de hoy en adelante. */
    public List<CitaAgendaResponse> pendientes(int idMedico) throws SQLException {
        String sql = SELECT_AGENDA +
                "WHERE LOWER(c.estado) IN ('pendiente','confirmada','programada','en curso') " +
                "  AND c.fecha_hora >= CURDATE() " +
                "  AND (? = 0 OR c.id_medico = ?) " +
                "ORDER BY c.fecha_hora";
 
        List<CitaAgendaResponse> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setInt(2, idMedico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }
 
    /**
     * Cuenta cuántas citas tiene cada día del mes indicado.
     * Sirve para marcar los días ocupados en el DatePicker (mini calendario).
     */
    public Map<LocalDate, Integer> conteoPorDia(LocalDate mes, int idMedico) throws SQLException {
        String sql = "SELECT DATE(c.fecha_hora) AS dia, COUNT(*) AS total " +
                     "FROM citas c " +
                     "WHERE YEAR(c.fecha_hora) = ? AND MONTH(c.fecha_hora) = ? " +
                     "  AND LOWER(c.estado) <> 'cancelada' " +
                     "  AND (? = 0 OR c.id_medico = ?) " +
                     "GROUP BY DATE(c.fecha_hora)";
 
        Map<LocalDate, Integer> mapa = new HashMap<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
 
            ps.setInt(1, mes.getYear());
            ps.setInt(2, mes.getMonthValue());
            ps.setInt(3, idMedico);
            ps.setInt(4, idMedico);
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getDate("dia").toLocalDate(), rs.getInt("total"));
                }
            }
        }
        return mapa;
    }
 
    /**
     * Calendario sin cruces: como la tabla solo guarda fecha_hora, el bloque de
     * la cita se asume de duracionMinutos. Dos bloques chocan si
     * |inicioA - inicioB| < duracion.
     */
    public boolean existeCruce(int idMedico, LocalDateTime inicio, int duracionMinutos, int idCitaExcluir)
            throws SQLException {
 
        String sql = "SELECT 1 FROM citas " +
                     "WHERE id_medico = ? AND id_cita <> ? AND LOWER(estado) <> 'cancelada' " +
                     "  AND fecha_hora <  DATE_ADD(?, INTERVAL ? MINUTE) " +
                     "  AND DATE_ADD(fecha_hora, INTERVAL ? MINUTE) > ? LIMIT 1";
 
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
 
            Timestamp ts = Timestamp.valueOf(inicio);
            ps.setInt(1, idMedico);
            ps.setInt(2, idCitaExcluir);
            ps.setTimestamp(3, ts);
            ps.setInt(4, duracionMinutos);
            ps.setInt(5, duracionMinutos);
            ps.setTimestamp(6, ts);
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public boolean pacienteOcupado(int idPaciente, LocalDateTime inicio, int duracionMinutos, int idCitaExcluir)
            throws SQLException {
 
        String sql = "SELECT 1 FROM citas " +
                     "WHERE id_paciente = ? AND id_cita <> ? AND LOWER(estado) <> 'cancelada' " +
                     "  AND fecha_hora <  DATE_ADD(?, INTERVAL ? MINUTE) " +
                     "  AND DATE_ADD(fecha_hora, INTERVAL ? MINUTE) > ? LIMIT 1";
 
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
 
            Timestamp ts = Timestamp.valueOf(inicio);
            ps.setInt(1, idPaciente);
            ps.setInt(2, idCitaExcluir);
            ps.setTimestamp(3, ts);
            ps.setInt(4, duracionMinutos);
            ps.setInt(5, duracionMinutos);
            ps.setTimestamp(6, ts);
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    public int insertar(int idPaciente, int idMedico, LocalDateTime fechaHora) throws SQLException {
        String sql = "INSERT INTO citas (id_paciente, id_medico, fecha_hora, estado) VALUES (?,?,?,?)";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setInt(1, idPaciente);
            ps.setInt(2, idMedico);
            ps.setTimestamp(3, Timestamp.valueOf(fechaHora));
            ps.setString(4, EstadoCita.PROGRAMADA);
            ps.executeUpdate();
 
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
 
    public void cambiarEstado(int idCita, String nuevoEstado) throws SQLException {
        String sql = "UPDATE citas SET estado = ? WHERE id_cita = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCita);
            ps.executeUpdate();
        }
    }
 
    public void reprogramar(int idCita, LocalDateTime nuevaFechaHora) throws SQLException {
        String sql = "UPDATE citas SET fecha_hora = ?, estado = ? WHERE id_cita = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(nuevaFechaHora));
            ps.setString(2, EstadoCita.PROGRAMADA);
            ps.setInt(3, idCita);
            ps.executeUpdate();
        }
    }
 
    private CitaAgendaResponse mapear(ResultSet rs) throws SQLException {
        CitaAgendaResponse c = new CitaAgendaResponse();
        c.setIdCita(rs.getInt("id_cita"));
        c.setIdMedico(rs.getInt("id_medico"));
        c.setIdPaciente(rs.getInt("id_paciente"));
        c.setMedico(rs.getString("medico"));
        c.setEspecialidad(rs.getString("especialidad"));
        c.setPaciente(rs.getString("paciente"));
        c.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        c.setEstado(EstadoCita.normalizar(rs.getString("estado")));
 
        int idConsulta = rs.getInt("id_consulta");
        c.setIdConsulta(rs.wasNull() ? null : idConsulta);
        return c;
    }
}