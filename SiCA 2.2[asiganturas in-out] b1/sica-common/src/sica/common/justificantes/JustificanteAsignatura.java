package sica.common.justificantes;

import java.util.Date;
import sica.common.Utils;

public class JustificanteAsignatura extends Folio {
    private String fecha;
    private String crn;
    private String nombremateria;

    public String getFecha() {
        return fecha;
    }
    
    public Date getFechaD(){
        return Utils.parseDate(fecha);
    }

    public String getCrn() {
        return crn;
    }

    public String getNombremateria() {
        return nombremateria;
    }    

    public void setNombremateria(String nombremateria) {
        this.nombremateria = nombremateria;
    }
    
    

}
