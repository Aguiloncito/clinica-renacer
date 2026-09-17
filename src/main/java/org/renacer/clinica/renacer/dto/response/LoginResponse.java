/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.dto.response;

/**
 *
 * @author AGUILON
 */
public class LoginResponse {
    
    private String nombres;
    private String apellidos;
    private String password_hash;

    public LoginResponse(String nombres, String apellidos, String password_hash) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.password_hash = password_hash;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
    
    
}
