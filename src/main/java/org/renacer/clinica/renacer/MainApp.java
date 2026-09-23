package main.java.org.renacer.clinica.renacer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.org.renacer.clinica.renacer.controller.PacienteController;

import java.io.IOException;

/**
 * @author AGUILON
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Se apunta directamente a la vista del paciente
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/paciente-view.fxml"));
        Parent root = loader.load();

        // Se envía un ID de prueba para inicializar el módulo del paciente
        PacienteController controller = loader.getController();
        controller.setPacienteAutenticado(1, "Paciente de Prueba");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Clínica Renacer - Módulo de Paciente");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}