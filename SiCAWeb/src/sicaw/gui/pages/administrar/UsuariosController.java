package sicaw.gui.pages.administrar;

import java.io.File;
import sicaw.gui.pages.administrar.usuarios.JustificacionesController;
import sicaw.gui.pages.administrar.usuarios.HorarioController;
import sicaw.gui.pages.administrar.usuarios.RegistrosController;
import sicaw.gui.pages.administrar.usuarios.AsignaturasController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javax.swing.JOptionPane;
import sica.common.Autenticator;
import sica.common.Configs;
import sica.common.DBQueries;
import sica.common.objetos.Departamento;
import sica.common.usuarios.CorreoUsuario;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.StatusUsuario;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;
import sicaw.gui.Principal;
import sicaweb.FileManagerTask;

public class UsuariosController implements Initializable {

    private RegistrosController registrosTab;
    private HorarioController horariosTab;
    private AsignaturasController clasesTab;
    private JustificacionesController justifTab;
    
    @FXML private SplitPane split1;
    @FXML private Button addNew;
    
    @FXML private TableView <Usuario> tablaUsuarios;
    @FXML private TableColumn <Usuario, String> codigoColUs; 
    @FXML private TableColumn <Usuario, String> nombreColUs;
    @FXML private TableColumn <Usuario, String> tipoColUs;
    @FXML private TableColumn <Usuario, String> deptoColUs;    
    
    @FXML private Label nombreInfo;
    
    @FXML private ImageView fotoUser;
    @FXML private GridPane infoPanel;
    @FXML private TextField infoNombre;
    @FXML private TextField infoCodigo;
    @FXML private VBox vboxCorreos;
    @FXML private ListView<CorreoUsuario> infoCorreo;
    @FXML private TextField correoInput;
    @FXML private Button addCorreoBtn;
    @FXML private ComboBox <TipoUsuario> infoTipo;
    @FXML private ComboBox <Departamento> infoDepto;
    @FXML private ComboBox <StatusUsuario> infoStatus;
    @FXML private TextField infoTelefono;
    @FXML private TextArea infoComent;
    @FXML private HBox botonesNuevoUsuario;
    @FXML private Button updateBtn;
    
    @FXML private Tab regTab;
    @FXML private Tab horTab;    
    @FXML private Tab clasTab;
    @FXML private Tab justTab;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarVistaTablas();        
        fillBoxes();        
        loadUsuarios();        
        createFancyCells();        
        crearListeners();        
        infoPanel.setVisible(false);
           
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){
            addNew.setVisible(false);
        }
        
        FXMLLoader load = new FXMLLoader(RegistrosController.class.getResource("registros.fxml"));
        FXMLLoader load2 = new FXMLLoader(RegistrosController.class.getResource("horario.fxml"));
        FXMLLoader load3 = new FXMLLoader(RegistrosController.class.getResource("asignaturas.fxml"));
        FXMLLoader load4 = new FXMLLoader(RegistrosController.class.getResource("justificantes.fxml"));
        
        try {              
            Parent node = (Parent) load.load();
            registrosTab = load.getController();
            regTab.setContent(node);
            
            Parent node2 = (Parent) load2.load();
            horariosTab = load2.getController();
            horTab.setContent(node2);
            
            Parent node3 = (Parent) load3.load();
            clasesTab = load3.getController();
            clasTab.setContent(node3);
            
            Parent node4 = (Parent) load4.load();
            justifTab = load4.getController();
            justTab.setContent(node4);
            
        } catch (IOException ex) {            
            System.out.println(ex.getMessage());
        }
        
        MenuItem cmItem1 = new MenuItem("Eliminar");
        cmItem1.setOnAction((ActionEvent e) -> {
            eliminarUsuario();
        });
        
        final ContextMenu cMenu = new ContextMenu();
        cMenu.getItems().add(cmItem1);
                
        tablaUsuarios.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY && !tablaUsuarios.getSelectionModel().isEmpty()){
                cMenu.getItems().get(0).setText("Eliminar usuario "+
                        tablaUsuarios.getSelectionModel().getSelectedItem().getCodigo());
                
                cMenu.show(tablaUsuarios, e.getScreenX(), e.getScreenY());
            } else if (cMenu.isShowing()) {
                cMenu.hide();
            }
        });       
        
    }    
    
    private void loadUsuarios(){   
        nombreInfo.setText("Seleccionar usuario");
        tablaUsuarios.setItems(DBQueries.getTodosUsuarios());                
    }
    
    private void showDatosUsuario(){

        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        Scanner scan = new Scanner(currentUser.getNombre());
        String name="";
        while(scan.hasNext()){
            String t = scan.next();
            name += t.charAt(0)+t.substring(1).toLowerCase()+" ";
        }
        nombreInfo.setText(name);                
        
        infoCodigo.setText(currentUser.getCodigo());
        infoNombre.setText(currentUser.getNombre());
        loadCorreos();
        
        for (TipoUsuario t : infoTipo.getItems()){
            if (t.getDescripcion().equals(currentUser.getTipo())){
                infoTipo.getSelectionModel().select(t);
                break;
            }
        }
        for (StatusUsuario s : infoStatus.getItems()){
            if (s.getDescripcion().equals(currentUser.getStatus())){
                infoStatus.getSelectionModel().select(s);
                break;
            }
        }        
        
        infoTelefono.setText(currentUser.getTelefono());
        for (Departamento d : infoDepto.getItems()){
            if (d.getNombre().equals(currentUser.getDepartamento())){
                infoDepto.getSelectionModel().select(d);
                break;
            }
        }
        infoPanel.setVisible(true);  
        tablaUsuarios.requestFocus();
        
        ObservableList<Usuario> rs2 = DBQueries.getComentarios(currentUser.getCodigo());
        if (!rs2.isEmpty()){
            currentUser.setComentario(rs2.get(0).getComentario());
        }
        infoComent.setText(currentUser.getComentario());
        
        loadFoto();
        registrosTab.setUsuario(currentUser);
        horariosTab.setUser(currentUser);   
        clasesTab.setUser(currentUser);
        justifTab.setUser(currentUser);
        
    }
    
    @FXML protected void subirFoto(){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );        
        File file = fileChooser.showOpenDialog(split1.getScene().getWindow());
        
        if (file != null){
            if (file.length() > 512000){ // 500Kb
                JOptionPane.showConfirmDialog(null,
                        "No se permiten archivos superiores a los 500Kb", 
                        "Error", 
                        JOptionPane.CLOSED_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {                
                FileManagerTask fotoup = new FileManagerTask(file, 
                        FileManagerTask.Type.UPLOAD,
                        FileManagerTask.Option.FOTOGRAFIA,
                        currentUser.getCodigo()+".jpg");
                
                fotoup.setOnSucceeded((WorkerStateEvent t) -> {
                    loadFoto();
                });
                new Thread(fotoup).start();
            }
        }
    }
    
    @FXML protected void eliminarFoto(){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        FileManagerTask fotodel = new FileManagerTask(
                FileManagerTask.Type.DELETE,
                FileManagerTask.Option.FOTOGRAFIA,
                currentUser.getCodigo()+".jpg");

        fotodel.setOnSucceeded((WorkerStateEvent t) -> {
            loadFoto();
        });
        
        new Thread(fotodel).start();
    }
    
    private void loadFoto(){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        String host = Configs.SERVER_FOTOS()+ currentUser.getCodigo()+".jpg";

        Image imagen = new Image(host);

        if (imagen.isError()){
            host = Configs.SERVER_FOTOS()+"error.jpg";
            imagen = new Image(host);
        }

        if (!imagen.isError()){
            fotoUser.setCache(false);
            fotoUser.setImage(imagen);
            fotoUser.setFitHeight(300);
            fotoUser.setPreserveRatio(true);
        } else {
            System.out.println("Error estableciendo imagenes");
        }
    }
    
    @FXML protected void updateUsuario(){ 
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
                
        if (!infoCodigo.getText().isEmpty() && !infoNombre.getText().isEmpty()){
            
            String cod = infoCodigo.getText();
            String nom = infoNombre.getText();
            int status = infoStatus.getSelectionModel().getSelectedItem().getStatus();
            int tipocod = infoTipo.getSelectionModel().getSelectedItem().getTipo();
            String depto =  infoDepto.getSelectionModel().getSelectedItem().getCodigo();
            String tel = infoTelefono.getText();
              
            currentUser.setNombre(nom);
            currentUser.setStatus(infoStatus.getSelectionModel().getSelectedItem().getDescripcion());
            currentUser.setTipo(infoTipo.getSelectionModel().getSelectedItem().getDescripcion());
            currentUser.setDepartamento(infoDepto.getSelectionModel().getSelectedItem().getNombre());
            currentUser.setTelefono(tel);
            
            boolean result = DBQueries.updateUsuario(cod, nom, tipocod, status, depto, tel, infoComent.getText());
            
            if (result){
                tablaUsuarios.requestFocus();
                loadUsuarios();
            } else {
                System.out.println("Error actualizando usuario");
            }            
            
        }
    }
    
    private void eliminarUsuario(){
        
        if (!tablaUsuarios.getSelectionModel().isEmpty()){
            Usuario u = tablaUsuarios.getSelectionModel().getSelectedItem();
            int resp = JOptionPane.showConfirmDialog(null,  
                    "¿Confirma que desea eliminar al usuario \n"
                    +u.getNombre()+" ("+u.getCodigo()+")? \n "
                    +"Se eliminará toda la información de registros, huellas, justificaciones, horarios, etc. \n"
                    +"Esta acción no se puede deshacer! ", 
                    "Precaución!", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
            
            if (resp == JOptionPane.OK_OPTION){                
                DBQueries.deleteUsuario(u.getCodigo());
                loadUsuarios();
            }
        }
    }
    
    @FXML protected void crearUsuario(){        
        infoNombre.setText("Agregando usuario...");
        infoTipo.getSelectionModel().clearSelection();
        infoStatus.getSelectionModel().clearSelection();
        infoTelefono.clear();
        infoDepto.getSelectionModel().clearSelection();
        fotoUser.setImage(null);
        tablaUsuarios.getSelectionModel().clearSelection();   
        infoCodigo.clear();
        infoCodigo.setDisable(false);
        infoPanel.setVisible(true);
        botonesNuevoUsuario.setVisible(true);
        updateBtn.setVisible(false);
        infoComent.clear();
        infoCorreo.getItems().clear();
    }
    
    @FXML protected void cancelarNuevoUsuario(){
        infoPanel.setVisible(false);
        infoCodigo.setDisable(true);
        botonesNuevoUsuario.setVisible(false);
        updateBtn.setVisible(true);
    }
    
    @FXML protected void guardarNuevoUsuario(){
        
        if ( !infoCodigo.getText().isEmpty() 
                && !infoNombre.getText().isEmpty() 
                && !infoTipo.getSelectionModel().isEmpty()
                && !infoStatus.getSelectionModel().isEmpty()) {
        
            String cod = infoCodigo.getText();
            
            String nom = infoNombre.getText();
            int status = infoStatus.getSelectionModel().getSelectedItem().getStatus();
            
            int tipocod = infoTipo.getSelectionModel().getSelectedItem().getTipo();
            String depto =  infoDepto.getSelectionModel().getSelectedItem().getCodigo();
                
            String tel = infoTelefono.getText();
            
            boolean result = DBQueries.insertUsuario(cod, nom, tipocod, status, depto, tel, infoComent.getText());            

            if (result){
                loadUsuarios();
                cancelarNuevoUsuario();

            } else {
                Principal.avisar("Error guardando usuario");
            }

        } else {            
            Principal.avisar("Introducir todos los datos obligatorios");
        } 
    }
    
    
    private void inicializarVistaTablas() {
        tablaUsuarios.prefWidthProperty().set(split1.widthProperty().multiply(0.5f).doubleValue());
        tablaUsuarios.prefWidthProperty().bind(split1.widthProperty().multiply(0.5f));
                
        codigoColUs.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nombreColUs.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tipoColUs.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        deptoColUs.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        codigoColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(1/10f));
        nombreColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/10f));
        tipoColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(1/10f));        
        deptoColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/10f));        
                
        botonesNuevoUsuario.setVisible(false);
    }
    
    private void fillBoxes(){
        
        infoTipo.setItems(DBQueries.getTipoUsuarios());        
        infoDepto.setItems(DBQueries.getDepartamentos());
        infoStatus.setItems(DBQueries.getStatusUsuarios());
        
    }

    private void crearListeners(){
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Usuario> observable, Usuario oldValue, Usuario newValue) -> {
            cancelarNuevoUsuario();
            showDatosUsuario();
        });
    }
     
    @SuppressWarnings("unchecked")
    private void createFancyCells() {
        infoTipo.setCellFactory(new Callback<ListView<TipoUsuario>, ListCell<TipoUsuario>>(){
            @Override
            public ListCell<TipoUsuario> call(ListView<TipoUsuario> p) {
                return new ListCell<TipoUsuario>(){
                    @Override
                    protected void updateItem(TipoUsuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {                            
                            //setFont(new Font(16));
                            setText(item.getDescripcion());
                            
                        } else {
                          setText(null);
                        }
                    }
                };
            }
        });
        
        infoDepto.setCellFactory(new Callback<ListView<Departamento>, ListCell<Departamento>>(){
            @Override
            public ListCell<Departamento> call(ListView<Departamento> p) {
                return new ListCell<Departamento>(){
                    @Override
                    protected void updateItem(Departamento item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {                            
                            //setFont(new Font(16));
                            setText(item.getNombre());
                            
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        
        infoStatus.setCellFactory(new Callback<ListView<StatusUsuario>,ListCell<StatusUsuario>>() {            
            @Override
            public ListCell<StatusUsuario> call(ListView<StatusUsuario> p) {            
                return new ListCell<StatusUsuario>() {
                    @Override
                    protected void updateItem(StatusUsuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {                            
                            //setFont(new Font(16));
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
     
        infoStatus.setButtonCell((ListCell) infoStatus.getCellFactory().call(null));
        infoDepto.setButtonCell((ListCell) infoDepto.getCellFactory().call(null));
        infoTipo.setButtonCell((ListCell) infoTipo.getCellFactory().call(null));
        
        infoCorreo.setCellFactory(new Callback<ListView<CorreoUsuario>, ListCell<CorreoUsuario>>() {
            @Override public ListCell<CorreoUsuario> call(ListView<CorreoUsuario> p) {
                return new ListCell<CorreoUsuario>(){
                    @Override protected void updateItem(final CorreoUsuario item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item!=null && !bln){
                            HBox h = new HBox(10);
                            h.setPadding(new Insets(0, 10, 0, 10));
                            h.setAlignment(Pos.CENTER_LEFT);
                            
                            CheckBox ch = new CheckBox();                            
                            ch.setSelected(item.getPrincipal());
                            ch.setDisable(item.getPrincipal());
                            ch.setOnAction((ActionEvent t) -> {
                                updatePrincipalCorreo(item);
                            });
                            HBox hl = new HBox();
                            hl.setAlignment(Pos.CENTER_LEFT);
                            hl.getChildren().add(new Label(item.getCorreo()));
                            
                            Hyperlink hp = new Hyperlink("Eliminar");
                            hp.setOnAction((ActionEvent t) -> {
                                deleteCorreo(item);
                            });
                            h.getChildren().addAll(ch,hl,hp);
                            HBox.setHgrow(hl, Priority.ALWAYS);
                            setGraphic(h);
                        } else {
                            setGraphic(null);
                        }
                    }
                    
                };
            }
        });
    }
    
    private void updatePrincipalCorreo(CorreoUsuario correo){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        DBQueries.updateCorreoPrincipal(currentUser.getCodigo(), correo.getCorreo());
        loadCorreos();
    }
    
    private void deleteCorreo(CorreoUsuario correo){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        DBQueries.deleteCorreoUsuario(currentUser.getCodigo(),correo.getCorreo(),correo.getPrincipal());
        loadCorreos();      
    }
   
    @FXML protected void addCorreo(){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        if (correoInput.getText().matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")){
            
            DBQueries.addCorreoUsuario(currentUser.getCodigo(), correoInput.getText());
            loadCorreos();
            correoInput.clear();
            
        } else {
            Principal.avisar("Error validando email o usuario inexistente");
        }
    }
    
    private void loadCorreos(){
        Usuario currentUser = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        
        infoCorreo.setItems(DBQueries.getCorreosUsurio(currentUser.getCodigo()));        
        vboxCorreos.setPrefHeight((infoCorreo.getItems().size()*27)+30);
        infoCorreo.setVisible(!infoCorreo.getItems().isEmpty());
    }
   
 }
