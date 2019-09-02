package sica;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.screen.*;
import sica.screen.menuviews.Folio;
import sica.screen.menuviews.Justificar;

public class ScreenManager {
    private static final Logger log = LoggerFactory.getLogger(ScreenManager.class);      
    public static Map<Screens,Screen> list = new HashMap<>();
  
    public static Screen getScreen(Screens s){      
        if (log.isDebugEnabled()) log.debug("opening {}",s);
        
        if (!list.containsKey(s)){
            loadScreen(s);
        }
        list.get(s).start();
        return list.get(s);
    }
    
    private static void loadScreen(Screens s){
        try {
            if (log.isDebugEnabled()) log.debug("loading {}",s);
            
            FXMLLoader myLoader = new FXMLLoader(ScreenManager.class.getResource(s.getResourceName()));
            Parent p = ((Parent)myLoader.load());
            Screen sc = (Screen)myLoader.getController();
            sc.setParent(p);
            
            list.put(s, sc);
            
        } catch (IOException | IllegalStateException e) {
            log.error("{} {}",e.getMessage(),s.getResourceName());
            e.printStackTrace(System.out);
        }
    }
    
    public static HuellaTeclado huellaTeclado(){
        if (!list.containsKey(Screens.HUELLATECLADO)){
            loadScreen(Screens.HUELLATECLADO);
        }             
        return (HuellaTeclado)list.get(Screens.HUELLATECLADO);
    }
    
    public static InfoRegistro infoRegistro(){
        if (!list.containsKey(Screens.INFOREGISTRO)){
            loadScreen(Screens.INFOREGISTRO);
        }                  
        return (InfoRegistro)list.get(Screens.INFOREGISTRO);
    }
    
    public static Menu menu(){
        if (!list.containsKey(Screens.MENU)){
            loadScreen(Screens.MENU);
        }                  
        return (Menu)list.get(Screens.MENU);
    }
    
    public static Principal principal(){
        if (!list.containsKey(Screens.PRINCIPAL)){
            loadScreen(Screens.PRINCIPAL);
        }                   
        return (Principal)list.get(Screens.PRINCIPAL);
    }
        
    public static Justificar justificar(){
        if (!list.containsKey(Screens.MENU_JUSTIFICAR)){
            loadScreen(Screens.MENU_JUSTIFICAR);
        }
        return (Justificar)list.get(Screens.MENU_JUSTIFICAR);
    }
    
    public static Folio folio(){
        if (!list.containsKey(Screens.MENU_FOLIO)){
            loadScreen(Screens.MENU_FOLIO);
        }
        return (Folio)list.get(Screens.MENU_FOLIO);
    }
}
