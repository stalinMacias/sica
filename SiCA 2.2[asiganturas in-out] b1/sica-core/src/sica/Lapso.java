/**
 * Esta clase se basa en la tabla "horarioscrn_lapsos" de la DB.
 * Funciona para identificar si en determinado lapso se debe registrar asisitencia solo de entrada o de entrada y salida a las clases
 */
package sica;

import java.util.Date;

/**
 *
 * @author Cuvalles.SicaTeam
 */
public class Lapso {
    
    private String checar; //entrada, salida u otro
    private Date fechaInicial;
    
    public Lapso(){
        checar = "entrada";
        fechaInicial = new Date();
    }

    public String getChecar() {
        return checar;
    }

    public void setChecar(String checar) {
        this.checar = checar;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }
    
    
    
}
