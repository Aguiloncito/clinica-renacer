/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.exception;

/**
 *
 * @author AGUILON
 */
public class AutorizacionException extends RuntimeException {
    //atributos
    private final AuthErrorCode codigo;
    //CONSTRUCTOR
    public AutorizacionException(AuthErrorCode codigo){
        super(codigo.name());
        this.codigo = codigo;
    }
}