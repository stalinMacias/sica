package sicaw;

import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


public class ScreenManager extends StackPane {
     private final HashMap<String, Node> screens = new HashMap<>();
     private final HashMap<String, Object> controllers = new HashMap<>();
    
    public ScreenManager() {
        super();
    }

    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }    

    public Node getScreen(String name) {
        return screens.get(name);
    }
    
    public void addController(String name, Object controller){
        controllers.put(name, controller);
    }
    
    public Object getController(String name){
        return controllers.get(name);
    }
    
    public boolean loadScreen( String resource) {
        String name = resource;
        try {
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent loadScreen = (Parent) myLoader.load();              
            
            ScreenInterface myScreenControler = ((ScreenInterface) myLoader.getController());
            myScreenControler.setScreenParent(this);
            
            addScreen(name, loadScreen);
            addController(name, myLoader.getController());
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

    public boolean setScreen(final String name) {       
        if (screens.get(name) != null) {   
            final DoubleProperty opacity = opacityProperty();

            if (!getChildren().isEmpty()) { 
                
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                        new KeyFrame(new Duration(500), (ActionEvent t) -> {
                            getChildren().remove(0);
                            getChildren().add(0, screens.get(name));
                            Timeline fadeIn = new Timeline(
                                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                                    new KeyFrame(new Duration(500), new KeyValue(opacity, 1.0)));
                            fadeIn.play();
                }, new KeyValue(opacity, 0.0)));
                fade.play();

            } else {
                
                setOpacity(0.0);
                getChildren().add(screens.get(name));       
                
                new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0) ),
                    new KeyFrame(new Duration(1500),new KeyValue (opacity, 1.0))
                    ).play();                                
            }
            
            return true;
            
        } else {
            System.out.println("Esta pantalla aun no se ha cargado ");
            return false;
        }       
    }
   
    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Pantalla no existia");
            return false;
        } else {
            return true;
        }
    }
}
