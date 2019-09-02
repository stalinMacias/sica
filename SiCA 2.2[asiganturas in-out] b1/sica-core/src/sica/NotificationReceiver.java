package sica;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.Utils;

public class NotificationReceiver {
    
    private final static Logger log = LoggerFactory.getLogger(NotificationReceiver.class);
    private static WebView browser;
    
    public static void initialize(){
        if (browser == null){
            browser = new WebView();
            browser.setPrefSize(0, 0);
            browser.setVisible(false);
            startListening();            
        }        
    }
    
    public static WebView getNotifReceiver(){         
        return browser;
    }
    
    public static void startListening(){  
        log.info("Iniciando receptor de notificaciones"); 
        if (!Utils.urlExist(Configs.PUSH_NOTIF())){            
            log.error("Error de configuraciones, no se esperaran notificaciones");
            return;
        }
        
        WebEngine webEngine = browser.getEngine();               
        
        ChangeListener<Worker.State> chl = ((o,ov,nv) -> {
            if (nv == Worker.State.SUCCEEDED) {
                JSObject jsobj = (JSObject) webEngine.executeScript("window");
                jsobj.setMember("app", new Bridge());
                log.info("### jsobj contenido = " + jsobj.toString());
                log.info("Esperando notificaciones");
            }
        });
        
        webEngine.getLoadWorker().stateProperty().addListener(chl);
        webEngine.load( Configs.PUSH_NOTIF() );   
        
    }
    
    public static class Bridge {
        public void executeUpdate() {
            log.info("Notificación de actualización recibida");
            Updater.update();
        }
    }
    
}
