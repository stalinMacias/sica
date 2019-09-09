package sicaw.gui.pages.asistencia;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.objetos.Registro;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.TipoUsuario;
import sicaw.gui.Principal;
import sicaweb.Utils;
import sicaweb.reportes.pdf.ReporteAsistenciaDia;

public class PorDiaController implements Initializable {

    private DatePicker diaFecha;
    private Service<ObservableList<AsistenciaUsuario>> getListService;
    
    @FXML private HBox fechaPanel;
    @FXML private ComboBox <TipoUsuario> tipoUsuarios;
    @FXML private HBox opciones;
    @FXML private Button imprimir;
    @FXML private Button buscar;
    @FXML private ProgressIndicator progress;
    @FXML private TableView <AsistenciaUsuario> tablaPorDia;    
    @FXML private TableColumn <AsistenciaUsuario, String> codigoColDia; 
    @FXML private TableColumn <AsistenciaUsuario, String> nombreColDia;
    @FXML private TableColumn <AsistenciaUsuario, AsistenciaUsuario> entradaColDia;
    @FXML private TableColumn <AsistenciaUsuario, AsistenciaUsuario> salidaColDia;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tipoUsuarios.getItems().clear();
        
        for (TipoUsuario t : DBQueries.getTipoUsuarios()){
            if (!t.getDescripcion().equals("Asignatura")){
                tipoUsuarios.getItems().add(t);
            }
        }
        tipoUsuarios.getSelectionModel().selectFirst();
        
        inicializarVistaTablas();        
                 
        createDatePickers();        
        createFancyCells();    
        
        diaFecha.setSelectedDate(Calendar.getInstance().getTime());
                 
        getListService = new Service<ObservableList<AsistenciaUsuario>>() {
            @Override protected Task<ObservableList<AsistenciaUsuario>> createTask() {
                return ReportesAsistencias.getAsistenciaUsuariosPorDia(
                    diaFecha.getSelectedDate(),
                    Autenticator.getCurrentUser(),
                    tipoUsuarios.getValue());
            }
        };
        tablaPorDia.itemsProperty().bind(getListService.valueProperty());
        progress.visibleProperty().bind(getListService.runningProperty());
        progress.progressProperty().bind(getListService.progressProperty());
        buscar.disableProperty().bind(getListService.runningProperty());
        
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.USUARIO){
            opciones.getChildren().remove(0);
            opciones.getChildren().remove(0);            
        } 
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){
            imprimir.setDisable(true);
        }
        createListeners();
    }    
        
    private void updateTablaPorDia(boolean start){        
        if (getListService.isRunning()) getListService.cancel();        
        getListService.reset();              
        if (start) getListService.start();
    }
    
    @FXML protected void updateTablePorDia(){
        updateTablaPorDia(true);
    }
    
    @FXML protected void printPDF(){
        if (!tablaPorDia.getItems().isEmpty()){
            Principal.avisar("Creando archivo PDF para impresión");
            new Thread(new ReporteAsistenciaDia(
                    tablaPorDia.getItems(),
                    diaFecha.getSelectedDate(),
                    tipoUsuarios.getSelectionModel().getSelectedItem())
            ).start();
        }
    }
    
    @FXML protected void exportXML(){
        if (!tablaPorDia.getItems().isEmpty()){
            /*new Thread(new ExportAsistenciaDia(
                    tablaPorDia.getItems(),
                    diaFecha.getSelectedDate(),
                    tipoUsuarios.getSelectionModel().getSelectedItem())
            ).start();*/
        }
    }
    
    private void inicializarVistaTablas() {        
        codigoColDia.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        nombreColDia.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        Callback<TableColumn.CellDataFeatures<AsistenciaUsuario, AsistenciaUsuario>, ObservableValue<AsistenciaUsuario>> cb = 
            (TableColumn.CellDataFeatures<AsistenciaUsuario, AsistenciaUsuario> p) -> new SimpleObjectProperty<>(p.getValue());
        entradaColDia.setCellValueFactory(cb);
        salidaColDia.setCellValueFactory(cb);
        
        codigoColDia.prefWidthProperty().bind(tablaPorDia.widthProperty().multiply(1/10f));
        nombreColDia.prefWidthProperty().bind(tablaPorDia.widthProperty().multiply(4/10f));
        entradaColDia.prefWidthProperty().bind(tablaPorDia.widthProperty().multiply(5/20f));
        salidaColDia.prefWidthProperty().bind(tablaPorDia.widthProperty().multiply(5/20f));
        
    }
    
    private void createListeners() {        
        tipoUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TipoUsuario> ov, TipoUsuario t, TipoUsuario t1) -> {
            updateTablaPorDia(false);
        }); 
        
        diaFecha.selectedDateProperty().addListener(
                (ObservableValue <? extends Date> ov, Date t, Date t1) -> {
            updateTablaPorDia(false);
        });               
    }
    
    private void createDatePickers() {
        diaFecha = Utils.newDatePicker("Seleccionar fecha");
        fechaPanel.getChildren().addAll(diaFecha);
        HBox.setHgrow(diaFecha, Priority.ALWAYS);
    }

    private void createFancyCells() {
        
        Callback<ListView<TipoUsuario>, ListCell<TipoUsuario>> factory = 
         new Callback<ListView<TipoUsuario>, ListCell<TipoUsuario>>(){
            @Override public ListCell<TipoUsuario> call(ListView<TipoUsuario> p) {
                return new ListCell<TipoUsuario>(){
                    @Override protected void updateItem(TipoUsuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {       
                            setText(item.getDescripcion());                            
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
        
        tipoUsuarios.setCellFactory(factory);
        tipoUsuarios.setButtonCell((ListCell<TipoUsuario>) factory.call(null));
                
        entradaColDia.setCellFactory(regColFactory(1));
        salidaColDia.setCellFactory(regColFactory(2));
                
    }
    
    public Callback<TableColumn<AsistenciaUsuario, AsistenciaUsuario>, 
                        TableCell<AsistenciaUsuario, AsistenciaUsuario>> regColFactory(final int col) {
                            
        return  new Callback<TableColumn<AsistenciaUsuario, AsistenciaUsuario>, TableCell<AsistenciaUsuario, AsistenciaUsuario>>() {
        @Override
        public TableCell<AsistenciaUsuario, AsistenciaUsuario> call(TableColumn<AsistenciaUsuario, AsistenciaUsuario> param) {
            return new TableCell<AsistenciaUsuario, AsistenciaUsuario>() {
                @Override
                protected void updateItem(AsistenciaUsuario item, boolean empty) {    
                    super.updateItem(item, empty);                                        
                    if (item!=null && !empty) {   
                        Registro r = col == 1? item.getRegistroEntrada(): item.getRegistroSalida();
                                                
                        Text t = new Text();
                        if (r != null){
                            t.setText(Utils.formatTime(r.getFechahora()) );
                            if (r.getModificado()!=null && !r.getModificado().isEmpty()){
                                setTooltip(new Tooltip("Registro modificado por "+ r.getModificado()));  
                                t.setFill(Color.GREEN);                            
                            } else {
                                t.setFill(Color.BLACK);
                            }
                        } else {
                            String s = col == 1 ? item.getEntrada() : item.getSalida() ;

                            if (item.getJustif() != null){
                                if (item.getJustif() instanceof Evento){
                                    Evento e = (Evento)item.getJustif();
                                    s = " "+e.getNombre()+" ";
                                    t.setFill(Color.BLUE); 

                                } else if (item.getJustif() instanceof JustificantePeriodo){
                                    JustificantePeriodo j = (JustificantePeriodo) item.getJustif();
                                    s += " ("+j.getNombrejustificante()+") ";
                                    setTooltip(new Tooltip("Justificado por: "+ j.getAprobadonombre()));                            
                                    t.setFill(Color.DARKORANGE); 
                                }
                            } else {                        
                                t.setFill(Color.RED);     
                            }
                            t.setText(s);                        
                        }  
                        setGraphic(t);                    
                    } else {                        
                        setGraphic(null);
                    }
                }

            };
           
        }};
    }        
     
}


