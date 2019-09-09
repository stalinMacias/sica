package sicaw.gui.pages.administrar.usuarios;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import sica.common.Configs;
import sica.common.DBQueries;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;
import sicaweb.Utils;
import sicaw.gui.Principal;
import sicaweb.reportes.pdf.ReporteRegistros;

public class RegistrosController implements Initializable {

    private DatePicker desdeFecha, hastaFecha;
    private Usuario currentUser;
    private final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
    private Image imgIcono;
    
    @FXML private HBox desdeFechaPanel, hastaFechaPanel;
    
    @FXML private TableView <Registro> tablaRegistros;
    @FXML private TableColumn <Registro, Date> fechaCol;
    @FXML private TableColumn <Registro, Date> horaCol;
    @FXML private TableColumn <Registro, String> tipoRegCol;
    @FXML private TableColumn <Registro, String> modifCol;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        imgIcono = new Image(Configs.SERVER_IMGS()+"foto.png");
        inicializarVistaTablas();
        createDatePickers();
        crearFancyCells();
        
        Calendar c = Calendar.getInstance();
        hastaFecha.setSelectedDate(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -7);
        desdeFecha.setSelectedDate(c.getTime()); 
    }    
 
    public void setUsuario(Usuario usr){
        currentUser = usr;
        tablaRegistros.getItems().clear();
    }
    
    @FXML protected void buscar(){        
       
        Calendar desde = desdeFecha.getCalendarView().getCalendar();
        Calendar hasta = hastaFecha.getCalendarView().getCalendar();        
        
        if ( currentUser != null && desdeFecha.getSelectedDate() != null
                && hastaFecha.getSelectedDate() != null
                && desde.before(hasta) 
                && hasta.compareTo(Calendar.getInstance()) < 0){
                                
            final ObservableList<Registro> rs = DBQueries.registros(
                    currentUser.getCodigo(),
                    desdeFecha.getDateFormat().format(desdeFecha.getSelectedDate()), 
                    hastaFecha.getDateFormat().format(hastaFecha.getSelectedDate()));            
            
            tablaRegistros.setItems(rs);            
            
        } else {
            Principal.avisar("Error con las fechas seleccionadas");
        }       
    
    }
    
    @FXML protected void printRegs(){
        if (currentUser!=null && desdeFecha.getSelectedDate()!=null && hastaFecha.getSelectedDate()!=null ){
            new Thread(new ReporteRegistros(currentUser,tablaRegistros.getItems()
                ,desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() )).start();
        }
    }
    
    private void inicializarVistaTablas() {
        
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fechahora"));
        horaCol.setCellValueFactory(new PropertyValueFactory<>("fechahora"));
        tipoRegCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        modifCol.setCellValueFactory(new PropertyValueFactory<>("modificado"));
        
        fechaCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(3/8f));
        horaCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/4f));
        tipoRegCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/8f));
        modifCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/4f));
        
    }
    
    private void crearFancyCells(){
        final ObservableMap<String,Image> imagenes = FXCollections.observableHashMap();
        
        horaCol.setCellFactory(new Callback<TableColumn<Registro, Date>, TableCell<Registro, Date>>() {
            @Override public TableCell<Registro, Date> call(TableColumn<Registro, Date> param) {
                return new TableCell<Registro, Date>() {
                    @Override protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item!=null) {
                            Label l = new Label(Utils.formatTime(item));
                            
                            HBox h = new HBox();
                            h.setAlignment(Pos.CENTER_LEFT);
                            h.getChildren().add(l);
                                                        
                            if (!tablaRegistros.getItems().get(getIndex()).getTipo().equals("huella")){
                                String foto = Configs.SERVER_CAPTURAS()
                                    +currentUser.getCodigo()+"_"
                                    +tablaRegistros.getItems().get(getIndex()).getFechahoraS()
                                    .replace(":", "-").replace(" ", "_")+".jpg";
                            
                                if (!imagenes.containsKey(foto)){
                                    Image image = new Image(foto);                                
                                    imagenes.put(foto, image.isError()? null : image);
                                }                            

                                if (imagenes.containsKey(foto) && imagenes.get(foto)!=null){
                                    Tooltip tooltip = new Tooltip();                               
                                    tooltip.setGraphic(new ImageView(imagenes.get(foto)));

                                    ImageView img = new ImageView(imgIcono);
                                    img.setFitHeight(25);
                                    img.setFitWidth(25);
                                    
                                    Label ltmp = new Label();
                                    ltmp.setGraphic(img);
                                    ltmp.setTooltip(tooltip);
                                    
                                    h.getChildren().add(ltmp);                                

                                }

                                
                            }

                            this.setGraphic(h);
                        } else {
                            setGraphic(null);
                        }                        
                    }
                };
            }
        });
        
        fechaCol.setCellFactory(new Callback<TableColumn<Registro, Date>, TableCell<Registro, Date>>() {
            @Override
            public TableCell<Registro, Date> call(TableColumn<Registro, Date> param) {
                return new TableCell<Registro, Date>() {
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);                        
                        if (!empty && item!=null) {
                            setText( df.format(item).toUpperCase() );
                            setTooltip(new Tooltip ( Utils.formatDate(item) ) );
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        
    }

    private void createDatePickers() {
        
        desdeFecha = Utils.newDatePicker("Seleccionar fecha");        
        desdeFechaPanel.getChildren().addAll(desdeFecha);
        desdeFecha.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue <? extends Date> ov, Date t, Date t1) {
                tablaRegistros.getItems().clear();                
            }
        });
        
        hastaFecha = Utils.newDatePicker("Seleccionar fecha");
        hastaFechaPanel.getChildren().addAll(hastaFecha);
        hastaFecha.selectedDateProperty().addListener(new ChangeListener<Date>(){
            @Override
            public void changed (ObservableValue <? extends Date> ov, Date t, Date t1) {              
                tablaRegistros.getItems().clear();                
            }
        });
    }
    
}
