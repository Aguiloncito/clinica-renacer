/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.dto.response;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 
/**
 * Fila de la agenda: ya trae resueltos el nombre del médico, su especialidad
 * y el nombre del paciente (viene del JOIN / de la vista agenda_medicos).
 *
 * @author AGUILON
 */
public class CitaAgendaResponse {
 
    private static final DateTimeFormatter HORA  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    private int idCita;
    private int idMedico;
    private int idPaciente;
    private String medico;
    private String especialidad;
    private String paciente;
    private LocalDateTime fechaHora;
    private String estado;
    private Integer idConsulta;   // null si la cita aún no tiene consulta registrada
 
    public CitaAgendaResponse() {
    }
 
    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }
 
    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
 
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
 
    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }
 
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
 
    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }
 
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
 
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
 
    public Integer getIdConsulta() { return idConsulta; }
    public void setIdConsulta(Integer idConsulta) { this.idConsulta = idConsulta; }
 
    // --- columnas calculadas para el TableView ---
 
    public String getHora() {
        return fechaHora == null ? "" : fechaHora.format(HORA);
    }
 
    public String getFecha() {
        return fechaHora == null ? "" : fechaHora.format(FECHA);
    }
 
    public boolean tieneConsulta() {
        return idConsulta != null && idConsulta > 0;
    }
 
    @Override
    public String toString() {
        return getHora() + " - " + paciente + " (" + medico + ")";
    }
}
 