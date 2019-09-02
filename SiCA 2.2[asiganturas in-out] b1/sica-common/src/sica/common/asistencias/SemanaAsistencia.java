package sica.common.asistencias;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class SemanaAsistencia {

    private final ObservableMap <Integer,AsistenciaUsuario> semana;
    private final ObservableMap <Integer,Integer> fechas;
    private final ObservableMap <Integer,Boolean> debioasistir;
    private final EstadisticasAsistencias estadisticas;
    
    private SemanaAsistencia instance;
    private boolean special;
       
    
    public SemanaAsistencia(){
        semana = FXCollections.observableHashMap();
        fechas = FXCollections.observableHashMap();                
        debioasistir = FXCollections.observableHashMap();  
        special = false;
        estadisticas = new EstadisticasAsistencias();
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }    
    
    public void addDay(Integer fecha, Integer dia, AsistenciaUsuario au, Boolean b){
        semana.put(dia, au);
        fechas.put(dia, fecha);
        debioasistir.put(dia,b);
        estadisticas.addDay(au,b);
    }
    
    public void addDay(Integer dia, AsistenciaUsuario au){
        if (special){
            semana.put(dia, au);
        }
    }
    
    public boolean contiene(Integer dia){
        return semana.containsKey(dia);
    }
    
    public ObservableMap <Integer,AsistenciaUsuario> getSemana(){
        return semana;
    }
    
    public AsistenciaUsuario getDia(Integer dia){
        return semana.get(dia);        
    }
    
    public Integer getFecha(Integer dia){
        return fechas.get(dia);
    }
    
    public SemanaAsistencia getInstance(){
        if (instance == null){
            instance = this;
        }
        return instance;
    }
    
    public Boolean debioAsistir(Integer dia){
        return debioasistir.containsKey(dia)? debioasistir.get(dia): false;
    }

    public EstadisticasAsistencias getEstadisticas() {
        return estadisticas;
    }
    
}
