package sicaw.gui.pages.faltas;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBGetter;
import sica.common.DBQueries;
import sica.common.faltas.FaltaClase;
import sica.common.faltas.FaltasUsuario;
import sica.common.faltas.ReportesFaltas;
import sica.common.justificantes.Evento;
import sica.common.objetos.Departamento;
import sica.common.usuarios.Privilegios;
import sicaweb.Utils;
import sicaw.gui.Principal;
import sicaweb.reportes.pdf.ReporteFaltasAsignatura;
import sicaweb.reportes.xml.ExportFaltasAsignatura;

public class AsignaturasController implements Initializable {

    private DatePicker desdeFecha, hastaFecha;
    private Service <ObservableList<FaltasUsuario>> service;
    
    
    @FXML private HBox topPanel;
    @FXML private ComboBox<Departamento> departamento;
    @FXML private Button exportarBtn, imprimirBtn;
    
    @FXML private ProgressIndicator progress;
    @FXML private TableView <FaltasUsuario> tabla;
    @FXML private TableColumn <FaltasUsuario,String> codigoProf;
    @FXML private TableColumn <FaltasUsuario,String> profesor;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaClase>> faltas;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaClase>> detalle;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaClase>> checados;
    @FXML private TableColumn <FaltasUsuario,ObservableList<FaltaClase>> tolerancias;    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        createService();
        inicializarVistaTablas();        
        createFancyCells();                
        createDatePickers();
        
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        hastaFecha.setSelectedDate(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -31);
        desdeFecha.setSelectedDate(c.getTime());   
        
        departamento.getItems().clear();
        ObservableList<Departamento> departamentos = DBQueries.getDepartamentos();
        for (Departamento d : departamentos){
            if ( d.getCodigo().matches("[A-Z]*")&& !d.getCodigo().equals("NA")){
                departamento.getItems().add(d);
            }
        }
        departamento.getSelectionModel().selectFirst();        
        
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.USUARIO){            
            desdeFecha.setDisable(true);
            hastaFecha.setDisable(true);
        }
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){            
            exportarBtn.setDisable(true);
            imprimirBtn.setDisable(true);
        }
              
        createListeners();
    }    
    private void createService(){
        
        service = new Service<ObservableList<FaltasUsuario>>() {
            @Override protected Task<ObservableList<FaltasUsuario>> createTask() {
                return ReportesFaltas.getFaltasAsignaturasDepartamentoPeriodo(
                            desdeFecha.getSelectedDate(), 
                            hastaFecha.getSelectedDate(), 
                            departamento.getValue(), 
                            Autenticator.getCurrentUser()
                    );
            }
        };
        service.stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) -> {
            switch (t1){
                case CANCELLED: case FAILED: case SUCCEEDED:
                    if (service.valueProperty().get() != null && service.valueProperty().get().size()>0){
                        tabla.getSortOrder().clear();
                        profesor.setSortType(TableColumn.SortType.ASCENDING);
                        tabla.getSortOrder().add(profesor);
                    }
                    break;
            }
        });
        
        tabla.itemsProperty().bind(service.valueProperty());
        progress.visibleProperty().bind(service.runningProperty());
        progress.progressProperty().bind(service.progressProperty());
        exportarBtn.visibleProperty().bind(service.valueProperty().isNotNull());
        imprimirBtn.visibleProperty().bind(service.valueProperty().isNotNull()); 
    }
    
    private void runService(boolean start){
        if (service.isRunning()) service.cancel();
        if (service.getState() != Worker.State.READY) service.reset();
        if (start) service.start();
    }
    
    @FXML public void loadFaltas(){        
        if (desdeFecha.getSelectedDate()!=null && hastaFecha.getSelectedDate()!=null &&
                desdeFecha.getSelectedDate().before(hastaFecha.getSelectedDate())){
            runService(true);            

        } else {
            runService(false);
            Principal.avisar("Error con las fechas seleccionadas");
        } 
    }
    
    @FXML protected void exportTable(){
        if (!tabla.getItems().isEmpty()){
            Principal.avisar("Exportando información a hoja de cálculo");
            ExportFaltasAsignatura export = new ExportFaltasAsignatura(
                    tabla.getItems(),departamento.getSelectionModel().getSelectedItem(),
                    desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() );
            
            new Thread(export).start();
        }
    }
    
    @FXML protected void printTable(){
        if (!tabla.getItems().isEmpty()){
            Principal.avisar("Creando archivo PDF para impresión");
            ReporteFaltasAsignatura reporte = new ReporteFaltasAsignatura(
                    tabla.getItems(),departamento.getSelectionModel().getSelectedItem(),
                    desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() );                    
            
            new Thread(reporte).start();
        }
    }
    
    private void createFancyCells() {
        
        faltas.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaClase>>, TableCell<FaltasUsuario,ObservableList<FaltaClase>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaClase>> call(TableColumn<FaltasUsuario, ObservableList<FaltaClase>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaClase>>(){
                    @Override protected void updateItem(ObservableList<FaltaClase> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (item!=null && !empty){                 
                            int faltas = 0;
                            for (FaltaClase f: item){
                                if (f.getJustifcante()==null)
                                        faltas++;
                            }
                            setText(faltas+"");
                        } else {
                            setText(null);
                        }
                    }
                };
            }
                    
        });
        
       
        detalle.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaClase>>, TableCell<FaltasUsuario,ObservableList<FaltaClase>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaClase>> call(TableColumn<FaltasUsuario, ObservableList<FaltaClase>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaClase>>(){
                    @Override protected void updateItem(ObservableList<FaltaClase> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (item!=null && !empty){
                            VBox vbox = new VBox();
                            vbox.setAlignment(Pos.TOP_LEFT);                            
                            vbox.setPadding(new Insets(5));
                            vbox.setSpacing(5);
                            
                            int i = 1;
                            HBox hbox = new HBox();
                            hbox.setSpacing(5);
                            
                            for (FaltaClase f: item){
                                if ((f.getJustifcante()==null)||
                                        (f.getJustifcante()!=null && !(f.getJustifcante() instanceof Evento))){
                                    Label l = new Label(Utils.formatDate(f.getFecha())+": ");
                                    l.setTooltip(new Tooltip(Utils.formatDate(f.getFecha())));
                                  

                                    Text t = new Text(f.getCrn().getCrn()+ 
                                            ", "+f.getCrn().getMateria()                                        
                                            + (f.getJustifcante()!=null? " (Justificada)":
                                               ", "+f.getDia()+",  "+f.getHorario().substring(0, 5))
                                    );

                                    hbox.getChildren().addAll(l,t);

                                    if (i%2 == 0){
                                        vbox.getChildren().add(hbox);
                                        hbox = new HBox();
                                        hbox.setSpacing(5);
                                    }
                                    i++;
                                }
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
        checados.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaClase>>, TableCell<FaltasUsuario,ObservableList<FaltaClase>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaClase>> call(TableColumn<FaltasUsuario, ObservableList<FaltaClase>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaClase>>(){
                    @Override protected void updateItem(ObservableList<FaltaClase> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (item!=null && !empty){                 
                      //DBQueries.get_checking_regfull(inicio, fin, depa, usuario);
                        int checado =  0;
                            do {
                                for (Iterator<FaltaClase> iterator = item.iterator(); iterator.hasNext();) {
                                    FaltaClase next = iterator.next();
                                    if (empty = checados.equals(next)) {
                                        //ListCell(DBQueries.get_checking_regfull(inicio, fin, depa, usuario));
                                        double USE_COMPUTED_SIZE1 = ListCell.USE_COMPUTED_SIZE;
//                                        DBQueries _checking_regfull = DBQueries.get_checking_regfull(inicio, fin, usuario);
                                        Bindings.size(item);
                                        /*
                                        aqui seria ya el metodo de filtrado
                                        unas ves arreglando el problema de la consulta
                                        la parte de obtencion de datos y llenado esta arriba aqui solo es 
                                        FILTRADO
                                        */
                                    }
                                }
                            } while (desdeFecha == hastaFecha);
                                              
                        } else {
                            setText("hola mundo");
                        }
                    }
                };
            }
                    
        });
        
       tolerancias.setCellFactory(new Callback<TableColumn<FaltasUsuario,ObservableList<FaltaClase>>, TableCell<FaltasUsuario,ObservableList<FaltaClase>>>(){
            @Override public TableCell<FaltasUsuario, ObservableList<FaltaClase>> call(TableColumn<FaltasUsuario, ObservableList<FaltaClase>> p) {
                return new TableCell<FaltasUsuario,ObservableList<FaltaClase>>(){
                    @Override protected void updateItem(ObservableList<FaltaClase> item, boolean empty) {
                        super.updateItem(item,empty);
                        if (item!=null && !empty){                 
                            checados.cellValueFactoryProperty();
                            /*
                            llenado de datos hecho con lo de arriba aqui solo se calcula la diferencia entre 20 minutos antes y 20 despues haciendo
                            referencia a las otras partes del petododo de checados
                            */
                        } else {
                            setText("hola mundo");
                        }
                    }
                };
            }
                    
        });
        
        departamento.setCellFactory(new Callback<ListView<Departamento>,ListCell<Departamento>>(){            
            @Override public ListCell<Departamento> call(ListView<Departamento> p) {
                return new ListCell<Departamento>(){
                    @Override public void updateItem(Departamento d, boolean empty) {
                        super.updateItem(d,empty);                        
                        if (!empty){
                            setText(d.getNombre());
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
        codigoProf.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        profesor.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        faltas.setCellValueFactory(new PropertyValueFactory <>("faltas"));
        detalle.setCellValueFactory(new PropertyValueFactory <>("faltas"));
        checados.setCellValueFactory(new PropertyValueFactory <> ("checados"));
        tolerancias.setCellValueFactory(new PropertyValueFactory <> ("checados"));
        
        codigoProf.prefWidthProperty().bind(tabla.widthProperty().multiply(1/20f).subtract(3));
        profesor.prefWidthProperty().bind(tabla.widthProperty().multiply(4/20f).subtract(3));
        faltas.prefWidthProperty().bind(tabla.widthProperty().multiply(1/20f).subtract(3));
        detalle.prefWidthProperty().bind(tabla.widthProperty().multiply(14/20f).subtract(3));
        checados.prefWidthProperty().bind(tabla.widthProperty().multiply(4/20f).subtract(3));        
        tolerancias.prefWidthProperty().bind(tabla.widthProperty().multiply(4/20f).subtract(3));
    }
    
    private void createDatePickers() {
        desdeFecha = Utils.newDatePicker("Seleccionar fecha");        
        topPanel.getChildren().add(1,desdeFecha);
        
        hastaFecha = Utils.newDatePicker("Seleccionar fecha");
        topPanel.getChildren().add(3,hastaFecha);        
    }
    
    public void createListeners(){
        ChangeListener<Date> chl = (ObservableValue <? extends Date> ov, Date t, Date t1) -> {
            runService(false);
        };
    }
}