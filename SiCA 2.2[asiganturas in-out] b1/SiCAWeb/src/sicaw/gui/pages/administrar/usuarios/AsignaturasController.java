package sicaw.gui.pages.administrar.usuarios;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import sica.common.DBQueries;
import sica.common.horarios.HorarioCrn;
import sica.common.usuarios.Usuario;

public class AsignaturasController implements Initializable {

    private Usuario currentUser;
    
    @FXML private ChoiceBox <Integer> anio;
    @FXML private ChoiceBox <String> ciclo;
    
    @FXML private TableView <HorarioCrn> tabla;
    @FXML private TableColumn <HorarioCrn, String> bloqueCol;
    @FXML private TableColumn <HorarioCrn, String> crnCol;
    @FXML private TableColumn <HorarioCrn, String> materiaCol;
    @FXML private TableColumn <HorarioCrn, String> diaCol;
    @FXML private TableColumn <HorarioCrn, String> horarioCol;
    @FXML private TableColumn <HorarioCrn, String> aulaCol;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        inicializarVistaTablas();  
        
        anio.getItems().clear();
        Calendar c = Calendar.getInstance();        
        for (int i=0; i<3; i++){
            anio.getItems().add(c.get(Calendar.YEAR));
            c.add(Calendar.YEAR, -1);
        }
        anio.getSelectionModel().selectFirst();
        
        ciclo.getItems().clear();
        ciclo.getItems().add("A");
        ciclo.getItems().add("B");
        ciclo.getSelectionModel().selectFirst();
        
    }  
    
    public void setUser(Usuario usr){
        currentUser = usr;
        tabla.getItems().clear();
    }
    
    @FXML protected void update(){
        if (currentUser!=null){
            tabla.setItems(DBQueries.getHorarioCrnsUsuario(
                    currentUser.getCodigo(), anio.getValue(), ciclo.getValue()));
        }
    }
    
    private void inicializarVistaTablas() {
        
        bloqueCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("bloque"));
        materiaCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("materia"));
        crnCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("crn"));
        diaCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("dia"));
        horarioCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("horario"));
        aulaCol.setCellValueFactory(new PropertyValueFactory<HorarioCrn, String>("aula"));
        
        bloqueCol.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        crnCol.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        materiaCol.prefWidthProperty().bind(tabla.widthProperty().multiply(5/10f));
        diaCol.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        horarioCol.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        aulaCol.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        
    }
    
}
