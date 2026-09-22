package main.java.org.renacer.clinica.renacer.util;

/**
 * Nombres de rol tal como están guardados en la tabla roles (columna nombre_rol).
 */
public final class Roles {

    public static final String ADMINISTRADOR = "administrador";
    public static final String MEDICO        = "medico";
    public static final String PACIENTE      = "paciente";

    private Roles() {
    }

    public static boolean esAdministrador(String rol) {
        return ADMINISTRADOR.equalsIgnoreCase(rol == null ? "" : rol.trim());
    }
}
