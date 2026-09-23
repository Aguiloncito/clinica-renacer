package main.java.org.renacer.clinica.renacer.model;

public class Medico {

    private String idMedico;
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String numeroColegiado;

    public Medico() {
    }

    public Medico(String idMedico, String nombres, String apellidos, String especialidad, String numeroColegiado) {
        this.idMedico = idMedico;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.numeroColegiado = numeroColegiado;
    }

    public String getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(String idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroColegiado() {
        return numeroColegiado;
    }

    public void setNumeroColegiado(String numeroColegiado) {
        this.numeroColegiado = numeroColegiado;
    }

    @Override
    public String toString() {
        if ("0".equals(idMedico)) {
            return nombres + " " + apellidos;
        }
        return "Dr. " + nombres + " " + apellidos + " - " + especialidad;
    }
}