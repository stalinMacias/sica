package sica.common.asistencias;

import sica.common.justificantes.JustificanteInterface;
import sica.common.objetos.Registro;

/**
 *  Modificada octubre 27, 2016, para darle compatibilidad con EYS
 * @author Uh, posterior Cuvalles.SicaTeam
 */
public class AsistenciaClase{
    
    private String usuario;
    private String nombre;
    private String tipo;
    private String materia;
    private String horario;
    private String crn;
    //-- nuevas para EYS --
    private String duracion;
    
    
    private Registro registro;
    private JustificanteInterface justificante;
    
    public String getUsuario() {
        return usuario;
    }

    public JustificanteInterface getJustificante() {
        return justificante;
    }

    public void setJustificante(JustificanteInterface justificante) {
        this.justificante = justificante;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Registro getRegistro() {
        return registro;
    }

    public void setRegistro(Registro registro) {
        this.registro = registro;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    
}
