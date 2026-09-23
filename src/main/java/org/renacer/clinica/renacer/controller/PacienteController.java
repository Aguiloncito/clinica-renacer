package main.java.org.renacer.clinica.renacer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import main.java.org.renacer.clinica.renacer.dto.response.CitaAgendaResponse;
import main.java.org.renacer.clinica.renacer.model.Medico;
import main.java.org.renacer.clinica.renacer.repository.PacienteRepository;
import main.java.org.renacer.clinica.renacer.service.CitaService;
import main.java.org.renacer.clinica.renacer.service.MedicoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class PacienteController {

    @FXML private Label lblNombrePaciente;
    @FXML private Button btnCerrarSesion;

    // --- Controles de Agendar Cita ---
    @FXML private ComboBox<Medico> cmbMedicos;
    @FXML private DatePicker dpFechaCita;
    @FXML private TextField txtHoraCita;

    // --- Tabla Citas ---
    @FXML private TableView<CitaAgendaResponse> tblMisCitas;
    @FXML private TableColumn<CitaAgendaResponse, Integer> colCitaId;
    @FXML private TableColumn<CitaAgendaResponse, String> colCitaMedico;
    @FXML private TableColumn<CitaAgendaResponse, String> colCitaEspecialidad;
    @FXML private TableColumn<CitaAgendaResponse, String> colCitaFecha;
    @FXML private TableColumn<CitaAgendaResponse, String> colCitaEstado;

    // --- Tabla Recetas ---
    @FXML private TableView<Map<String, String>> tblMisRecetas;
    @FXML private TableColumn<Map<String, String>, String> colRecetaFecha;
    @FXML private TableColumn<Map<String, String>, String> colRecetaMedico;
    @FXML private TableColumn<Map<String, String>, String> colRecetaDiagnostico;
    @FXML private TableColumn<Map<String, String>, String> colRecetaMedicamentos;
    @FXML private TableColumn<Map<String, String>, String> colRecetaIndicaciones;

    // Servicios
    private final MedicoService medicoService = new MedicoService();
    private final CitaService citaService = new CitaService();
    private final PacienteRepository pacienteRepo = new PacienteRepository();

    private Integer idPacienteLogueado = 1;

    @FXML
    public void initialize() {
        // Mapeo directo de propiedades del DTO CitaAgendaResponse
        colCitaId.setCellValueFactory(new PropertyValueFactory<>("idCita"));
        colCitaMedico.setCellValueFactory(new PropertyValueFactory<>("medico"));
        colCitaEspecialidad.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colCitaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCitaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Mapeo columnas de la tabla de recetas
        colRecetaFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fechaHora")));
        colRecetaMedico.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("medico")));
        colRecetaDiagnostico.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("diagnostico")));
        colRecetaMedicamentos.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("medicamentos")));
        colRecetaIndicaciones.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("indicaciones")));

        cargarMedicos();
    }

    public void setPacienteAutenticado(Integer idPaciente, String nombreCompleto) {
        this.idPacienteLogueado = idPaciente;
        if (lblNombrePaciente != null) {
            lblNombrePaciente.setText("Bienvenido, " + nombreCompleto);
        }
        cargarMisCitas();
        cargarMisRecetas();
    }

    @FXML
    public void onAgendarCita() {
        Medico medico = cmbMedicos.getValue();
        LocalDate fecha = dpFechaCita.getValue();
        String horaTexto = txtHoraCita.getText();

        try {
            if (medico == null) {
                throw new IllegalArgumentException("Por favor, seleccione un médico.");
            }
            if (fecha == null) {
                throw new IllegalArgumentException("Por favor, seleccione una fecha para la cita.");
            }
            if (horaTexto == null || horaTexto.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la hora manualmente (Ejemplo: 09:30 o 14:15).");
            }

            LocalTime hora;
            try {
                hora = LocalTime.parse(horaTexto.trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("El formato de hora debe ser de 24 horas (HH:mm). Ejemplo: 09:30 o 14:15.");
            }

            citaService.agendar(
                    String.valueOf(this.idPacienteLogueado),
                    String.valueOf(medico.getIdMedico()),
                    fecha,
                    hora
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Cita Agendada", "La cita con Dr. " + medico.getNombres() + " " + medico.getApellidos() + " ha sido registrada correctamente.");
            limpiarFormulario();
            cargarMisCitas();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación de Datos", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", "No se pudo agendar la cita: " + e.getMessage());
        }
    }

    @FXML
    public void onCerrarSesion() {
        Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
        stage.close();
    }

    private void cargarMedicos() {
        try {
            cmbMedicos.setItems(FXCollections.observableArrayList(medicoService.listar()));
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar la lista de médicos: " + e.getMessage());
        }
    }

    private void cargarMisCitas() {
        if (idPacienteLogueado == null || idPacienteLogueado == 0) return;
        try {
            List<CitaAgendaResponse> lista = citaService.pendientes(String.valueOf(idPacienteLogueado));
            ObservableList<CitaAgendaResponse> obsList = FXCollections.observableArrayList(lista);
            tblMisCitas.setItems(obsList);
            tblMisCitas.refresh();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar las citas: " + e.getMessage());
        }
    }

    private void cargarMisRecetas() {
        if (idPacienteLogueado == null || idPacienteLogueado == 0) return;
        try {
            tblMisRecetas.setItems(FXCollections.observableArrayList(
                    pacienteRepo.obtenerRecetasPorPaciente(idPacienteLogueado)
            ));
            tblMisRecetas.refresh();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar las recetas: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        cmbMedicos.getSelectionModel().clearSelection();
        dpFechaCita.setValue(null);
        txtHoraCita.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}