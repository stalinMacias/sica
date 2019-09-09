package sicaw.gui.pages.faltas;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.DBQueries;
import sica.common.faltas.Falta;
import sica.common.faltas.FaltaDia;
import sica.common.faltas.FaltasUsuario;
import sica.common.faltas.ReportesFaltas;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.usuarios.TipoUsuario;
import sicaweb.Utils;
import sicaw.gui.Principal;
import sicaweb.reportes.pdf.ReporteFaltasPeriodo;
import sicaweb.reportes.xml.ExportFaltasPeriodo;

public class PeriodoController implements Initializable {

    private DatePicker desdeFecha, hastaFecha;
    private Service <ObservableList<FaltasUsuario>> service;
    
    @FXML private HBox desdePanel;
    @FXML private HBox hastaPanel;
    @FXML private ComboBox<TipoUsuario> departamento;
    @FXML private Button exportarBtn, imprimirBtn;
    
    @FXML private ProgressIndicator progress;
    @FXML private TableView <FaltasUsuario> tabla;
    @FXML private TableColumn <FaltasUsuario,String> profesor;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaDia>> faltas;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaDia>> detalle;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createService();
        inicializarVistaTablas();        
        createFancyCells();                
        createDatePickers();
        
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        hastaFecha.setSelectedDate(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -30);
        desdeFecha.setSelectedDate(c.getTime());   
        
        departamento.getItems().clear();
        
        ObservableList<TipoUsuario> tipoU = DBQueries.getTipoUsuarios();
        for (TipoUsuario t : tipoU){
            if (!t.getDescripcion().equals("Asignatura")){
                departamento.getItems().add(t);
            }
        }
        
        departamento.getSelectionModel().selectFirst();
        createListeners();
    }    
    
    public void createService(){
        service = new Service<ObservableList<FaltasUsuario>>() {
            @Override protected Task<ObservableList<FaltasUsuario>> createTask() {
                
                Task<ObservableList<FaltasUsuario>> taskFaltas = ReportesFaltas.getFaltasUsuariosPeriodo(
                            desdeFecha.getSelectedDate(),
                            hastaFecha.getSelectedDate(), 
                            departamento.getValue()
                    );

                return taskFaltas;
            }
        };
        service.stateProperty().addListener((ov,t,t1) -> {            
            if (service.getValue() != null){
                switch (t1){
                    case CANCELLED: case FAILED: case SUCCEEDED:
                        if (service.valueProperty().get().size()>0){
                            tabla.getSortOrder().clear();
                            profesor.setSortType(TableColumn.SortType.ASCENDING);
                            tabla.getSortOrder().add(profesor);
                        }
                        break;
                }
            }
        });
        
        tabla.itemsProperty().bind(service.valueProperty());
        progress.visibleProperty().bind(service.runningProperty());
        progress.progressProperty().bind(service.progressProperty());
        exportarBtn.visibleProperty().bind(service.valueProperty().isNotNull());
        imprimirBtn.visibleProperty().bind(service.valueProperty().isNotNull());
        
    }
    
    private void runService(boolean start) {
        if (service.isRunning()) {
            service.cancel();
        }
        if (service.getState() != Worker.State.READY) {
            service.reset();
        }
        if (start) {
            service.start();
        }

    }
    
    @FXML public void loadFaltas(){        
        if (desdeFecha.getSelectedDate()!=null && hastaFecha.getSelectedDate()!=null &&
                desdeFecha.getSelectedDate().before(hastaFecha.getSelectedDate()) &&
                desdeFecha.getSelectedDate().compareTo(Calendar.getInstance().getTime())<=0 ){
             runService(true);           
            
        } else {
            runService(false);
            Principal.avisar("Error con las fechas seleccionadas");
        }        
    }
       
    @FXML protected void exportTable(){
        if (!tabla.getItems().isEmpty()){
            Principal.avisar("Exportando información a hoja de cálculo");
            new Thread( new ExportFaltasPeriodo(
                tabla.getItems(),departamento.getSelectionModel().getSelectedItem(),
                desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() )                    
            ).start();
        }
    }
    @FXML protected void printTable(){
        if (!tabla.getItems().isEmpty()){
            Principal.avisar("Creando archivo PDF para impresión");
            new Thread( new ReporteFaltasPeriodo(
                tabla.getItems(),departamento.getSelectionModel().getSelectedItem(),
                desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() )                    
            ).start();
        }
    }
    
    private void createFancyCells() {        
        faltas.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaDia>>, TableCell<FaltasUsuario,ObservableList<FaltaDia>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaDia>> call(TableColumn<FaltasUsuario, ObservableList<FaltaDia>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaDia>>(){
                    @Override protected void updateItem(ObservableList<FaltaDia> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (!empty){    
                            int i = 0;
                            for (FaltaDia f : item){
                                if (f.getJustificante()==null) i++;                                        
                            }
                            setText(i+"");
                        } else {
                            setText(null);
                        }
                    }
                };
            }
                    
        });
        
        detalle.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaDia>>, TableCell<FaltasUsuario,ObservableList<FaltaDia>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaDia>> call(TableColumn<FaltasUsuario, ObservableList<FaltaDia>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaDia>>(){
                    @Override protected void updateItem(ObservableList<FaltaDia> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (!empty){
                            VBox vbox = new VBox();
                            vbox.setAlignment(Pos.TOP_LEFT);                            
                            vbox.setPadding(new Insets(5));
                            vbox.setSpacing(5);
                            
                            int i = 1;
                            HBox hbox = new HBox();
                            hbox.setSpacing(10);
                            
                            for (FaltaDia f: item){                                  
                                
                                if (f.getJustificante()!=null && !(f.getJustificante() instanceof Evento)){
                                    Label t = new Label();
                                    t.setText(" ("+Utils.formatDate(f.getFecha())+": "+f.getJustificante().getNombrejustificante()+") "); 
                                    
                                    if (f.getJustificante() instanceof JustificanteAsignatura){
                                        JustificanteAsignatura e = (JustificanteAsignatura)f.getJustificante();
                                        //t.setTooltip(new Tooltip("Autorizado por: "+e.getAutorizado()));
                                    }
                                    
                                    hbox.getChildren().addAll(t);
                                    i+=3;
                                    
                                } else if (f.getJustificante()==null) {
                                    Text l = new Text(Utils.formatDate(f.getFecha())+" ");
                                    hbox.getChildren().addAll(l);                                
                                }
                                
                                if ( i>10 ){
                                    vbox.getChildren().add(hbox);
                                    hbox = new HBox();
                                    hbox.setSpacing(5);
                                    i=0;
                                }
                                i++;
                            }
                            
                            vbox.getChildren().add(hbox);                            
                            setGraphic(vbox);
                        } else {
                            setGraphic(null);                            
                        }
                    }
                };
            }
                    
        });
        
        departamento.setCellFactory(new Callback<ListView<TipoUsuario>,ListCell<TipoUsuario>>(){            
            @Override public ListCell<TipoUsuario> call(ListView<TipoUsuario> p) {
                return new ListCell<TipoUsuario>(){
                    @Override public void updateItem(TipoUsuario d, boolean empty) {
                        super.updateItem(d,empty);                        
                        if (!empty){
                            setText(d.getDescripcion());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        departamento.setButtonCell(departamento.getCellFactory().call(null));
    }
    
    private void inicializarVistaTablas(){
        
        profesor.setCellValueFactory(new PropertyValueFactory<FaltasUsuario, String>("nombre"));
        faltas.setCellValueFactory(new PropertyValueFactory <FaltasUsuario,ObservableList<FaltaDia>>("faltas"));
        detalle.setCellValueFactory(new PropertyValueFactory <FaltasUsuario,ObservableList<FaltaDia>>("faltas"));
        
        profesor.prefWidthProperty().bind(tabla.widthProperty().multiply(4/20f).subtract(3));
        faltas.prefWidthProperty().bind(tabla.widthProperty().multiply(1/20f).subtract(3));
        detalle.prefWidthProperty().bind(tabla.widthProperty().multiply(15/20f).subtract(3));
                
    }
    
    private void createDatePickers() {
        desdeFecha = Utils.newDatePicker("Seleccionar fecha");
        desdePanel.getChildren().addAll(desdeFecha);
        
        hastaFecha = Utils.newDatePicker("Seleccionar fecha");
        hastaPanel.getChildren().addAll(hastaFecha);
    }
    
    private void createListeners(){
        ChangeListener<Date> chl = new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue <? extends Date> ov, Date t, Date t1) {
                runService(false);
            }
        };
        desdeFecha.selectedDateProperty().addListener(chl);
        hastaFecha.selectedDateProperty().addListener(chl);
        
        ChangeListener<TipoUsuario> chl2 = new ChangeListener<TipoUsuario>(){
            @Override
            public void changed (ObservableValue <? extends TipoUsuario> ov, TipoUsuario t, TipoUsuario t1) {
                runService(false);
            }
        };
        departamento.getSelectionModel().selectedItemProperty().addListener(chl2);
    }
    
}
