/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.util.sceneManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import main.java.org.renacer.clinica.renacer.controller.autorizacion.LoginController;
import main.java.org.renacer.clinica.renacer.controller.autorizacion.RegistroController;
import main.java.org.renacer.clinica.renacer.repository.autorizacion.AuthRepository;
import main.java.org.renacer.clinica.renacer.repository.autorizacion.RegistroRepository;
import main.java.org.renacer.clinica.renacer.service.autorizacion.AuthService;
import main.java.org.renacer.clinica.renacer.service.autorizacion.RegistroViewService;

/**
 *
 * @author AGUILON
 */
public class SceneManager {
    //atributos
    private Stage primaryStage;
    private final String FXML_PATH = "/main/resources/view/";
    //constructor
    public SceneManager(Stage primaryStage){
        this.primaryStage = primaryStage;
    }
    
    //metodo
    public void showLoginView()throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH + "login-view.fxml"));
        
        loader.setControllerFactory(
        clazz->{
            if(clazz == LoginController.class){
                AuthRepository authRepository = new AuthRepository();
                AuthService authService = new AuthService(authRepository);
                return new LoginController(authService, this);
            }
            try{
                return clazz.getDeclaredConstructor().newInstance();
            }catch(Exception e){
                throw new RuntimeException("Error al crear el constructor" + e.getMessage());
        }
        });
        
        Parent root = loader.load();
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    // NOTA: el dashboard todavia no esta implementado en este proyecto.
    // Cuando se construya la vista dashboard-view.fxml y su controlador,
    // agregar aqui un metodo showDashBoardView() equivalente a los de arriba.

    //venta modal, para mostrar alerta
    
    public void showInfoAlert(String head, String title, String content, Alert.AlertType type){
    Alert alert = new Alert(type);
    alert.initOwner(this.primaryStage);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.setHeaderText(head);
    alert.showAndWait();
    }
    
    public void showRegistroView() throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH + "registro-view.fxml"));

    loader.setControllerFactory(
            clazz -> {
                if (clazz == RegistroController.class) {
                    RegistroRepository registroRepository = new RegistroRepository();
                    RegistroViewService registroService = new RegistroViewService(registroRepository);
                    return new RegistroController(registroService, this);
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("error al crear el constructor" + e.getMessage());
                }
            }
    );

    Parent root = loader.load();
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
    primaryStage.show();
}
}
