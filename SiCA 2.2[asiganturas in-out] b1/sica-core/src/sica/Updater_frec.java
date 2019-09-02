package sica;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.Utils;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;
import sica.objetos.Huella;

public class Updater_frec {
        
    private final static Logger log = LoggerFactory.getLogger(Updater_frec.class);    
    private static Updater_frec instance;
    
    private final Service<Void> updateService;
    private boolean requested;
       
    public static void update(){
        update(false);
    }
    
    public static void update(boolean requested){
        if (instance == null){
            instance = new Updater_frec();
        } else if (instance.updateService.isRunning()){
            instance.updateService.cancel();
        }
        instance.requested = requested;
        instance.updateService.reset();
        instance.updateService.start();
        
    }
    
    public static ReadOnlyBooleanProperty runningProperty(){
         if (instance == null){
            instance = new Updater_frec();
        }
        return instance.updateService.runningProperty();
    }
    
    private Updater_frec(){
        updateService = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override protected Void call() throws Exception {
                        callUpdate();
                        return null;
                    }
                };
            }
        };            
    }
    
    private void callUpdate(){
        
        log.info("Iniciando actualizador");
        log.info("# requested = " + requested);
        //Utils.updateSistemTime("");
        
        if (ConnectionServer.isConnected() ){
            try {                
                //mandar registros offline al servidor                
                List<Registro> regs = LocalDB.getRegistrosOffline();                
                log.info("Enviando {} registros offline a servidor", regs.size());
                
                if (regs.size()>0){
                    regs.forEach((r) -> {                    
                        int resp = ConnectionServer.insertRegistroOffline(
                                r.getUsuario(), r.getFechahoraS(), r.getTipo(), r.getEquipo());
                        if (resp!=0) {
                            LocalDB.deleteRegistroOffline(r);
                        }                
                    });
                    log.info("Envio terminado");

                }
                
                //Sincronizar datos
                ResultSet lu = ConnectionServer.getLastUpdate();                
                if (lu!=null && lu.next()){        
                    String lastUpdated = lu.getString("actualizado");
                    Boolean updateConfigs = false;
                    
                    if (lu.getString("usuario").equals("0") && !requested){
                        log.info("Actualizacion de configuraciones recibida");
                        updateConfigs = true;
                        updateConfigs();
                    } 
                    
                    List<Huella> localHuellas = LocalDB.getHuellas();
                    if ( localHuellas == null || localHuellas.isEmpty() || Configs.LASTUPDATE.get().equals("0000-00-00 00:00:00") || requested ){
                        if (requested) LocalDB.deleteAll();                        
                        doFirstTime();

                    } else if (!updateConfigs){
                        log.info("Verificando por cambios en servidor");                
                        Date d = Utils.parseFullDateTime(Configs.LASTUPDATE.get());    
                        Date d2 = Utils.parseFullDateTime(lastUpdated);
                       
                        if (d.compareTo(d2) < 0){
                           
                           log.info("Sincronizando usuarios");
                           try(ResultSet up = ConnectionServer.getUpdatedUsers( Configs.LASTUPDATE.get() )){
                               boolean huellasBien = true;
                                while(up.next()){                         
                                    if (up.getString("huella")!=null){
                                        log.trace("Actualizando huella {}",up.getInt("huella"));
                                        // indicar que hay actualizacion de huellas, para cargarlas de nuevo en el ScannerValidator
                                       //creo que no es necesario 
                                       //Configs.HUELLAS_UPDATE.set(true);
                                       
                                       //primero verificamos que existan en ram las listas de huellas y referencias:
                                       if( !ScannerValidator_Frec.existenListas() ){
                                           System.out.println("No existen listas de huellas, ubicaciones o referencias en ram, procediendo a recargarlas");
                                           ScannerValidator_Frec.recargarHuellas();
                                       }

                                        ResultSet hue = ConnectionServer.getHuella(up.getInt("huella"));
                                        System.out.println("Eliminando la huella: " + up.getInt("huella"));
                                        LocalDB.deleteHuella(up.getInt("huella"));
                                        ScannerValidator_Frec.eliminarHuellaRam(up.getInt("huella"));
                                        //ScannerValidator.acutalizarUbicacionesLocalDB();

                                        if (hue!=null && hue.next()){
                                             Huella h = new Huella();
                                             h.setUsuario( hue.getString("usuario") );
                                             h.setId( hue.getInt("id") );                               
                                             h.setHuella( hue.getBytes("huella") );                                         
                                             LocalDB.saveHuella(h);
                                             
                                             //agregar la huella a RAM
                                             if(ScannerValidator_Frec.agregarHuellaRam(h) == false){
                                                 //si sucede un error indicarlo en una bandera
                                                 huellasBien = false;
                                             }
                                             
                                        } 
                                        if (hue!=null) hue.close();

                                    } else {
                                        log.debug("Actualizando usuario {}",up.getString("usuario"));

                                        ResultSet usr = ConnectionServer.getUsuario(up.getString("usuario"));
                                        LocalDB.deleteUsuario(up.getString("usuario"));

                                        if (usr!=null && usr.next()){
                                            Usuario u = new Usuario();
                                            u.setCodigo(usr.getString("usuario"));
                                            u.setDepartamento(usr.getString("departamento"));
                                            u.setNombre(usr.getString("nombre"));
                                            u.setTipo(usr.getString("tipo"));
                                            u.setTelefono(usr.getString("telefono"));
                                            u.setStatus(usr.getString("status"));
                                            u.setCorreo(usr.getString("correo"));
                                            u.setAdmin(usr.getString("isadmin"));

                                            LocalDB.saveUsuario(u);          
                                        } 
                                        if (usr!=null) usr.close();
                                    }
                                }     
                                if (up!=null) up.close();
                                
                                //Si sucedio error con agregarHuellasRam, recargar las huellas en ram por completo
                                if(huellasBien == false){
                                    //mostrar letrero: favor de esperar - recargando huellas
                                    ScannerValidator_Frec.recargarHuellas();
                                }
                           }
                       } else {
                           log.info("No hay cambios en servidor, no se realizara ninguna acción");
                       }
                    }                    
                    Configs.LASTUPDATE.set(lastUpdated);
                   
                } else {
                    log.error("Imposible obtener fecha de ultima actualización");
                }      
                if (lu!=null) lu.close();
                
                log.info("Sincronizacion finalizada");
                
            } catch (SQLException e){
                log.info("Error sincronizando bases de datos");
                log.info(e.getMessage());                
            }   
            
        } else {
            log.info("No hay internet");
        }
    }
    
    private void doFirstTime(){
        try {
            
            log.info("Actualizando la base de datos completa");                           
            
            log.info("Iniciando migracion de usuarios");            
            try(ResultSet rs = ConnectionServer.getAllUsuarios()){            
                while (rs.next()){
                    Usuario u = new Usuario();
                    u.setCodigo(rs.getString("usuario"));
                    u.setDepartamento(rs.getString("departamento"));
                    u.setNombre(rs.getString("nombre"));
                    u.setTipo(rs.getString("tipo"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setStatus(rs.getString("status"));
                    u.setCorreo(rs.getString("correo"));
                    u.setAdmin(rs.getString("isadmin"));
                    LocalDB.saveUsuario(u);
                    
                }
                if (rs!=null) rs.close();
            }
            if (log.isDebugEnabled())log.debug("{} Usuarios en LocalDB ",LocalDB.getUsuarios().size());
            
            log.info("iniciando migracion de huellas");        

            try (ResultSet rs = ConnectionServer.getHuellas()){            
                while(rs.next()){                
                    Huella h = new Huella();
                    h.setUsuario( rs.getString("usuario") );
                    h.setId( rs.getInt("id") );                               
                    h.setHuella( rs.getBytes("huella") );

                    LocalDB.saveHuella(h);                
                }
                if (rs!=null) rs.close();
                log.info("Migracion de huellas terminada");
                
                //en lugar de esto, mejor recargar las huellas de una vez cuando sucede la actualizacion
                //Configs.HUELLAS_UPDATE.set(true); //este hara que se recarguen por completo las huellas en RAM
                ScannerValidator_Frec.recargarHuellas();
                
            }
            if (log.isDebugEnabled())log.debug("{} Huellas en LocalDB ",LocalDB.getHuellas().size());      
            
        } catch (SQLException e){
            log.error(e.getMessage());
        }
    }

    private void updateConfigs(){
        if (Configs.SERVER_CONFIGS.get()){          
            Configs.loadServerConfigs();                        

        } else {
            log.info("Ignorando peticion en este checador");
        }
    }    
    
    private void migrateJustificaciones(){
        System.out.println("Iniciando migracion de justificaciones");
        try {
            System.out.println("migrando justificaciones por periodo");
            ResultSet rs = ConnectionServer.executeQuery("Select * from justificaciones inner join usuarios using(usuario)");

            while (rs.next()){
                String just;
                String fracc = "";
                
                if(rs.getString("tipo").equals("3")){
                    switch (rs.getString("justificacion")){
                        case "0": case "1": just = "5"; fracc="I"; break;
                        case "3": case "14": case "15": just = "8"; break;
                        case "4": case "7": case "8": case "10": default: just = "15"; break;
                        case "5": just = "9"; break;
                        case "6": just = "10"; break;
                        case "9": just = "1"; break;
                        case "11": case "16": just = "3"; break;
                        case "12": just = "5"; break;
                        case "13": just = "7"; break;   
                    }
                } else {
                    switch (rs.getString("justificacion")){
                        case "0": case "1": just = "11"; fracc="I"; break;
                        case "2": case "3": case "7": case "14": case "15": just = "11"; fracc = "VI"; break;
                        case "4": case "8": case "17": default: just = "15"; break;
                        case "10": just = "11"; break;
                        case "16": just = "12"; break;
                        case "18": just = "16"; break;
                    }
                }
                
                String q = String.format("CALL insert_justificante_periodo(%s,%s,'%s','%s','%s','')", 
                        rs.getString("usuario"),
                        just,
                        fracc,
                        rs.getString("desde"),
                        rs.getString("hasta")
                        );                
                ResultSet rs2 = ConnectionServer.executeQuery(q);
                
                if (rs2.next()){
                    String q2 = String.format("insert into justificantes_aprobaciones (folio,usuario) values (%s,%s)",
                            rs2.getString("folio"),rs.getString("autorizado"));
                    ConnectionServer.executeUpdate(q2);
                }
                
            }
            System.out.println("migrando justificaiones de asignaturas");
            rs = ConnectionServer.executeQuery("Select * from justificacionesclases inner join usuarios using(usuario)");

            while (rs.next()){
                String just;
                String fracc = "";
                
                if(rs.getString("tipo").equals("3")){
                    switch (rs.getString("justificacion")){
                        case "0": case "1": just = "5"; fracc="I"; break;
                        case "3": case "14": case "15": just = "8"; break;
                        case "4": case "7": case "8": case "10": case"17": default: just = "15"; break;
                        case "5": just = "9"; break;
                        case "6": just = "10"; break;
                        case "9": just = "1"; break;
                        case "11": case "16": just = "3"; break;
                        case "12": just = "5"; break;
                        case "13": just = "7"; break;   
                    }
                } else {
                    switch (rs.getString("justificacion")){
                        case "0": case "1": just = "11"; fracc="I"; break;
                        case "2": case "3": case "7": case "14": case "15": just = "11"; fracc = "VI"; break;
                        case "4": case "8": case "17": default: just = "15"; break;
                        case "10": just = "11"; break;
                        case "16": just = "12"; break;
                        case "18": just = "16"; break;
                    }
                }
                
                String q = String.format("CALL insert_justificante_asignatura(%s,%s,'%s','%s','%s','')", 
                        rs.getString("usuario"),
                        just,
                        fracc,
                        rs.getString("fecha"),
                        rs.getString("crn")
                        );                
                ResultSet rs2 = ConnectionServer.executeQuery(q);
                
                if (rs2.next()){
                    String q2 = String.format("insert into justificantes_aprobaciones (folio,usuario) values (%s,%s)",
                            rs2.getString("folio"),rs.getString("autorizacion"));
                    ConnectionServer.executeUpdate(q2);
                }            
            }
            System.out.println("Migracion finalizada");
        
        } catch (SQLException e){
            e.printStackTrace(System.err);
        }
    }
}
