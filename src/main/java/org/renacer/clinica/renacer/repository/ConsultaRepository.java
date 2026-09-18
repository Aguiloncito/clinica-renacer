/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.repository;


import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.dto.request.ConsultaRequest;
 
import java.sql.*;
 
/**
 * Registra la consulta y, si aplica, la receta. Ambas en una sola transacción
 * junto con el cambio de estado de la cita a 'finalizada'.
 *
 * @author AGUILON
 */
public class ConsultaRepository {
 
    /**
     * @return id de la consulta creada
     */
    public int registrarConsultaConReceta(ConsultaRequest req, String estadoFinal) throws SQLException {
 
        String sqlConsulta = "INSERT INTO consultas (id_cita, motivo, diagnostico, observaciones) VALUES (?,?,?,?)";
        String sqlReceta   = "INSERT INTO recetas (id_consulta, medicamentos, indicaciones) VALUES (?,?,?)";
        String sqlEstado   = "UPDATE citas SET estado = ? WHERE id_cita = ?";
 
        Connection cn = null;
        try {
            cn = DataBaseConnection.getConnectionDataBase();
            cn.setAutoCommit(false);
 
            int idConsulta;
            try (PreparedStatement ps = cn.prepareStatement(sqlConsulta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, req.getIdCita());
                ps.setString(2, req.getMotivo());
                ps.setString(3, req.getDiagnostico());
                ps.setString(4, req.getObservaciones());
                ps.executeUpdate();
 
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    idConsulta = rs.next() ? rs.getInt(1) : 0;
                }
            }
 
            if (req.llevaReceta() && idConsulta > 0) {
                try (PreparedStatement ps = cn.prepareStatement(sqlReceta)) {
                    ps.setInt(1, idConsulta);
                    ps.setString(2, req.getMedicamentos());
                    ps.setString(3, req.getIndicaciones());
                    ps.executeUpdate();
                }
            }
 
            try (PreparedStatement ps = cn.prepareStatement(sqlEstado)) {
                ps.setString(1, estadoFinal);
                ps.setInt(2, req.getIdCita());
                ps.executeUpdate();
            }
 
            cn.commit();
            return idConsulta;
 
        } catch (SQLException ex) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) { }
            }
            throw ex;
        } finally {
            if (cn != null) {
                try { cn.setAutoCommit(true); cn.close(); } catch (SQLException ignore) { }
            }
        }
    }
 
    /** Agrega una receta adicional a una consulta que ya existe. */
    public void agregarReceta(int idConsulta, String medicamentos, String indicaciones) throws SQLException {
        String sql = "INSERT INTO recetas (id_consulta, medicamentos, indicaciones) VALUES (?,?,?)";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            ps.setString(2, medicamentos);
            ps.setString(3, indicaciones);
            ps.executeUpdate();
        }
    }
 
    public boolean existeConsulta(int idCita) throws SQLException {
        String sql = "SELECT 1 FROM consultas WHERE id_cita = ? LIMIT 1";
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
 
