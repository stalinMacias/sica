package sica.common.faltas;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import sica.common.DBQueries;
import static sica.common.DBQueries.registros;
import sica.common.Utils;
import sica.common.horarios.HorarioCrn;
import sica.common.horarios.HorarioUsuario;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.objetos.Crn;
import sica.common.objetos.Departamento;
import sica.common.objetos.Registro;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;

public class ReportesFaltas {

    public static Task<ObservableList<FaltasUsuario>> getFaltasUsuariosPeriodo(
        final Date desde, final Date hasta, final TipoUsuario tipoUsuario){
        
        return new Task<ObservableList<FaltasUsuario>>() {
            @Override protected ObservableList<FaltasUsuario> call() throws Exception {
                Calendar actual = Calendar.getInstance();
                String desdeTxt = Utils.formatDate(desde);
                String hastaTxt = Utils.formatDate(hasta);

                ObservableList<FaltasUsuario> faltas = FXCollections.observableArrayList();
                
                
               
                ObservableList<Registro> registros = DBQueries.getRegistrosPeriodoTipousuarios(desdeTxt,hastaTxt,tipoUsuario.getTipo());
                ObservableList<Usuario> usuarios = DBQueries.getAlgunosUsuarios(tipoUsuario.getTipo());
                ObservableList<Evento> eventos = DBQueries.getEventosJornadaFechas(desdeTxt,hastaTxt); 
                ObservableList<JustificantePeriodo> justif = DBQueries.getJustificantesAprobadosPeriodo(desdeTxt,hastaTxt);                                
                    
                System.out.println("**"+registros.size()+" registros descargados");

                /*
                
                usuarios.forEach( u -> {
                    actual.setTime(desde);
                    
                    FaltasUsuario fu = new FaltasUsuario(u.getCodigo(),u.getNombre());
                    
                    // Comentario añadido: 2016-julio-16
                    // por Diego:
                    
                    // analizando el código estoy medio comprendiendo que con getHorariosUsuarioPerdiodo se obtienen una lista 
                    // pasada a iterator, con un horario o con mas horarios, todos los que se encuentren dentro de un periodo
                    // de tiempo, incluyendo el siguiente posterior a la fecha "_hasta". 
                    
                    // Entonces se usa el itrartor horarios para obtener el primer elemento de la lista, el cual ¿de que fecha será?
                    // ¿segun por ser ordenado de forma ascendente por vigencia, tomaría el registro del  inicio: el de fecha vigencia mas pequeño?
                    // ¿afectara en algo que obtenga las fechas vigencia como "char" ?
                    
                    
                    Iterator<HorarioUsuario> horarios = DBQueries.getHorariosUsuarioPerdiodo(u.getCodigo(),desdeTxt,hastaTxt).iterator();
                    HorarioUsuario horario;

                    Calendar vigencia = Calendar.getInstance();

                    if (horarios.hasNext()){       
                        horario = horarios.next();
                        vigencia.setTime(horario.getVigencia());
                    } else {
                        horario = new HorarioUsuario();
                    }

                    
                    //¿como funciona lo de actual?
                    
                    while (actual.getTime().compareTo(hasta)<=0){
                        if (actual.after(vigencia) && horarios.hasNext()){
                            horario = horarios.next();
                            vigencia.setTime(horario.getVigencia());
                        }

                        boolean debioasistir = horario.debioAsistir(actual.get(Calendar.DAY_OF_WEEK));
                        //System.out.println("### debio asistir?: " + debioasistir);
                        if (debioasistir){
                            Registro reg = Utils.findPrimerRegistro(u.getCodigo(), actual.getTime(), registros);
                            if (reg==null){
                                FaltaDia f = new FaltaDia();
                                f.setFecha(actual.getTime());

                                Evento ev = Utils.findEvento(actual.getTime(),eventos);
                                if (ev != null){                                
                                    f.setJustificante(ev);

                                } else {
                                    JustificantePeriodo j = Utils.findJustificanteDia(u.getCodigo(), actual.getTime(), justif);
                                    if (j!=null){                                    
                                        f.setJustificante(j);
                                    }
                                }
                                System.out.println("### Agregando falta: " + f.getFecha().toString());
                                fu.addFalta(f);
                            }
                        }

                        actual.add(Calendar.DAY_OF_MONTH, 1);
                    }   
                    
                    System.out.println("######################################################");
                    System.out.println("Agregando Falta de Usuario: faltas.add(fu) ");
                    faltas.add(fu);
                    
                    
                    
                });
                
                
                */

                
                // metodo alternativo para pruebas
                for( Usuario u :  usuarios ){
                    
                    actual.setTime(desde);
                    
                    FaltasUsuario fu = new FaltasUsuario(u.getCodigo(),u.getNombre());
                    
                    // Comentario añadido: 2016-julio-16
                    // por Diego:
                    
                    // analizando el código estoy medio comprendiendo que con getHorariosUsuarioPerdiodo se obtienen una lista 
                    // pasada a iterator, con un horario o con mas horarios, todos los que se encuentren dentro de un periodo
                    // de tiempo, incluyendo el siguiente posterior a la fecha "_hasta". 
                    
                    // Entonces se usa el itrartor horarios para obtener el primer elemento de la lista, el cual ¿de que fecha será?
                    // ¿segun por ser ordenado de forma ascendente por vigencia, tomaría el registro del  inicio: el de fecha vigencia mas pequeño?
                    // ¿afectara en algo que obtenga las fechas vigencia como "char" ?
                    
                    
                    Iterator<HorarioUsuario> horarios = DBQueries.getHorariosUsuarioPerdiodo(u.getCodigo(),desdeTxt,hastaTxt).iterator();
                    HorarioUsuario horario;

                    Calendar vigencia = Calendar.getInstance();

                    if (horarios.hasNext()){       
                        horario = horarios.next();
                        vigencia.setTime(horario.getVigencia());
                    } else {
                        horario = new HorarioUsuario();
                    }

                    
                    //¿como funciona lo de actual?
                    // en el 9 < 10 sucede el error
                    //osea, (del 0 al 9) en el ciclo numero 10
                    while (actual.getTime().compareTo(hasta)<=0  ){
                        
                        if (actual.after(vigencia) && horarios.hasNext()){
                            horario = horarios.next();
                            vigencia.setTime(horario.getVigencia());
                        }

                        boolean debioasistir =  horario.debioAsistir(actual.get(Calendar.DAY_OF_WEEK));

                        //System.out.println("### debio asistir?: " + debioasistir);
                        if (debioasistir){
                            Registro reg = Utils.findPrimerRegistro(u.getCodigo(), actual.getTime(), registros);
    
                             
                            if (reg==null){
                                FaltaDia f = new FaltaDia();
                                f.setFecha(actual.getTime());
  
                                Evento ev = Utils.findEvento(actual.getTime(),eventos);
                                if (ev != null){                                
                                    f.setJustificante(ev);

                                } else {
                                    
                                    
                                    JustificantePeriodo j = Utils.findJustificanteDia(u.getCodigo(), actual.getTime(), justif);
                                    
                                    if (j!=null){                                    
                                        f.setJustificante(j);
                                    }

                                }
                                fu.addFalta(f);
                            }
                        }

                        actual.add(Calendar.DAY_OF_MONTH, 1);
                    }   

                    faltas.add(fu);

                }

                return faltas; 
               
            }
        };
        
    }
    
    

    public static Task<ObservableList<FaltasUsuario>> getFaltasAsignaturasDepartamentoPeriodo(
        final Date desde, final Date hasta, final Departamento depto, final Usuario usuario){
        
        return new Task<ObservableList<FaltasUsuario>>() {
            @Override protected ObservableList<FaltasUsuario> call() throws Exception {
                
                String desdeTxt = Utils.formatDate(desde);
                String hastaTxt = Utils.formatDate(hasta);

                ObservableList<HorarioCrn> materias = (usuario.getPrivilegios() == Privilegios.USUARIO)?
                        DBQueries.getMateriasParaAsistenciaPeriodo(desdeTxt, hastaTxt, depto.getCodigo(),usuario.getCodigo()):            
                        DBQueries.getMateriasParaAsistenciaPeriodo(desdeTxt, hastaTxt, depto.getCodigo());

                ObservableList<Registro> registros = (usuario.getPrivilegios() == Privilegios.USUARIO)?
                        DBQueries.getRegistrosUsuarioPeriodo(usuario.getCodigo(), desdeTxt, hastaTxt):
                        DBQueries.getRegistrosPeriodo(desdeTxt,hastaTxt); 

                ObservableList<Evento> eventos = DBQueries.getEventosAsignaturasFechas(desdeTxt, hastaTxt);
                ObservableList<JustificanteAsignatura> justifis = DBQueries.getJustificantesAprobadosClases(desdeTxt, hastaTxt);                    
                ObservableList<JustificantePeriodo> justif = DBQueries.getJustificantesAprobadosPeriodo(desdeTxt,hastaTxt);

                Map<String,FaltasUsuario> faltas = new HashMap<>();

                for (HorarioCrn h : materias){
                    if (!faltas.containsKey(h.getUsuario())){
                        faltas.put(h.getUsuario(), new FaltasUsuario(h.getUsuario(),h.getNombre()));
                    }

                    Calendar actual = Calendar.getInstance();
                    actual.setTime(desde);
                    actual.set(Calendar.HOUR_OF_DAY, 0);
                    actual.set(Calendar.MINUTE, 0);

                    while(Utils.diaDeLaSemana(h.getDia()) != actual.get(Calendar.DAY_OF_WEEK)){
                        actual.add(Calendar.DAY_OF_MONTH, 1);                    
                    }

                    while(actual.getTime().compareTo(hasta) <= 0){

                        if (actual.getTime().compareTo(h.getInicioDate()) >=0 && 
                                actual.getTime().compareTo(h.getFinDate()) <= 0 ){

                            Registro find = (usuario.getPrivilegios() == Privilegios.USUARIO)?
                                    Utils.findAndDeleteRegistro(actual.getTime(), h.getHorario(), registros) :
                                    Utils.findRegistro(h.getUsuario(),actual.getTime(),h.getHorario(),registros);

                            if ( find == null){

                                FaltaClase falta = new FaltaClase();
                                falta.setFecha(actual.getTime());
                                falta.setHorario(h.getHorario());
                                falta.setDia(h.getDia());

                                Crn crn = new Crn();
                                crn.setCrn(h.getCrn());
                                crn.setMateria(h.getMateria());
                                crn.setCodProf(h.getUsuario());

                                falta.setCrn(crn);


                                Evento e = Utils.findEvento(actual.getTime(), eventos);
                                if (e!=null){
                                    falta.setJustifcante(e);
                                } else {
                                    JustificanteAsignatura j = Utils.findAndDeleteJustificanteClase(h.getCrn(), actual.getTime(), justifis);
                                    if (j!=null){
                                        falta.setJustifcante(j);
                                    } else {
                                        JustificantePeriodo j2 = Utils.findJustificanteDia(h.getUsuario(), actual.getTime(), justif);
                                        if (j2 != null){
                                            falta.setJustifcante(j2);
                                        }
                                    }
                                }

                                faltas.get(h.getUsuario()).addFalta(falta);
                            } 
                        }
                        actual.add(Calendar.DAY_OF_MONTH, 7);
                    }

                }
                return FXCollections.observableArrayList(faltas.values());
            }
        };        
    }
   
    public static Task<ObservableList<FaltaClase>> getFaltasAsignaturasPeriodo(
        final Date desde, final Date hasta, final sica.common.usuarios.Usuario usuario){
        
        return new Task<ObservableList<FaltaClase>>() {
            @Override protected ObservableList<FaltaClase> call() throws Exception {
                
                String desdeTxt = Utils.formatDate(desde);
                String hastaTxt = Utils.formatDate(hasta);

                ObservableList<HorarioCrn> materias = FXCollections.observableArrayList();
                for (Departamento depto : DBQueries.getDepartamentos()){
                    materias.addAll(
                        DBQueries.getMateriasParaAsistenciaPeriodo(desdeTxt, hastaTxt, depto.getCodigo(),usuario.getCodigo())
                    );
                        
                }
                ObservableList<Registro> registros = DBQueries.getRegistrosUsuarioPeriodo(usuario.getCodigo(), desdeTxt, hastaTxt);
                ObservableList<Evento> eventos = DBQueries.getEventosAsignaturasFechas(desdeTxt, hastaTxt);
                ObservableList<JustificanteAsignatura> justifis = DBQueries.getJustificantesAprobadosClases(desdeTxt, hastaTxt);                    
                ObservableList<JustificantePeriodo> justif = DBQueries.getJustificantesAprobadosPeriodo(desdeTxt,hastaTxt);
                ObservableList<FaltaClase> faltas = FXCollections.observableArrayList();

                for (HorarioCrn h : materias){                    
                    Calendar actual = Calendar.getInstance();
                    actual.setTime(desde);
                    actual.set(Calendar.HOUR_OF_DAY, 0);
                    actual.set(Calendar.MINUTE, 0);

                    while(Utils.diaDeLaSemana(h.getDia()) != actual.get(Calendar.DAY_OF_WEEK)){
                        actual.add(Calendar.DAY_OF_MONTH, 1);                    
                    }

                    while(actual.getTime().compareTo(hasta) <= 0){

                        if (actual.getTime().compareTo(h.getInicioDate()) >=0 && 
                                actual.getTime().compareTo(h.getFinDate()) <= 0 ){

                            Registro find = Utils.findAndDeleteRegistro(actual.getTime(), h.getHorario(), registros);

                            if ( find == null && Utils.findEvento(actual.getTime(), eventos) == null){

                                FaltaClase falta = new FaltaClase();
                                falta.setFecha(actual.getTime());
                                falta.setHorario(h.getHorario());
                                falta.setDia(h.getDia());

                                Crn crn = new Crn();
                                crn.setCrn(h.getCrn());
                                crn.setMateria(h.getMateria());
                                crn.setCodProf(h.getUsuario());

                                falta.setCrn(crn);
                                  
                                JustificanteAsignatura j = Utils.findAndDeleteJustificanteClase(h.getCrn(), actual.getTime(), justifis);
                                if (j!=null){
                                    falta.setJustifcante(j);
                                } else {
                                    JustificantePeriodo j2 = Utils.findJustificanteDia(h.getUsuario(), actual.getTime(), justif);
                                    if (j2 != null){
                                        falta.setJustifcante(j2);
                                    }
                                }
                                faltas.add(falta);                                                                
                            } 
                        }
                        actual.add(Calendar.DAY_OF_MONTH, 7);
                    }

                }
                return faltas;
            }
        };
        
    }
}
