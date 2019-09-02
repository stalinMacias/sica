package sica.screen.menuviews;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.common.faltas.FaltaClase;
import sica.common.faltas.ReportesFaltas;
import sica.common.justificantes.JustificanteInterface;

public class FaltasAsignaturas extends Screen implements Initializable {

    private Service <ObservableList<FaltaClase>> getFaltasService;
    private Calendar desde;
    private Calendar hasta;
    private DateFormat df;
    
    @FXML private Label mesActual;
    @FXML private Label mesAnterior;
    @FXML private Label mesSiguiente;
    @FXML private ProgressIndicator loading;
    
    @FXML private TableView<FaltaClase> tabla;
    @FXML private TableColumn<FaltaClase, Date> fecha;
    @FXML private TableColumn<FaltaClase, String> horario;
    @FXML private TableColumn<FaltaClase, FaltaClase> materia;
    @FXML private TableColumn<FaltaClase, JustificanteInterface> status;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getFaltasService = new Service<ObservableList<FaltaClase>>() {

            @Override protected void scheduled() {
                super.scheduled();
                tabla.getSortOrder().clear();
            }
            
            @Override protected Task<ObservableList<FaltaClase>> createTask() {
                return ReportesFaltas.getFaltasAsignaturasPeriodo(
                        desde.getTime(),
                        hasta.getTime(),
                        ScreenManager.menu().getUsuario());
            }

            @Override protected void succeeded() {
                super.succeeded();
                tabla.getSortOrder().add(fecha);
            }            
        };
               
        loading.visibleProperty().bind(getFaltasService.runningProperty());
        tabla.itemsProperty().bind(getFaltasService.valueProperty());   
        initTabla(); 
        df = new SimpleDateFormat("EEEE dd");
        fecha.setSortable(true);
        fecha.setSortType(TableColumn.SortType.ASCENDING);
        
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, ov, nv) -> {            
            if (nv != null && nv.getJustifcante()==null){
                startJustification(nv);
            }
        });
    }    

    private void updateNombresMes(){
        Calendar act = Calendar.getInstance();
        act.setTime(desde.getTime());        
        String mes = act.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault());
        mesActual.setText(mes.toUpperCase().charAt(0)+mes.substring(1));        
        act.add(Calendar.MONTH, -1);
        mes = act.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault());
        mesAnterior.setText("< "+mes.toUpperCase().charAt(0)+mes.substring(1));
        act.add(Calendar.MONTH, 2);
        mes = act.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault());
        mesSiguiente.setText(mes.toUpperCase().charAt(0)+mes.substring(1)+" >");
    }
        
    @Override
    public void start() {
        mesSiguiente.setVisible(false);
        mesAnterior.setVisible(true);     
        desde = Calendar.getInstance();
        hasta = Calendar.getInstance();
        desde.set(Calendar.DAY_OF_MONTH, 1);           
        updateNombresMes();    
        loadTable();                
    }
    
     @FXML protected void nextMonth(){
        mesSiguiente.setVisible(false);
        mesAnterior.setVisible(true);        
        desde.add(Calendar.MONTH, 1);        
        hasta.add(Calendar.MONTH, 1); 
        hasta.set(Calendar.DAY_OF_MONTH,
            hasta.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)?
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH):
                hasta.getActualMaximum(Calendar.DAY_OF_MONTH));        
        updateNombresMes();
        loadTable();
    }
    
    @FXML protected void prevMonth(){
        mesSiguiente.setVisible(true);
        mesAnterior.setVisible(false);
        
        desde.add(Calendar.MONTH, -1);
        hasta.add(Calendar.MONTH, -1);
        hasta.set(Calendar.DAY_OF_MONTH, hasta.getActualMaximum(Calendar.DAY_OF_MONTH));        
        
        updateNombresMes();
        loadTable();
    }
    
    private void loadTable(){
        if (getFaltasService.isRunning()){
            getFaltasService.cancel();
        }
        getFaltasService.reset();
        getFaltasService.start();
        tabla.getSelectionModel().select(null);
    }
    
    private void startJustification(FaltaClase falta){
        ScreenManager.justificar().setClaseJustificando(falta);
        ScreenManager.menu().addScreen(Screens.MENU_JUSTIFICAR);
    }
    
    private void initTabla(){        
        fecha.setCellValueFactory(new PropertyValueFactory<> ("fecha"));
        horario.setCellValueFactory(new PropertyValueFactory<> ("horario"));
        materia.setCellValueFactory(new PropertyValueFactory<> ("crn"));    
        status.setCellValueFactory(new PropertyValueFactory<> ("justifcante"));
        
        fecha.prefWidthProperty().bind(tabla.widthProperty().multiply(1/6d));
        horario.prefWidthProperty().bind(tabla.widthProperty().multiply(1/6d));
        materia.prefWidthProperty().bind(tabla.widthProperty().multiply(3/6d));
        status.prefWidthProperty().bind(tabla.widthProperty().multiply(1/6d).subtract(3));
        
        fecha.setCellFactory(new Callback<TableColumn<FaltaClase, Date>, TableCell<FaltaClase, Date>>() {
            @Override public TableCell<FaltaClase, Date> call(TableColumn<FaltaClase, Date> param) {
                return new TableCell<FaltaClase,Date>(){
                    @Override protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item!=null){
                            setText(df.format(item).toUpperCase());
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        
        materia.setCellValueFactory((TableColumn.CellDataFeatures<FaltaClase, FaltaClase> param) -> 
                new SimpleObjectProperty<>(param.getValue()));
        
        materia.setCellFactory(new Callback<TableColumn<FaltaClase, FaltaClase>, TableCell<FaltaClase, FaltaClase>>() {
            @Override public TableCell<FaltaClase, FaltaClase> call(TableColumn<FaltaClase, FaltaClase> param) {
                return new TableCell<FaltaClase,FaltaClase>(){
                    @Override protected void updateItem(FaltaClase item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item!=null && item.getCrn()!=null){
                            setText(item.getCrn().getMateria()+" ("+item.getCrn().getCrn()+")");
                            setWrapText(true);
                            setMaxHeight(Double.MAX_VALUE);                                
                            
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        
        status.setCellFactory(new Callback<TableColumn<FaltaClase, JustificanteInterface>, TableCell<FaltaClase, JustificanteInterface>>() {
            @Override public TableCell<FaltaClase, JustificanteInterface> call(TableColumn<FaltaClase, JustificanteInterface> param) {
                return new TableCell<FaltaClase,JustificanteInterface>(){
                    @Override protected void updateItem(JustificanteInterface item, boolean empty) {
                        super.updateItem(item, empty);                        
                        if (!empty && item!=null){   
                            setText("Justificada");
                            setStyle("-fx-text-fill: green;");
                            setTooltip(new Tooltip(item.getDescripcionJustificante()));
                        } else if (!empty) {
                            setText("Inasistencia");
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setText(null);
                            setStyle(null);
                        }
                    }                    
                };
            }
        });
    }
}
