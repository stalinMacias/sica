package sica.common.asistencias;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import sica.common.DBQueries;
import sica.common.Utils;
import sica.common.horarios.HorarioUsuario;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.objetos.Registro;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;

public class ReportesAsistencias {
    
    public static Task<ObservableList<AsistenciaUsuario>> getAsistenciaUsuariosPorDia(
        final Date dia, final Usuario usuario, final TipoUsuario tipoUsuarios){
        
        return new Task<ObservableList<AsistenciaUsuario>>() {
            @Override protected ObservableList<AsistenciaUsuario> call() throws Exception {

                String txtDia = Utils.formatDate(dia);
                ObservableList<Evento> eventos = DBQueries.getEventoJornadaFecha(txtDia);    
                ObservableList<Registro> registros = DBQueries.getRegistrosFecha(txtDia); 
                ObservableList<JustificantePeriodo> justifs = DBQueries.getJustificantesAprobadosPeriodo(txtDia,txtDia);

                ObservableList<AsistenciaUsuario> usrs;
                
                     //comprueba que tenfa los privilegios
                if (usuario.getPrivilegios() == Privilegios.ADMINISTRADOR || usuario.getPrivilegios() == Privilegios.DIRECTIVO){
                    usrs = DBQueries.getAsistenciaUsuarios(txtDia,tipoUsuarios.getTipo()); 
                    
                }else if (usuario.getPrivilegios() == Privilegios.JEFE){
                    usrs = DBQueries.getAsistenciaUsuarios(txtDia,tipoUsuarios.getTipo(),usuario.getCodigo());

                } else {
                    usrs = DBQueries.getAsistenciaUsuario(txtDia,usuario.getCodigo());
                }

                for (AsistenciaUsuario au : usrs){
                        //si el tipo de usuario es administrativo
                    if (tipoUsuarios.getDescripcion().equals("Administrativo")){

                        for (Registro r : registros){
                            if (r.getUsuario().equals(au.getUsuario())){                        
                                if ( au.getRegistroEntrada() == null &&
                                        Utils.dentroDeHorarioAdmin( Utils.formatTime(r.getFechahora()) , au.getEntrada() )){
                                    au.setRegistroEntrada(r);

                                } else if ( au.getRegistroSalida()== null &&
                                        Utils.horarioTerminadoAdmin(Utils.formatTime(r.getFechahora()) , au.getSalida() )){
                                    au.setRegistroSalida(r);                    
                                } else if ( au.getRegistroEntrada() != null && au.getRegistroSalida()!= null){
                                    break;
                                }                                                
                            }
                        }                

                        if (au.getRegistroEntrada()!=null){
                            registros.remove(au.getRegistroEntrada());
                        }                 
                        if (au.getRegistroSalida() != null){
                            registros.remove(au.getRegistroSalida());
                        }

                        if (au.getRegistroEntrada() == null) {
                            Registro r = Utils.findPrimerRegistro(au.getUsuario(), dia, registros);
                            if (r!=null){
                                au.setHoraEntrada(Utils.formatTime(r.getFechahora())+" ("+au.getEntrada()+")");
                                registros.remove(r);
                            }
                        }

                        if (au.getRegistroSalida() == null) {
                            Registro r = Utils.findUltimoRegistro(au.getUsuario(), dia, registros);
                            if (r!=null){
                                au.setHoraSalida(Utils.formatTime(r.getFechahora())+" ("+au.getSalida()+")");
                                registros.remove(r);
                            }
                        }

                    } else {
                        au.setRegistroEntrada(Utils.findPrimerRegistro(au.getUsuario(), dia, registros));
                        au.setRegistroSalida(Utils.findUltimoRegistro(au.getUsuario(), dia, registros));
                    }

                    if ( au.getRegistroEntrada() == null || au.getRegistroSalida() == null ){
                        Evento e = Utils.findEvento(dia, eventos);                
                        if (e!=null) {
                            au.setJustif(e);

                        } else {
                            JustificantePeriodo j = Utils.findJustificanteDia(au.getUsuario(), dia, justifs);
                            if ( j!=null ){
                                au.setJustif(j);                        
                            }
                        }
                    }
                }
                return usrs;
            }
        };           
    }    
    
    public static Task<ObservableList<AsistenciaClase>> getAsistenciaClasesPorDia(
        final Date dia, final Usuario usuario, final Privilegios privilegios){
        
        return new Task<ObservableList<AsistenciaClase>>() {
            @Override protected ObservableList<AsistenciaClase> call() throws Exception {                
                
                String txtFecha = Utils.formatDate(dia);                 
                ObservableList<AsistenciaClase> asistencia = FXCollections.observableArrayList();        
                ObservableList<AsistenciaClase> rs;

                if (privilegios == Privilegios.ADMINISTRADOR || privilegios == Privilegios.DIRECTIVO){
                    rs = DBQueries.getClasesDia(txtFecha, Utils.diaDeLaSemana(dia));

                }else if (privilegios == Privilegios.JEFE){
                    rs = DBQueries.getClasesDia(txtFecha, Utils.diaDeLaSemana(dia), usuario.getCodigo());

                } else {
                    rs = DBQueries.getClasesDia(txtFecha, Utils.diaDeLaSemana(dia),usuario.getDepartamento(),usuario.getCodigo());
                }

                ObservableList<Evento> eventos = DBQueries.getEventoAsignaturasFecha(txtFecha);
                ObservableList<Registro> registros = DBQueries.getRegistrosFecha(txtFecha);
                ObservableList<JustificantePeriodo> justifDia = DBQueries.getJustificantesAprobadosPeriodo(txtFecha, txtFecha);
                ObservableList<JustificanteAsignatura> justifClase = DBQueries.getJustificantesAprobadosClases(txtFecha, txtFecha);

                rs.forEach(clase -> {

                    Registro finded = Utils.findRegistro(clase.getUsuario(), Utils.parseDate(txtFecha), clase.getHorario(), registros);            
                    if ( finded != null ) {
                        clase.setRegistro(finded);

                    } else if ( !eventos.isEmpty() ){
                        clase.setJustificante(eventos.get(0));    

                    } else {
                        JustificantePeriodo j = Utils.findJustificanteDia(clase.getUsuario(), dia, justifDia);
                        if ( j!=null ){
                            clase.setJustificante(j);                    

                        } else {  
                            JustificanteAsignatura j2 = Utils.findAndDeleteJustificanteClase(clase.getCrn(), dia, justifClase);
                            if ( j2!=null ){
                                j2.setNombremateria(clase.getMateria());
                                clase.setJustificante(j2);
                            } 
                        }
                    }
                    asistencia.add(clase);
                });
                return asistencia;
            }
        };
    }
    
    public static Task<ObservableList<SemanaAsistencia>> getAsistenciaUsuarioPeriodoFull(
            final Date desde, final Date hasta, final Usuario usuario ){
        
        return new Task<ObservableList<SemanaAsistencia>>() {
            @Override protected ObservableList<SemanaAsistencia> call() throws Exception {

                ObservableList<SemanaAsistencia> asistencia = FXCollections.observableArrayList();

                String txtDesde = Utils.formatDate(desde);
                String txtHasta = Utils.formatDate(hasta);

                Calendar actual = Calendar.getInstance();
                actual.setTime(desde);

                DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
                String[] months = dateFormatSymbols.getMonths();              

                ObservableList<HorarioUsuario> thorarios = DBQueries.getHorariosUsuarioPerdiodo(usuario.getCodigo(),txtDesde, txtHasta);
                ObservableList<Registro> registros = DBQueries.getRegistrosUsuarioPeriodo(usuario.getCodigo(),txtDesde, txtHasta);
                ObservableList<Evento> eventos = DBQueries.getEventosJornadaFechas(txtDesde, txtHasta);
                ObservableList<JustificantePeriodo> justif = DBQueries.getJustificantesAprobadosUsuario(usuario.getCodigo());

                SemanaAsistencia semana = new SemanaAsistencia();

                boolean first = true;
                while (hasta.compareTo(actual.getTime()) >= 0){              
                    AsistenciaUsuario au = new AsistenciaUsuario();

                    if (actual.get(Calendar.DAY_OF_MONTH)== 1 || first){
                        first = false;                            
                        if (!semana.getSemana().isEmpty()){
                            asistencia.add(semana);
                        }
                        semana = new SemanaAsistencia();
                        semana.setSpecial(true);

                        au.setNombre(months[actual.get(Calendar.MONTH)].toUpperCase());
                        semana.addDay(Calendar.TUESDAY, au);

                        au = new AsistenciaUsuario();
                        au.setNombre(""+actual.get(Calendar.YEAR));                        
                        semana.addDay(Calendar.WEDNESDAY, au);

                        asistencia.add(semana);
                        semana = new SemanaAsistencia();
                        au = new AsistenciaUsuario();
                    }

                    HorarioUsuario horario = null;
                    boolean debioasistir = false;
                    for (HorarioUsuario h : thorarios){
                        if ( h.getVigencia().compareTo(actual.getTime())>=0 
                                && h.debioAsistir(actual.get(Calendar.DAY_OF_WEEK)) ){
                            horario = h;
                            debioasistir=true;
                            break;
                        }                            
                    }

                    if (debioasistir && horario!=null){
                        au.setHoraEntrada(horario.getEntrada());
                        au.setHoraSalida(horario.getSalida());

                        Registro ent = Utils.findAndDeletePrimerRegistro(actual.getTime(), registros);                            
                        Registro sal = Utils.findAndDeleteUltimoRegistro(actual.getTime(), registros);

                        if (ent!=null && sal!=null){
                            au.setRegistroEntrada(ent);
                            au.setRegistroSalida(sal);
                        } else {
                            Registro r = ent!=null? ent: sal;
                            if (r!=null && Utils.cercaEntrada(r.getHora(), horario.getEntrada(), horario.getSalida())){
                                au.setRegistroEntrada(r);
                            } else {
                                au.setRegistroSalida(r);
                            }
                        }

                        if (au.getRegistroEntrada()==null || au.getRegistroSalida() == null){                               

                            Evento e = Utils.findEvento(actual.getTime(), eventos);
                            if ( e!=null) {
                                au.setJustif(e);                             
                            } else {                                
                                JustificantePeriodo j = Utils.findJustificanteDia(actual.getTime(), justif);
                                if ( j!=null ){
                                    au.setJustif(j);                                                    
                                }
                            }
                        } 
                    }

                    semana.addDay(actual.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_WEEK),au,debioasistir);

                    if (actual.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ){
                        asistencia.add(semana);
                        semana = new SemanaAsistencia();
                    }

                    actual.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (semana.getSemana().size() > 0)
                    asistencia.add(semana);

                return asistencia;                      
            }
        };
    }
    
    public static Task<ObservableList<SemanaAsistencia>> getAsistenciaUsuarioPeriodo(
            final Date desde, final Date hasta, final Usuario usuario ){
        
        return new Task<ObservableList<SemanaAsistencia>>() {
            @Override protected ObservableList<SemanaAsistencia> call() throws Exception {

                ObservableList<SemanaAsistencia> asistencia = FXCollections.observableArrayList();

                String txtDesde = Utils.formatDate(desde);
                String txtHasta = Utils.formatDate(hasta);

                Calendar actual = Calendar.getInstance();
                actual.setTime(desde);

                ObservableList<HorarioUsuario> thorarios = DBQueries.getHorariosUsuarioPerdiodo(usuario.getCodigo(),txtDesde, txtHasta);
                ObservableList<Registro> registros = DBQueries.getRegistrosUsuarioPeriodo(usuario.getCodigo(),txtDesde, txtHasta);
                ObservableList<Evento> eventos = DBQueries.getEventosJornadaFechas(txtDesde, txtHasta);
                ObservableList<JustificantePeriodo> justif = DBQueries.getJustificantesAprobadosUsuario(usuario.getCodigo());

                SemanaAsistencia semana = new SemanaAsistencia();

                while (hasta.compareTo(actual.getTime()) >= 0){              
                    AsistenciaUsuario au = new AsistenciaUsuario();     
                    au.setUsuario(usuario.getCodigo());
                    au.setFecha(actual.getTime());

                    HorarioUsuario horario = null;
                    boolean debioasistir = false;
                    for (HorarioUsuario h : thorarios){
                        if ( h.getVigencia().compareTo(actual.getTime())>=0 
                                && h.debioAsistir(actual.get(Calendar.DAY_OF_WEEK)) ){
                            horario = h;
                            debioasistir=true;
                            break;
                        }                            
                    }

                    if (debioasistir && horario!=null){
                        au.setHoraEntrada(horario.getEntrada());
                        au.setHoraSalida(horario.getSalida());

                        Registro ent = Utils.findAndDeletePrimerRegistro(actual.getTime(), registros);                            
                        Registro sal = Utils.findAndDeleteUltimoRegistro(actual.getTime(), registros);

                        if (ent!=null && sal!=null){
                            au.setRegistroEntrada(ent);
                            au.setRegistroSalida(sal);
                        } else {
                            Registro r = ent!=null? ent: sal;
                            if (r!=null && Utils.cercaEntrada(r.getHora(), horario.getEntrada(), horario.getSalida())){
                                au.setRegistroEntrada(r);
                            } else {
                                au.setRegistroSalida(r);
                            }
                        }

                        if (au.getRegistroEntrada()==null || au.getRegistroSalida() == null){                               

                            Evento e = Utils.findEvento(actual.getTime(), eventos);
                            if ( e!=null) {
                                au.setJustif(e);                             
                            } else {                                
                                JustificantePeriodo j = Utils.findJustificanteDia(actual.getTime(), justif);
                                if ( j!=null ){
                                    au.setJustif(j);                                                    
                                }
                            }
                        } 
                    }                    

                    semana.addDay(actual.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_WEEK),au,debioasistir);

                    if (actual.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ){
                        asistencia.add(semana);
                        semana = new SemanaAsistencia();
                    }

                    actual.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (semana.getSemana().size() > 0)
                    asistencia.add(semana);

                return asistencia;                      
            }
        };
    }
}
