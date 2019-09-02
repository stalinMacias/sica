package sica.common.objetos;

import java.util.Date;
import sica.common.Utils;

public class Documento {

    private String usuario;
    private String archivo;
    private String fecha;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getFecha(){
        return fecha;
    }
    
    public Date getFechaDate() {
        return Utils.parseDate(fecha);
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    
}
