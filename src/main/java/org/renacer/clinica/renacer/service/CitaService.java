/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.service;   
import main.java.org.renacer.clinica.renacer.dto.request.ConsultaRequest;
import main.java.org.renacer.clinica.renacer.dto.response.CitaAgendaResponse;
import main.java.org.renacer.clinica.renacer.repository.CitaRepository;
import main.java.org.renacer.clinica.renacer.repository.ConsultaRepository;
import main.java.org.renacer.clinica.renacer.repository.MedicoRepository;
import main.java.org.renacer.clinica.renacer.util.EstadoCita;
 
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
/**
 * Reglas de negocio del módulo: flujo de estados, registro de consulta/receta
 * y validación de traslapes en la agenda.
 *
 * @author AGUILON
 */
public class CitaService {
 
    /** Duración fija de cada consulta (la tabla citas solo guarda fecha_hora). */
    public static final int DURACION_MINUTOS = 30;
 
    /** Jornada de la clínica. */
    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE   = LocalTime.of(17, 0);
 
    private final CitaRepository citaRepo = new CitaRepository();
    private final ConsultaRepository consultaRepo = new ConsultaRepository();
 
 
    public List<CitaAgendaResponse> agendaPorDia(LocalDate dia, String idMedico) throws SQLException {
        return citaRepo.agendaPorDia(dia, MedicoRepository.parseId(idMedico));
    }
 
    public List<CitaAgendaResponse> pendientes(String idMedico) throws SQLException {
        return citaRepo.pendientes(MedicoRepository.parseId(idMedico));
    }
 
    public Map<LocalDate, Integer> conteoPorDia(LocalDate mes, String idMedico) throws SQLException {
        return citaRepo.conteoPorDia(mes, MedicoRepository.parseId(idMedico));
    }
 
    // ------------------- flujo de estados -------------------
 
    /** programada -> en curso */
    public void iniciarConsulta(CitaAgendaResponse cita) throws SQLException {
        exigirSeleccion(cita);
        EstadoCita.validarTransicion(cita.getEstado(), EstadoCita.EN_CURSO);
        citaRepo.cambiarEstado(cita.getIdCita(), EstadoCita.EN_CURSO);
    }
 
    /** en curso -> finalizada, guardando consulta y receta en una transacción. */
    public void finalizarConsulta(CitaAgendaResponse cita, ConsultaRequest req) throws SQLException {
        exigirSeleccion(cita);
        EstadoCita.validarTransicion(cita.getEstado(), EstadoCita.FINALIZADA);
 
        if (req.getDiagnostico() == null || req.getDiagnostico().trim().isEmpty()) {
            throw new IllegalArgumentException("El diagnóstico es obligatorio para finalizar la consulta.");
        }
        if (req.llevaReceta()
                && (req.getIndicaciones() == null || req.getIndicaciones().trim().isEmpty())) {
            throw new IllegalArgumentException("Si receta medicamentos debe escribir las indicaciones.");
        }
        if (consultaRepo.existeConsulta(cita.getIdCita())) {
            throw new IllegalArgumentException("Esa cita ya tiene una consulta registrada.");
        }
 
        req.setIdCita(cita.getIdCita());
        consultaRepo.registrarConsultaConReceta(req, EstadoCita.FINALIZADA);
    }
 
    public void cancelar(CitaAgendaResponse cita) throws SQLException {
        exigirSeleccion(cita);
        EstadoCita.validarTransicion(cita.getEstado(), EstadoCita.CANCELADA);
        citaRepo.cambiarEstado(cita.getIdCita(), EstadoCita.CANCELADA);
    }
 
    /** Receta adicional para una cita que ya fue finalizada. */
    public void agregarReceta(CitaAgendaResponse cita, String medicamentos, String indicaciones) throws SQLException {
        exigirSeleccion(cita);
        if (!cita.tieneConsulta()) {
            throw new IllegalArgumentException("Primero debe finalizar la consulta para poder recetar.");
        }
        if (medicamentos == null || medicamentos.trim().isEmpty()) {
            throw new IllegalArgumentException("Escriba los medicamentos de la receta.");
        }
        consultaRepo.agregarReceta(cita.getIdConsulta(), medicamentos.trim(),
                indicaciones == null ? "" : indicaciones.trim());
    }
 
    // ------------------- agendamiento sin cruces -------------------
 
    public void agendar(String idPaciente, String idMedico, LocalDate dia, LocalTime hora) throws SQLException {
        int pac = MedicoRepository.parseId(idPaciente);
        int med = MedicoRepository.parseId(idMedico);
 
        if (pac == 0) throw new IllegalArgumentException("Seleccione un paciente.");
        if (med == 0) throw new IllegalArgumentException("Seleccione un médico.");
        if (dia == null || hora == null) throw new IllegalArgumentException("Seleccione fecha y hora.");
 
        LocalDateTime inicio = LocalDateTime.of(dia, hora);
 
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en el pasado.");
        }
        if (hora.isBefore(APERTURA) || hora.plusMinutes(DURACION_MINUTOS).isAfter(CIERRE)) {
            throw new IllegalArgumentException("El horario de atención es de " + APERTURA + " a " + CIERRE + ".");
        }
        if (citaRepo.existeCruce(med, inicio, DURACION_MINUTOS, 0)) {
            throw new IllegalArgumentException("El médico ya tiene una cita que se cruza con ese horario.");
        }
        if (citaRepo.pacienteOcupado(pac, inicio, DURACION_MINUTOS, 0)) {
            throw new IllegalArgumentException("El paciente ya tiene otra cita a esa misma hora.");
        }
 
        citaRepo.insertar(pac, med, inicio);
    }
 
    public void reprogramar(CitaAgendaResponse cita, LocalDate dia, LocalTime hora) throws SQLException {
        exigirSeleccion(cita);
        if (EstadoCita.FINALIZADA.equals(cita.getEstado())) {
            throw new IllegalArgumentException("Una cita finalizada no se puede reprogramar.");
        }
        LocalDateTime inicio = LocalDateTime.of(dia, hora);
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nueva fecha y hora ya pasaron.");
        }
        if (citaRepo.existeCruce(cita.getIdMedico(), inicio, DURACION_MINUTOS, cita.getIdCita())) {
            throw new IllegalArgumentException("El nuevo horario se cruza con otra cita del médico.");
        }
        citaRepo.reprogramar(cita.getIdCita(), inicio);
    }
 
    /** Horas libres del médico en un día, en bloques de DURACION_MINUTOS. */
    public List<LocalTime> horariosDisponibles(String idMedico, LocalDate dia) throws SQLException {
        List<CitaAgendaResponse> ocupadas = agendaPorDia(dia, idMedico);
        List<LocalTime> libres = new ArrayList<>();
 
        LocalTime cursor = APERTURA;
        while (!cursor.plusMinutes(DURACION_MINUTOS).isAfter(CIERRE)) {
            LocalDateTime ini = LocalDateTime.of(dia, cursor);
            LocalDateTime fin = ini.plusMinutes(DURACION_MINUTOS);
 
            boolean choca = false;
            for (CitaAgendaResponse c : ocupadas) {
                if (!EstadoCita.ocupaAgenda(c.getEstado())) continue;
                LocalDateTime cIni = c.getFechaHora();
                LocalDateTime cFin = cIni.plusMinutes(DURACION_MINUTOS);
                if (ini.isBefore(cFin) && cIni.isBefore(fin)) {
                    choca = true;
                    break;
                }
            }
            if (!choca && ini.isAfter(LocalDateTime.now())) {
                libres.add(cursor);
            }
            cursor = cursor.plusMinutes(DURACION_MINUTOS);
        }
        return libres;
    }
 
    private void exigirSeleccion(CitaAgendaResponse cita) {
        if (cita == null) {
            throw new IllegalArgumentException("Seleccione una cita de la tabla.");
        }
    }
}


