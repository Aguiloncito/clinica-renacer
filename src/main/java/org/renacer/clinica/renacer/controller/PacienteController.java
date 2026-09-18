package main.java.org.renacer.clinica.renacer.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import main.java.org.renacer.clinica.renacer.dto.request.PacienteRequest;
import main.java.org.renacer.clinica.renacer.dto.response.PacienteResponse;
import main.java.org.renacer.clinica.renacer.service.PacienteService;

import java.time.LocalDate;

public class PacienteController {

    @FXML private TextField txtIdPaciente;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtBuscar;

    @FXML private TableView<PacienteResponse> tblPacientes;
    @FXML private TableColumn<PacienteResponse, Integer> colIdPaciente;
    @FXML private TableColumn<PacienteResponse, String> colNombres;
    @FXML private TableColumn<PacienteResponse, String> colApellidos;
    @FXML private TableColumn<PacienteResponse, LocalDate> colFechaNacimiento;
    @FXML private TableColumn<PacienteResponse, String> colTelefono;
    @FXML private TableColumn<PacienteResponse, String> colDireccion;

    private final PacienteService pacienteService = new PacienteService();

    @FXML
    public void initialize() {
        colIdPaciente.setCellValueFactory(new PropertyValueFactory<>("idPaciente"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tblPacientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtIdPaciente.setText(String.valueOf(newSel.getIdPaciente()));
                txtNombres.setText(newSel.getNombres());
                txtApellidos.setText(newSel.getApellidos());
                dpFechaNacimiento.setValue(newSel.getFechaNacimiento());
                txtTelefono.setText(newSel.getTelefono());
                txtDireccion.setText(newSel.getDireccion());
            }
        });

        cargarTabla();
    }

    @FXML
    public void onGuardar() {
        try {
            boolean esEdicion = txtIdPaciente.getText() != null && !txtIdPaciente.getText().isBlank();
            Integer id = esEdicion ? Integer.parseInt(txtIdPaciente.getText()) : null;

            PacienteRequest req = new PacienteRequest(
                    id,
                    txtNombres.getText(),
                    txtApellidos.getText(),
                    dpFechaNacimiento.getValue(),
                    txtTelefono.getText(),
                    txtDireccion.getText()
            );

            if (esEdicion) {
                pacienteService.actualizarPaciente(req);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Paciente actualizado correctamente.");
            } else {
                pacienteService.registrarPaciente(req);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Paciente registrado correctamente.");
            }

            onLimpiar();
            cargarTabla();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        PacienteResponse seleccionado = tblPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccione un paciente de la tabla para eliminar.");
            return;
        }

        try {
            pacienteService.eliminarPaciente(seleccionado.getIdPaciente());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Paciente eliminado correctamente.");
            onLimpiar();
            cargarTabla();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    public void onBuscar() {
        try {
            tblPacientes.setItems(FXCollections.observableArrayList(
                    pacienteService.buscarPacientes(txtBuscar.getText())
            ));
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al buscar: " + e.getMessage());
        }
    }

    @FXML
    public void onLimpiar() {
        txtIdPaciente.clear();
        txtNombres.clear();
        txtApellidos.clear();
        dpFechaNacimiento.setValue(null);
        txtTelefono.clear();
        txtDireccion.clear();
        tblPacientes.getSelectionModel().clearSelection();
    }

    private void cargarTabla() {
        try {
            tblPacientes.setItems(FXCollections.observableArrayList(pacienteService.listarPacientes()));
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar la tabla: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}