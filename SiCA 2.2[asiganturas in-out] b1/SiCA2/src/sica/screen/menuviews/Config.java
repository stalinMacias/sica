package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import sica.Configs;
import sica.Screen;
import sica.controls.SwitchButton;

public class Config extends Screen implements Initializable {
    
    @FXML private GridPane servidorBox;
    private SwitchButton configs;    
   
    @FXML private GridPane generalBox;
    private SwitchButton pantalla;
    private SwitchButton decorado;
    private SwitchButton cerrado;
    private SwitchButton completa;
    
    @FXML private ChoiceBox<Integer> duracionPantallaRegistro;
    @FXML private ChoiceBox<Integer> duracionPantallaBloqueo;
    @FXML private ChoiceBox<Integer> esperaCodigo;
    @FXML private ChoiceBox<Integer> esperaServidor;

     
    @FXML private TextField servidor;
    @FXML private TextField host;
    @FXML private TextField base;
    @FXML private TextField usuario;
    @FXML private TextField password;    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        duracionPantallaRegistro.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        duracionPantallaBloqueo.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        esperaCodigo.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        esperaServidor.getItems().addAll(FXCollections.observableArrayList(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30));
        
        
        configs = new  SwitchButton(); servidorBox.add(configs, 2, 0);  
        pantalla = new SwitchButton(); generalBox.add(pantalla, 1, 0);   
        decorado = new SwitchButton(); generalBox.add(decorado, 1, 1);
        cerrado = new SwitchButton();  generalBox.add(cerrado, 1, 2);
        completa = new SwitchButton(); generalBox.add(completa, 1, 3);
        
        loadConfigs();
        
        configs.switchOnProperty().addListener((ov,t,t1) -> {
            Configs.SERVER_CONFIGS.set(t1);
            if (t1){
                Configs.loadServerConfigs();
                loadConfigs();
            }
        });                
            
        pantalla.switchOnProperty().addListener((ov,t,t1) -> {
            Configs.FULLSCREEN.set(t1);
        });
        
        decorado.switchOnProperty().addListener((ov,t,t1) -> {
            Configs.UNDECORATED.set(!t1);
        });
        
        cerrado.switchOnProperty().addListener((ov,t,t1) -> {
            Configs.PREVENT_ALTF4.set(t1);
        });
        
        completa.switchOnProperty().addListener((ov,t,t1) -> {
            Configs.PREVENT_ESC.set(t1);
        });
        
        duracionPantallaRegistro.getSelectionModel().selectedItemProperty().addListener((ov,t,t1) -> {
            if (t1 != null){
                Configs.DATATIME.set(t1);
            }
        });
        duracionPantallaBloqueo.getSelectionModel().selectedItemProperty().addListener((ov,t,t1) -> {
            if (t1 != null){
                Configs.HUELLA_WAIT_TIME.set(t1);
            }
        });
        esperaCodigo.getSelectionModel().selectedItemProperty().addListener((ov,t,t1) -> {
            if (t1 != null){
                Configs.TECLADO_INPUT_TIME.set(t1);
            }
        });
        esperaServidor.getSelectionModel().selectedItemProperty().addListener((ov,t,t1) -> {
            if (t1 != null) {
                Configs.SERVER_TIMEOUT.set(t1);
            }
        });
        
        servidor.focusedProperty().addListener((ov,t,t1) -> {
            if (!t1 && !servidor.getText().equals(Configs.SERVER.get())){
                Configs.SERVER.set(servidor.getText());
            }
        });
        
        host.focusedProperty().addListener((ov,t,t1) -> {
            if (!t1){
                Configs.HOST.set(host.getText());
            }
        });
        
        base.focusedProperty().addListener((ov,t,t1) -> {
            if (!t1){
                Configs.BASEDEDATOS.set(base.getText());
            }
        });
        usuario.focusedProperty().addListener((ov,t,t1) -> {
            if (!t1){
                Configs.USER.set(usuario.getText());
            }
        });
        password.focusedProperty().addListener((ov,t,t1) -> {
            if (!t1){
                Configs.PASSWORD.set(password.getText());
            }
        });
        
        pantalla.disableProperty().bind(configs.switchOnProperty());
        decorado.disableProperty().bind(configs.switchOnProperty());
        cerrado.disableProperty().bind(configs.switchOnProperty());
        completa.disableProperty().bind(configs.switchOnProperty());
        duracionPantallaRegistro.disableProperty().bind(configs.switchOnProperty());
        duracionPantallaBloqueo.disableProperty().bind(configs.switchOnProperty());
        esperaCodigo.disableProperty().bind(configs.switchOnProperty());
        esperaServidor.disableProperty().bind(configs.switchOnProperty());
        
        servidor.disableProperty().bind(configs.switchOnProperty());
        
    }   
   
    
    public void loadConfigs(){
        configs.switchOnProperty().set(Configs.SERVER_CONFIGS.get());
        pantalla.switchOnProperty().set(Configs.FULLSCREEN.get());
        decorado.switchOnProperty().set(!Configs.UNDECORATED.get());
        cerrado.switchOnProperty().set(Configs.PREVENT_ALTF4.get());
        completa.switchOnProperty().set(Configs.PREVENT_ESC.get());
        duracionPantallaRegistro.getSelectionModel().select(Configs.DATATIME.get());
        duracionPantallaBloqueo.getSelectionModel().select(Configs.HUELLA_WAIT_TIME.get());
        esperaCodigo.getSelectionModel().select(Configs.TECLADO_INPUT_TIME.get());
        esperaServidor.getSelectionModel().select(Configs.SERVER_TIMEOUT.get());        
                
        servidor.setText(Configs.SERVER.get());
        host.setText(Configs.HOST.get());
        base.setText(Configs.BASEDEDATOS.get());
        usuario.setText(Configs.USER.get());
        password.setText(Configs.PASSWORD.get());
    }
    
    @FXML protected void loadDefaults(){
        Configs.saveDefaults();
        loadConfigs();
    }
    
    @Override public void start() {
        loadConfigs();
    }
}
