package sica.common.justificantes;

import java.util.Date;
import sica.common.Utils;

public class Comentario {
    private String usuario;
    private String nombreusuario;
    private String comentario;
    private String horayfecha;

    public String getUsuario() {
        return usuario;
    }

    public String getNombreusuario() {
        return nombreusuario;
    }

    public String getComentario() {
        return comentario;
    }

    public Date getHorayfecha() {
        return horayfecha.isEmpty()? new Date():Utils.parseFullDate(horayfecha);
    }        
}
