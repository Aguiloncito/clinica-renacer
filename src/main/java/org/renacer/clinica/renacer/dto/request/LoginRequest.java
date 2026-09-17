/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.dto.request;

/**
 *
 * @author AGUILON
 */
public class LoginRequest {
    
    private String usuario;
    private String password_hash;

    public LoginRequest(String usuario, String password_hash) {
        this.usuario = usuario;
        this.password_hash = password_hash;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
    
    
}
