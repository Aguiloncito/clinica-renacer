package main.java.org.renacer.clinica.renacer.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.dto.response.ExpedienteResponse;
import main.java.org.renacer.clinica.renacer.util.EstadoCita;

/**
 * Consultas del expediente clínico. Lee de la vista vista_expedientes
 * (ver sql/01_admin_setup.sql) y permite corregir recetas.
 */
public class ExpedienteRepository {

    private static final String COLUMNAS =
            "id_cita, fecha_cita, estado_cita, id_paciente, paciente, id_medico, medico, especialidad, " +
            "id_consulta, fecha_atencion, motivo, diagnostico, observaciones, id_receta, medicamentos, indicaciones";

    /** Historial completo de un paciente: una fila por receta (o por cita si no hay). */
    public List<ExpedienteResponse> historialPaciente(int idPaciente) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vista_expedientes " +
                     "WHERE id_paciente = ? ORDER BY fecha_cita DESC, id_receta";
        List<ExpedienteResponse> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Recetas emitidas por un médico (solo filas que sí tienen receta). */
    public List<ExpedienteResponse> recetasPorMedico(int idMedico) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vista_expedientes " +
                     "WHERE id_medico = ? AND id_receta IS NOT NULL ORDER BY fecha_cita DESC, id_receta";
        List<ExpedienteResponse> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Registro general de consultas: una fila por consulta. Si la consulta tiene
     * varias recetas, se unen en una sola celda separadas por " | ".
     */
    public List<ExpedienteResponse> registroConsultas(String filtro, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String f = filtro == null ? "" : filtro.trim();
        String like = "%" + f + "%";

        String sql = "SELECT id_cita, fecha_cita, estado_cita, id_paciente, paciente, id_medico, medico, " +
                     "       especialidad, id_consulta, fecha_atencion, motivo, diagnostico, observaciones, " +
                     "       NULL AS id_receta, " +
                     "       GROUP_CONCAT(medicamentos ORDER BY id_receta SEPARATOR ' | ') AS medicamentos, " +
                     "       GROUP_CONCAT(indicaciones ORDER BY id_receta SEPARATOR ' | ') AS indicaciones " +
                     "FROM vista_expedientes " +
                     "WHERE id_consulta IS NOT NULL " +
                     "  AND DATE(fecha_cita) BETWEEN ? AND ? " +
                     "  AND (? = '' OR paciente LIKE ? OR medico LIKE ? OR especialidad LIKE ? " +
                     "       OR motivo LIKE ? OR diagnostico LIKE ?) " +
                     "GROUP BY id_cita, fecha_cita, estado_cita, id_paciente, paciente, id_medico, medico, " +
                     "         especialidad, id_consulta, fecha_atencion, motivo, diagnostico, observaciones " +
                     "ORDER BY fecha_cita DESC";

        LocalDate d = desde != null ? desde : LocalDate.of(1900, 1, 1);
        LocalDate h = hasta != null ? hasta : LocalDate.of(9999, 12, 31);

        List<ExpedienteResponse> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(d));
            ps.setDate(2, java.sql.Date.valueOf(h));
            ps.setString(3, f);
            for (int i = 4; i <= 8; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Corrige el texto de una receta ya emitida. Devuelve cuántas filas cambió. */
    public int actualizarReceta(int idReceta, String medicamentos, String indicaciones) throws SQLException {
        String sql = "UPDATE recetas SET medicamentos = ?, indicaciones = ? WHERE id_receta = ?";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, medicamentos);
            ps.setString(2, indicaciones);
            ps.setInt(3, idReceta);
            return ps.executeUpdate();
        }
    }

    // ---- mapeo ---------------------------------------------------------------

    private ExpedienteResponse mapear(ResultSet rs) throws SQLException {
        ExpedienteResponse e = new ExpedienteResponse();
        e.setIdCita(rs.getInt("id_cita"));
        Timestamp fc = rs.getTimestamp("fecha_cita");
        e.setFechaCita(fc == null ? null : fc.toLocalDateTime());
        e.setEstado(EstadoCita.normalizar(rs.getString("estado_cita")));
        e.setIdPaciente(rs.getInt("id_paciente"));
        e.setPaciente(rs.getString("paciente"));
        e.setIdMedico(rs.getInt("id_medico"));
        e.setMedico(rs.getString("medico"));
        e.setEspecialidad(rs.getString("especialidad"));

        int idConsulta = rs.getInt("id_consulta");
        e.setIdConsulta(rs.wasNull() ? null : idConsulta);
        Timestamp fa = rs.getTimestamp("fecha_atencion");
        e.setFechaAtencion(fa == null ? null : fa.toLocalDateTime());
        e.setMotivo(rs.getString("motivo"));
        e.setDiagnostico(rs.getString("diagnostico"));
        e.setObservaciones(rs.getString("observaciones"));

        int idReceta = rs.getInt("id_receta");
        e.setIdReceta(rs.wasNull() ? null : idReceta);
        e.setMedicamentos(rs.getString("medicamentos"));
        e.setIndicaciones(rs.getString("indicaciones"));
        return e;
    }
}
