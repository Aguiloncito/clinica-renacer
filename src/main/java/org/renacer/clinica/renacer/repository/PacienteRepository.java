/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.repository;


import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.model.Paciente;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * @author AGUILON
 */
public class PacienteRepository {
 
    public List<Paciente> listar() throws SQLException {
        String sql = "SELECT id_paciente, nombres, apellidos, fecha_nacimiento, telefono, direccion " +
                     "FROM pacientes ORDER BY apellidos, nombres";
 
        List<Paciente> lista = new ArrayList<>();
        try (Connection cn = DataBaseConnection.getConnectionDataBase();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                Date fn = rs.getDate("fecha_nacimiento");
                lista.add(new Paciente(
                        String.valueOf(rs.getInt("id_paciente")),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        fn == null ? "" : fn.toString(),
                        rs.getString("telefono"),
                        rs.getString("direccion")));
            }
        }
        return lista;
    }
}
 