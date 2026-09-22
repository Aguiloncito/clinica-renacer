package main.java.org.renacer.clinica.renacer.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Una fila de la vista SQL vista_expedientes:
 * paciente -> cita -> médico -> consulta -> receta.
 *
 * idConsulta e idReceta pueden ser null (cita sin consulta, consulta sin receta).
 */
public class ExpedienteResponse {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idCita;
    private LocalDateTime fechaCita;
    private String estado;
    private int idPaciente;
    private String paciente;
    private int idMedico;
    private String medico;
    private String especialidad;
    private Integer idConsulta;
    private LocalDateTime fechaAtencion;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private Integer idReceta;
    private String medicamentos;
    private String indicaciones;

    public ExpedienteResponse() {
    }

    // ---- getters / setters -------------------------------------------------

    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }

    public LocalDateTime getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDateTime fechaCita) { this.fechaCita = fechaCita; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public Integer getIdConsulta() { return idConsulta; }
    public void setIdConsulta(Integer idConsulta) { this.idConsulta = idConsulta; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer idReceta) { this.idReceta = idReceta; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }

    // ---- textos listos para mostrar en las tablas ----------------------------

    public String getFechaCitaTexto() {
        return fechaCita == null ? "-" : fechaCita.format(FECHA_HORA);
    }

    /** Fecha en que el médico registró la consulta (cuándo fue atendido). */
    public String getFechaAtencionTexto() {
        return fechaAtencion == null ? "Sin atender" : fechaAtencion.format(FECHA_HORA);
    }

    public boolean tieneConsulta() {
        return idConsulta != null;
    }

    public boolean tieneReceta() {
        return idReceta != null;
    }
}
