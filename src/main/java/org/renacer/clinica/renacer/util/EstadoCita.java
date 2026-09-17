/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.util;


/**
 * Estados que maneja la columna citas.estado (VARCHAR(30)).
 * Se guardan en minúscula para respetar los datos que ya existen en la BD.
 *
 * @author AGUILON
 */
public final class EstadoCita {
 
    public static final String PROGRAMADA  = "programada";
    public static final String EN_CURSO    = "en curso";
    public static final String FINALIZADA  = "finalizada";
    public static final String CANCELADA   = "cancelada";
 
    private EstadoCita() {
    }
 
    /** Estados que todavía cuentan como "ocupa el horario del médico". */
    public static boolean ocupaAgenda(String estado) {
        return !CANCELADA.equalsIgnoreCase(normalizar(estado));
    }
 
    /** Los registros viejos traen 'pendiente' o 'confirmada': se tratan como programada. */
    public static String normalizar(String estado) {
        if (estado == null) return PROGRAMADA;
        String e = estado.trim().toLowerCase();
        switch (e) {
            case "pendiente":
            case "confirmada":
                return PROGRAMADA;
            case "atendida":
                return FINALIZADA;
            default:
                return e;
        }
    }
 
    /** Valida el flujo programada -> en curso -> finalizada. */
    public static void validarTransicion(String actual, String nuevo) {
        String a = normalizar(actual);
        String n = normalizar(nuevo);
 
        if (a.equals(n)) {
            throw new IllegalArgumentException("La cita ya está en estado '" + n + "'.");
        }
        if (FINALIZADA.equals(a)) {
            throw new IllegalArgumentException("Una cita finalizada ya no puede cambiar de estado.");
        }
        if (CANCELADA.equals(a)) {
            throw new IllegalArgumentException("Una cita cancelada ya no puede cambiar de estado.");
        }
        if (EN_CURSO.equals(n) && !PROGRAMADA.equals(a)) {
            throw new IllegalArgumentException("Solo una cita programada puede pasar a 'en curso'.");
        }
        if (FINALIZADA.equals(n) && !EN_CURSO.equals(a)) {
            throw new IllegalArgumentException("Primero debe iniciar la consulta (estado 'en curso').");
        }
    }
}