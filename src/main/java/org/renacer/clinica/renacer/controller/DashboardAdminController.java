package main.java.org.renacer.clinica.renacer.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.java.org.renacer.clinica.renacer.dto.response.ExpedienteResponse;
import main.java.org.renacer.clinica.renacer.dto.response.PacienteResponse;
import main.java.org.renacer.clinica.renacer.model.Medico;
import main.java.org.renacer.clinica.renacer.service.ExpedienteService;
import main.java.org.renacer.clinica.renacer.service.MedicoService;
import main.java.org.renacer.clinica.renacer.service.PacienteService;
import main.java.org.renacer.clinica.renacer.util.Alertas;
import main.java.org.renacer.clinica.renacer.util.sceneManager.SceneManager;

/**
 * Panel del administrador: gestión de médicos, expedientes de pacientes
 * y registro general de consultas.
 */
public class DashboardAdminController implements Initializable {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---- dependencias (inyectadas por SceneManager) ---------------------------
    private final MedicoService medicoService;
    private final PacienteService pacienteService;
    private final ExpedienteService expedienteService;
    private final SceneManager sceneManager;
    private final String nombreAdmin;

    // ---- general ---------------------------------------------------------------
    @FXML private TabPane tabPane;
    @FXML private Tab tabMedicos;
    @FXML private Tab tabExpedientes;
    @FXML private Tab tabConsultas;
    @FXML private Label lblUsuario;

    // ---- pestaña MÉDICOS ---------------------------------------------------------
    @FXML private TextField txtBuscarMedico;
    @FXML private TableView<Medico> tblMedicos;
    @FXML private TableColumn<Medico, String> colMedId;
    @FXML private TableColumn<Medico, String> colMedNombres;
    @FXML private TableColumn<Medico, String> colMedApellidos;
    @FXML private TableColumn<Medico, String> colMedEspecialidad;
    @FXML private TableColumn<Medico, String> colMedColegiado;

    @FXML private Label lblModoMedico;
    @FXML private TextField txtMedicoId;
    @FXML private TextField txtMedicoNombres;
    @FXML private TextField txtMedicoApellidos;
    @FXML private TextField txtMedicoEspecialidad;
    @FXML private TextField txtMedicoColegiado;
    @FXML private Button btnEliminarMedico;

    @FXML private TableView<ExpedienteResponse> tblRecetasMedico;
    @FXML private TableColumn<ExpedienteResponse, String> colRecFecha;
    @FXML private TableColumn<ExpedienteResponse, String> colRecPaciente;
    @FXML private TableColumn<ExpedienteResponse, String> colRecDiagnostico;
    @FXML private TableColumn<ExpedienteResponse, String> colRecMedicamentos;
    @FXML private TableColumn<ExpedienteResponse, String> colRecIndicaciones;
    @FXML private Label lblRecetaSeleccionada;
    @FXML private TextArea txtRecetaMedicamentos;
    @FXML private TextArea txtRecetaIndicaciones;
    @FXML private Button btnActualizarReceta;

    // ---- pestaña EXPEDIENTES -----------------------------------------------------
    @FXML private TextField txtBuscarPaciente;
    @FXML private TableView<PacienteResponse> tblPacientes;
    @FXML private TableColumn<PacienteResponse, String> colPacNombres;
    @FXML private TableColumn<PacienteResponse, String> colPacApellidos;
    @FXML private TableColumn<PacienteResponse, String> colPacNacimiento;
    @FXML private TableColumn<PacienteResponse, String> colPacTelefono;

    @FXML private Label lblExpedienteNombre;
    @FXML private Label lblExpedienteDatos;
    @FXML private Label lblExpedienteResumen;
    @FXML private TableView<ExpedienteResponse> tblHistorial;
    @FXML private TableColumn<ExpedienteResponse, String> colHisFechaCita;
    @FXML private TableColumn<ExpedienteResponse, String> colHisAtendido;
    @FXML private TableColumn<ExpedienteResponse, String> colHisMedico;
    @FXML private TableColumn<ExpedienteResponse, String> colHisEstado;
    @FXML private TableColumn<ExpedienteResponse, String> colHisDiagnostico;
    @FXML private TableColumn<ExpedienteResponse, String> colHisReceta;
    @FXML private TextArea txtDetalleHistorial;

    // ---- pestaña REGISTRO DE CONSULTAS ---------------------------------------------
    @FXML private TextField txtFiltroConsultas;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblResumenConsultas;
    @FXML private TableView<ExpedienteResponse> tblConsultas;
    @FXML private TableColumn<ExpedienteResponse, String> colConFechaCita;
    @FXML private TableColumn<ExpedienteResponse, String> colConAtendido;
    @FXML private TableColumn<ExpedienteResponse, String> colConPaciente;
    @FXML private TableColumn<ExpedienteResponse, String> colConMedico;
    @FXML private TableColumn<ExpedienteResponse, String> colConEspecialidad;
    @FXML private TableColumn<ExpedienteResponse, String> colConEstado;
    @FXML private TableColumn<ExpedienteResponse, String> colConMotivo;
    @FXML private TableColumn<ExpedienteResponse, String> colConDiagnostico;
    @FXML private TableColumn<ExpedienteResponse, String> colConRecetas;
    @FXML private TextArea txtDetalleConsulta;
    @FXML private Button btnVerExpediente;

    // ---- constructor ---------------------------------------------------------------
    public DashboardAdminController(MedicoService medicoService,
                                    PacienteService pacienteService,
                                    ExpedienteService expedienteService,
                                    SceneManager sceneManager,
                                    String nombreAdmin) {
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
        this.expedienteService = expedienteService;
        this.sceneManager = sceneManager;
        this.nombreAdmin = nombreAdmin;
    }

    // ============================================================================
    //  INICIALIZACIÓN
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblUsuario.setText("Sesión: " + nombreAdmin);

        configurarColumnas();
        configurarSelecciones();

        btnEliminarMedico.disableProperty().bind(tblMedicos.getSelectionModel().selectedItemProperty().isNull());
        btnActualizarReceta.disableProperty().bind(tblRecetasMedico.getSelectionModel().selectedItemProperty().isNull());
        btnVerExpediente.disableProperty().bind(tblConsultas.getSelectionModel().selectedItemProperty().isNull());

        // Los mensajes de error usan showAndWait, por eso se carga cuando la vista ya terminó de armarse.
        Platform.runLater(() -> {
            cargarMedicos("");
            cargarPacientes("");
            cargarConsultas(false);
        });
    }

    private void configurarColumnas() {
        // Médicos
        columna(colMedId, Medico::getIdMedico);
        columna(colMedNombres, Medico::getNombres);
        columna(colMedApellidos, Medico::getApellidos);
        columna(colMedEspecialidad, Medico::getEspecialidad);
        columna(colMedColegiado, Medico::getNumeroColegiado);

        // Recetas del médico
        columna(colRecFecha, ExpedienteResponse::getFechaCitaTexto);
        columna(colRecPaciente, ExpedienteResponse::getPaciente);
        columna(colRecDiagnostico, ExpedienteResponse::getDiagnostico);
        columna(colRecMedicamentos, ExpedienteResponse::getMedicamentos);
        columna(colRecIndicaciones, ExpedienteResponse::getIndicaciones);

        // Pacientes
        columna(colPacNombres, PacienteResponse::getNombres);
        columna(colPacApellidos, PacienteResponse::getApellidos);
        columna(colPacNacimiento, p -> p.getFechaNacimiento() == null ? "" : p.getFechaNacimiento().format(FECHA));
        columna(colPacTelefono, PacienteResponse::getTelefono);

        // Historial del paciente
        columna(colHisFechaCita, ExpedienteResponse::getFechaCitaTexto);
        columna(colHisAtendido, ExpedienteResponse::getFechaAtencionTexto);
        columna(colHisMedico, ExpedienteResponse::getMedico);
        columna(colHisEstado, ExpedienteResponse::getEstado);
        columna(colHisDiagnostico, ExpedienteResponse::getDiagnostico);
        columna(colHisReceta, e -> e.tieneReceta() ? e.getMedicamentos() : "Sin receta");

        // Registro de consultas
        columna(colConFechaCita, ExpedienteResponse::getFechaCitaTexto);
        columna(colConAtendido, ExpedienteResponse::getFechaAtencionTexto);
        columna(colConPaciente, ExpedienteResponse::getPaciente);
        columna(colConMedico, ExpedienteResponse::getMedico);
        columna(colConEspecialidad, ExpedienteResponse::getEspecialidad);
        columna(colConEstado, ExpedienteResponse::getEstado);
        columna(colConMotivo, ExpedienteResponse::getMotivo);
        columna(colConDiagnostico, ExpedienteResponse::getDiagnostico);
        columna(colConRecetas, e -> e.getMedicamentos() == null ? "Sin receta" : e.getMedicamentos());
    }

    private void configurarSelecciones() {
        tblMedicos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, m) -> {
            if (m == null) {
                limpiarRecetaSeleccionada();
                tblRecetasMedico.getItems().clear();
                return;
            }
            llenarFormularioMedico(m);
            cargarRecetasMedico(m);
        });

        tblRecetasMedico.getSelectionModel().selectedItemProperty().addListener((obs, anterior, r) -> {
            if (r == null) {
                limpiarRecetaSeleccionada();
                return;
            }
            lblRecetaSeleccionada.setText("Receta #" + r.getIdReceta() + " - " + r.getPaciente());
            txtRecetaMedicamentos.setText(r.getMedicamentos());
            txtRecetaIndicaciones.setText(r.getIndicaciones() == null ? "" : r.getIndicaciones());
        });

        tblPacientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, p) -> mostrarExpediente(p));

        tblHistorial.getSelectionModel().selectedItemProperty().addListener((obs, anterior, e) ->
                txtDetalleHistorial.setText(e == null ? "" : detalle(e)));

        tblConsultas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, e) ->
                txtDetalleConsulta.setText(e == null ? "" : detalle(e)));
    }

    // ============================================================================
    //  GENERAL
    // ============================================================================
    @FXML
    private void handleCerrarSesion() {
        try {
            sceneManager.showLoginView();
        } catch (Exception e) {
            Alertas.error("No se pudo volver al inicio de sesión: " + e.getMessage());
        }
    }

    // ============================================================================
    //  PESTAÑA 1: MÉDICOS
    // ============================================================================
    @FXML
    private void handleBuscarMedico() {
        cargarMedicos(txtBuscarMedico.getText());
    }

    @FXML
    private void handleVerTodosMedicos() {
        txtBuscarMedico.clear();
        cargarMedicos("");
    }

    @FXML
    private void handleNuevoMedico() {
        tblMedicos.getSelectionModel().clearSelection();
        limpiarFormularioMedico();
    }

    @FXML
    private void handleGuardarMedico() {
        Medico m = new Medico(
                txtMedicoId.getText(),
                txtMedicoNombres.getText().trim(),
                txtMedicoApellidos.getText().trim(),
                txtMedicoEspecialidad.getText().trim(),
                txtMedicoColegiado.getText().trim());
        boolean eraNuevo = txtMedicoId.getText() == null || txtMedicoId.getText().isBlank();
        try {
            medicoService.guardar(m);
            cargarMedicos(txtBuscarMedico.getText());
            seleccionarMedico(m.getIdMedico());
            refrescarDatosClinicos();
            Alertas.info(eraNuevo ? "Médico registrado correctamente." : "Datos del médico actualizados.");
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    @FXML
    private void handleEliminarMedico() {
        Medico m = tblMedicos.getSelectionModel().getSelectedItem();
        if (m == null) {
            Alertas.advertencia("Seleccione un médico de la tabla.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al médico " + m.getNombres() + " " + m.getApellidos() + "?")) {
            return;
        }
        try {
            medicoService.eliminar(m);
            cargarMedicos(txtBuscarMedico.getText());
            limpiarFormularioMedico();
            refrescarDatosClinicos();
            Alertas.info("Médico eliminado.");
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    @FXML
    private void handleActualizarReceta() {
        ExpedienteResponse r = tblRecetasMedico.getSelectionModel().getSelectedItem();
        if (r == null) {
            Alertas.advertencia("Seleccione una receta de la tabla.");
            return;
        }
        try {
            expedienteService.actualizarReceta(r.getIdReceta(),
                    txtRecetaMedicamentos.getText(), txtRecetaIndicaciones.getText());

            Integer idReceta = r.getIdReceta();
            Medico m = tblMedicos.getSelectionModel().getSelectedItem();
            if (m != null) {
                cargarRecetasMedico(m);
                for (ExpedienteResponse fila : tblRecetasMedico.getItems()) {
                    if (idReceta.equals(fila.getIdReceta())) {
                        tblRecetasMedico.getSelectionModel().select(fila);
                        break;
                    }
                }
            }
            refrescarDatosClinicos();
            Alertas.info("Receta actualizada correctamente.");
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    private void cargarMedicos(String filtro) {
        try {
            List<Medico> lista = medicoService.buscar(filtro);
            tblMedicos.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    private void cargarRecetasMedico(Medico m) {
        try {
            int id = Integer.parseInt(m.getIdMedico());
            tblRecetasMedico.setItems(FXCollections.observableArrayList(expedienteService.recetasDeMedico(id)));
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    private void seleccionarMedico(String idMedico) {
        for (Medico fila : tblMedicos.getItems()) {
            if (fila.getIdMedico().equals(idMedico)) {
                tblMedicos.getSelectionModel().select(fila);
                tblMedicos.scrollTo(fila);
                return;
            }
        }
    }

    private void llenarFormularioMedico(Medico m) {
        lblModoMedico.setText("Editando médico #" + m.getIdMedico());
        txtMedicoId.setText(m.getIdMedico());
        txtMedicoNombres.setText(m.getNombres());
        txtMedicoApellidos.setText(m.getApellidos());
        txtMedicoEspecialidad.setText(m.getEspecialidad());
        txtMedicoColegiado.setText(m.getNumeroColegiado());
    }

    private void limpiarFormularioMedico() {
        lblModoMedico.setText("Nuevo médico");
        txtMedicoId.clear();
        txtMedicoNombres.clear();
        txtMedicoApellidos.clear();
        txtMedicoEspecialidad.clear();
        txtMedicoColegiado.clear();
    }

    private void limpiarRecetaSeleccionada() {
        lblRecetaSeleccionada.setText("Ninguna receta seleccionada");
        txtRecetaMedicamentos.clear();
        txtRecetaIndicaciones.clear();
    }

    // ============================================================================
    //  PESTAÑA 2: EXPEDIENTES DE PACIENTES
    // ============================================================================
    @FXML
    private void handleBuscarPaciente() {
        cargarPacientes(txtBuscarPaciente.getText());
    }

    private void cargarPacientes(String filtro) {
        try {
            List<PacienteResponse> lista = pacienteService.buscarPacientes(filtro == null ? "" : filtro);
            tblPacientes.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    /** Arma el expediente (datos personales + historial) del paciente elegido. */
    private void mostrarExpediente(PacienteResponse p) {
        txtDetalleHistorial.clear();
        if (p == null) {
            lblExpedienteNombre.setText("Seleccione un paciente");
            lblExpedienteDatos.setText("");
            lblExpedienteResumen.setText("");
            tblHistorial.getItems().clear();
            return;
        }

        lblExpedienteNombre.setText(p.getNombres() + " " + p.getApellidos() + "  (Paciente #" + p.getIdPaciente() + ")");
        lblExpedienteDatos.setText("Nacimiento: "
                + (p.getFechaNacimiento() == null ? "-" : p.getFechaNacimiento().format(FECHA))
                + "   |   Teléfono: " + vacioComoGuion(p.getTelefono())
                + "   |   Dirección: " + vacioComoGuion(p.getDireccion()));

        try {
            List<ExpedienteResponse> historial = expedienteService.historialPaciente(p.getIdPaciente());
            tblHistorial.setItems(FXCollections.observableArrayList(historial));

            long citas = historial.stream().map(ExpedienteResponse::getIdCita).distinct().count();
            long consultas = historial.stream().filter(ExpedienteResponse::tieneConsulta)
                    .map(ExpedienteResponse::getIdConsulta).distinct().count();
            long recetas = historial.stream().filter(ExpedienteResponse::tieneReceta).count();
            lblExpedienteResumen.setText(citas + " cita(s)   |   " + consultas + " consulta(s)   |   " + recetas + " receta(s)");
        } catch (Exception e) {
            mostrarError(e);
        }
    }

    // ============================================================================
    //  PESTAÑA 3: REGISTRO DE CONSULTAS
    // ============================================================================
    @FXML
    private void handleBuscarConsultas() {
        cargarConsultas(true);
    }

    @FXML
    private void handleLimpiarFiltros() {
        txtFiltroConsultas.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cargarConsultas(true);
    }

    /** Salta al expediente del paciente de la consulta seleccionada y marca esa cita. */
    @FXML
    private void handleVerExpediente() {
        ExpedienteResponse sel = tblConsultas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alertas.advertencia("Seleccione una consulta de la tabla.");
            return;
        }

        txtBuscarPaciente.clear();
        cargarPacientes("");
        for (PacienteResponse p : tblPacientes.getItems()) {
            if (p.getIdPaciente() == sel.getIdPaciente()) {
                tblPacientes.getSelectionModel().select(p);
                tblPacientes.scrollTo(p);
                break;
            }
        }
        for (ExpedienteResponse fila : tblHistorial.getItems()) {
            if (fila.getIdCita() == sel.getIdCita()) {
                tblHistorial.getSelectionModel().select(fila);
                break;
            }
        }
        tabPane.getSelectionModel().select(tabExpedientes);
    }

    private void cargarConsultas(boolean avisarSiHayError) {
        try {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();
            List<ExpedienteResponse> lista =
                    expedienteService.registroConsultas(txtFiltroConsultas.getText(), desde, hasta);
            tblConsultas.setItems(FXCollections.observableArrayList(lista));
            lblResumenConsultas.setText(lista.size() + " consulta(s)");
            txtDetalleConsulta.clear();
        } catch (Exception e) {
            if (avisarSiHayError) {
                mostrarError(e);
            }
        }
    }

    // ============================================================================
    //  UTILIDADES
    // ============================================================================

    /** Después de cambiar un médico o una receta, las otras pestañas se vuelven a leer. */
    private void refrescarDatosClinicos() {
        PacienteResponse p = tblPacientes.getSelectionModel().getSelectedItem();
        if (p != null) {
            mostrarExpediente(p);
        }
        cargarConsultas(false);
    }

    private String detalle(ExpedienteResponse e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Paciente: ").append(vacioComoGuion(e.getPaciente())).append('\n');
        sb.append("Médico: ").append(vacioComoGuion(e.getMedico()))
          .append(" (").append(vacioComoGuion(e.getEspecialidad())).append(")\n");
        sb.append("Cita: ").append(e.getFechaCitaTexto())
          .append("   |   Estado: ").append(vacioComoGuion(e.getEstado())).append('\n');

        if (!e.tieneConsulta()) {
            sb.append("\nEsta cita todavía no tiene una consulta registrada.");
            return sb.toString();
        }

        sb.append("Atendido el: ").append(e.getFechaAtencionTexto()).append('\n');
        sb.append("Motivo: ").append(vacioComoGuion(e.getMotivo())).append('\n');
        sb.append("Diagnóstico: ").append(vacioComoGuion(e.getDiagnostico())).append('\n');
        sb.append("Observaciones: ").append(vacioComoGuion(e.getObservaciones())).append('\n');
        sb.append("Medicamentos: ").append(e.getMedicamentos() == null ? "Sin receta" : e.getMedicamentos()).append('\n');
        sb.append("Indicaciones: ").append(vacioComoGuion(e.getIndicaciones()));
        return sb.toString();
    }

    private String vacioComoGuion(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    /** Configura una columna de texto a partir de un getter (null se muestra vacío). */
    private <T> void columna(TableColumn<T, String> col, Function<T, String> getter) {
        col.setCellValueFactory(cell -> {
            String valor = getter.apply(cell.getValue());
            return new SimpleStringProperty(valor == null ? "" : valor);
        });
    }

    private void mostrarError(Exception e) {
        if (e instanceof IllegalArgumentException) {
            Alertas.advertencia(e.getMessage());
        } else {
            Alertas.error("Ocurrió un error: " + e.getMessage());
        }
    }
}
