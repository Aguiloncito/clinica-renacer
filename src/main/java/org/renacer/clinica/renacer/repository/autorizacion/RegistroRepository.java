package main.java.org.renacer.clinica.renacer.repository.autorizacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import main.java.org.renacer.clinica.renacer.config.DataBaseConnection;
import main.java.org.renacer.clinica.renacer.dto.request.RegistroRequest;

/**
 * Repositorio para el registro de nuevos pacientes y sus credenciales de usuario.
 * @author AGUILON
 */
public class RegistroRepository {

    // Nombre del rol que se asigna a todo usuario que se registra desde esta pantalla
    private static final String NOMBRE_ROL_PACIENTE = "paciente";

    // Busca el id_rol correspondiente a 'paciente' en la tabla roles.
    // Se consulta en vez de dejarlo fijo en el código para no depender del orden
    // en que se hayan insertado los roles en la base de datos.
    private int obtenerIdRolPaciente(Connection conn) throws SQLException {
        String sql = "SELECT id_rol FROM roles WHERE nombre_rol = ?";
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, NOMBRE_ROL_PACIENTE);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_rol");
                }
            }
        }
        throw new SQLException("No existe el rol '" + NOMBRE_ROL_PACIENTE + "' en la tabla roles");
    }

    // Valida que el nombre de usuario no se encuentre registrado en la base de datos
    public boolean existsByUsuario(String usuario) throws SQLException {
        String sql = "SELECT id_usuario FROM usuarios WHERE usuario = ?";
        try (PreparedStatement pstm = DataBaseConnection
                .getConnectionDataBase()
                .prepareStatement(sql)) {
            pstm.setString(1, usuario);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Registra el paciente (id autoincremental) y su usuario con el rol 'paciente' dentro de una transacción
    public boolean save(RegistroRequest registroRequest, String contrasenaHasheada) throws SQLException {
        String sqlPaciente = "INSERT INTO pacientes (nombres, apellidos, fecha_nacimiento, telefono, direccion) VALUES (?, ?, ?, ?, ?)";
        String sqlUsuario = "INSERT INTO usuarios (id_rol, id_paciente, usuario, password_hash) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnectionDataBase();
            conn.setAutoCommit(false); // Inicia la transacción

            int idPacienteGenerado;

            // 1. Insertar en la tabla pacientes y obtener el id_paciente autogenerado
            try (PreparedStatement pstmPaciente = conn.prepareStatement(sqlPaciente, Statement.RETURN_GENERATED_KEYS)) {
                pstmPaciente.setString(1, registroRequest.getNombres());
                pstmPaciente.setString(2, registroRequest.getApellidos());
                pstmPaciente.setDate(3, Date.valueOf(registroRequest.getFechaNacimiento())); // Formato YYYY-MM-DD
                pstmPaciente.setString(4, registroRequest.getTelefono());
                pstmPaciente.setString(5, registroRequest.getDireccion());

                pstmPaciente.executeUpdate();

                try (ResultSet rsKeys = pstmPaciente.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idPacienteGenerado = rsKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Insertar en la tabla usuarios asignando el id_rol de 'paciente' y el id_paciente recién creado
            int idRolPaciente = obtenerIdRolPaciente(conn);
            try (PreparedStatement pstmUsuario = conn.prepareStatement(sqlUsuario)) {
                pstmUsuario.setInt(1, idRolPaciente);
                pstmUsuario.setInt(2, idPacienteGenerado);
                pstmUsuario.setString(3, registroRequest.getUsuario());
                pstmUsuario.setString(4, contrasenaHasheada);

                int filasAfectadas = pstmUsuario.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit(); // Confirma la inserción en ambas tablas
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revierte los cambios en caso de error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }
}