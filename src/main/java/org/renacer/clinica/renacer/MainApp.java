package main.java.org.renacer.clinica.renacer;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.org.renacer.clinica.renacer.util.sceneManager.SceneManager;

/**
 *
 * @author AGUILON
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Clinica Renacer");
        SceneManager sceneManager = new SceneManager(primaryStage);
        // Por el momento la app inicia mostrando el login;
        // al iniciar sesion correctamente se muestra el mensaje de Bienvenido.
        sceneManager.showLoginView();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}




