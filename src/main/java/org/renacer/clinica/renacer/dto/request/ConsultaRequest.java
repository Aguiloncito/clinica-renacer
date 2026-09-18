/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.dto.request;

/**
 * Datos que el médico llena al finalizar una cita:
 * la consulta (motivo, diagnóstico, observaciones) y, opcionalmente, la receta.
 *
 * @author AGUILON
 */
public class ConsultaRequest {
 
    private int idCita;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private String medicamentos;   // si va vacío, no se genera receta
    private String indicaciones;
 
    public ConsultaRequest() {
    }
 
    public ConsultaRequest(int idCita, String motivo, String diagnostico, String observaciones,
                           String medicamentos, String indicaciones) {
        this.idCita = idCita;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.observaciones = observaciones;
        this.medicamentos = medicamentos;
        this.indicaciones = indicaciones;
    }
 
    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }
 
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
 
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
 
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
 
    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }
 
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
 
    public boolean llevaReceta() {
        return medicamentos != null && !medicamentos.trim().isEmpty();
    }
}
 