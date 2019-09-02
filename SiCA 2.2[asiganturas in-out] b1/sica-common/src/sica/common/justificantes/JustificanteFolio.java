package sica.common.justificantes;

import java.util.Date;
import sica.common.Utils;

public class JustificanteFolio extends Folio {
    private String tipo;
    private String fecha;
    private String fecha_inicial;
    private String fecha_final;
    private String crn;
    private String nombremateria;    

    public String getTipo() {
        return tipo;
    }
    
    public boolean isAsignatura() {
        return tipo.equals("a");
    }
    
    public boolean isPeriodo(){
        return tipo.equals("p");
    }

    public boolean isDia(){
        return isPeriodo()? fecha_inicial.equals(fecha_final): false;
    }            
    
    public Date getFecha() {
        return Utils.parseDate(fecha);
    }

    public Date getFecha_inicial() {
        return Utils.parseDate(fecha_inicial);
    }

    public Date getFecha_final() {
        return Utils.parseDate(fecha_final);
    }

    public String getCrn() {
        return crn;
    }    

    public String getNombremateria() {
        return nombremateria;
    }
        
}
