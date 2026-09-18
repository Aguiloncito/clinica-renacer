package main.java.org.renacer.clinica.renacer.service;

import main.java.org.renacer.clinica.renacer.dto.request.PacienteRequest;
import main.java.org.renacer.clinica.renacer.dto.response.PacienteResponse;
import main.java.org.renacer.clinica.renacer.model.Paciente;
import main.java.org.renacer.clinica.renacer.repository.PacienteRepository;

import java.util.List;
import java.util.stream.Collectors;

public class PacienteService {

    private final PacienteRepository pacienteRepository = new PacienteRepository();

    public void registrarPaciente(PacienteRequest req) throws Exception {
        validarCamposObligatorios(req);

        Paciente paciente = new Paciente(
                req.getNombres(),
                req.getApellidos(),
                req.getFechaNacimiento(),
                req.getTelefono(),
                req.getDireccion()
        );
        pacienteRepository.guardar(paciente);
    }

    public void actualizarPaciente(PacienteRequest req) throws Exception {
        if (req.getIdPaciente() == null || req.getIdPaciente() <= 0) {
            throw new Exception("El ID del paciente es inválido.");
        }
        validarCamposObligatorios(req);

        Paciente paciente = new Paciente(
                req.getIdPaciente(),
                req.getNombres(),
                req.getApellidos(),
                req.getFechaNacimiento(),
                req.getTelefono(),
                req.getDireccion()
        );
        pacienteRepository.actualizar(paciente);
    }

    public void eliminarPaciente(Integer idPaciente) throws Exception {
        if (idPaciente == null || idPaciente <= 0) {
            throw new Exception("Debe seleccionar un paciente válido para eliminar.");
        }
        pacienteRepository.eliminar(idPaciente);
    }

    public List<PacienteResponse> listarPacientes() throws Exception {
        return pacienteRepository.obtenerTodos().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public List<PacienteResponse> buscarPacientes(String criterio) throws Exception {
        if (criterio == null || criterio.isBlank()) {
            return listarPacientes();
        }
        return pacienteRepository.buscarPorCriterio(criterio).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    private void validarCamposObligatorios(PacienteRequest req) throws Exception {
        if (req.getNombres() == null || req.getNombres().isBlank() ||
            req.getApellidos() == null || req.getApellidos().isBlank() ||
            req.getFechaNacimiento() == null) {
            throw new Exception("Nombres, Apellidos y Fecha de Nacimiento son campos obligatorios.");
        }
    }

    private PacienteResponse convertirAResponse(Paciente p) {
        return new PacienteResponse(
                p.getIdPaciente(),
                p.getNombres(),
                p.getApellidos(),
                p.getFechaNacimiento(),
                p.getTelefono(),
                p.getDireccion()
        );
    }
}