package sicaw.gui.pages.asistencia;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.asistencias.AsistenciaClase;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.usuarios.Privilegios;
import sicaw.gui.Principal;
import sicaweb.Utils;
import sicaweb.reportes.pdf.ReporteAsignaturasDia;

public class AsignaturaController implements Initializable {

    private DatePicker diaFecha;
    private Service<ObservableList<AsistenciaClase>> getService;
    
    @FXML private HBox topBox;
    @FXML private Button imprimirBtn;
    @FXML private ProgressIndicator progress;
    @FXML protected TableView <AsistenciaClase> tabla;    
    @FXML protected TableColumn <AsistenciaClase, String> codigoColDia; 
    @FXML protected TableColumn <AsistenciaClase, String> nombreColDia;
    @FXML protected TableColumn <AsistenciaClase, String> materiaColDia;
    @FXML protected TableColumn <AsistenciaClase, String> horarioColDia;
    @FXML protected TableColumn <AsistenciaClase, AsistenciaClase> statusColDia;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        inicializarVistaTablas();                 
        createDatePickers();        
        createFancyCells();   
        
        getService = new Service<ObservableList<AsistenciaClase>>() {
            @Override protected Task<ObservableList<AsistenciaClase>> createTask() {
                return ReportesAsistencias.getAsistenciaClasesPorDia(
                        diaFecha.getSelectedDate(), 
                        Autenticator.getCurrentUser(),
                        Autenticator.getCurrentUser().getPrivilegios()
                );
            }
        };
        tabla.itemsProperty().bind(getService.valueProperty());
        progress.visibleProperty().bind(getService.runningProperty());
        progress.progressProperty().bind(getService.progressProperty());
        
        diaFecha.setSelectedDate(Calendar.getInstance().getTime());
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.JEFE){
            topBox.getChildren().add(topBox.getChildren().size()-1, new Label(" (Para jefes de departamento) "));
        }
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){
            imprimirBtn.setDisable(true);
        }
        
    }    
    
    @FXML protected void printPDF(){
        if (!tabla.getItems().isEmpty()){
            Principal.avisar("Creando archivo PDF para impresión");
            new Thread(new ReporteAsignaturasDia(
                    tabla.getItems(),
                    diaFecha.getSelectedDate())
            ).start();
        }
    }
    
    private void updateTable(boolean start){
        if (getService.isRunning())getService.cancel();        
        getService.reset();
        if (start) getService.start();
    }
    
    @FXML protected void updateTablePorDia(){
        updateTable(true);
    }
    
    private void inicializarVistaTablas() {
        
        codigoColDia.setCellValueFactory(new PropertyValueFactory<>("crn"));
        nombreColDia.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        Callback<TableColumn.CellDataFeatures<AsistenciaClase, AsistenciaClase>, ObservableValue<AsistenciaClase>> cb = 
                (TableColumn.CellDataFeatures<AsistenciaClase, AsistenciaClase> p) -> new SimpleObjectProperty<>(p.getValue());
        statusColDia.setCellValueFactory(cb);
        materiaColDia.setCellValueFactory(new PropertyValueFactory<>("materia"));
        horarioColDia.setCellValueFactory(new PropertyValueFactory<>("horario"));
        
        codigoColDia.prefWidthProperty().bind(tabla.widthProperty().multiply(2/20f).subtract(2));
        nombreColDia.prefWidthProperty().bind(tabla.widthProperty().multiply(5/20f).subtract(2));
        statusColDia.prefWidthProperty().bind(tabla.widthProperty().multiply(5/20f).subtract(2));
        materiaColDia.prefWidthProperty().bind(tabla.widthProperty().multiply(6/20f).subtract(2));
        horarioColDia.prefWidthProperty().bind(tabla.widthProperty().multiply(2/20f).subtract(2));
        
    }

    private void createDatePickers() {
        diaFecha = Utils.newDatePicker("Seleccionar fecha");
        topBox.getChildren().add(1,diaFecha); 
        diaFecha.selectedDateProperty().addListener((ObservableValue<? extends Date> ov, Date t, Date t1) -> {
            updateTable(false);
        });
    }

    private void createFancyCells() {
        statusColDia.setCellFactory(new Callback<TableColumn<AsistenciaClase, AsistenciaClase>, TableCell<AsistenciaClase, AsistenciaClase>>() {
            @Override public TableCell<AsistenciaClase, AsistenciaClase> call(TableColumn<AsistenciaClase, AsistenciaClase> param) {
                return new TableCell<AsistenciaClase, AsistenciaClase>() {
                    @Override protected void updateItem(AsistenciaClase item, boolean empty) {
                        super.updateItem(item, empty);                        
                        if (!empty && item!=null) {
                            Text t;
                            if (item.getRegistro() != null){                                
                                t = new Text(Utils.formatTime(item.getRegistro().getFechahora()));
                                
                                if (item.getRegistro().getModificado()!=null && !item.getRegistro().getModificado().isEmpty()){
                                    setTooltip(new Tooltip("Registro modificado por: "
                                            +item.getRegistro().getModificado()));
                                    t.setFill(Color.GREEN);                                    
                                } else {
                                    t.setFill(Color.BLACK);
                                }
                                
                            } else if (item.getJustificante() != null){
                                t = new Text("J. - "+item.getJustificante().getNombrejustificante());
                                
                                if (item.getJustificante() instanceof JustificanteAsignatura){
                                    JustificanteAsignatura j = (JustificanteAsignatura)item.getJustificante();
                                    setTooltip(new Tooltip("Autorizado por: "+j.getAprobadonombre()));
                                    t.setFill(Color.ORANGERED);
                                    
                                } else if (item.getJustificante() instanceof JustificantePeriodo){
                                    JustificantePeriodo j = (JustificantePeriodo)item.getJustificante();
                                    setTooltip(new Tooltip("Autorizado por: "+j.getAprobadonombre()));
                                    t.setFill(Color.ORANGERED);
                                    
                                } else if (item.getJustificante() instanceof Evento){                                   
                                    t.setFill(Color.BLUE);
                                }
                                
                            } else {
                                t = new Text("No checó");
                                t.setFill(Color.RED);
                            }
                            setGraphic(t);
                        } else {
                            setGraphic(null);
                        }                        
                        
                    }
                };
            }
          });
    }
    
}
