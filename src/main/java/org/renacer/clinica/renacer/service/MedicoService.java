/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.service;

import main.java.org.renacer.clinica.renacer.model.Medico;
import main.java.org.renacer.clinica.renacer.repository.MedicoRepository;
import org.mindrot.jbcrypt.BCrypt;
 
import java.sql.SQLException;
import java.util.List;
 
/**
 * @author AGUILON
 */
public class MedicoService {
 
    private final MedicoRepository repo = new MedicoRepository();
 
    public List<Medico> listar() throws SQLException {
        return repo.listar();
    }
 
    public List<Medico> buscar(String filtro) throws SQLException {
        return repo.buscar(filtro);
    }
 
    /** Crea un médico nuevo junto con su usuario de acceso (rol "medico"). */
    public void crear(Medico m, String usuario, String passwordPlano) throws SQLException {
        validar(m);
        if (repo.existeColegiado(m.getNumeroColegiado(), m.getIdMedico())) {
            throw new IllegalArgumentException("Ya existe un médico con el colegiado " + m.getNumeroColegiado() + ".");
        }
        if (vacio(usuario)) {
            throw new IllegalArgumentException("El usuario de acceso es obligatorio.");
        }
        if (vacio(passwordPlano)) {
            throw new IllegalArgumentException("Debe asignar una contraseña para el médico.");
        }
        if (passwordPlano.trim().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (repo.existeUsuario(usuario.trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre \"" + usuario.trim() + "\".");
        }
        String passwordHasheada = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
        repo.insertarConUsuario(m, usuario.trim(), passwordHasheada);
    }
 
    /** Actualiza los datos de un médico existente (no toca sus credenciales). */
    public void guardar(Medico m) throws SQLException {
        validar(m);
        if (repo.existeColegiado(m.getNumeroColegiado(), m.getIdMedico())) {
            throw new IllegalArgumentException("Ya existe un médico con el colegiado " + m.getNumeroColegiado() + ".");
        }
        if (MedicoRepository.parseId(m.getIdMedico()) == 0) {
            throw new IllegalArgumentException("Use la opción de creación para registrar un médico nuevo.");
        }
        repo.actualizar(m);
    }
 
    public void eliminar(Medico m) throws SQLException {
        if (m == null) {
            throw new IllegalArgumentException("Seleccione un médico de la tabla.");
        }
        int citas = repo.contarCitasActivas(m.getIdMedico());
        if (citas > 0) {
            throw new IllegalArgumentException(
                    "El médico tiene " + citas + " cita(s) registradas. Cancélelas o reasígnelas antes de eliminarlo.");
        }
        repo.eliminar(m.getIdMedico());
    }
 
    private void validar(Medico m) {
        if (vacio(m.getNombres()))         throw new IllegalArgumentException("El nombre es obligatorio.");
        if (vacio(m.getApellidos()))       throw new IllegalArgumentException("Los apellidos son obligatorios.");
        if (vacio(m.getEspecialidad()))    throw new IllegalArgumentException("La especialidad es obligatoria.");
        if (vacio(m.getNumeroColegiado())) throw new IllegalArgumentException("El número de colegiado es obligatorio.");
    }
 
    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}