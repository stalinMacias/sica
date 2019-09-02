package sica.screen.menuviews;

import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.common.Utils;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.asistencias.SemanaAsistencia;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificantePeriodo;

public class AsistenciaJornada extends Screen implements Initializable {

    private Service<ObservableList<SemanaAsistencia>> getJornadaService;
    private Calendar desde;
    private Calendar hasta;
    
    @FXML private Label mesActual;
    @FXML private Label mesAnterior;
    @FXML private Label mesSiguiente;
    @FXML private ProgressIndicator loading;
    
    @FXML private TableView<SemanaAsistencia> tabla;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> domingo;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> lunes;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> martes;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> miercoles;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> jueves;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> viernes;
    @FXML private TableColumn<SemanaAsistencia, SemanaAsistencia> sabado;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        getJornadaService = new Service<ObservableList<SemanaAsistencia>>() {
            @Override protected Task<ObservableList<SemanaAsistencia>> createTask() {                                
                return ReportesAsistencias.getAsistenciaUsuarioPeriodo(
                        desde.getTime(), 
                        hasta.getTime(), 
                        ScreenManager.menu().getUsuario());
            }
        };
        
        loading.visibleProperty().bind(getJornadaService.runningProperty());
        tabla.itemsProperty().bind(getJornadaService.valueProperty());   
        initTabla();        
    }    

    private void initTabla(){        
        domingo.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        lunes.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        martes.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        miercoles.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        jueves.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        viernes.setCellValueFactory(new PropertyValueFactory<> ("instance"));
        sabado.setCellValueFactory(new PropertyValueFactory<> ("instance"));        
        
        domingo.setCellFactory(diaColFactory(Calendar.SUNDAY));
        lunes.setCellFactory(diaColFactory(Calendar.MONDAY));
        martes.setCellFactory(diaColFactory(Calendar.TUESDAY));
        miercoles.setCellFactory(diaColFactory(Calendar.WEDNESDAY));
        jueves.setCellFactory(diaColFactory(Calendar.THURSDAY));
        viernes.setCellFactory(diaColFactory(Calendar.FRIDAY));
        sabado.setCellFactory(diaColFactory(Calendar.SATURDAY));
    }
    
    private void loadTable(){
        if (getJornadaService.isRunning()){
            getJornadaService.cancel();
        }
        getJornadaService.reset();
        getJornadaService.start();
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
        hasta.set( Calendar.DAY_OF_MONTH,
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
    
    private void startJustification(AsistenciaUsuario asistencia){
        ScreenManager.justificar().setDiaJustificando(asistencia);
        ScreenManager.menu().addScreen(Screens.MENU_JUSTIFICAR);
    }
    
    public Callback<TableColumn<SemanaAsistencia, SemanaAsistencia>, 
                 TableCell<SemanaAsistencia, SemanaAsistencia>> diaColFactory (final Integer dia){
             
         return new Callback<TableColumn<SemanaAsistencia, SemanaAsistencia>, TableCell<SemanaAsistencia, SemanaAsistencia>>() {
            @Override public TableCell<SemanaAsistencia, SemanaAsistencia> call( TableColumn<SemanaAsistencia, SemanaAsistencia> param) {
                return new TableCell<SemanaAsistencia, SemanaAsistencia>() {
                    @Override protected void updateItem(SemanaAsistencia item, boolean empty) {
                        super.updateItem(item,empty);                        
                        if (item!=null && !empty && item.contiene(dia)){                                                            
                            VBox vbox = new VBox(5);
                            AsistenciaUsuario a = item.getDia(dia);

                            Text l1 = (a.getRegistroEntrada() != null)?                                    
                                new Text("E - "+Utils.formatTime(a.getRegistroEntrada().getFechahora())):
                                new Text("E - No checó"); 
                            
                            Text l2 = (a.getRegistroSalida() != null)?
                                new Text("S - "+Utils.formatTime(a.getRegistroSalida().getFechahora())):
                                new Text("S - No checó");
                            
                            l1.setFill((a.getRegistroEntrada() != null)? Color.BLACK: Color.RED);                                
                            l2.setFill((a.getRegistroSalida() != null)? Color.BLACK: Color.RED);                                
                            
                            vbox.getChildren().addAll(l1,l2);

                            if (a.getJustif()!=null && 
                                    (a.getRegistroEntrada() == null || a.getRegistroSalida() == null) ){                                    

                                if (a.getRegistroEntrada()==null) l1.setText("J - "+a.getEntrada());
                                if (a.getRegistroSalida()==null) l2.setText("J - "+a.getSalida());

                                
                                Text l3 = new Text(a.getJustif().getNombrejustificante());
                                if (a.getJustif() instanceof Evento){
                                    if (a.getRegistroEntrada()==null && a.getRegistroSalida()==null)
                                        vbox.getChildren().clear();
                                    
                                    l3.setFill(Color.BLUE);

                                } else if (a.getJustif() instanceof JustificantePeriodo){
                                    JustificantePeriodo j = (JustificantePeriodo) a.getJustif();

                                    l3.setFill(Color.ORANGERED) ;                                    
                                    l1.setFill(Color.BLACK);
                                    l2.setFill(Color.BLACK);
                                    setTooltip(new Tooltip("Justificante aprobado por :"+j.getAprobadonombre()));
                                } 
                                vbox.getChildren().add(l3);

                            } else if (item.debioAsistir(dia) && 
                                    a.getRegistroEntrada() == null && a.getRegistroSalida() == null) {
                                vbox.getChildren().clear();
                                l1.setText("No asistió");
                                l1.setFill(Color.RED);
                                vbox.getChildren().add(l1);

                            } else if (!item.debioAsistir(dia)){
                                vbox.getChildren().clear();
                                l1.setText("Dia libre");
                                l1.setFill(Color.GREY);
                                vbox.getChildren().add(l1);
                            }
                            
                            if (item.debioAsistir(dia) && a.getJustif()==null && 
                                    (a.getRegistroEntrada() == null || a.getRegistroSalida() == null)){   
                                setCursor(Cursor.HAND);
                                setOnMouseClicked((MouseEvent event) -> {
                                    startJustification(a);
                                });
                            } else {
                                setCursor(Cursor.DEFAULT);                                
                                setOnMouseClicked(null);
                            }
                            
                            Text diaT = new Text(item.getFecha(dia).toString());
                            diaT.setStyle("-fx-font-size : 20;");
                            diaT.setFill(Color.BLUE);
                            
                            HBox hbox = new HBox(5);
                            hbox.getChildren().addAll(diaT,vbox);                          
                            hbox.minHeightProperty().bind(tabla.heightProperty().divide(9));
                            setGraphic(hbox);

                        } else {
                            setGraphic(null);
                        }                        
                    }
                };
            }
        };
    }      
                 
                 
}
