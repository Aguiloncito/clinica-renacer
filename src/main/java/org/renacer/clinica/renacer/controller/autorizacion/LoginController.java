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
            LoginResponse userLogged = new LoginResponse(responseService.getNombres(), responseService.getApellidos());
            sceneManager.showInfoAlert("Clinica Renacer", "Inicio exitoso", "Bienvenido: "+ userLogged.getNombres(), Alert.AlertType.INFORMATION);
            // TODO: cuando exista el dashboard, navegar aqui con sceneManager.showDashBoardView();
            }catch(Exception e){
                sceneManager.showInfoAlert("Datos incorrectos", "Revisa tu información", "Intenta de nuevo", Alert.AlertType.INFORMATION);
            }
        }
    }
     public void handleRegistrarse() throws Exception {
        sceneManager.showRegistroView();
    }

}
