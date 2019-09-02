package sica;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.DBGetterFromResultSet;
import sica.common.DBQueries;

public class SiCA  {
    private static final Logger log = LoggerFactory.getLogger(SiCA.class); 
    
    protected static boolean running = false;
        
    public static void initialize(final boolean debug, final boolean reset) {
        running = true;
        
        //Verificamos si estamos en modo debug        
        Log.initialize(debug);        
        if (debug) Log.show(); 
        
        log.info("Iniciando SiCA v2.2 b2");
        
        if ( !LocalDB.initialize() ){
            JOptionPane.showMessageDialog(null, "La aplicación ya se esta ejectuando", "Error", JOptionPane.OK_OPTION);
            log.error("La aplicación ya se esta ejecutando");            
            System.exit(0);
            
        } else {      
            
            // Carga la configuracion fuentes e inicia la conexion a los servidores
            Configs.loadLocalConfig(reset);            
            ConnectionServer.initialize(); 
            
            ConnectionServer.conectedProperty().addListener((ov, old, niu) -> {
                if (niu){
                    log.info("Conexion a servidor correcta/restablecida");                    
                    Platform.runLater(() ->  Updater.update() ); 
                    if (Configs.SERVER_CONFIGS.get()){
                        Configs.loadServerConfigs();
                    }
                }
            });
            
            ChangeListener<String> ch = (o, ov, nv) -> 
                  Platform.runLater( () -> ConnectionServer.startConnection(true));            
            
            Configs.HOST.addListener(ch);
            Configs.BASEDEDATOS.addListener(ch);
            Configs.PORT.addListener(ch);
            Configs.USER.addListener(ch);
            Configs.PASSWORD.addListener(ch);            
               
            
            DBQueries.setDBGetter(new DBGetterFromResultSet(ConnectionServer.connectionProperty()));
            
            NotificationReceiver.initialize();
        }    
        
        if (log.isDebugEnabled()) log.debug("Inicializacion finalizada");
    }
    
    public static void stop(){
        running = false;   
        LocalDB.closeLocalDB();                           
        ConnectionServer.closeConectionDaemon();   
        Scanner.stopAllScanners();
    }
    
    public static Boolean isRunning(){
        return running;
    }
}
