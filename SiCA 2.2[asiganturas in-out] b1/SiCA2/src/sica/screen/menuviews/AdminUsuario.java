package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.ConnectionServer;
import sica.LocalDB;
import sica.ScannerCapturator;
import sica.Screen;
import sica.ScreenManager;
import sica.common.DBQueries;
import sica.common.objetos.Departamento;
import sica.common.usuarios.StatusUsuario;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;

public class AdminUsuario extends Screen implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(AdminUsuario.class);
    
    private SimpleObjectProperty<Usuario> usuario;    
    private ScannerCapturator scan;
    private IntegerProperty cantidadHuellas;
    
    @FXML private TextField codigo;
    @FXML private TextField nombre;
    @FXML private Button limpiar;
    @FXML private ComboBox <TipoUsuario> tipo;
    @FXML private ComboBox <Departamento> depto;
    @FXML private ComboBox <StatusUsuario> status;
    @FXML private TextField correo;
    @FXML private TextField telefono;
    @FXML private ProgressBar progress;
    @FXML private Label cantHuellas;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {      
        codigo.getProperties().put("vkType", "numeric");
        correo.getProperties().put("vkType", "email");
        telefono.getProperties().put("vkType", "numeric");
        
        cantidadHuellas = new SimpleIntegerProperty(0);
        usuario = new SimpleObjectProperty<>(null);
        scan = new ScannerCapturator();
        createCellFactorys();
        populateComboBoxes();
        
        codigo.focusedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (!t1 && usuario.get() == null){
                buscarUsuario();
            }
        });
        
        usuario.addListener((Observable o) -> {
            Usuario t1 = usuario.get();
            if (t1 != null){
                nombre.setText(t1.getNombre());
                correo.setText(t1.getCorreo());
                correo.setEditable(false);
                telefono.setText(t1.getTelefono());
                for (TipoUsuario i : tipo.getItems()){
                    if (i.getDescripcion().equals(t1.getTipo())){
                        tipo.getSelectionModel().select(i);
                        break;           
                    }
                }
                for (StatusUsuario i : status.getItems()){
                    if (i.getDescripcion().equals(t1.getStatus())){
                        status.getSelectionModel().select(i);
                        break;           
                    }
                }
                for (Departamento i : depto.getItems()){
                    if (i.getNombre().equals(t1.getDepartamento())){
                        depto.getSelectionModel().select(i);
                        break;
                    }
                }
            } else {
                nombre.clear();
                correo.clear();
                correo.setEditable(true);
                telefono.clear();
                tipo.getSelectionModel().clearSelection();
                status.getSelectionModel().clearSelection();
                depto.getSelectionModel().clearSelection();                
            }
        });
        cantHuellas.textProperty().bind(cantidadHuellas.asString());
        progress.progressProperty().bind(scan.progressHuellasNecesariasProperty());
        scan.setOnFailed((Event event) -> {
            ScreenManager.principal().avisar("Error capturando huella, empezar nuevamente");
        });
        
        scan.setOnSuccess((Event event) -> {
            Platform.runLater(() -> {
                cantidadHuellas.set(cantidadHuellas.add(1).get());
            });
            ScreenManager.principal().avisar("Huella guardada exitosamente");
        });        
    } 
    
    @FXML protected void buscarUsuario(){        
        Usuario u = codigo.getText()==null? null: LocalDB.getUsuario(codigo.getText());
        usuario.set(u);        
        if (u!=null){                  
            scan.setUser(codigo.getText());
            scan.startCapturing();
            codigo.setEditable(false);
            nombre.requestFocus();
            cantidadHuellas.set(ConnectionServer.cantidadHuellas(u.getCodigo()));
        } else {
            cantidadHuellas.set(0);
        }
        
    }
    
    @FXML protected void guardarActualizar(){
        if (usuario.get() != null){ //Editando
            if (nombre.getText()!=null
                    && !nombre.getText().isEmpty() 
                    && !tipo.getSelectionModel().isEmpty()
                    && !status.getSelectionModel().isEmpty()
                    && !depto.getSelectionModel().isEmpty()){

                boolean res = DBQueries.updateUsuario(
                            codigo.getText(),
                            nombre.getText().toUpperCase(), 
                            tipo.getValue().getTipo(), 
                            status.getValue().getStatus(),
                            depto.getValue().getCodigo(),                            
                            telefono.getText(),
                            "");

                if (res){
                    ScreenManager.principal().avisar("Usuario actualizado");
                    codigo.setEditable(false);
                } else {
                    ScreenManager.principal().avisar("Error actualizando usuario!");
                }
            } else {
                ScreenManager.principal().avisar("Introducir los datos obligatorios");
                log.error("Introducir los datos obligatorios del usuario");
            }
            
        } else {  //Nuevo             
        
            if ( codigo.getText()!=null
                    && codigo.getText().matches("[0-9]+")
                    && nombre.getText()!=null
                    && !nombre.getText().isEmpty() 
                    && !tipo.getSelectionModel().isEmpty()
                    && !status.getSelectionModel().isEmpty()
                    && !depto.getSelectionModel().isEmpty()) {
                
                boolean res = DBQueries.insertUsuario(
                        codigo.getText(),
                        nombre.getText().toUpperCase(), 
                        tipo.getValue().getTipo(),
                        status.getValue().getStatus(),
                        depto.getValue().getCodigo(),                         
                        telefono.getText(),
                        "");
                
                DBQueries.addCorreoUsuario(codigo.getText(),correo.getText());
                
                if (res){
                    ScreenManager.principal().avisar("Usuario guardado");
                    codigo.setEditable(false);
                    scan.setUser(codigo.getText());
                    scan.startCapturing();
                } else {
                    ScreenManager.principal().avisar("Error guardando usuario!");
                }
                
            }  else {
                ScreenManager.principal().avisar("Introducir los datos obligatorios");
                log.error("Introducir los datos obligatorios");
            }
        }
            
        
    }

    @FXML private void limpiar(){
        codigo.setText(null);
        codigo.setEditable(true);
        usuario.set(null);   
        scan.setUser(null);
        scan.stopCapturing();
        cantidadHuellas.set(0);
        limpiar.requestFocus();
    }
    
    @Override public void start() {        
        limpiar();
    }
    
    @FXML protected void cancelCapturaHuella(){
        scan.cleanCapturador();
    }
    
    @FXML protected void deleteHuellas(){
        if (usuario.get() != null){
            ConnectionServer.eliminarHuella(usuario.get().getCodigo());
            cantidadHuellas.set(ConnectionServer.cantidadHuellas(usuario.get().getCodigo()));
        }
    }
    
    private void populateComboBoxes(){        
        depto.getItems().setAll(DBQueries.getDepartamentos());
        tipo.getItems().setAll(DBQueries.getTipoUsuarios());
        status.getItems().setAll(DBQueries.getStatusUsuarios());
    }
    
    private void createCellFactorys(){
        tipo.setCellFactory(new Callback<ListView<TipoUsuario>, ListCell<TipoUsuario>>(){
            @Override public ListCell<TipoUsuario> call(ListView<TipoUsuario> p) {
                return new ListCell<TipoUsuario>(){ @Override
                    protected void updateItem(TipoUsuario item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(!empty? item.getDescripcion():null);                        
                    }
                };
            }
        });
        
        depto.setCellFactory(new Callback<ListView<Departamento>, ListCell<Departamento>>(){
            @Override public ListCell<Departamento> call(ListView<Departamento> p) {
                return new ListCell<Departamento>(){ @Override
                    protected void updateItem(Departamento item, boolean empty) {
                        super.updateItem(item, empty);      
                        setText(!empty? item.getNombre():null);                        
                    }
                };
            }
        });
        
        status.setCellFactory(new Callback<ListView<StatusUsuario>,ListCell<StatusUsuario>>() {            
            @Override public ListCell<StatusUsuario> call(ListView<StatusUsuario> p) {            
                return new ListCell<StatusUsuario>() {
                    @Override protected void updateItem(StatusUsuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {                                                        
                            setText(item.getDescripcion());
                            switch(item.getDescripcion()){
                                case "Activo":
                                    setTextFill(Color.GREEN);
                                    break;
                                case "Inactivo":
                                    setTextFill(Color.RED);
                                    break;
                                default:
                                    setTextFill(Color.BLACK);
                            }
                            
                        } else {
                          setText(null);
                        }                        
                    }
                };
            }
          }
        );
     
        status.setButtonCell( status.getCellFactory().call(null));
        depto.setButtonCell( depto.getCellFactory().call(null));
        tipo.setButtonCell( tipo.getCellFactory().call(null));
    }
}
