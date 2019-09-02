package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import sica.Scanner;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.Updater;

public class Opciones extends Screen implements Initializable {
    
    @FXML private ImageView updateImage;

    @Override 
    public void initialize(URL url, ResourceBundle rb) {        
                
    }    
            
    @FXML protected void goToConfig(){
        ScreenManager.menu().addScreen(Screens.MENU_CONFIG);
    }
    
    @FXML protected void goToUsers(){
        ScreenManager.menu().addScreen(Screens.MENU_ADMIN_USR);
    }
    
    @FXML protected void update(){
        new Timeline(new KeyFrame(Duration.seconds(1), 
                new KeyValue(updateImage.rotateProperty(), updateImage.rotateProperty().get()+180)
        )).play();        
        Updater.update(true);        
    }
    
    @FXML protected void goToHorario(){
        ScreenManager.menu().addScreen(Screens.MENU_HORARIO);
    }
    
    @FXML protected void goToAsistencias(){
        ScreenManager.menu().addScreen(Screens.MENU_ASISTENCIA_JORNADA);
    }
    
    @FXML protected void goToFaltas(){
        ScreenManager.menu().addScreen(Screens.MENU_FALTAS_ASIGNATURAS);
    }
    
    @FXML protected void goToJustificantes(){
        ScreenManager.menu().addScreen(Screens.MENU_JUSTIFICANTES);
    }
    
    @FXML protected void goToHome(){
        ScreenManager.menu().goBack();
    }    
    
    @Override public void start() {
        Scanner.stopAllScanners();
    }
    
}
