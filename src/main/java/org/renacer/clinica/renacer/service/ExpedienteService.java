package main.java.org.renacer.clinica.renacer.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import main.java.org.renacer.clinica.renacer.dto.response.ExpedienteResponse;
import main.java.org.renacer.clinica.renacer.repository.ExpedienteRepository;

/**
 * Reglas del expediente clínico para el administrador.
 */
public class ExpedienteService {

    private static final int MAX_TEXTO = 300; // VARCHAR(300) en la tabla recetas

    private final ExpedienteRepository repo;

    public ExpedienteService(ExpedienteRepository repo) {
        this.repo = repo;
    }

    public List<ExpedienteResponse> historialPaciente(int idPaciente) throws SQLException {
        if (idPaciente <= 0) {
            throw new IllegalArgumentException("Seleccione un paciente.");
        }
        return repo.historialPaciente(idPaciente);
    }

    public List<ExpedienteResponse> recetasDeMedico(int idMedico) throws SQLException {
        if (idMedico <= 0) {
            throw new IllegalArgumentException("Seleccione un médico.");
        }
        return repo.recetasPorMedico(idMedico);
    }

    public List<ExpedienteResponse> registroConsultas(String filtro, LocalDate desde, LocalDate hasta)
            throws SQLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
        }
        return repo.registroConsultas(filtro, desde, hasta);
    }

    public void actualizarReceta(Integer idReceta, String medicamentos, String indicaciones) throws SQLException {
        if (idReceta == null || idReceta <= 0) {
            throw new IllegalArgumentException("Seleccione una receta de la tabla.");
        }
        String med = medicamentos == null ? "" : medicamentos.trim();
        String ind = indicaciones == null ? "" : indicaciones.trim();

        if (med.isEmpty()) {
            throw new IllegalArgumentException("Los medicamentos son obligatorios.");
        }
        if (med.length() > MAX_TEXTO || ind.length() > MAX_TEXTO) {
            throw new IllegalArgumentException("Medicamentos e indicaciones aceptan máximo " + MAX_TEXTO + " caracteres.");
        }
        int filas = repo.actualizarReceta(idReceta, med, ind.isEmpty() ? null : ind);
        if (filas == 0) {
            throw new IllegalArgumentException("La receta ya no existe. Actualice la lista.");
        }
    }
}
