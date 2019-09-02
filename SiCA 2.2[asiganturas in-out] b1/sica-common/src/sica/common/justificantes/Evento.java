package sica.common.justificantes;

import java.util.Calendar;
import java.util.Date;
import sica.common.Utils;

public class Evento extends TipoEvento implements JustificanteInterface {
    
    private String fin;
    private String inicio;
    private String asignaturas;

    public boolean isAsignatura(){
        return asignaturas != null && asignaturas.equals("1");
    }

    public String getAsignaturas() {
        return asignaturas;
    }
        
    public Date getFin() {
        Calendar c = Calendar.getInstance();
        c.setTime(Utils.parseDate(fin));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        
        return c.getTime();
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public Date getInicio() {
        Calendar c = Calendar.getInstance();
        c.setTime(Utils.parseDate(inicio));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        
        return c.getTime();
        
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    @Override
    public String getNombrejustificante() {
        return getNombre();
    }

    @Override
    public String getDescripcionJustificante() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
