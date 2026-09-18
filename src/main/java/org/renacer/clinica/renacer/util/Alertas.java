/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
 
import java.util.Optional;
 
/**
 * @author AGUILON
 */
public final class Alertas {
 
    private Alertas() {
    }
 
    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }
 
    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Atención", mensaje);
    }
 
    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }
 
    public static boolean confirmar(String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar");
        a.setHeaderText(null);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.YES;
    }
 
    public static Optional<String> pedirTexto(String titulo, String mensaje) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(titulo);
        d.setHeaderText(null);
        d.setContentText(mensaje);
        return d.showAndWait();
    }
 
    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}