package sicaw.gui.pages.administrar;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import sica.common.DBQueries;
import sica.common.objetos.Bloque;
import sicaw.gui.Principal;
import sicaweb.Utils;

public class BloquesController implements Initializable {

    private DatePicker ini1, fin1, ini2, fin2, ini0, fin0;
    private Boolean newBloque;
    
    @FXML private ChoiceBox<Integer> anioBox;
    @FXML private ChoiceBox<String> cicloBox;
    @FXML private Label infoBloques;
    @FXML private GridPane fechas;
    @FXML private Button saveButton;
    
    @FXML private TableView<Bloque> tabla;
    @FXML private TableColumn <Bloque,String> anioC;
    @FXML private TableColumn <Bloque,String> cicloC;
    @FXML private TableColumn <Bloque,String> bloqueC;
    @FXML private TableColumn <Bloque,String> inicioC;
    @FXML private TableColumn <Bloque,String> finC;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -2);
        
        anioBox.getItems().clear();
        for (int i=0; i<5; i++){
            anioBox.getItems().add(c.get(Calendar.YEAR));
            c.add(Calendar.YEAR, 1);
        }
        cicloBox.getItems().clear();
        cicloBox.getItems().add("A");
        cicloBox.getItems().add("B");
        
        createDatePickers();
        inicializarVistaTablas();
        loadBloques();
        
        
    }    
    
    private void loadBloques(){        
        tabla.setItems(DBQueries.getBloques());    
        anioBox.getSelectionModel().clearSelection();
        cicloBox.getSelectionModel().clearSelection();
        saveButton.setVisible(false);
        ini0.setDisable(true);
        fin0.setDisable(true);
        fin1.setDisable(true);
    }
    
    @FXML protected void searchBloque(){
        if (!anioBox.getSelectionModel().isEmpty() && !cicloBox.getSelectionModel().isEmpty()){
            infoBloques.setText("Mostrando bloques "+anioBox.getValue()+cicloBox.getValue());
            newBloque = true;
            for (Bloque b : tabla.getItems()){                                
                if (b.getAnio().equals(anioBox.getValue()) && b.getCiclo().equals(cicloBox.getValue())){
                    newBloque = false;
                    switch(b.getBloque()){
                        case "0": 
                            ini0.setSelectedDate(Utils.parseDate(b.getInicio()));
                            fin0.setSelectedDate(Utils.parseDate(b.getFin()));
                            break;
                        case "1":
                            ini1.setSelectedDate(Utils.parseDate(b.getInicio()));
                            fin1.setSelectedDate(Utils.parseDate(b.getFin()));                            
                            break;                            
                        case "2":
                            ini2.setSelectedDate(Utils.parseDate(b.getInicio()));
                            fin2.setSelectedDate(Utils.parseDate(b.getFin()));
                            
                            break;
                    }
                }
            }
            if (newBloque){
                ini0.selectedDateProperty().set(null);
                fin0.selectedDateProperty().set(null);
                fin1.selectedDateProperty().set(null);
            }
            saveButton.setVisible(true);
            ini0.setDisable(false);
            fin0.setDisable(false);
            fin1.setDisable(false);
        } else {
            Principal.avisar("Seleccionar año y ciclo");
            saveButton.setVisible(false);
        }
            
    }
    
    @FXML protected void saveBloques(){
        if (ini0.getSelectedDate()!=null && fin0.getSelectedDate()!=null && fin1.getSelectedDate()!=null){
            Integer anio = anioBox.getValue();
            String ciclo = cicloBox.getValue();

            if (newBloque){
                DBQueries.insertBloque(anio,ciclo,0, Utils.formatDate(ini0.getSelectedDate()), Utils.formatDate(fin0.getSelectedDate()));
                DBQueries.insertBloque(anio,ciclo,1, Utils.formatDate(ini1.getSelectedDate()), Utils.formatDate(fin1.getSelectedDate()));
                DBQueries.insertBloque(anio,ciclo,2, Utils.formatDate(ini2.getSelectedDate()), Utils.formatDate(fin2.getSelectedDate()));
            } else {
                DBQueries.updateBloque(anio, ciclo, 0, Utils.formatDate(ini0.getSelectedDate()), Utils.formatDate(fin0.getSelectedDate()));
                DBQueries.updateBloque(anio, ciclo, 1, Utils.formatDate(ini1.getSelectedDate()), Utils.formatDate(fin1.getSelectedDate()));
                DBQueries.updateBloque(anio, ciclo, 2, Utils.formatDate(ini2.getSelectedDate()), Utils.formatDate(fin2.getSelectedDate()));
            }

            loadBloques();
        } else {
            Principal.avisar("Seleccionar fechas");
        }
        
    }
    
    private void createDatePickers(){
        
        ini0 = Utils.newDatePicker("inicio");
        ini1 = Utils.newDatePicker("inicio");
        ini2 = Utils.newDatePicker("inicio");
        fin0 = Utils.newDatePicker("fin");
        fin1 = Utils.newDatePicker("fin");
        fin2 = Utils.newDatePicker("fin");        
        
        fechas.add(ini0, 1, 1); fechas.add(fin0, 2, 1);
        fechas.add(ini1, 1, 2); fechas.add(fin1, 2, 2);
        fechas.add(ini2, 1, 3); fechas.add(fin2, 2, 3);
        
        ini1.setDisable(true);
        ini2.setDisable(true);
        fin2.setDisable(true);        
        
        ini0.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue ov, Date t, Date t1) {                
                ini1.setSelectedDate(t1);
            }
        });
        
        fin0.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue ov, Date t, Date t1) {                
                fin2.setSelectedDate(t1);
            }
        });
        
        fin1.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue ov, Date t, Date t1) {                
                ini2.setSelectedDate(t1);
            }
        });
        
    }
    
    private void inicializarVistaTablas() {
                
        anioC.setCellValueFactory(new PropertyValueFactory<Bloque, String>("anio"));
        cicloC.setCellValueFactory(new PropertyValueFactory<Bloque, String>("ciclo"));
        bloqueC.setCellValueFactory(new PropertyValueFactory<Bloque, String>("bloque"));
        inicioC.setCellValueFactory(new PropertyValueFactory<Bloque, String>("inicio"));
        finC.setCellValueFactory(new PropertyValueFactory<Bloque, String>("fin"));
        
        anioC.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        cicloC.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        bloqueC.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        inicioC.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        finC.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
    }
    
}
