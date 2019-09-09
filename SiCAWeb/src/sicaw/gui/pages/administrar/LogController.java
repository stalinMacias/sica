package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import sica.common.DBQueries;
import sica.common.objetos.EntradaLog;

public class LogController implements Initializable {

    @FXML private TableView <EntradaLog> tablaLog;
    @FXML private TableColumn <EntradaLog, String> usuario;
    @FXML private TableColumn <EntradaLog, String> nombre;
    @FXML private TableColumn <EntradaLog, String> fecha;
    @FXML private TableColumn <EntradaLog, String> descrip;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        inicializarTabla();
        
        loadLogs();
        
    }   
    
    private void loadLogs(){        
        tablaLog.setItems(DBQueries.getLog());
        
    }
    
    private void inicializarTabla(){
        usuario.setCellValueFactory(new PropertyValueFactory<EntradaLog, String>("usuario"));
        nombre.setCellValueFactory(new PropertyValueFactory<EntradaLog, String>("nombre"));
        fecha.setCellValueFactory(new PropertyValueFactory<EntradaLog, String>("fecha"));
        descrip.setCellValueFactory(new PropertyValueFactory<EntradaLog, String>("descripcion"));
        
        usuario.prefWidthProperty().bind(tablaLog.widthProperty().multiply(1/10f));
        nombre.prefWidthProperty().bind(tablaLog.widthProperty().multiply(2/10f));
        fecha.prefWidthProperty().bind(tablaLog.widthProperty().multiply(2/10f));        
        descrip.prefWidthProperty().bind(tablaLog.widthProperty().multiply(5/10f));        
        
    }
}

