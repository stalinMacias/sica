package sica.screen;

import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import sica.CapturaUploader;
import sica.CodigoValidator;
import sica.Configs;
import sica.ConnectionServer;
import sica.Main;
import sica.Scanner;
import sica.ScannerValidator;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.SiCA;
import sica.Updater;
import sica.UserData;

public class HuellaTeclado extends Screen implements Initializable{

    private static final Logger log = Logger.getLogger(HuellaTeclado.class);   

    private enum Estatus{HUELLA,TECLADO,ESPERANDO};
    
    private ChangeListener<UserData> userListener;
    private CodigoValidator search;
    private ScannerValidator scan;
    private Webcam webcam;
    private static ObjectProperty<Estatus> estatus;
    public static boolean disponible = false; //Modificacion hice aqui
    private Service<Void> codigoTimer;
    private long codigoTimerIni;
    
    @FXML private VBox contenedor;
    @FXML private ImageView imagen;
    @FXML private TextField codigo;
    @FXML private Text infoText;    
    @FXML private ProgressIndicator progress;
    
    @FXML private ImageView updating;
    @FXML private ImageView disconected;
    @FXML private ImageView unpluged;
    
    @Override public void initialize(URL url, ResourceBundle rb) {          
        estatus = new SimpleObjectProperty<>(Estatus.ESPERANDO);
        disponible = true;
        scan = new ScannerValidator(true);
        search = new CodigoValidator(codigo.textProperty());
        webcam = Webcam.getDefault();                   
        
        updating.visibleProperty().bind(Updater.runningProperty());
        disconected.visibleProperty().bind(ConnectionServer.conectedProperty().not());
        unpluged.visibleProperty().bind(ScannerValidator.connectedProperty().not());
                
        Tooltip.install(updating, new Tooltip("Actualizando base de datos local"));
        Tooltip.install(disconected, new Tooltip("No hay acceso a la red, haciendo registros de forma local"));
        Tooltip.install(unpluged, new Tooltip("Sensor de huellas desconectado"));
                        
        scan.runningProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            estatus.set(t1? Estatus.HUELLA: Estatus.ESPERANDO);
        });    
        search.runningProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (!t1){
                estatus.set(Estatus.ESPERANDO);
                disponible = true;
            } else if (!estatus.get().equals(Estatus.TECLADO)){
                estatus.set(Estatus.TECLADO);
                disponible = false;
            }
        });
        
        userListener = (ObservableValue<? extends UserData> ov, UserData t, UserData t1) -> {
            if (t1 != null){
                log.debug("Nuevo registro realizado por "+t1.getUsuario());
                showUserData(t1);
                log.debug("Verificando envio de foto, estatus"+estatus.get());
                if (estatus.get().equals(Estatus.TECLADO) && webcam != null){
                    CapturaUploader.upload(t1, webcam.getImage());
                } 
            }
        };        
        scan.addListener(userListener);
        search.addListener(userListener);
        
        ChangeListener<String> estatusChange = (ObservableValue<? extends String> ov, String t, String t1) -> {
            if (t1 != null && !t1.isEmpty()){
                ScreenManager.principal().avisar(t1);
            }
        };
        
        scan.estatusProperty().addListener(estatusChange);
        search.estatusProperty().addListener(estatusChange);
        
        estatus.addListener((ObservableValue<? extends Estatus> ov, Estatus t, Estatus t1) -> {
            switch (t1){
                case TECLADO:
                    startCodigoTimer();
                    mostrarCamara();
                    break;
                    
                case HUELLA:
                    codigo.clear();
                    codigo.setEditable(false);
                    if (codigoTimer.isRunning()) codigoTimer.cancel();
                    break;
                    
                case ESPERANDO:
                    if (codigoTimer.isRunning()) codigoTimer.cancel();
                    codigo.clear();
                    codigo.setEditable(true);
                    setImagenMuestra();
                    break;
            }
            

            
        });
        
        codigoTimer = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                return new Task<Void>(){
                    @Override protected Void call() {  
                        updateProgress(0, 1); 
                        codigoTimerIni = System.currentTimeMillis();  
                        int time = Configs.TECLADO_INPUT_TIME.get() * 1000;
                        for (long i=0; i <= time; i = System.currentTimeMillis()-codigoTimerIni) {                            
                            updateProgress(i, time);                            
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                if (!isCancelled() && log.isDebugEnabled()){
                                    log.debug(e.getMessage());
                                }
                            }
                            if (isCancelled()) break;
                        }
                        return null;
                    }            
                };           
            }
        };
        
        codigoTimer.stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) -> {
            switch (t1){
                case SUCCEEDED: case FAILED:
                    stopCodigoTimer();                    
            }
        });
        
        progress.progressProperty().bind(Bindings.when(codigoTimer.runningProperty()).then(codigoTimer.progressProperty()).otherwise(-1));
        progress.visibleProperty().bind(Bindings.or(codigoTimer.runningProperty(),scan.runningProperty()).or(search.runningProperty()));
        
        
        contenedor.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent inputevent) -> {
            if (inputevent.getText().matches("[0-9]{1}") && codigo.getText().length()<=10 ){                
                codigo.setText(codigo.getText()+inputevent.getText());
                if (estatus.get().equals(Estatus.TECLADO)) {            
                    startCodigoTimer();
                } else {
                    estatus.set(Estatus.TECLADO);
                    disponible=false;
                }  
            } else if (inputevent.getCode().equals(KeyCode.ENTER)) {
                makeRegistroPorCodigo();
            } else if (inputevent.getCode().equals(KeyCode.BACK_SPACE) && codigo.getText().length()>0 ) {                
                codigo.setText(codigo.getText().substring(0, codigo.getText().length()-1));
            } 
            
            inputevent.consume();                          
        });       
        
        codigo.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) -> {
            if (nv) contenedor.requestFocus();
        });
        
        StringBinding text = Bindings.when(scan.runningProperty().or(search.runningProperty()))
            .then("Analizando")
            .otherwise(Bindings.when(ScannerValidator.connectedProperty().not())
                    .then("Esperando código, deberá ser visible su rostro para ser efectivo")
                    .otherwise("Esperando código, deberá ser visible su rostro para ser efectivo"));
        
        infoText.textProperty().bind(text);        
        
        
        
            //inicializando la huellas en RAM
            // #HuellasFrec ScannerValidator_Frec.recargarHuellas();
    }    
    
    private void showUserData(UserData ud){
        ScreenManager.infoRegistro().setUserData(ud);
        ScreenManager.principal().goTo(Screens.INFOREGISTRO);
    }
    
    @FXML protected void addNumber(ActionEvent a){              
        if (estatus.get().equals(Estatus.ESPERANDO)){
            estatus.set(Estatus.TECLADO);
            disponible=false;
        }         
        if (codigo.getText().length() < 10){
            startCodigoTimer();
            Button b = (Button) a.getSource();
            codigo.setText(codigo.getText().concat(b.getText()));                
        }            
    }
    
    private void startCodigoTimer(){
        switch (codigoTimer.getState()){                            
            case SCHEDULED: case RUNNING: codigoTimerIni=System.currentTimeMillis(); break;
            case CANCELLED: case FAILED: case SUCCEEDED: codigoTimer.reset();
            case READY: codigoTimer.start();                        
        }           
    }
    private void stopCodigoTimer(){
        estatus.set(Estatus.ESPERANDO);
        disponible=true;
    }
    
    @FXML protected void goToMenu(){
        ScreenManager.principal().goTo(Screens.MENU);
    }
    
    @FXML protected void returnKey(){
        if (codigo.getText().length() > 0){
            codigo.setText(codigo.getText().substring(0,codigo.getText().length()-1));                
        }   
    }
    @FXML protected void makeRegistroPorCodigo(){   
        
        if (search.isRunning() || !estatus.get().equals(Estatus.TECLADO)) return;
        
        if (search.getState() != Worker.State.READY){
            search.reset();
        }
        search.start();
        if (codigoTimer.isRunning()) codigoTimer.cancel();
    }
    
    private void setImagenMuestra(){                      
        Image imageMuestra = new Image(Main.class.getResource("images/biometrics.jpg").toExternalForm());
        imagen.setImage(imageMuestra);        
    }
    
    private void mostrarCamara(){                       
        new Thread ( () -> {
            if (webcam!=null){           
                webcam.open(disponible);
            }
           
            if (log.isDebugEnabled()) log.debug("iniciando cámara");
            Image noImg = new Image(Main.class.getResource("images/sin_camara.jpg").toExternalForm());
            
            while ( estatus.get().equals(Estatus.TECLADO) && SiCA.isRunning() ) {
                BufferedImage img = (webcam!=null)? webcam.getImage() : null;
                
                final Image im = (img != null)? SwingFXUtils.toFXImage(img, null):noImg;
                
                Platform.runLater(() -> { imagen.setImage(im); });
                
                try {
                    Thread.sleep(100000/10000); // 1000/100 = 10 FPS  100000/10000 = 30 FPS
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }            
            if (log.isDebugEnabled()) log.debug("Cámara detenienda");
            
            try { Thread.sleep(100); } 
            catch (InterruptedException ex) { ex.printStackTrace(System.out); }
            
            Platform.runLater(() -> { setImagenMuestra(); });
            
        }).start();
    }    
         
    @Override
    public void start() {
        estatus.set(Estatus.ESPERANDO);
        disponible=true;
        setImagenMuestra();        
        Scanner.stopAllScanners();        
        scan.startCapturing();
    }
    
    public static boolean isDisponible(){
           return estatus.get().equals(Estatus.ESPERANDO);
    }
    
}
