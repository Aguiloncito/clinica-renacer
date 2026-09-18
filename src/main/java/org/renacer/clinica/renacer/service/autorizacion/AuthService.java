/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.service.autorizacion;

import main.java.org.renacer.clinica.renacer.dto.request.LoginRequest;
import main.java.org.renacer.clinica.renacer.dto.response.LoginResponse;
import main.java.org.renacer.clinica.renacer.repository.autorizacion.AuthRepository;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author AGUILON
 */
public class AuthService {
    private final AuthRepository authRepository;
    private boolean status = false;
    public AuthService(AuthRepository authRepository){
        this.authRepository = authRepository;
    }
public LoginResponse login (LoginRequest loginRequest) throws Exception{
    if ((loginRequest == null)) {
      throw new RuntimeException("Credenciales vacias.");
    }else if(loginRequest.getUsuario() == null || loginRequest.getPassword_hash()== null){
      throw new RuntimeException("El usuario o la contraseña no pueden estar vacios");
    }
    LoginResponse response = authRepository.findUserByUser(loginRequest);
    if (response== null) {
      throw new RuntimeException("Usuario no encontado");
    }
  String contrasenaHashed = response.getPassword_hash();
  if(contrasenaHashed == null){
      throw new RuntimeException("contrasena invalida.");
  }else{
      if (BCrypt.checkpw(loginRequest.getPassword_hash(),contrasenaHashed )){
      return response;
      }   
  }
  return null;
}
}
