package sica.common.horarios;

import java.util.List;
import javafx.collections.ObservableList;
import sica.common.DBQueries;
import sica.common.Utils;

public class ReportesHorarios {
           
    public static long getCargaHorariaSemanal(String usuario){
        return getCargaHorariaSemanal(DBQueries.getHorarioActualUsuario(usuario));
    }
    
    public static long getCargaHorariaSemanal(ObservableList<HorarioUsuario> hors){        
          
        long millis = 0;

        for (HorarioUsuario h : hors ){
            for (char c : h.getDias().toCharArray()){
                millis += Utils.getTimeDiff(h.getEntrada(), h.getSalida());
            }
        }
        return millis;                        
    }
    
    public static long getCargaHorariaSemanal(List<HorarioUsuario> hors){        
          
        long millis = 0;

        for (HorarioUsuario h : hors ){
            for (char c : h.getDias().toCharArray()){
                millis += Utils.getTimeDiff(h.getEntrada(), h.getSalida());
            }
        }
        return millis;                        
    }
    
    
    /*
    public static long getCargaHorariaPeriodo(String usr, Date inicio, Date fin){
        return getCargaHorariaPeriodo(
                MyConn.getHorariosUsuarioPerdiodo(usr, 
                        Utils.formatDate(inicio), 
                        Utils.formatDate(fin)),
                inicio,
                fin
        );
    }
    public static long getCargaHorariaPeriodo(ObservableList<HorarioUsuario> hors, Date inicio, Date fin){
        long millis = 0l;
        Calendar c = Calendar.getInstance();
        c.setTime(inicio);
        
        while(c.getTime().compareTo(fin) <= 0){
            for (HorarioUsuario h : hors){
                if (c.getTime().compareTo(h.getVigencia())<=0 && h.debioAsistir(c.get(Calendar.DAY_OF_WEEK))){
                    millis += Utils.getTimeDiff(h.getEntrada(), h.getSalida());
                }                
            }                        
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return millis;
    }
    
    public static long getHorasJustificadaPeriodo(ObservableList<SemanaAsistencia> p){
        long millis = 0;
        
        for (SemanaAsistencia a : p){
            for ( AsistenciaUsuario au : a.getSemana().values()){
                if (au.getJustif()!=null && !(au.getJustif() instanceof Evento)){                    
                    millis += Utils.getTimeDiff(
                            au.getRegistroEntrada()==null? au.getEntrada(): 
                                    Utils.formatTime(au.getRegistroEntrada().getFechahora()), 
                            au.getRegistroSalida()==null? au.getSalida():
                                    Utils.formatTime(au.getRegistroSalida().getFechahora())
                    );
                }
            }
        }        
        return millis;
    }
    
    public static long getHorasInhabilesPeriodo(ObservableList<SemanaAsistencia> p){
        long millis = 0;
        
        for (SemanaAsistencia a : p){
            for ( AsistenciaUsuario au : a.getSemana().values()){
                if (au.getJustif()!=null && (au.getJustif() instanceof Evento)){                    
                    millis += Utils.getTimeDiff(
                            au.getRegistroEntrada()==null? au.getEntrada(): 
                                    Utils.formatTime(au.getRegistroEntrada().getFechahora()), 
                            au.getRegistroSalida()==null? au.getSalida():
                                    Utils.formatTime(au.getRegistroSalida().getFechahora())
                    );
                }
            }
        }        
        return millis;
    }
    */
}
