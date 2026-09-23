/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.repository.autorizacion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.dto.request.LoginRequest;
import main.java.org.renacer.clinica.renacer.dto.response.LoginResponse;

/**
 *
 * @author AGUILON
 */
public class AuthRepository {
    //atributos
    private boolean sqlStatus = false;
    //constructor
    public LoginResponse findUserByUser(LoginRequest loginRequest)throws SQLException{
        // LEFT JOIN con pacientes: el administrador no es paciente (id_paciente = NULL)
        // y con un JOIN normal nunca podria iniciar sesion.
        String sql = "select coalesce(p.nombres, u.usuario) as nombres," +
            " coalesce(p.apellidos, '') as apellidos," +
            " u.password_hash, r.nombre_rol from usuarios as u" +
            " join roles as r on r.id_rol = u.id_rol" +
            " left join pacientes as p" +
            " on p.id_paciente = u.id_paciente" +
            " where u.usuario = ? ";
        try(PreparedStatement pstm = DataBaseConnection.getConnectionDataBase().prepareStatement(sql)){
            pstm.setString(1, loginRequest.getUsuario());
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
            return new LoginResponse(rs.getString("nombres"),rs.getString("apellidos"),rs.getString("password_hash"), rs.getString("nombre_rol"));
            }
        }catch(SQLException e){
            System.out.println("Error al encontrar el usuario " + e.getMessage());
        }
        return null;
        }
}
