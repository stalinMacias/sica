package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import sica.common.DBQueries;
import sica.common.objetos.Configuracion;

public class AplicacionController implements Initializable {
    
    @FXML private ComboBox <Integer> timedata;
    @FXML private ComboBox <Integer> tiempoServidor;    
    @FXML private ComboBox <Integer> huellatime;
    @FXML private ComboBox <Integer> tecladotime;
    @FXML private CheckBox chkDecorated;
    @FXML private CheckBox chkFullScreen;
    @FXML private CheckBox chkAltf4;
    @FXML private CheckBox chkEsc;    
    @FXML private TextField txtServidor;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tiempoServidor.getItems().clear();
        timedata.getItems().clear();        
        huellatime.getItems().clear();
        tecladotime.getItems().clear();
        
        timedata.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        huellatime.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        tecladotime.getItems().addAll(FXCollections.observableArrayList(3,4,5,6,7,8,9,10));
        tiempoServidor.getItems().addAll(FXCollections.observableArrayList(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30));
        
        loadConfigs();
    }    
    
    @FXML protected void loadConfigs(){
        for (Configuracion c : DBQueries.getAppConfigs()){
            switch (c.getConfiguracion()){
                case "hostserver": txtServidor.setText(c.getValor()); break;                
                case "decorated": chkDecorated.setSelected(Boolean.valueOf(c.getValor())); break;
                case "fullscreen": chkFullScreen.setSelected(Boolean.valueOf(c.getValor())); break;                
                case "preventesc": chkEsc.setSelected(Boolean.valueOf(c.getValor())); break;
                case "preventaltf4": chkAltf4.setSelected(Boolean.valueOf(c.getValor())); break;
                    
                case "datatime": timedata.getSelectionModel().select(Integer.valueOf(c.getValor())); break;                
                case "huellatime": huellatime.getSelectionModel().select(Integer.valueOf(c.getValor())); break;                
                case "tecladotime": tecladotime.getSelectionModel().select(Integer.valueOf(c.getValor())); break;                
                case "servertimeout": tiempoServidor.getSelectionModel().select(Integer.valueOf(c.getValor())); break;
                default: System.out.println("configuracion no reconocida: "+c.getConfiguracion());
                        
            }
        }
    }
    
    @FXML protected void saveConfigs(){
        DBQueries.updateConfig("hostserver", txtServidor.getText());
        
        DBQueries.updateConfig("decorated", Boolean.toString(chkDecorated.isSelected()));
        DBQueries.updateConfig("fullscreen", Boolean.toString(chkFullScreen.isSelected()));
        DBQueries.updateConfig("preventesc", Boolean.toString(chkEsc.isSelected()));
        DBQueries.updateConfig("preventaltf4", Boolean.toString(chkAltf4.isSelected()));
        
        DBQueries.updateConfig("datatime", Integer.toString(timedata.getValue()));
        DBQueries.updateConfig("servertimeout", Integer.toString(tiempoServidor.getValue()));
        DBQueries.updateConfig("huellatime", Integer.toString(huellatime.getValue()));
        DBQueries.updateConfig("tecladotime", Integer.toString(tecladotime.getValue()));
        
        DBQueries.sendConfigUpdateRequest();
        
    }  
    
}
