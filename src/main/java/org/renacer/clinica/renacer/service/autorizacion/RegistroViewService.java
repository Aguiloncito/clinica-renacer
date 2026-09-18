package main.java.org.renacer.clinica.renacer.service.autorizacion;

import main.java.org.renacer.clinica.renacer.dto.request.RegistroRequest;
import main.java.org.renacer.clinica.renacer.dto.response.RegistroResponse;
import main.java.org.renacer.clinica.renacer.repository.autorizacion.RegistroRepository;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio para manejar la lógica de registro de pacientes y sus usuarios
 * @author AGUILON
 */
public class RegistroViewService {

    private final RegistroRepository registroRepository;

    public RegistroViewService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    public RegistroResponse registrar(RegistroRequest registroRequest) throws Exception {
        // 1. Validar que la petición no sea nula
        if (registroRequest == null) {
            throw new RuntimeException("La solicitud de registro está vacía");
        }

        // 2. Validar campos obligatorios de autenticación (tabla usuarios)
        if (registroRequest.getUsuario() == null || registroRequest.getUsuario().isBlank()
                || registroRequest.getPasswordHash()== null || registroRequest.getPasswordHash().isBlank()) {
            throw new RuntimeException("El nombre de usuario y la contraseña no pueden estar vacíos");
        }

        // 3. Validar campos obligatorios del paciente (tabla pacientes)
        if (registroRequest.getNombres() == null || registroRequest.getNombres().isBlank()
                || registroRequest.getApellidos() == null || registroRequest.getApellidos().isBlank()
                || registroRequest.getFechaNacimiento() == null || registroRequest.getFechaNacimiento().isBlank()) {
            throw new RuntimeException("Los campos de nombres, apellidos y fecha de nacimiento son obligatorios");
        }

        // 4. Validar que el nombre de usuario no exista ya en la tabla usuarios[cite: 1]
        if (registroRepository.existsByUsuario(registroRequest.getUsuario())) {
            throw new RuntimeException("Ese nombre de usuario ya está registrado");
        }

        // 5. Encriptar contraseña (la columna password_hash es VARCHAR(255), BCrypt encaja perfecto)[cite: 1]
        String contrasenaHasheada = BCrypt.hashpw(registroRequest.getPasswordHash(), BCrypt.gensalt(12));

        // 6. Guardar usando el repositorio (que ahora inserta en pacientes y luego en usuarios)
        boolean guardado = registroRepository.save(registroRequest, contrasenaHasheada);

        if (!guardado) {
            throw new RuntimeException("No se pudo registrar al paciente en el sistema");
        }

        // 7. Retornar respuesta exitosa
        return new RegistroResponse(registroRequest.getUsuario(), "paciente");
    }
}