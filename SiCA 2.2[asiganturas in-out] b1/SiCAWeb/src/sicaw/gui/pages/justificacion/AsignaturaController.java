package sicaw.gui.pages.justificacion;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.asistencias.AsistenciaClase;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.justificantes.Evento;
import sica.common.justificantes.Folio;
import sica.common.justificantes.Fraccion;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.ReportesJustificantes;
import sica.common.justificantes.TipoJustificante;
import sica.common.usuarios.Privilegios;
import sicaw.gui.Principal;
import sicaweb.Utils;

public class AsignaturaController implements Initializable {

    private DatePicker diaFecha;
    private Service<ObservableList<AsistenciaClase>> getAsistenciaService;
    private ObservableMap<String, ObservableList<TipoJustificante>> nuevoJustificante;
            
    @FXML private HBox topPanel;
    @FXML private ProgressIndicator progress;
    @FXML private TableView <AsistenciaClase> clasesTable;
    @FXML private TableColumn <AsistenciaClase,String> profesor;
    @FXML private TableColumn <AsistenciaClase,String> materia;
    @FXML private TableColumn <AsistenciaClase,String> horario;
    @FXML private TableColumn <AsistenciaClase,String> crn;
    @FXML private TableColumn <AsistenciaClase,AsistenciaClase> status;
    @FXML private TableColumn <AsistenciaClase,AsistenciaClase> justificacion;
    
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        createDatePickers();
        inicializarVistaTablas();
        createFancyCell();
        
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR)
            justificacion.setVisible(false);
        
        getAsistenciaService = new Service<ObservableList<AsistenciaClase>>() {
            @Override protected Task<ObservableList<AsistenciaClase>> createTask() {
                return ReportesAsistencias.getAsistenciaClasesPorDia(
                        diaFecha.getSelectedDate(), 
                        Autenticator.getCurrentUser(),
                        Autenticator.getCurrentUser().getPrivilegios()
                );
            }
        };
        clasesTable.itemsProperty().bind(getAsistenciaService.valueProperty());
        progress.visibleProperty().bind(getAsistenciaService.runningProperty());
        progress.progressProperty().bind(getAsistenciaService.progressProperty());
        
        diaFecha.setSelectedDate(Calendar.getInstance().getTime());
        nuevoJustificante = ReportesJustificantes.getListaJustificantes();
    }    
    
    private void updateTable(boolean start){
        if (getAsistenciaService.isRunning()) getAsistenciaService.cancel();
        getAsistenciaService.reset();
        if (start) getAsistenciaService.start();
    }
    @FXML protected void updateTable(){
        updateTable(true);
    }
        
    private void createJustificante(String usuario, String crn, TipoJustificante just, Fraccion fracc){
        
        if (!usuario.isEmpty() && !crn.isEmpty() && just!=null && !(just.getFracciones().size()>0 && fracc==null) ){
            
            Folio folio = DBQueries.insertJustificanteClase(usuario, just.getId(), fracc!=null?fracc.getFraccion():"",
                    Utils.formatDate(diaFecha.getSelectedDate()), crn, "");

            if (folio != null){
                Principal.avisar("Justificante creado, folio: "+folio.getFolio(),5000);
                if (DBQueries.aprobarJustificanteFolio(folio.getFolio())){
                    Principal.avisar("Justificante aprobado");
                } else {
                    Principal.avisar("Error aprobando justificante, utilizar menu folios pendientes");
                }

            } else {
                Principal.avisar("Error creando justificante");
            }

            updateTable();
        } else {
            Principal.avisar("Verificar datos de justificante");
        }
        
    }
    
    private void deleteJustificante(Folio f){
        boolean res = DBQueries.deleteJustificanteFolio(f.getFolio());
        Principal.avisar(res?"Justificante eliminado":"Error eliminando justificante");
        updateTable();
    }
    
    private void createFancyCell(){
        final Callback<ListView<TipoJustificante>, ListCell<TipoJustificante>> cbJustif = 
                new Callback<ListView<TipoJustificante>, ListCell<TipoJustificante>>() {
                    
            @Override public ListCell<TipoJustificante> call(ListView<TipoJustificante> p) {
                return new ListCell<TipoJustificante>(){
                    @Override protected void updateItem(TipoJustificante t, boolean bln) {
                        super.updateItem(t, bln);
                        if ( t!=null ){
                            setText(t.getNombre());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
        final Callback<ListView<Fraccion>, ListCell<Fraccion>> cbFracc = new Callback<ListView<Fraccion>, ListCell<Fraccion>>() {
            @Override public ListCell<Fraccion> call(ListView<Fraccion> p) {
                return new ListCell<Fraccion>(){ 
                    @Override protected void updateItem(Fraccion t, boolean bln) {
                        super.updateItem(t, bln);                        
                        if (t!=null){
                            Label l = new Label(t.getCategoria());
                            l.setTooltip(new Tooltip(t.getDescripcion()));                            
                            setGraphic(l);
                        } else {
                            setText(null);
                        }
                    }                    
                };
            }
        };
        justificacion.setCellFactory(new Callback<TableColumn<AsistenciaClase, AsistenciaClase>, TableCell<AsistenciaClase, AsistenciaClase>>() {
            @Override public TableCell<AsistenciaClase, AsistenciaClase> call(TableColumn<AsistenciaClase, AsistenciaClase> p) {
                return new TableCell<AsistenciaClase,AsistenciaClase>(){
                    @Override protected void updateItem(final AsistenciaClase t, boolean bln) {
                        super.updateItem(t, bln);                       
                        if (t!=null && !bln){
                            HBox hbox = new HBox(5);
                            
                            if (t.getJustificante() != null){                                
                                if  (t.getJustificante() instanceof JustificanteAsignatura){                                    
                                    Button btn = new Button("Eliminar"); 
                                    btn.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override public void handle(ActionEvent a) {
                                            deleteJustificante((Folio)t.getJustificante());
                                        }
                                    });
                                    hbox.getChildren().add(btn);
                                }
                            } else if (t.getRegistro()==null){
                                
                                final ComboBox<TipoJustificante> just = new ComboBox<>();
                                just.setCellFactory(cbJustif);
                                just.setButtonCell(just.getCellFactory().call(null));
                                just.setPrefWidth(160);            
                                if (nuevoJustificante.containsKey(t.getTipo()))
                                    just.getItems().addAll(nuevoJustificante.get(t.getTipo()));
                                else {
                                    System.out.println(t.getUsuario()+","+t.getTipo());
                                }
                                
                                final ComboBox<Fraccion> fracc = new ComboBox<>();
                                fracc.setCellFactory(cbFracc);
                                fracc.setButtonCell(fracc.getCellFactory().call(null));
                                fracc.setMaxWidth(50);                                
                                just.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TipoJustificante>() {
                                    @Override public void changed(ObservableValue<? extends TipoJustificante> ov, TipoJustificante t, TipoJustificante t1) {
                                        fracc.getSelectionModel().clearSelection();
                                        fracc.getItems().clear();                                        
                                        if (t1.getFracciones()!=null && t1.getFracciones().size()>0){
                                            fracc.getItems().addAll(t1.getFracciones());  
                                            fracc.setDisable(false);
                                        } else {
                                            fracc.setDisable(true);
                                        }
                                    }
                                });                                                               
                                
                                Button btn = new Button("Agregar");
                                btn.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override public void handle(ActionEvent a) {
                                        createJustificante(
                                                t.getUsuario(), 
                                                t.getCrn(),
                                                just.getSelectionModel().getSelectedItem(), 
                                                fracc.getSelectionModel().getSelectedItem());
                                    }
                                });

                                hbox.getChildren().addAll(just,fracc,btn);
                                
                            }
                            setGraphic(hbox);
                        } else {
                            setGraphic(null);
                        }
                    }                    
                };
            }
        });
        
        status.setCellFactory(new Callback<TableColumn<AsistenciaClase, AsistenciaClase>, TableCell<AsistenciaClase, AsistenciaClase>>() {
            @Override public TableCell<AsistenciaClase, AsistenciaClase> call(TableColumn<AsistenciaClase, AsistenciaClase> param) {
                return new TableCell<AsistenciaClase, AsistenciaClase>() {
                    @Override protected void updateItem(final AsistenciaClase item, boolean empty) {
                        super.updateItem(item, empty);                        
                        if (!empty) {    
                            Text t;
                            if (item.getRegistro()!=null){
                                t = new Text(Utils.formatTime(item.getRegistro().getFechahora()));
                                t.setFill(Color.BLACK);
                            } else if (item.getJustificante()!=null){
                                t = new Text(item.getJustificante().getNombrejustificante());
                                t.setFill(Color.ORANGE);
                                if (!(item.getJustificante() instanceof Evento)){
                                    t.setCursor(Cursor.HAND);
                                    t.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                        @Override public void handle(MouseEvent t) {
                                            Principal.showJustificante((Folio)item.getJustificante());
                                        }
                                    });
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
    
    private void inicializarVistaTablas() {
        
        crn.setCellValueFactory(new PropertyValueFactory<AsistenciaClase, String>("crn"));
        profesor.setCellValueFactory(new PropertyValueFactory<AsistenciaClase, String>("nombre"));
        materia.setCellValueFactory(new PropertyValueFactory<AsistenciaClase, String>("materia"));
        horario.setCellValueFactory(new PropertyValueFactory<AsistenciaClase, String>("horario"));
        
        Callback<TableColumn.CellDataFeatures<AsistenciaClase, AsistenciaClase>, ObservableValue<AsistenciaClase>> cb = 
                new Callback<TableColumn.CellDataFeatures<AsistenciaClase, AsistenciaClase>, ObservableValue<AsistenciaClase>>() {
            @Override public ObservableValue<AsistenciaClase> call(TableColumn.CellDataFeatures<AsistenciaClase, AsistenciaClase> p) {
                return new SimpleObjectProperty<>(p.getValue());
            }
        };
        status.setCellValueFactory(cb);
        justificacion.setCellValueFactory(cb);
        
        crn.prefWidthProperty().bind(clasesTable.widthProperty().multiply(3/48f));
        profesor.prefWidthProperty().bind(clasesTable.widthProperty().multiply(3/12f));
        materia.prefWidthProperty().bind(clasesTable.widthProperty().multiply(3/12f));
        horario.prefWidthProperty().bind(clasesTable.widthProperty().multiply(1/12f));
        status.prefWidthProperty().bind(clasesTable.widthProperty().multiply(1/12f));
        justificacion.prefWidthProperty().bind(clasesTable.widthProperty().multiply(15/48f).subtract(5));
                
    }
    
    private void createDatePickers() {
        diaFecha = Utils.newDatePicker("Seleccionar fecha");
        topPanel.getChildren().add(1,diaFecha);
        diaFecha.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue <? extends Date> ov, Date t, Date t1) {                
                updateTable(t1 != null);                
            }
        });
    }
}
