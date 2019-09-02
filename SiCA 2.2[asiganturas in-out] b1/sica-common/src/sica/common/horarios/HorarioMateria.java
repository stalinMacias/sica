package sica.common.horarios;

import sica.common.objetos.Registro;

public class HorarioMateria {

    private String crn;
    private String anio;
    private String ciclo;
    private String bloque;
    private String horario;
    private String duracion = "02:00:00"; //por default 2 hrs que dura una clase estandar
    private String checar = "entrada";
    private String materia;
    private String aula;
    private Registro registroEntrada;

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public Registro getRegistroEntrada() {
        return registroEntrada;
    }

    public void setRegistroEntrada(Registro registroEntrada) {
        this.registroEntrada = registroEntrada;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public String getCiclo() {
        return ciclo;
    }

    public void setCiclo(String ciclo) {
        this.ciclo = ciclo;
    }

    public String getBloque() {
        return bloque;
    }

    public void setBloque(String bloque) {
        this.bloque = bloque;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getChecar() {
        return checar;
    }

    public void setChecar(String checar) {
        this.checar = checar;
    }
    
    
    
    
            
}
