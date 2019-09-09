package sicaw.gui.pages.mensajes;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.objetos.Mensaje;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;

public class PersonalizadoController implements Initializable {

    private Usuario currentUser;
    
    @FXML private SplitPane split1;
    @FXML private TableView <Usuario> tablaUsuarios;
    @FXML private TableColumn <Usuario, String> codCol;
    @FXML private TableColumn <Usuario, String> nomCol;
    @FXML private TableColumn <Usuario, String> tipoCol;
    
    @FXML private Text destino;
    @FXML private TextArea textArea;
    @FXML private Text info;
    @FXML private Label tamMsj;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        inicializarVistaTablas();        
        crearListeners();        
        loadUsuarios();          
        textArea.setEditable(false);
        destino.setText("A: ");        
    }   
    
    private void loadUsuarios(){
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.JEFE){
            tablaUsuarios.setItems(DBQueries.getAlgunosUsuarios(Autenticator.getCurrentUser().getCodigo()));
        } else {
            tablaUsuarios.setItems(DBQueries.getTodosUsuarios());  
        }                                  
    }
    
    private void showMensajeUsuario() {
        
        destino.setText("A: "+currentUser.getNombre());
        textArea.setEditable(true);
        
        try {
            
            ObservableList<Mensaje> rs = DBQueries.getMensaje(currentUser.getCodigo());
            
            if ( !rs.isEmpty() ){
                textArea.setText(rs.get(0).getMensaje());
                info.setText("*Mensaje aun no se ha entregado al usuario");                
                info.setFill(Color.RED);
                info.setVisible(true);
            } else {
                textArea.clear();                
                info.setVisible(false);                
            }
            
            
        } catch (Exception e){
            e.printStackTrace(System.out);
        }
        
    }
    @FXML protected void limpiarMensaje(){        
        textArea.clear();        
        DBQueries.elimMensaje(currentUser.getCodigo());
        
    }
    
    @FXML protected void guardarMensaje(){
        info.setText(null);
        
        if (textArea.getText().length()>0){
            DBQueries.elimMensaje(currentUser.getCodigo());        
            DBQueries.guardarMensaje(currentUser.getCodigo(), textArea.getText());

            info.setFill(Color.BLACK);
            info.setText("Mensaje sera entregado la proxima vez que el usuario cheque");
            
        } else {
            info.setFill(Color.RED);
            info.setText("Favor de introducir el mensaje");
        }
        
        info.setVisible(true);
        
    }
    
    private void updateEspacioLibre(){
        info.setVisible(false);
        
        if (textArea.getText().length() > 100 )            
            textArea.setText(textArea.getText().substring(0, 100));
                
        tamMsj.setText(textArea.getText().length()+" de 100");
        
    }

    private void inicializarVistaTablas() {
        tablaUsuarios.prefWidthProperty().set(split1.widthProperty().multiply(0.35f).doubleValue());
        tablaUsuarios.prefWidthProperty().bind(split1.widthProperty().multiply(0.35f));
                
        codCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tipoCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        codCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(2/10f));
        nomCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(5/10f));
        tipoCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/10f));        
        
    }

    private void crearListeners() {
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends Usuario> observable, Usuario t, Usuario t1) -> {
                if (t1!=null){
                    currentUser = t1;
                    showMensajeUsuario();
                }
        });
        
        textArea.textProperty().addListener(
            (ObservableValue<? extends String> ov, String t, String t1) -> {
                updateEspacioLibre();  
        });
    }
}
