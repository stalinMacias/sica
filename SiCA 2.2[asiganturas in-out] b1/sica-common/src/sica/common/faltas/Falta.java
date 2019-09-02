package sica.common.faltas;

import java.util.Date;

public abstract class Falta {
    
    private Date fecha;
    
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
}
