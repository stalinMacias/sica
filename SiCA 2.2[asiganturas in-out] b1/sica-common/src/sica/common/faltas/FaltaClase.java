package sica.common.faltas;

import sica.common.justificantes.JustificanteInterface;
import sica.common.objetos.Crn;

public class FaltaClase extends Falta {
    
    private Crn crn;
    private String horario;
    private String dia;   
    private JustificanteInterface justifcante;
    private String checado;
        
    public Crn getCrn() {
        return crn;
    }
    public String getChecado(){
        return checado;
    }

    public void setCrn(Crn crn) {
        this.crn = crn;
    }

    public JustificanteInterface getJustifcante() {
        return justifcante;
    }

    public void setJustifcante(JustificanteInterface justifcante) {
        this.justifcante = justifcante;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }
    public void setChecado(String checado){
        this.checado= checado;
    }
    public void getChecado(String checado){
        this.checado = checado;
    }
           
    
    
    
}
