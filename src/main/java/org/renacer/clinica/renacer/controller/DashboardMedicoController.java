package main.java.org.renacer.clinica.renacer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import main.java.org.renacer.clinica.renacer.dto.request.ConsultaRequest;
import main.java.org.renacer.clinica.renacer.dto.response.CitaAgendaResponse;
import main.java.org.renacer.clinica.renacer.model.Medico;
import main.java.org.renacer.clinica.renacer.model.Paciente;
import main.java.org.renacer.clinica.renacer.repository.PacienteRepository;
import main.java.org.renacer.clinica.renacer.service.CitaService;
import main.java.org.renacer.clinica.renacer.util.Alertas;
import main.java.org.renacer.clinica.renacer.util.EstadoCita;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dashboard del médico:
 *  - Calendario por día con las citas (médico, especialidad, paciente).
 *  - Lista de citas pendientes.
 *  - Flujo de estados: programada -> en curso -> finalizada.
 *  - Registro de consulta y receta al finalizar.
 *
 * @author AGUILON
 */
public class DashboardMedicoController implements Initializable {

    // ---- filtros / calendario ----
    @FXML private ComboBox<Medico> cboMedico;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblResumen;

    // ---- tabla de la agenda del día ----
    @FXML private TableView<CitaAgendaResponse> tblAgenda;
    @FXML private TableColumn<CitaAgendaResponse, String> colHora;
    @FXML private TableColumn<CitaAgendaResponse, String> colPaciente;
    @FXML private TableColumn<CitaAgendaResponse, String> colMedico;
    @FXML private TableColumn<CitaAgendaResponse, String> colEspecialidad;
    @FXML private TableColumn<CitaAgendaResponse, String> colEstado;

    // ---- pendientes ----
    @FXML private ListView<CitaAgendaResponse> lstPendientes;

    // ---- consulta / receta ----
    @FXML private Label lblCitaSeleccionada;
    @FXML private TextField txtMotivo;
    @FXML private TextArea txtDiagnostico;
    @FXML private TextArea txtObservaciones;
    @FXML private TextArea txtMedicamentos;
    @FXML private TextArea txtIndicaciones;
    @FXML private Button btnIniciar;
    @FXML private Button btnFinalizar;
    @FXML private Button btnCancelar;
    @FXML private Button btnReceta;

    // ---- agendar nueva cita ----
    @FXML private ComboBox<Paciente> cboPacienteNuevo;
    @FXML private ComboBox<LocalTime> cboHoraLibre;

    private final CitaService citaService = new CitaService();
    private final PacienteRepository pacienteRepository = new PacienteRepository();

    private final ObservableList<CitaAgendaResponse> agenda =
            FXCollections.observableArrayList();

    private final ObservableList<CitaAgendaResponse> pendientes =
            FXCollections.observableArrayList();

    /** Conteo de citas por día del mes visible, para pintar el calendario. */
    private Map<LocalDate, Integer> conteoMes = new java.util.HashMap<>();

    /** null = todos los médicos. */
    private static final Medico TODOS =
            new Medico("0", "Todos", "los médicos", "—", "—");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarListaPendientes();
        configurarCombos();

        dpFecha.setValue(LocalDate.now());

        cargarMedicos();
        cargarPacientes();
        refrescarTodo();
    }

    // =====================================================
    // Configuración
    // =====================================================

    private void configurarTabla() {
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        colMedico.setCellValueFactory(new PropertyValueFactory<>("medico"));
        colEspecialidad.setCellValueFactory(
                new PropertyValueFactory<>("especialidad"));
        colEstado.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getEstado()));

        // Color por estado
        tblAgenda.setRowFactory(tv -> new TableRow<CitaAgendaResponse>() {
            @Override
            protected void updateItem(CitaAgendaResponse c, boolean empty) {
                super.updateItem(c, empty);

                if (empty || c == null) {
                    setStyle("");
                    return;
                }

                switch (c.getEstado()) {
                    case EstadoCita.EN_CURSO:
                        setStyle("-fx-background-color: #fff3cd;");
                        break;

                    case EstadoCita.FINALIZADA:
                        setStyle("-fx-background-color: #d4edda;");
                        break;

                    case EstadoCita.CANCELADA:
                        setStyle(
                                "-fx-background-color: #f0f0f0; "
                                + "-fx-text-fill: #9e9e9e;");
                        break;

                    default:
                        setStyle("");
                }
            }
        });

        tblAgenda.setItems(agenda);

        tblAgenda.getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ant, act) -> mostrarSeleccion(act));
    }

    private void configurarListaPendientes() {
        lstPendientes.setItems(pendientes);

        lstPendientes.setCellFactory(lv ->
                new ListCell<CitaAgendaResponse>() {

            @Override
            protected void updateItem(
                    CitaAgendaResponse c, boolean empty) {

                super.updateItem(c, empty);

                if (empty || c == null) {
                    setText(null);
                } else {
                    setText(
                            c.getFecha() + "  "
                            + c.getHora() + "   "
                            + c.getPaciente()
                            + "\n   "
                            + c.getMedico()
                            + " · "
                            + c.getEspecialidad()
                            + "  ["
                            + c.getEstado()
                            + "]"
                    );
                }
            }
        });

        // Al hacer clic en una pendiente, salta a ese día
        // y la selecciona en la tabla
        lstPendientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ant, act) -> {

            if (act == null) {
                return;
            }

            dpFecha.setValue(act.getFechaHora().toLocalDate());
            cargarAgenda();

            for (CitaAgendaResponse c : agenda) {
                if (c.getIdCita() == act.getIdCita()) {
                    tblAgenda.getSelectionModel().select(c);
                    break;
                }
            }
        });
    }

    private void configurarCombos() {

        cboMedico.setConverter(new StringConverter<Medico>() {

            @Override
            public String toString(Medico m) {
                if (m == null) {
                    return "";
                }

                if ("0".equals(m.getIdMedico())) {
                    return "Todos los médicos";
                }

                return m.getNombres()
                        + " "
                        + m.getApellidos()
                        + " ("
                        + m.getEspecialidad()
                        + ")";
            }

            @Override
            public Medico fromString(String s) {
                return null;
            }
        });

        cboPacienteNuevo.setConverter(
                new StringConverter<Paciente>() {

            @Override
            public String toString(Paciente p) {
                return p == null
                        ? ""
                        : p.getNombres()
                                + " "
                                + p.getApellidos();
            }

            @Override
            public Paciente fromString(String s) {
                return null;
            }
        });

        cboMedico.valueProperty()
                .addListener((o, a, b) -> refrescarTodo());

        dpFecha.valueProperty()
                .addListener((o, a, b) -> refrescarTodo());

        marcarDiasConCitas();
    }

    /** Pinta en el DatePicker los días que ya tienen citas. */
    private void marcarDiasConCitas() {

        dpFecha.setDayCellFactory(picker ->
                new DateCell() {

            @Override
            public void updateItem(
                    LocalDate fecha, boolean empty) {

                super.updateItem(fecha, empty);

                setTooltip(null);
                setStyle("");

                if (empty || fecha == null) {
                    return;
                }

                Integer total = conteoMes.get(fecha);

                if (total != null && total > 0) {
                    setStyle(
                            "-fx-background-color: #cfe2ff; "
                            + "-fx-font-weight: bold;");

                    setTooltip(
                            new Tooltip(total + " cita(s)"));
                }
            }
        });
    }

    // =====================================================
    // Carga de datos
    // =====================================================

    private String idMedicoFiltro() {
        Medico m = cboMedico.getValue();
        return m == null ? "0" : m.getIdMedico();
    }

    private void cargarMedicos() {
        try {

            ObservableList<Medico> lista =
                    FXCollections.observableArrayList(TODOS);

            lista.addAll(
                    new main.java.org.renacer.clinica.renacer.service.MedicoService()
                            .listar());

            cboMedico.setItems(lista);
            cboMedico.setValue(TODOS);

        } catch (SQLException ex) {
            Alertas.error(
                    "No se pudieron cargar los médicos: "
                    + ex.getMessage());
        }
    }

    private void cargarPacientes() {
        try {

            // PacienteRepository tiene obtenerTodos(), no listar()
            cboPacienteNuevo.setItems(
                    FXCollections.observableArrayList(
                            pacienteRepository.obtenerTodos()));

        } catch (SQLException ex) {
            Alertas.error(
                    "No se pudieron cargar los pacientes: "
                    + ex.getMessage());
        }
    }
}
 
    private void refrescarTodo() {
        cargarConteoMes();
        cargarAgenda();
        cargarPendientes();
        cargarHorasLibres();
    }

    private void cargarConteoMes() {
        try {

            LocalDate base =
                    dpFecha.getValue() == null
                            ? LocalDate.now()
                            : dpFecha.getValue();

            conteoMes =
                    citaService.conteoPorDia(
                            base,
                            idMedicoFiltro());

            marcarDiasConCitas();

        } catch (SQLException ex) {
            conteoMes = new java.util.HashMap<>();
        }
    }

    private void cargarAgenda() {
        try {

            LocalDate dia =
                    dpFecha.getValue() == null
                            ? LocalDate.now()
                            : dpFecha.getValue();

            List<CitaAgendaResponse> datos =
                    citaService.agendaPorDia(
                            dia,
                            idMedicoFiltro());

            agenda.setAll(datos);

            long activas =
                    datos.stream()
                            .filter(c ->
                                    EstadoCita.ocupaAgenda(
                                            c.getEstado()))
                            .count();

            lblResumen.setText(
                    "Citas del "
                    + dia
                    + ": "
                    + activas
                    + " activa(s) de "
                    + datos.size()
                    + " registrada(s).");

        } catch (SQLException ex) {
            Alertas.error(
                    "No se pudo cargar la agenda: "
                    + ex.getMessage());
        }
    }

    private void cargarPendientes() {
        try {

            pendientes.setAll(
                    citaService.pendientes(
                            idMedicoFiltro()));

        } catch (SQLException ex) {
            Alertas.error(
                    "No se pudieron cargar las citas pendientes: "
                    + ex.getMessage());
        }
    }

    private void cargarHorasLibres() {
        try {

            String idMedico = idMedicoFiltro();

            if ("0".equals(idMedico)
                    || dpFecha.getValue() == null) {

                cboHoraLibre.getItems().clear();
                return;
            }

            List<LocalTime> libres =
                    citaService.horariosDisponibles(
                            idMedico,
                            dpFecha.getValue());

            cboHoraLibre.setItems(
                    FXCollections.observableArrayList(libres));

            if (!libres.isEmpty()) {
                cboHoraLibre.setValue(libres.get(0));
            }

        } catch (SQLException ex) {
            Alertas.error(
                    "No se pudo calcular la disponibilidad: "
                    + ex.getMessage());
        }
    }

    // =====================================================
    // Selección
    // =====================================================

    private void mostrarSeleccion(CitaAgendaResponse c) {

        if (c == null) {

            lblCitaSeleccionada.setText(
                    "Ninguna cita seleccionada");

            btnIniciar.setDisable(true);
            btnFinalizar.setDisable(true);
            btnCancelar.setDisable(true);
            btnReceta.setDisable(true);

            return;
        }

        lblCitaSeleccionada.setText(
                c.getFecha()
                + " "
                + c.getHora()
                + " · "
                + c.getPaciente()
                + "  |  "
                + c.getMedico()
                + " ("
                + c.getEspecialidad()
                + ")  ·  "
                + c.getEstado());

        String estado = c.getEstado();

        btnIniciar.setDisable(
                !EstadoCita.PROGRAMADA.equals(estado));

        btnFinalizar.setDisable(
                !EstadoCita.EN_CURSO.equals(estado));

        btnCancelar.setDisable(
                EstadoCita.FINALIZADA.equals(estado)
                || EstadoCita.CANCELADA.equals(estado));

        btnReceta.setDisable(
                !c.tieneConsulta());
    }

    // =====================================================
    // Acciones
    // =====================================================

    @FXML
    private void onHoy() {
        dpFecha.setValue(LocalDate.now());
    }

    @FXML
    private void onRefrescar() {
        refrescarTodo();
    }

    @FXML
    private void onIniciarConsulta() {

        CitaAgendaResponse c =
                tblAgenda.getSelectionModel()
                        .getSelectedItem();

        try {

            citaService.iniciarConsulta(c);

            Alertas.info(
                    "La cita pasó a estado 'en curso'.");

            refrescarTodo();

        } catch (IllegalArgumentException ex) {

            Alertas.advertencia(ex.getMessage());

        } catch (SQLException ex) {

            Alertas.error(
                    "Error de base de datos: "
                    + ex.getMessage());
        }
    }

    @FXML
    private void onFinalizarConsulta() {

        CitaAgendaResponse c =
                tblAgenda.getSelectionModel()
                        .getSelectedItem();

        try {

            ConsultaRequest req =
                    new ConsultaRequest(
                            c == null ? 0 : c.getIdCita(),
                            txtMotivo.getText().trim(),
                            txtDiagnostico.getText().trim(),
                            txtObservaciones.getText().trim(),
                            txtMedicamentos.getText().trim(),
                            txtIndicaciones.getText().trim());

            citaService.finalizarConsulta(c, req);

            Alertas.info(
                    req.llevaReceta()
                            ? "Consulta finalizada y receta generada."
                            : "Consulta finalizada.");

            limpiarFormularioConsulta();
            refrescarTodo();

        } catch (IllegalArgumentException ex) {

            Alertas.advertencia(ex.getMessage());

        } catch (SQLException ex) {

            Alertas.error(
                    "Error de base de datos: "
                    + ex.getMessage());
        }
    }

    @FXML
    private void onCancelarCita() {

        CitaAgendaResponse c =
                tblAgenda.getSelectionModel()
                        .getSelectedItem();

        if (c == null) {
            Alertas.advertencia(
                    "Seleccione una cita de la tabla.");
            return;
        }

        if (!Alertas.confirmar(
                "¿Cancelar la cita de "
                + c.getPaciente()
                + " del "
                + c.getFecha()
                + " a las "
                + c.getHora()
                + "?")) {

            return;
        }

        try {

            citaService.cancelar(c);

            Alertas.info("Cita cancelada.");

            refrescarTodo();

        } catch (IllegalArgumentException ex) {

            Alertas.advertencia(ex.getMessage());

        } catch (SQLException ex) {

            Alertas.error(
                    "Error de base de datos: "
                    + ex.getMessage());
        }
    }

    @FXML
    private void onAgregarReceta() {

        CitaAgendaResponse c =
                tblAgenda.getSelectionModel()
                        .getSelectedItem();

        try {

            citaService.agregarReceta(
                    c,
                    txtMedicamentos.getText(),
                    txtIndicaciones.getText());

            Alertas.info(
                    "Receta agregada a la consulta.");

            txtMedicamentos.clear();
            txtIndicaciones.clear();

            refrescarTodo();

        } catch (IllegalArgumentException ex) {

            Alertas.advertencia(ex.getMessage());

        } catch (SQLException ex) {

            Alertas.error(
                    "Error de base de datos: "
                    + ex.getMessage());
        }
    }

    @FXML
    private void onAgendarCita() {

        try {

            Paciente p = cboPacienteNuevo.getValue();

            citaService.agendar(
                    // Se convierte Integer a String
                    p == null
                            ? null
                            : String.valueOf(p.getIdPaciente()),

                    idMedicoFiltro(),
                    dpFecha.getValue(),
                    cboHoraLibre.getValue());

            Alertas.info(
                    "Cita agendada correctamente.");

            refrescarTodo();

        } catch (IllegalArgumentException ex) {

            Alertas.advertencia(ex.getMessage());

        citaService.agendar(
                String.valueOf(p.getIdPaciente()),
                idMedicoFiltro(),
                dpFecha.getValue(),
                cboHoraLibre.getValue()
        );

        Alertas.info("Cita agendada correctamente.");
        refrescarTodo();
    } catch (IllegalArgumentException ex) {
        Alertas.advertencia(ex.getMessage());
    } catch (Exception ex) {
        Alertas.error("Error de base de datos o sistema: " + ex.getMessage());
    }

    @FXML
    private void onCerrarSesion() {

        if (!Alertas.confirmar("¿Desea cerrar sesión?")) {
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/login-view.fxml"));

            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage =
                    (javafx.stage.Stage) tblAgenda.getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Renacer - Iniciar sesión");
            stage.centerOnScreen();

        } catch (Exception ex) {
            Alertas.error(
                    "No se pudo cerrar la sesión: "
                    + ex.getMessage());
        }
    }

    private void limpiarFormularioConsulta() {

        txtMotivo.clear();
        txtDiagnostico.clear();
        txtObservaciones.clear();
        txtMedicamentos.clear();
        txtIndicaciones.clear();
    }
}