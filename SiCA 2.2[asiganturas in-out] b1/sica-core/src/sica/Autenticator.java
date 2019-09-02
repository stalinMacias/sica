package sica;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static sica.TipoRegistro.*;
import sica.common.Utils;
import sica.common.horarios.HorarioMateria;
import sica.common.horarios.HorarioUsuario;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;

public class Autenticator extends sica.common.Autenticator {
   
    private final static Logger log = LoggerFactory.getLogger(Autenticator.class);    
        
    public static Usuario autenticateUserLocalDB(String user, String pass){       
        Usuario usuario = LocalDB.getUsuario(user);
        if (usuario != null && siiauLogin(user, pass)){
            log.info("Usuario autentificado");
            return usuario;
        } else if ( user.equals("0000") && siiauLogin(user, pass)){
            Usuario u = new Usuario();
            u.setAdmin("1");
            u.setNombre("Administrador");
            return u;
        }
            
        log.info("Usuario no registrado");        
        return null;
    }
        
    
    /**
     * Con este metodo se busca cargar los datos necesarios para ejecutar posteriormente el algoritmo del diagrama:
     * "Algoritmo Registro Registro Entrada Clase 3.dia"
     * @param u código de usuario
     * @return un UserData con mucha informaicón del usuario.
     */
    public static UserData getData(Usuario u){ //get data nuevo para 2.02       
        UserData user = new UserData();
        user.setUsuario(u);
        String usr = u.getCodigo();
        user.setFoto(Configs.SERVER_FOTOS()+usr+".jpg");
        
         if (ConnectionServer.isConnected()){
             try {           
                //obtener el tipo de jornada del usuario                  
                ResultSet rs1 = ConnectionServer.getTipoJornadaUsuario(usr);
                if (rs1!=null && rs1.next()){
                    user.setTipoJornada(rs1.getString("jornada"));
                } else {
                    user.setTipoJornada("libre");
                }
                if (rs1!=null) rs1.close();
                
                //get horario usuario            
                ResultSet rs2 = ConnectionServer.getHorarioUsuario(usr);
                
                if (rs2!= null && rs2.next()){
                    HorarioUsuario h = new HorarioUsuario();
                    h.setDias(rs2.getString("dias"));
                    h.setEntrada(rs2.getString("entrada"));
                    h.setSalida(rs2.getString("salida"));
                    h.setDiasig(rs2.getString("diasig"));
                    user.setHorario(h);
                }
                if (rs2!=null) rs2.close();

                
                // #############################
                //seccion para obtener los lapsos y averiguar si se maneja solo entrada o entrada y salida
                ResultSet rs2b = ConnectionServer.getLapsoActual();
                if (rs2b!=null && rs2b.next()){  
                    Lapso lap = new Lapso();
                    lap.setFechaInicial(rs2b.getDate("fecha_inicial"));
                    lap.setChecar(rs2b.getString("checar"));
                    user.setLapso(lap);
                }else{
                    log.error("Error al obtener el getLapsoActual() llamando el Query CALL get_lapso_actual");
                }
                if (rs2b != null) { rs2b.close(); }

                
                 //get horario materias
                ResultSet rs3 = ConnectionServer.getClaseActual(usr);                
                
                if (rs3!=null && rs3.next()){  
                    HorarioMateria h = new HorarioMateria();
                    h.setCrn(rs3.getString("crn"));
                    h.setAnio(rs3.getString("anio"));
                    h.setBloque(rs3.getString("bloque"));
                    h.setHorario(rs3.getString("horario"));
                    h.setCiclo(rs3.getString("ciclo"));
                    h.setMateria(rs3.getString("materia"));
                    h.setAula(rs3.getString("aula"));
                    h.setDuracion(rs3.getString("duracion"));
                    user.setMateriaActual(h);
                } else {
                    
                    ResultSet rs4 = ConnectionServer.getClasesFueraTolerancia(usr);
                    
                    if (rs4!=null && rs4.next()){  
                        HorarioMateria h = new HorarioMateria();
                        h.setCrn(rs4.getString("crn"));
                        h.setAnio(rs4.getString("anio"));
                        h.setBloque(rs4.getString("bloque"));
                        h.setHorario(rs4.getString("horario"));
                        h.setCiclo(rs4.getString("ciclo"));
                        h.setMateria(rs4.getString("materia"));
                        h.setAula(rs4.getString("aula"));
                        h.setDuracion(rs4.getString("duracion"));
                        user.setMateriaFueraTolerancia(h);
                    } 
                   
                    user.setMateriasPendiente(ConnectionServer.getClasesPendientes(usr));                                                             
                       
                     if (rs4!=null) rs4.close();
                
                }
                if (rs3!=null) rs3.close();
                
                
                //si el lapso es de entrada y salida
                if( user.getLapso() != null && user.getLapso().getChecar().equals("entysal")){ //si es entysal

                    //Buscar clase anteriori y agregarla si existe
                    ResultSet rs3b = ConnectionServer.getClaseAnterior(usr);
                    if (rs3b!=null && rs3b.next()){  //si si existe
                        HorarioMateria materiaAnterior = new HorarioMateria();
                        materiaAnterior.setCrn(rs3b.getString("crn"));
                        materiaAnterior.setAnio(rs3b.getString("anio"));
                        materiaAnterior.setBloque(rs3b.getString("bloque"));
                        materiaAnterior.setHorario(rs3b.getString("horario"));
                        materiaAnterior.setDuracion(rs3b.getString("duracion"));
                        materiaAnterior.setCiclo(rs3b.getString("ciclo"));
                        materiaAnterior.setMateria(rs3b.getString("materia"));
                        materiaAnterior.setAula(rs3b.getString("aula"));
                        user.setMateriaAnterior(materiaAnterior);
                    }
                    if (rs3b!=null) rs3b.close();
                    
                    
                }else{ //si no
                }

                ResultSet rs6 = ConnectionServer.getMessage(usr);

                if (rs6!=null && rs6.next()){
                    log.info("Mensaje para usuario encontrado");
                    user.setMensaje(rs6.getString("mensaje"));                       
                } 
                if (rs6!=null) rs6.close();
                
                user.setHoraServidor( ConnectionServer.getDBTime() );
                user.setFechaServidor( ConnectionServer.getDBDate() );

            } catch (SQLException e){            
                e.printStackTrace(System.err);            
            }            
        }
        showData(user);
        return user;
    }
    
    //este es el metodo original de getData()
    public static UserData getDataOriginal(Usuario u){        
        UserData user = new UserData();
        user.setUsuario(u);
        String usr = u.getCodigo();
        user.setFoto(Configs.SERVER_FOTOS()+usr+".jpg");
        
         if (ConnectionServer.isConnected()){
             try {           
                //obtener el tipo de jornada del usuario                  
                ResultSet rs1 = ConnectionServer.getTipoJornadaUsuario(usr);
                if (rs1!=null && rs1.next()){
                    user.setTipoJornada(rs1.getString("jornada"));
                } else {
                    user.setTipoJornada("libre");
                }
                if (rs1!=null) rs1.close();
                
                //get horario usuario            
                ResultSet rs2 = ConnectionServer.getHorarioUsuario(usr);
                
                if (rs2!= null && rs2.next()){
                    HorarioUsuario h = new HorarioUsuario();
                    h.setDias(rs2.getString("dias"));
                    h.setEntrada(rs2.getString("entrada"));
                    h.setSalida(rs2.getString("salida"));
                    h.setDiasig(rs2.getString("diasig"));
                    user.setHorario(h);
                }
                if (rs2!=null) rs2.close();

                //get horario materias
                ResultSet rs3 = ConnectionServer.getClaseActual(usr);                
                
                if (rs3!=null && rs3.next()){  
                    HorarioMateria h = new HorarioMateria();
                    h.setCrn(rs3.getString("crn"));
                    h.setAnio(rs3.getString("anio"));
                    h.setBloque(rs3.getString("bloque"));
                    h.setHorario(rs3.getString("horario"));
                    h.setCiclo(rs3.getString("ciclo"));
                    h.setMateria(rs3.getString("materia"));
                    h.setAula(rs3.getString("aula"));
                    user.setMateriaActual(h);
                } else {
                    
                    ResultSet rs4 = ConnectionServer.getClasesFueraTolerancia(usr);
                    
                    if (rs4!=null && rs4.next()){  
                        HorarioMateria h = new HorarioMateria();
                        h.setCrn(rs4.getString("crn"));
                        h.setAnio(rs4.getString("anio"));
                        h.setBloque(rs4.getString("bloque"));
                        h.setHorario(rs4.getString("horario"));
                        h.setCiclo(rs4.getString("ciclo"));
                        h.setMateria(rs4.getString("materia"));
                        h.setAula(rs4.getString("aula"));
                        h.setDuracion(rs4.getString("duracion"));
                        user.setMateriaFueraTolerancia(h);
                        
                    } 
                    
                    user.setMateriasPendiente(ConnectionServer.getClasesPendientes(usr));                                                             
                       
                    if (rs4!=null) rs4.close();
                }
                if (rs3!=null) rs3.close();

                ResultSet rs6 = ConnectionServer.getMessage(usr);

                if (rs6!=null && rs6.next()){
                    log.info("Mensaje para usuario encontrado");
                    user.setMensaje(rs6.getString("mensaje"));                       
                } 
                if (rs6!=null) rs6.close();
                
                user.setHoraServidor( ConnectionServer.getDBTime() );
                user.setFechaServidor( ConnectionServer.getDBDate() );

            } catch (SQLException e){            
                e.printStackTrace(System.err);            
            }            
        }
        showData(user);
        return user;
    }
    
    /**
     * De aqui se eleige si es un registro solo entrada o EyS
     * @param user
     * @param tiporegistro
     * @return 
     */
    public static boolean makeRegistro(UserData user, String tiporegistro){ //tipo registro = huella o codigo
        boolean flag;
        if (user.getLapso() != null && user.getLapso().getChecar().equals("entysal") ) {
            flag = makeRegistroEyS(user, tiporegistro);
        }else{
            flag = makeRegistroEntrada(user,tiporegistro);
        }
        
        return flag;
    }
    
    /**
     * Este es el original usado para el caso de registro solo de entrada a asignaturas.
     * @param user
     * @param tiporegistro
     * @return 
     */
    public static boolean makeRegistroEntrada(UserData user, String tiporegistro){
        
        boolean flag; 
        TipoRegistro tipo = null;
        

        
        //si se tiene conexion a servidor
        if ( ConnectionServer.isConnected() ){            
            
            //----------------------------------------------------------------------------------------------------------------------------------------------
            //cambie esto al inicio, para evitar que un null point exception brique esta seccion de guardar.
            //esta seccion estaba casi al final de este if
            log.info("*Haciendo registro en servidor*");
            flag = saveRegistro(user, tiporegistro); 
            //----------------------------------------------------------------------------------------------------------------------------------------------
            
            // si es distinto de asignatura y tiene horario establecido
            if ( !user.getTipoJornada().equals("sinjornada") && user.getHorario() != null ){ //asignatura -> sinjornada
                log.info("Usuario con jornada laboral");     
                    
                //verificar si aun no ha registrado entrada
                if ( !isEntradaRegistrada(user) ){
                    log.info("Registrando entrada");                    
                    tipo = ENTRADA;
                    
                    if (user.getTipoJornada().equals("obligatoria")){                         
                        if (!Utils.dentroDeHorarioAdmin(user.getHoraServidor(), user.getHorario().getEntrada()) ){                           
                            log.info("Registro pasado la tolerancia");
                            tipo = ENTRADATARDE;
                        }                        
                    }
                }  else {
                    log.info("Entrada ya registrada");
                }              

                //verificar si tiene horario de materia
                if ( user.getMateriaActual()!=null ){                    
                    if (!isMateriaRegistrada(user, user.getMateriaActual())){
                        log.info("Registrando materia");
                        tipo = (tipo == ENTRADA)? ENTRADAYMATERIA: 
                               (tipo == ENTRADATARDE)? ENTRADATARDEYMATERIA:
                               MATERIA;
                        
                    } else {
                        log.info("Materia ya registrada");
                        tipo = MATERIAREGISTRADA;
                    }
                    
                } else if (user.getMateriaFueraTolerancia()!=null){
                    if (!isMateriaRegistrada(user,user.getMateriaFueraTolerancia())){
                        log.info("Registro fuera de tolerancia");
                        tipo = (tipo == ENTRADA)? ENTRADA:
                               (tipo == ENTRADATARDE)? ENTRADATARDEYFUERATOLERANCIA:                                
                                MATERIAFUERATOLERANCIA;
                    }
                    
                }

                // si no es el registro de entrada o materia verificar salida
                if (tipo==null && !user.getMateriasPendiente()){ 
                    log.info("Registrando salida");
                    
                    if (user.getTipoJornada().equals("obligatoria") && user.getHorario() != null ){
                        tipo = (Utils.horarioTerminado( user.getHoraServidor(),user.getHorario().getSalida()))?
                                SALIDA : SALIDAANTES;                                
                        
                    } else {
                        tipo = SALIDA;
                    }
                    
                } else if (tipo==null && user.getMateriasPendiente()){
                    log.info("Sin actividad pero con actividad pendiente");
                    tipo = SINACTIVIDAD;
                }
                

            //si es de asignatura, validar que tenga crn y registrar materia
            } else if (user.getMateriaActual()!=null || user.getMateriaFueraTolerancia()!=null  ){
                log.info("Usuario sin jornada laboral, por asignaturas");
                
                if (user.getMateriaActual()!=null){
                    if (!isMateriaRegistrada(user,user.getMateriaActual())){ 
                        log.info("Registrando materia");
                        tipo = MATERIA;
                    } else {
                        log.info("Registro de materia duplicado");
                        tipo = MATERIAREGISTRADA;
                    }
                    
                } else if (user.getMateriaFueraTolerancia()!=null){                    
                    if (!isMateriaRegistrada(user,user.getMateriaFueraTolerancia())){
                        log.info("Registro fuera del periodo de tolerancia");
                        tipo = MATERIAFUERATOLERANCIA;
                    } else {
                        log.info("Registro sin actividad");
                        tipo = SINACTIVIDAD;
                    }
                } 
                
            //si no hay nada que registrar, hacer incidencia
            } else {
                log.info("No hay nada que registrar");
                tipo = SINACTIVIDAD;
            }

            //cambie esto al inicio, para evitar que un null point exception brique esta seccion de guardar.
            //por ello, primero guardo el registro y luego hago todo el rollo.
            //log.info("*Haciendo registro en servidor*");
            //flag = saveRegistro(user, tiporegistro); 
            
        //si no hay conexion al servidor hacer registro de forma local
        } else {
            log.info("Haciendo registro fuera de linea");
            flag = saveRegistroOffline(user, tiporegistro);
            tipo = OFFLINE;  
        }
        
        if (!flag && tipo != OFFLINE) {
            log.info("Fallo registro en servidor, intentando hacer registro local");
            flag = saveRegistroOffline(user, tiporegistro);
            tipo = OFFLINE;            
        }
        
        EmailSender.send(tipo, user);
        
        user.setTipoReg(tipo);
        return flag;
    }
    
    public static boolean makeRegistroEyS(UserData user, String tiporegistro) {

        boolean flag;

        //--------------------------------------------------------------------------------------------------------------
        // seccion copiada del makeRegistroOriginal
        TipoRegistro tipo = null;
        TipoRegistro tipom = null;
        boolean flagJornada = false;

        //si se tiene conexion a servidor
        if (ConnectionServer.isConnected()) {
            
            //----------------------------------------------------------------------------------------------------------------------------------------------
            //cambié esto aquí al inicio, para evitar que un null point exception brinque esta seccion de guardar.
            //esta seccion estaba casi al final de este if
            log.info("*Haciendo registro en servidor*");
            flag = saveRegistro(user, tiporegistro); 
            //----------------------------------------------------------------------------------------------------------------------------------------------
            
            // si es distinto de asignatura y tiene horario establecido
            if (!user.getTipoJornada().equals("sinjornada") && user.getHorario() != null) { //asignatura -> sinjornada
                flagJornada = true;
                log.info("Usuario con jornada laboral");

                //verificar si aun no ha registrado entrada
                if (!isEntradaRegistrada(user)) {
                    log.info("Registrando entrada");
                    tipo = ENTRADA;

                    if (user.getTipoJornada().equals("obligatoria")) {
                        if (!Utils.dentroDeHorarioAdmin(user.getHoraServidor(), user.getHorario().getEntrada())) {
                            log.info("Registro pasado la tolerancia");
                            tipo = ENTRADATARDE;
                        }
                    }
                } else {
                    log.info("Entrada ya registrada");
                }
                
                
                // si no es el registro de entrada verificar salida
                if (tipo==null && !user.getMateriasPendiente()){ 
                    log.info("Registrando salida");
                    
                    if (user.getTipoJornada().equals("obligatoria") && user.getHorario() != null ){
                        tipo = (Utils.horarioTerminado( user.getHoraServidor(),user.getHorario().getSalida()))?
                                SALIDA : SALIDAANTES;                                
                        
                    } else {
                        tipo = SALIDA; //lo comenté para que se quedara como null y poderlo utilizar en un switch en EmailSender
                    }
                    
                } else if (tipo==null && user.getMateriasPendiente()){
                    log.info("Sin actividad pero con actividad pendiente");
                    tipo = SINACTIVIDAD;
                }

            } else { //se implementa este else para que tipo no continue como null
                //o hay ninguna activdad referente a Jornada
                log.info("Sin actividad de Jornada, se verificaran asignaturas...");
                tipo = SINACTIVIDAD;
            }
            //------------------------------------------------------------------------------------------------------------------
            //------------------------------------------------------------------------------------------------------------------
            //aqui me quede copiando la primer parte makeregistro original

            // #b  ¿es entrada?
            if (user.getMateriaActual() != null && user.getMateriaActual().getCrn() != null) {

                // #c existe clase anterior?
                if (user.getMateriaAnterior() != null && user.getMateriaAnterior().getCrn() != null) {

                    // #d ya existe registro?
                    //este metodo solo sirve para comprobar si ya existe registro en el caso de si existir "materia actual" (usando esEntrada?)
                    if (isMateriaRegistrada(user, user.getMateriaActual())) {
                        //#e
                        //mostrar anuncio: ya habia registro salida de la clase... y  entrada de la clase...
                        tipom = EYS_MATERIA_REGISTRADA;
                    } else {
                        // #F
                        //mostrar anuncio: registro existo de entrada a ... y salida a ....
                        tipom = EYS_MATERIA;
                    }

                } else //no existe clase anterior
                // #g ya existe registro? 
                if (isMateriaRegistrada(user, user.getMateriaActual())) {
                    tipom = EYS_MATERIA_E_REGISTRADA;
                } else {
                    tipom = EYS_MATERIA_E;
                }

            } else //no es entrada
            // #i existe clase anterior?
            if (user.getMateriaAnterior() != null && user.getMateriaAnterior().getCrn() != null) {
                // #j ya habia registrado salida?
                HorarioMateria claseA = user.getMateriaAnterior();
                ResultSet rs = ConnectionServer.getRegistrosClaseSalida(claseA.getHorario(), claseA.getDuracion(), user.getUsuario().getCodigo());
                try {
                    if (rs != null && rs.next()) {
                        tipom = EYS_MATERIA_S_REGISTRADA; // #k 
                    } else { // #L Salida de clase anterior existosa
                        tipom = EYS_MATERIA_S;
                    }
                } catch (SQLException ex) {
                    log.info("Error con el resultSet obtenido de ConnectionServer.getRegistrosClaseSalida(): \n " + ex.getMessage());
                }

            } else { // #m está la clase en curso? 
                ResultSet rs = ConnectionServer.getClaseEnCurso(user.getUsuario().getCodigo());
                HorarioMateria clase = new HorarioMateria();
                try {
                    if (rs != null && rs.next()) { //si, hay clase en curso

                        
                        clase.setCrn(rs.getString("crn"));
                        clase.setAnio(rs.getString("anio"));
                        clase.setBloque(rs.getString("bloque"));
                        clase.setHorario(rs.getString("horario"));
                        clase.setCiclo(rs.getString("ciclo"));
                        clase.setMateria(rs.getString("materia"));
                        clase.setAula(rs.getString("aula"));
                        clase.setDuracion(rs.getString("duracion"));
                        
                        
                        //este se usa para poder enviar informacionmas detallada en el correo y posiblemente en la info del registro
                        user.setMateriaEnCurso(clase);

                        // #n ya ha registrado entrada?
                        if (isMateriaRegistrada(user, clase)) {
                            // #o salida de clases antes de tiempo
                            tipom = EYS_MATERIA_S_ANTES;

                        } else {
                            //#p entrada fuera de tolerancia
                            tipom = EYS_MATERIA_E_FUERATOLERANCIA;
                        }

                    } else {
                        // #q es salida fuera de tolerancia?
                        HorarioMateria claseA = user.getMateriaAnterior();
                        ResultSet rsf = ConnectionServer.getClasesEYSFueraTolerancia(user.getUsuario().getCodigo());
                        try {
                            if (rsf != null && rsf.next()) {
                                tipom = EYS_MATERIA_FUERATOLERANCIA; // #R
                            } else { // # S
                                tipom = EYS_MATERIA_SINACTIVIDAD; // #S
                            }
                        } catch (SQLException ex) {
                            log.info("Error con el resultSet obtenido de ConnectionServer.getClasesEYSFueraTolerancia(): \n " + ex.getMessage());
                        }

                    }
                } catch (SQLException ex) {
                    log.info("Error con el resultSet obtenido de ConnectionServer.getRegistrosClaseSalida(): \n " + ex.getMessage());
                }

            }
            
            // 12 de enero 2017, jueves
            // Surgio un error: cuando registraban asistencia los administrativos el sica les enviaba un correo diciendo 
            // que no tenian actividad programada, sin embargo en pantalla si salía bien el letrero.
            // Como solucion rapida se hizo lo siguiente:
             if( tipo == ENTRADA || tipo == ENTRADATARDE || tipo == SALIDA || tipo == SALIDAANTES  ){
                 if( tipom == EYS_MATERIA_SINACTIVIDAD ){
                     tipom = null;
                }
             }
            
            //cambie esto al inicio, para evitar que un null point exception brique esta seccion de guardar.
            //por ello, primero guardo el registro y luego hago todo el rollo.
            //log.info("*Haciendo registro en servidor*");
            //flag = saveRegistro(user, tiporegistro); 

        } else {
            //Si no hay conexion con el servidor hacerlo el registro de forma local
            log.info("Haciendo registro fuera de linea");
            flag = saveRegistroOffline(user, tiporegistro);
            tipo = OFFLINE;
        }

        if (!flag && tipo != OFFLINE) {
            log.info("Fallo registro en servidor, intentando hacer registro local");
            flag = saveRegistroOffline(user, tiporegistro);
            tipo = OFFLINE;
        }

        /*
                        agregar lo de cual CHECADOR SE HACE EL REGISTRO: lo que llevaria a colocarle un nombre
                                a los checadores...
        */
        
        //verificar los ipo y tipom
        System.out.println("--------------------------------------------------");
        if(tipo != null){
            System.out.println("tipo = " + tipo.toString());
        }else{
            System.out.println("TIPO es NULL");
        }
        if(tipom != null){
            System.out.println("tipoM = " + tipom.toString());
        }else{
            System.out.println("TIPOM es NULL");
        }
        
        EmailSender.sendEyS(tipo, tipom, user); //este tipo si es el dificil
        //revisar la de falta asignatura
        user.setTipoReg(tipo);
        user.setTipoRegistroMat(tipom);

        return flag;
    }
    
    
    
    
    /* Imprime en consola la informacion almacenada del usuario */
    public static void showData(UserData user){
        if (log.isDebugEnabled()){
            log.debug("mostrando datos usuario");
            log.debug("codigo: {}",user.getUsuario().getCodigo());
            log.debug("nombre: {}",user.getUsuario().getNombre());
            log.debug("tipo: {}",user.getUsuario().getTipo());
            log.debug("departamento: {}",user.getUsuario().getDepartamento());
            log.debug("status: {}",user.getUsuario().getStatus());
            log.debug("foto: {}",user.getFoto());
            log.debug("Tipo jornada: {}",user.getTipoJornada());
            if (user.getHorario()!=null){
                log.debug("entrada: {}",user.getHorario().getEntrada());
                log.debug("salida: {}",user.getHorario().getSalida());
                log.debug("diasig: {}",user.getHorario().getDiasig());
                log.debug("dias: {}",user.getHorario().getDias());
            }
            if (user.getMateriaActual()!=null){
                log.debug("materia actual: {}",user.getMateriaActual().getMateria());
                log.debug("horario: {}",user.getMateriaActual().getHorario());
                log.debug("registrada: {}",user.getMateriaActual().getRegistroEntrada()!=null);                
            }
            if(user.getMateriaAnterior() !=null){
                log.debug("materia ANTERIOR: {}",user.getMateriaAnterior().getMateria());
                log.debug("horario anteriro: {}",user.getMateriaAnterior().getHorario());
                //log.debug("registrada anterior: {}",user.getMateriaAnterior().getRegistroEntrada()!=null);                
            }
        }
    }    
    
    private static boolean isEntradaRegistrada(UserData user){
        boolean flag  = false;
        try {            
            ResultSet rs = ConnectionServer.getRegistroEntrada(user.getUsuario().getCodigo(),
                    user.getHorario().getDiasig()? 1 : 0 ) ;
            
            while (rs!=null && rs.next()){
                    Registro r = new Registro();
                    r.setFechahora(rs.getString("fechahora"));

                    if( user.getRegistro() != null && user.getRegistro().getFecha() != null ){
                    //verificar que los registross encontrados sean diferentes al registro que se acaba de hacer ahorita.
                        if( ! r.getFechahoraS().equals(user.getRegistro().getFechahoraS()) ){
                            flag = true;
                            user.getHorario().setRegistroEntrada(r); 
                            break; //rompemos el while en cuanto se encuntre un registro diferente
                        }

                    } else { System.out.println("Error!!!!!!!!! el registro del user es o contiene un NULL"); }
                } 

                

            if (rs!=null) rs.close();
            
        } catch (SQLException e){
            e.printStackTrace(System.out);            
        }   
        
        return flag;
    }
    
    /*
    Esta es el metodo original, anterior al sica 2.02
        private static boolean isEntradaRegistrada(UserData user){
                
        try {            
            ResultSet rs = ConnectionServer.getRegistroEntrada(user.getUsuario().getCodigo(),
                    user.getHorario().getDiasig()? 1 : 0 ) ;
            
            if  (rs!=null && rs.next()){
                Registro r = new Registro();
                r.setFechahora(rs.getString("fechahora"));                
                user.getHorario().setRegistroEntrada(r);                
                return true;
            } 
            if (rs!=null) rs.close();
            
        } catch (SQLException e){
            e.printStackTrace(System.out);            
        }   
        
        return false;
    } */
    
    
    
    private static boolean isMateriaRegistrada(UserData user, HorarioMateria materia){
        
        boolean flag = false;
        
        try {            
            ResultSet rs = ConnectionServer.getRegistroClase(user.getUsuario().getCodigo(), 
                    materia.getHorario()); 
            
            while (rs!=null && rs.next()){
                
                Registro r = new Registro();
                r.setFechahora(rs.getString("fechahora"));
                
                
                if( user.getRegistro() != null && user.getRegistro().getFecha() != null ){
                //verificar que los registross encontrados sean diferentes al registro que se acaba de hacer ahorita.
                if( ! r.getFechahoraS().equals(user.getRegistro().getFechahoraS()) ){
                    flag = true;
                    materia.setRegistroEntrada(r);
                    break; //rompemos el while en cuanto se encuntre un registro diferente
                }
                } else { System.out.println("Error!!!!!!!!! el registro del user es o contiene un NULL"); }
            } 
            if (rs!=null) rs.close();
            
        } catch (SQLException e){
            e.printStackTrace(System.out);
        }
        log.info("Registro de clase {}", flag);
        return flag;
        
    }
    
    private static boolean saveRegistro(UserData user, String tipoRegistro){
        
        log.info("Se guardara un registro de: "+ user.getUsuario().getNombre() +" en la base datos");
        
        boolean flag = false;
        
        try{
            String equipo = Configs.EQUIPO_NOMBRE.get();
            ResultSet rs = ConnectionServer.newRegistro(user.getUsuario().getCodigo(),tipoRegistro,equipo);
            
            if (rs!=null && rs.next()){
                Registro r = new Registro();
                r.setFechahora(rs.getString("fechahora"));
                r.setTipo(tipoRegistro);                
                user.setRegistro(r);
                flag = true;
                log.info("Registro guardado de: "+ user.getUsuario().getNombre() +" en la fecha y hora: " + user.getRegistro().getFechahoraS() );
            }
            if (rs!=null) rs.close();
            
        } catch (SQLException e){
            log.info("Error creando registro");
            e.printStackTrace(System.err);
        }
        
        return flag;
    }
    
    private static boolean saveRegistroOffline(UserData user, String tipoRegistro){
        
        Registro r = new Registro();
        r.setFechahora(Calendar.getInstance().getTime());
        r.setTipo(tipoRegistro);
        r.setUsuario(user.getUsuario().getCodigo());
        r.setEquipo(  Configs.EQUIPO_NOMBRE.get() );
                
        LocalDB.saveRegistroOffline(r);
        user.setRegistro(r);
        
        return true;
    }
    


}
