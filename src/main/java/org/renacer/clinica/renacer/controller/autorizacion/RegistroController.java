/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.controller.autorizacion;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import main.java.org.renacer.clinica.renacer.dto.request.RegistroRequest;
import main.java.org.renacer.clinica.renacer.service.autorizacion.RegistroViewService;
import main.java.org.renacer.clinica.renacer.util.sceneManager.SceneManager;

/**
 * FXML Controller class
 *
 * @author AGUILON
 */
public class RegistroController implements Initializable {
    //atributos
    private final RegistroViewService registroService;
    private final SceneManager sceneManager;

    @FXML
    private TextField txtFieldNombre;
    @FXML
    private TextField txtFieldApellido;
    @FXML
    private TextField txtFieldFechaNacimiento;
    @FXML
    private TextField txtFieldTelefono;
    @FXML
    private TextField txtFieldDireccion;
    @FXML
    private TextField txtFieldUsuario;
    @FXML
    private TextField txtFieldPassword;

    //Constructor
    public RegistroController(RegistroViewService registroService, SceneManager sceneManager) {
        this.registroService = registroService;
        this.sceneManager = sceneManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    //metodo para registrar
    public void handleRegistrar() throws Exception {
        String nombre = txtFieldNombre.getText();
        String apellido = txtFieldApellido.getText();
        String fechaNacimiento = txtFieldFechaNacimiento.getText();
        String telefono = txtFieldTelefono.getText();
        String direccion = txtFieldDireccion.getText();
        String usuario = txtFieldUsuario.getText();
        String password = txtFieldPassword.getText();

        if (usuario == null || usuario.isEmpty()
                || password == null || password.isEmpty()
                || nombre == null || nombre.isEmpty()) {
            sceneManager.showInfoAlert("Campos faltantes", "Revisar información", "Uno o mas campos están vacios", Alert.AlertType.CONFIRMATION);
        } else {
            try {
                RegistroRequest request = new RegistroRequest(nombre, apellido, fechaNacimiento, telefono, direccion, usuario, password);
                registroService.registrar(request);
                sceneManager.showInfoAlert("Registro exitoso", "Usuario creado", "El usuario fue registrado correctamente", Alert.AlertType.INFORMATION);
                sceneManager.showLoginView();
            } catch (Exception e) {
                sceneManager.showInfoAlert("Error al registrar", "Revisa tu información", e.getMessage(), Alert.AlertType.INFORMATION);
            }
        }
    }

    //metodo para regresar al login
    public void handleRegresar() throws Exception {
        sceneManager.showLoginView();
    }
}