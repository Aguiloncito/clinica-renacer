/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.model.autorizacion;

/**
 *
 * @author AGUILON
 */
public class Usuario {
    private String idUsuario;
    private String idRol;
    private String idPaciente;
    private String usuario;
    private String passwordHash;

    public Usuario(String idUsuario, String idRol, String idPaciente, String usuario, String passwordHash) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.idPaciente = idPaciente;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
    }

    public Usuario(){
        
    }
    
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdRol() {
        return idRol;
    }

    public void setIdRol(String idRol) {
        this.idRol = idRol;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    
}
