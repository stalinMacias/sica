package sica.common.justificantes;

import java.util.Calendar;
import java.util.Date;
import sica.common.Utils;

public class JustificantePeriodo extends Folio {

    private String fecha_inicial;
    private String fecha_final;

    public boolean isDia(){
        return fecha_inicial.equals(fecha_final);
    }            
    
    public String getFecha_inicial() {
        return fecha_inicial;
    }

    public String getFecha_final() {
        return fecha_final;
    }
    
    public Date getFechaInicial() {
        Calendar c = Calendar.getInstance();
        c.setTime(Utils.parseDate(fecha_inicial));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        
        return c.getTime();        
    }

    public Date getFechaFinal() {
        Calendar c = Calendar.getInstance();
        c.setTime(Utils.parseDate(fecha_final));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        
        return c.getTime();
    }
    
    
}
