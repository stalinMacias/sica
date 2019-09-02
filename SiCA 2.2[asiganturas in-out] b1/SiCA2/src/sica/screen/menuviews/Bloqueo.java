package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import sica.Autenticator;
import sica.Configs;
import sica.Scanner;
import sica.Screen;
import sica.ScreenManager;
import sica.common.Utils;
import sica.common.usuarios.Usuario;

public class Bloqueo extends Screen implements Initializable {

    private Service<Void> timer;    
    private Service<Usuario> autenticateTask;
    private long iniTimer;
    
    @FXML private Label info;
    @FXML private TextField user;
    @FXML private PasswordField pass;    
    @FXML private HBox hbox;
    @FXML private ProgressBar progress;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {  
        user.getProperties().put("vkType", "numeric");
        
        progress.setProgress(0);
        timer = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                return new  Task<Void>(){
                    @Override protected Void call() throws Exception {   
                        iniTimer = System.currentTimeMillis();
                        int time = Configs.HUELLA_WAIT_TIME.get() * 1000;
                        for (long fin = iniTimer; (fin - iniTimer) < time ; fin = System.currentTimeMillis()){
                            updateProgress(fin-iniTimer, time);
                            try { Thread.sleep(10); } catch (InterruptedException e){
                                if (isCancelled()) break;
                            }                                                        
                        }
                        return null;
                    }                    
                };
            }      

            @Override protected void succeeded() {
                super.succeeded();
                //Utils.closeOSK();
                ScreenManager.menu().goBack();
            }
            
        };
        
        autenticateTask = new Service<Usuario>() {
            @Override protected Task<Usuario> createTask() {
                return new Task<Usuario>() {
                    @Override protected Usuario call() throws Exception {
                        return Autenticator.autenticateUserLocalDB(user.getText(), pass.getText());
                    }
                };                 
            }
            
            @Override protected void failed() {
                super.failed(); 
                ScreenManager.menu().setUsuario(autenticateTask.getValue());           
            }

            @Override protected void succeeded() {
                super.succeeded(); 
                ScreenManager.menu().setUsuario(autenticateTask.getValue());
            }
        };
                       
        //Scanner.stopAllScanners();               
        progress.progressProperty().bind(Bindings.when(timer.runningProperty()).then(timer.progressProperty()).otherwise(-1));       
        user.disableProperty().bind(autenticateTask.runningProperty());
        pass.disableProperty().bind(autenticateTask.runningProperty());
           
    }   
    
    public void restartTimer(){
        if (timer.isRunning()){
            iniTimer = System.currentTimeMillis();
        } 
    }
    
    @FXML protected void focusPass(){
        pass.requestFocus();
    }
    
    @FXML protected void waitMore(){
        restartTimer();
    }
    
    @FXML protected void access(){        
        info.setText("Autentificando...");        
        timer.cancel(); 
        if (!autenticateTask.isRunning()){
            autenticateTask.reset();
            autenticateTask.start();
        }                
    }    
        
    @Override public void start() {
        Scanner.stopAllScanners();
        info.setText("Acceder al menu:");
        user.clear();
        pass.clear();        
        
        if (timer.isRunning())
            timer.cancel();        
        if (timer.getState() != Worker.State.READY)
            timer.reset();
        timer.start();  
        user.requestFocus();
    }
    
    @Deprecated
    private void openOSK(){   
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ex) {}
                        
            Scene scene = hbox.getScene();
            Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
            Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
            Point2D nodeCoord = hbox.localToScene(0.0, 0.0);
            Long posX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
            Long posY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY());

            Number val = (ScreenManager.principal().getParent().getScaleX() * hbox.widthProperty().get())-7;
            Number val2 = (ScreenManager.principal().getParent().getScaleY() * hbox.heightProperty().get())-25;
            if (val2.intValue()>300){
                val2 = 300;
            }
            
            Utils.openOSK(posX.intValue(), posY.intValue(), val.intValue(), val2.intValue());
        }).start();        
    }
        
}
