package sica.common.asistencias;

import sica.common.Utils;
import sica.common.justificantes.Evento;

public class EstadisticasAsistencias {
    private int diasTotales;
    private int diasConJornada;
    private int diasLibres;
    private int diasInhabiles;
    private int diasAsistidos;
    private int diasConErrores;
    private int diasConFaltas;
    private int diasConJustificantes;
    
    private long tiempoTrabajado;
    private long tiempoCargaPeriodo;
    private long tiempoJustificado;
    private long tiempoPendJustificar;
    private long tiempoInhabil;
    
    
    public void addDay(AsistenciaUsuario au, boolean dAsist){
        diasTotales++;
        
        if (au.getRegistroEntrada()==null && au.getRegistroSalida()==null){
            if (dAsist && (au.getJustif()==null 
                    || ( au.getJustif()!=null && !(au.getJustif()instanceof Evento)))) 
                diasConFaltas++;
            
        } else if (au.getRegistroEntrada()==null || au.getRegistroSalida()==null){
            diasAsistidos++;
            if (dAsist && au.getJustif() == null){
                diasConErrores++;
                tiempoPendJustificar+=timeDiff(au);
            }
        } else {
            tiempoTrabajado+=timeDiff(au);
            diasAsistidos++;
        }
        
        if (au.getJustif() != null){
            if (au.getJustif() instanceof Evento){
                diasInhabiles++;
                tiempoInhabil+=timeDiff(au);
            } else {
                diasConJustificantes++;
                tiempoJustificado+=timeDiff(au);
            }
        }
        
        if (dAsist) {
            diasConJornada++;
            tiempoCargaPeriodo+=Utils.getTimeDiff(au.getEntrada(), au.getSalida());
        } else {
            diasLibres++;
        }
    }
    
    private long timeDiff(AsistenciaUsuario au){
        return Utils.getTimeDiff(
            au.getRegistroEntrada()==null? au.getEntrada(): 
                Utils.formatTime(au.getRegistroEntrada().getFechahora()), 
            au.getRegistroSalida()==null? au.getSalida():
                Utils.formatTime(au.getRegistroSalida().getFechahora())
        );
    }
    
    public void add(EstadisticasAsistencias e){
        diasTotales += e.getDiasTotales();
        diasConJornada += e.getDiasConJornada();
        diasLibres += e.getDiasLibres();
        diasInhabiles += e.getDiasInhabiles();
        diasAsistidos += e.getDiasAsistidos();
        diasConErrores += e.getDiasConErrores();
        diasConFaltas += e.getDiasConFaltas();
        diasConJustificantes += e.getDiasConJustificantes();

        tiempoTrabajado += e.getTiempoTrabajado();
        tiempoCargaPeriodo += e.getTiempoCargaPeriodo();
        tiempoJustificado += e.getTiempoJustificado();
        tiempoPendJustificar += e.getTiempoPendJustificar();
        tiempoInhabil += e.getTiempoInhabil();
    }

    public int getDiasTotales() {
        return diasTotales;
    }
        
    public int getDiasConJornada() {        
        return diasConJornada;
    }

    public int getDiasLibres(){
        return diasLibres;
    }
    
    public int getDiasConErrores() {
        return diasConErrores;
    }

    public int getDiasConFaltas() {
        return diasConFaltas;
    }

    public int getDiasInhabiles() {
        return diasInhabiles;
    }
    
    public int getDiasAsistidos(){
        return diasAsistidos;
    }
    
    public int getDiasConJustificantes() {
        return diasConJustificantes;
    }    
    
    public long getTiempoTrabajado() {
        return tiempoTrabajado;
    }

    public long getTiempoCargaPeriodo() {
        return tiempoCargaPeriodo;
    }

    public long getTiempoJustificado() {
        return tiempoJustificado;
    }

    public long getTiempoPendJustificar() {
        return tiempoPendJustificar;
    }

    public long getTiempoInhabil() {
        return tiempoInhabil;
    }
    
}
