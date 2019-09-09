package sica;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.ConfigProperty;
import sica.common.DBQueries;
import sica.common.objetos.Configuracion;

public class Configs extends sica.common.Configs {//"148.202.89.3");
    public static ConfigProperty<String> HOST = new ConfigProperty<>("host",   "127.0.0.1"); //148.202.89.3
    public static ConfigProperty<String> BASEDEDATOS = new ConfigProperty<>("basededatos",  "checador");    
    public static ConfigProperty<String> PORT = new ConfigProperty<>("puerto","3306");
    public static ConfigProperty<String> USER = new ConfigProperty<>("usuario","frank"); //frank
    public static ConfigProperty<String> PASSWORD = new ConfigProperty<>("pass","frankvalles65"); //frankvalles65
    
    public static ConfigProperty<Boolean> SERVER_CONFIGS = new ConfigProperty<>("serverconfigs",true);
    public static ConfigProperty<Boolean> UNDECORATED = new ConfigProperty<>("decorated",true);
    public static ConfigProperty<Boolean> FULLSCREEN = new ConfigProperty<>("fullscreen",true);
    public static ConfigProperty<Boolean> PREVENT_ESC = new ConfigProperty<>("preventesc",true); 
    public static ConfigProperty<Boolean> PREVENT_ALTF4 = new ConfigProperty<>("preventaltf4",true);
    
    public static ConfigProperty<Integer> DATATIME = new ConfigProperty<>("datatime",4);
    public static ConfigProperty<Integer> SERVER_TIMEOUT = new ConfigProperty<>( "servertimeout",20);
    public static ConfigProperty<Integer> TECLADO_INPUT_TIME = new ConfigProperty<>("tecladotime",6);
    public static ConfigProperty<Integer> HUELLA_WAIT_TIME = new ConfigProperty<>("huellatime",6);
    
    //bandera para saber cuando hay actualizaciones de huellas
    /**  Indica si ha habido alguna actualiuzacion de huellas, especialmente para que el ScannerValidator refresque sus huellas    */
    public static ConfigProperty<Boolean> HUELLAS_UPDATE = new ConfigProperty<>("huellasupdate",false);
    public static ConfigProperty<String> EQUIPO_NOMBRE = new ConfigProperty<>("equiponombre","ninguno");
    
    public static ConfigProperty<String> LASTUPDATE = new ConfigProperty<>("lastupdate","0000-00-00 00:00:00");    
      
    // -------------------------------------------------------------------------
    private final static Logger log = LoggerFactory.getLogger(Configs.class);
           
    public static void loadServerConfigs(){
        log.info("Cargando configuraciones del servidor");
        for (Configuracion c : DBQueries.getAppConfigs()){
            switch (c.getConfiguracion()){
                case "hostserver": SERVER.set(c.getValor()); break;
                case "decorated": UNDECORATED.set(Boolean.valueOf(c.getValor())); break;
                case "fullscreen": FULLSCREEN.set(Boolean.valueOf(c.getValor())); break;
                case "preventaltf4": PREVENT_ALTF4.set(Boolean.valueOf(c.getValor())); break;
                case "datatime": DATATIME.set(Integer.valueOf(c.getValor())); break;
                case "servertimeout": SERVER_TIMEOUT.set(Integer.valueOf(c.getValor())); break;
                case "tecladotime": TECLADO_INPUT_TIME.set(Integer.valueOf(c.getValor())); break;
                case "huellatime": HUELLA_WAIT_TIME.set(Integer.valueOf(c.getValor())); break;              
            }}          
        printConfigs();                     
    }
    
       
    public static void loadLocalConfig(boolean reset){ 
        
        
        Preferences prefsRoot = Preferences.userRoot(); 
        try {             
            boolean finded = prefsRoot.nodeExists(sica.common.Configs.class.getName());
            log.info(finded? "Preferencias encontradas" : "Preferencias NO encontradas");                
            Preferences myPrefs = prefsRoot.node(sica.common.Configs.class.getName());
            ConfigProperty.setPreferences(myPrefs);
            
            if ( !finded | reset ){
                saveDefaults();
            } else {
                loadConfigs();
            }           
            
            
            SERVER = new ConfigProperty<>("hostserver","http://127.0.0.1/sica"); 
            HOST = new ConfigProperty<>("host",   "127.0.0.1");
            BASEDEDATOS = new ConfigProperty<>("basededatos",  "checador");    
            PORT = new ConfigProperty<>("puerto","3306");
            USER = new ConfigProperty<>("usuario","frank"); //frank
            PASSWORD = new ConfigProperty<>("pass","frankvalles65"); //frankvalles65
            
            saveDefaults();
            
            printConfigs();
            
            //saveDefaults();
            //Agregando el nombre de equipo a las preferencias
            EQUIPO_NOMBRE.set( getEquipoNombre() );
            
        } catch (BackingStoreException e) {
            log.error(e.getMessage());
        }
    }
    
    public static void loadConfigs(){
        SERVER.load();
        HOST.load();
        BASEDEDATOS.load();
        PORT.load();
        USER.load();
        PASSWORD.load();
        SERVER_CONFIGS.load();
        UNDECORATED.load();
        FULLSCREEN.load();
        PREVENT_ESC.load();
        PREVENT_ALTF4.load();
        DATATIME.load();
        SERVER_TIMEOUT.load();
        LASTUPDATE.load();
        TECLADO_INPUT_TIME.load();
        HUELLA_WAIT_TIME.load();
    }
    
    public static void saveDefaults(){ 
        SERVER.saveDefault();
        HOST.saveDefault();
        BASEDEDATOS.saveDefault();
        PORT.saveDefault();
        USER.saveDefault();
        PASSWORD.saveDefault();
        SERVER_CONFIGS.saveDefault();
        UNDECORATED.saveDefault();
        FULLSCREEN.saveDefault();
        PREVENT_ESC.saveDefault();
        PREVENT_ALTF4.saveDefault();
        DATATIME.saveDefault();
        SERVER_TIMEOUT.saveDefault();
        LASTUPDATE.saveDefault();
        TECLADO_INPUT_TIME.saveDefault();
        HUELLA_WAIT_TIME.saveDefault();    
    }     
    
    public static void printConfigs(){ 
        System.out.println(SERVER);
        System.out.println(HOST);
        System.out.println(BASEDEDATOS);
        System.out.println(PORT);
        System.out.println(USER);
        System.out.println(SERVER_CONFIGS);
        System.out.println(UNDECORATED);
        System.out.println(FULLSCREEN);
        System.out.println(PREVENT_ESC);
        System.out.println(PREVENT_ALTF4);
        System.out.println(DATATIME);
        System.out.println(SERVER_TIMEOUT);
        System.out.println(LASTUPDATE);
        System.out.println(TECLADO_INPUT_TIME);
        System.out.println(HUELLA_WAIT_TIME);
    }
    
    
    public static String getEquipoNombre(){
        String name = "NoName";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println(localHost.getHostName());
            System.out.println(localHost.getHostAddress());
            name = localHost.getHostName();
            if(name == null || name.isEmpty() || name.equals("NoName")  ){
                log.error("No se pudo obtener el nombre del equipo");
                name = "NoName";
            }
            
            //para recortar el nombre si mide mas de 7 letras
            if(name.length() >= 7){
                //ejemplo: computadora -> 11 letras
                int index = name.length();
                index -= 7; //11 -7 = 4
                name = name.substring(index); //4 -> utadora
            }
           
        } catch (UnknownHostException ex) {
            log.error(ex.getMessage());
        }
        
         return name;
    }
    
}
