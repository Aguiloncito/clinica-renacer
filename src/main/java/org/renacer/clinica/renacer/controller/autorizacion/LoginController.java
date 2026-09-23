/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.controller.autorizacion;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import main.java.org.renacer.clinica.renacer.dto.request.LoginRequest;
import main.java.org.renacer.clinica.renacer.dto.response.LoginResponse;
import main.java.org.renacer.clinica.renacer.service.autorizacion.AuthService;
import main.java.org.renacer.clinica.renacer.util.Roles;
import main.java.org.renacer.clinica.renacer.util.sceneManager.SceneManager;

/**
 * FXML Controller class
 *
 * @author AGUILON
 */
public class LoginController implements Initializable {
    //atributos
     private final AuthService authService;
     private final SceneManager sceneManager;
     @FXML
     private TextField txtFieldUsuario;
     @FXML
     private TextField txtFieldPass;
    //Constructor
     public LoginController(AuthService authService, SceneManager sceneManager){
         this.authService = authService;
         this.sceneManager = sceneManager;
     }
             
             
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("TODO LO QUE ESTE ACA, SE EJECUTA CUANDO SE MUESTRA LA VISTA");
    } 
    
    //metodo
    public void handleLogin() throws Exception{
        if(txtFieldUsuario.getText().isEmpty()||txtFieldPass.getText().isEmpty()){
            sceneManager.showInfoAlert("Campos faltantes", "Revisar información", "Uno o mas campos están vacios", Alert.AlertType.CONFIRMATION);
        }else{
            try{
                
            LoginResponse responseService = authService.login(new LoginRequest(txtFieldUsuario.getText(), txtFieldPass.getText()));
            // NUEVO: si la contrasena es incorrecta, login() devuelve null
            if (responseService == null) {
                sceneManager.showInfoAlert("Datos incorrectos", "Revisa tu información", "Intenta de nuevo", Alert.AlertType.INFORMATION);
                return;
            }
            String rol = responseService.getRol();
            String nombre = (responseService.getNombres() + " " + responseService.getApellidos()).trim();

            // NUEVO: el administrador entra a su panel
            if (Roles.esAdministrador(rol)) {
                sceneManager.showDashboardAdminView(nombre);
                return;
            }
            // NUEVO: el medico entra a su panel de agenda/consultas
            if (Roles.esMedico(rol)) {
                sceneManager.showDashboardMedicoView();
                return;
            }
            // NUEVO: el paciente entra a su vista
            if (Roles.esPaciente(rol)) {
                sceneManager.showPacienteView();
                return;
            }

            // Rol no reconocido: se informa en lugar de dejar la sesion "colgada"
            sceneManager.showInfoAlert("Rol no reconocido", "No se pudo redirigir", "El usuario no tiene un rol valido asignado.", Alert.AlertType.WARNING);
            }catch(Exception e){
                sceneManager.showInfoAlert("Datos incorrectos", "Revisa tu información", "Intenta de nuevo", Alert.AlertType.INFORMATION);
            }
        }
    }
     public void handleRegistrarse() throws Exception {
        sceneManager.showRegistroView();
    }

}
