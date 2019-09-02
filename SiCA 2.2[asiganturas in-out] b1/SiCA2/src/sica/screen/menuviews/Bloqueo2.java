package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import sica.Scanner;
import sica.ScannerValidator;
import sica.Screen;
import sica.ScreenManager;
import sica.UserData;

public class Bloqueo2 extends Screen implements Initializable {

    private Service<Void> timer;  
    private ScannerValidator scan;
    private long iniTimer;
    private ChangeListener<UserData> userListener;
    
    @FXML private ProgressBar progress;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        scan = new ScannerValidator(false);
        userListener = (ObservableValue<? extends UserData> ov, UserData t, UserData t1) -> {
            if (t1 != null){
                ScreenManager.menu().setUsuario(t1.getUsuario());
                timer.cancel();                
            } 
        };     
        
        scan.addListener(userListener);
        
        scan.estatusProperty().addListener((ObservableValue<? extends String> ov, String t, String t1)  -> {
            if (t1 != null && t1.length()>0)
                ScreenManager.principal().avisar(t1);
        });
        
        timer = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                return new  Task<Void>(){
                    @Override protected Void call() throws Exception {   
                        iniTimer = System.currentTimeMillis();
                        for (long fin = iniTimer; (fin - iniTimer) < 6000 ; fin = System.currentTimeMillis()){
                            updateProgress(fin-iniTimer, 6000);
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
                ScreenManager.menu().goBack();
            }
            
        };
        
        progress.progressProperty().bind(timer.progressProperty());
    }    

    @Override
    public void start() {
        Scanner.stopAllScanners();        
        scan.startCapturing();
        
        if (timer.isRunning())
            timer.cancel();        
        timer.reset();
        timer.start();
        
    }
    
}
