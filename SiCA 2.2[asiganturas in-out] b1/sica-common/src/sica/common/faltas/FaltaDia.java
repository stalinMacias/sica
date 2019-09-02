package sica.common.faltas;

import sica.common.justificantes.JustificanteInterface;

public class FaltaDia extends Falta{
    
    private JustificanteInterface justificante = null;

    public JustificanteInterface getJustificante() {
        return justificante;
    }

    public void setJustificante(JustificanteInterface justificante) {
        this.justificante = justificante;
    }   
    
    
}
