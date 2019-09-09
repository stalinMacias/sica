package sicaw.gui.pages.asistencia;

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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.asistencias.SemanaAsistencia;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.ReportesUsuarios;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;
import sicaw.gui.Principal;
import sicaweb.Utils;
import sicaweb.reportes.pdf.ReporteUsuarioPeriodo;

public class PorPeriodoController implements Initializable {

    private DatePicker desdeFecha, hastaFecha;
    private Service<ObservableList<SemanaAsistencia>> getService;
    private Usuario currentUser;
    
    @FXML private SplitPane split;
    @FXML private VBox calendarios;
    @FXML private HBox desdeFechaPanel, hastaFechaPanel;
    @FXML private Button imprimirBtn;
    @FXML private ComboBox <TipoUsuario> tipoUsuarios2;
    
    @FXML private TableView <Usuario> tablaUsuarios;
    @FXML private TableColumn <Usuario, String> codigoColUs; 
    @FXML private TableColumn <Usuario, String> nombreColUs;

    @FXML private ProgressIndicator progress;
    @FXML private TableView <SemanaAsistencia> tablaPeriodo;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> domingoColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> lunesColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> martesColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> miercolesColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> juevesColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> viernesColPer;
    @FXML private TableColumn <SemanaAsistencia, SemanaAsistencia> sabadoColPer;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tipoUsuarios2.getItems().clear();
        
        for (TipoUsuario t : DBQueries.getTipoUsuarios()){
            if (!t.getJornada().equals("sinjornada")){
                tipoUsuarios2.getItems().add(t); 
                if (t.getDescripcion().equals(Autenticator.getCurrentUser().getTipo())){
                    tipoUsuarios2.getSelectionModel().select(t);
                }
            }           
        }
        if (tipoUsuarios2.getSelectionModel().isEmpty()){
            tipoUsuarios2.getSelectionModel().selectFirst();
        }
            
        
        inicializarVistaTablas();                         
        createDatePickers();        
        createFancyCells();             
        
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        hastaFecha.setSelectedDate(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -30);
        desdeFecha.setSelectedDate(c.getTime());
                
        getService = new Service<ObservableList<SemanaAsistencia>>() {
            @Override protected Task<ObservableList<SemanaAsistencia>> createTask() {
                return ReportesAsistencias.getAsistenciaUsuarioPeriodoFull(
                        desdeFecha.getCalendarView().getCalendar().getTime(), 
                        hastaFecha.getCalendarView().getCalendar().getTime(), 
                        currentUser
                );
            }
        };
        tablaPeriodo.itemsProperty().bind(getService.valueProperty());
        progress.visibleProperty().bind(getService.runningProperty());
        progress.progressProperty().bind(getService.progressProperty());
                
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.USUARIO){
            split.getItems().remove(0);
            calendarios.getChildren().remove(0);            
            updateTablaPeriodo(Autenticator.getCurrentUser());
            
        } else {
            updateUsuarios();
        }
        
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){
            imprimirBtn.setDisable(true);
        }
        
        createListeners();
        
    }    
     
    private void updateUsuarios(){        
        tablaUsuarios.setItems(
                ReportesUsuarios.getUsuariosPorTipo(
                        tipoUsuarios2.getValue(), 
                        Autenticator.getCurrentUser()
                )
        );          
    }
    
    private void updateTablaPeriodo(boolean start){
        if (getService.isRunning()) getService.cancel();
        getService.reset();
        if (start) getService.start();
    }
    
    private void updateTablaPeriodo(final Usuario usr){
        currentUser = usr;
        if (usr == null){
            updateTablaPeriodo(false);
            return;
        } 
        
        if ( desdeFecha.getSelectedDate() != null
                && hastaFecha.getSelectedDate() != null
                && desdeFecha.getSelectedDate().before(hastaFecha.getSelectedDate()) 
                && hastaFecha.getSelectedDate().compareTo(Calendar.getInstance().getTime()) < 0){
            
            updateTablaPeriodo(true);
            
        } else {
            updateTablaPeriodo(false);
            Principal.avisar("Error con las fechas seleccinadas");
        }
    }
     
    @FXML protected void printPeriodo(){                
        if (currentUser != null 
                && tablaPeriodo.getItems()!=null 
                && desdeFecha.getSelectedDate()!=null 
                && hastaFecha.getSelectedDate()!=null){
            
            Principal.avisar("Creando archivo PDF para impresión");
            
            new Thread(
                    new ReporteUsuarioPeriodo(
                            currentUser, 
                            tablaPeriodo.getItems()
                    )                    
            ).start();
            
        }
    }
    
    private void inicializarVistaTablas() {
        
        tablaUsuarios.prefWidthProperty().bind(split.widthProperty().multiply(0.2f));
       
        codigoColUs.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nombreColUs.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        codigoColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(1/4f));
        nombreColUs.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/4f));
        
        lunesColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        martesColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        miercolesColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        juevesColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        viernesColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        sabadoColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        domingoColPer.setCellValueFactory(new PropertyValueFactory<> ("instance"));
                
    }
    
    private void createListeners() {
        tipoUsuarios2.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends TipoUsuario> ov, TipoUsuario t, TipoUsuario t1) -> {
                if (t1 != null){
                    updateUsuarios();
                    updateTablaPeriodo(null);
                }
        });
        
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends Usuario> observable, Usuario oldValue, Usuario newValue) -> {
                updateTablaPeriodo(newValue);
        });
        
        ChangeListener<Date> ch = (ObservableValue <? extends Date> ov, Date t, Date t1) -> {
            if (tablaUsuarios.getSelectionModel().isEmpty() && tablaPeriodo.getItems()!=null
                    && tablaPeriodo.getItems().size()>0){
                updateTablaPeriodo(null);
            } else {
                updateTablaPeriodo(tablaUsuarios.getSelectionModel().getSelectedItem());
            }
        };
        desdeFecha.selectedDateProperty().addListener(ch);
        hastaFecha.selectedDateProperty().addListener(ch);
    }
    
    private void createDatePickers() {        
        desdeFecha = Utils.newDatePicker("Seleccionar fecha");
        desdeFechaPanel.getChildren().addAll(desdeFecha);        
        
        hastaFecha = Utils.newDatePicker("Seleccionar fecha");
        hastaFechaPanel.getChildren().addAll(hastaFecha);        
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
        
        tipoUsuarios2.setCellFactory(factory);
        tipoUsuarios2.setButtonCell((ListCell<TipoUsuario>) factory.call(null));
                
        domingoColPer.setCellFactory(diaColFactory(Calendar.SUNDAY));
        lunesColPer.setCellFactory(diaColFactory(Calendar.MONDAY));
        martesColPer.setCellFactory(diaColFactory(Calendar.TUESDAY));
        miercolesColPer.setCellFactory(diaColFactory(Calendar.WEDNESDAY));
        juevesColPer.setCellFactory(diaColFactory(Calendar.THURSDAY));
        viernesColPer.setCellFactory(diaColFactory(Calendar.FRIDAY));
        sabadoColPer.setCellFactory(diaColFactory(Calendar.SATURDAY));
       
    }
    
    public Callback<TableColumn<SemanaAsistencia, SemanaAsistencia>, 
                 TableCell<SemanaAsistencia, SemanaAsistencia>> diaColFactory (final Integer dia){
             
         return new Callback<TableColumn<SemanaAsistencia, SemanaAsistencia>, TableCell<SemanaAsistencia, SemanaAsistencia>>() {
            @Override public TableCell<SemanaAsistencia, SemanaAsistencia> call( TableColumn<SemanaAsistencia, SemanaAsistencia> param) {
                return new TableCell<SemanaAsistencia, SemanaAsistencia>() {
                    @Override protected void updateItem(SemanaAsistencia item, boolean empty) {
                        super.updateItem(item,empty);                        
                        if (item!=null && !empty && item.contiene(dia)){                                
                            if (!item.isSpecial()) {
                                VBox vbox = new VBox();
                                AsistenciaUsuario a = item.getDia(dia);
                                
                                Text l1 = (a.getRegistroEntrada() != null)?
                                        TextBuilder.create()
                                    .text("E - "+Utils.formatTime(a.getRegistroEntrada().getFechahora()))
                                    .font(new Font(11))
                                    .build():
                                        TextBuilder.create()
                                    .text("E - No checó")
                                    .font(new Font(11))
                                    .fill(Color.RED)
                                    .build();
                                                                 
                                Text l2 = (a.getRegistroSalida() != null)?
                                        TextBuilder.create()
                                    .text("S - "+Utils.formatTime(a.getRegistroSalida().getFechahora()))
                                    .font(new Font(11))
                                    .build() : 
                                        TextBuilder.create()
                                    .text("S - No checó")
                                    .font(new Font(11))
                                    .fill(Color.RED)                                    
                                    .build();

                                vbox.getChildren().addAll(l1,l2);
                                    
                                if (a.getJustif()!=null && 
                                        (a.getRegistroEntrada() == null || a.getRegistroSalida() == null) ){                                    
                                    
                                    if (a.getRegistroEntrada()==null) l1.setText("J - "+a.getEntrada());
                                    if (a.getRegistroSalida()==null) l2.setText("J - "+a.getSalida());
                                        
                                    Text l3;
                                    if (a.getJustif() instanceof Evento){
                                        if (a.getRegistroEntrada()==null && a.getRegistroSalida()==null)
                                            vbox.getChildren().clear();
                                        
                                        Evento e = (Evento)a.getJustif();
                                        l3 = TextBuilder.create()
                                                .text(e.getNombre())
                                                .font(new Font(12))
                                                .fill(Color.BLUE)
                                                .build();
                                        
                                    } else if (a.getJustif() instanceof JustificantePeriodo){
                                        JustificantePeriodo j = (JustificantePeriodo) a.getJustif();
                                        
                                        l3 = TextBuilder.create()
                                                .text(j.getNombrejustificante())
                                                .font(new Font(12))
                                                .fill(Color.ORANGE)                                               
                                                .build();  
                                        l1.setFill(Color.BLACK);
                                        l2.setFill(Color.BLACK);
                                        setTooltip(new Tooltip("Justificante aprobado por :"+j.getAprobadonombre()));
                                    } else {
                                        l3 = TextBuilder.create().build();
                                    }
                                    vbox.getChildren().add(l3);
                                    
                                } else if (item.debioAsistir(dia) && 
                                        a.getRegistroEntrada() == null && a.getRegistroSalida() == null) {
                                    vbox.getChildren().clear();
                                    l1 = TextBuilder.create()
                                            .text("No asistió")                                            
                                            .font(new Font(12))
                                            .fill(Color.RED)
                                            .build();
                                    vbox.getChildren().add(l1);
                                    
                                } else if (!item.debioAsistir(dia)){
                                    vbox.getChildren().clear();
                                    l1 = TextBuilder.create()
                                            .text("Dia libre")
                                            .font(new Font(12))
                                            .fill(Color.GREY)
                                            .build();
                                    vbox.getChildren().add(l1);
                                }
                                
                                vbox.setSpacing(-1);
                                vbox.setPadding(new Insets(0));
                                
                                HBox hbox = HBoxBuilder.create().children(
                                        TextBuilder.create()
                                                .text(item.getFecha(dia).toString())
                                                .wrappingWidth(20)
                                                .styleClass("label")
                                                .fill(Color.BLUE)
                                                .build(),
                                            vbox)
                                        .spacing(3)
                                        .padding(new Insets(3)) 
                                        .minHeight(45)
                                        .prefHeight(45)                             
                                        .build();                                     
                                
                                if (item.getFecha(dia)%2 == 0){
                                    hbox.setStyle("-fx-background-color: #ebebeb;");
                                } else {
                                    hbox.setStyle("-fx-background-color: white;");
                                }
                                setGraphic(hbox);
                            } else {
                                setGraphic(LabelBuilder.create()
                                            .text(item.getDia(dia).getNombre())
                                            .style("-fx-font-size: 18pt;")
                                            .minHeight(37)
                                            .prefHeight(37) 
                                            .build());
                                setAlignment(Pos.CENTER);
                            }
                            
                        } else {
                            setGraphic(null);
                        }                        
                    }
                };
            }
        };
    }      
                
}
