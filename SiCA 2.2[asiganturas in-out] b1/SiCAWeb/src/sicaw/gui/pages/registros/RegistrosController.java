package sicaw.gui.pages.registros;

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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
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

    private Usuario currentUser;
    private DatePicker desdeFecha;
    private DatePicker hastaFecha;
    private DatePicker addFecha;
    private final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
    private Image imgIcono;
    
    @FXML private HBox desdeFechaPanel;
    @FXML private HBox hastaFechaPanel;    
    @FXML private SplitPane split1;
    @FXML private Label infoNombre;
    
    @FXML private HBox addFechaPanel;
    @FXML private TextField addHora;
    
    @FXML private TableView <Usuario> tablaUsuarios;
    @FXML private TableColumn <Usuario, String> codigoCol;
    @FXML private TableColumn <Usuario, String> nombreCol;
    @FXML private TableColumn <Usuario, String> tipoCol;
        
    @FXML private TableView <Registro> tablaRegistros;
    @FXML private TableColumn <Registro, Date> fechaCol;
    @FXML private TableColumn horaCol;
    @FXML private TableColumn <Registro, String> imgRegCol;
    @FXML private TableColumn <Registro, String> tipoRegCol;
    @FXML private TableColumn <Registro, String> modifCol;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imgIcono = new Image(Configs.SERVER_IMGS()+"foto.png");
        inicializarVistaTablas();        
        crearListeners();        
        crearDatePickers();        
        loadUsuarios();        
        crearFancyCells();
        
        Calendar c = Calendar.getInstance();
        hastaFecha.setSelectedDate(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -7);
        desdeFecha.setSelectedDate(c.getTime());
        
        infoNombre.setText("Seleccionar usuario");
    }  
    
    private void loadUsuarios(){
        tablaUsuarios.setItems(DBQueries.getTodosUsuarios());       
    }
            
    @FXML protected void printRegs(){
        if (currentUser!=null && desdeFecha.getSelectedDate()!=null && hastaFecha.getSelectedDate()!=null ){
            Principal.avisar("Creando archivo PDF para impresión");
            new Thread(new ReporteRegistros(currentUser,tablaRegistros.getItems()
                ,desdeFecha.getSelectedDate(),hastaFecha.getSelectedDate() )).start();
        }
    }
    
    @FXML protected void addRegistro(){
        
        if (addFecha.getSelectedDate() != null && addHora.getText().matches("[0-2]?[0-9]{1}:[0-9]{2}:[0-9]{2}")
                && currentUser != null ){
            
            String fechahora = Utils.formatDate(addFecha.getSelectedDate()) +" "+addHora.getText();
            
            if (DBQueries.addRegistro(currentUser.getCodigo(), fechahora)){
                addFecha.setSelectedDate(null);
                addHora.clear();
                loadRegistros();
            }
            
        } else {
            Principal.avisar("Error con el usuario, fecha y/u hora");
        }
        
    }
    
    private void loadRegistros(){        
        Calendar desde = desdeFecha.getCalendarView().getCalendar();
        Calendar hasta = hastaFecha.getCalendarView().getCalendar();        
        
        if ( desdeFecha.getSelectedDate() != null
                && hastaFecha.getSelectedDate() != null
                && desde.before(hasta) 
                && hasta.compareTo(Calendar.getInstance()) < 0){
        
            ObservableList<Registro> rs = DBQueries.registros(
                    currentUser.getCodigo(),
                    desdeFecha.getDateFormat().format(desdeFecha.getSelectedDate()), 
                    hastaFecha.getDateFormat().format(hastaFecha.getSelectedDate()));

            tablaRegistros.setItems(rs);                
            
        } else {
            Principal.avisar("Error con las fechas seleccionadas");
        }
        
    }
    
    public void updateRegistro(int line, String txt){
        Registro tmp = tablaRegistros.getItems().get(line);
        
        if (txt.matches("[0-2]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}")){            
            DBQueries.updateRegistro(currentUser.getCodigo(), tmp.getFechahoraS(), txt);                        
        } 
        
        loadRegistros();
    }
    
    @SuppressWarnings("unchecked")
    private void inicializarVistaTablas() {        
        tablaUsuarios.prefWidthProperty().set(split1.widthProperty().multiply(0.4f).doubleValue());
        tablaUsuarios.prefWidthProperty().bind(split1.widthProperty().multiply(0.4f));
                
        codigoCol.setCellValueFactory(new PropertyValueFactory<Usuario, String>("codigo"));
        nombreCol.setCellValueFactory(new PropertyValueFactory<Usuario, String>("nombre"));
        tipoCol.setCellValueFactory(new PropertyValueFactory<Usuario, String>("tipo"));
        codigoCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(2/10f));
        nombreCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(5/10f));
        tipoCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/10f));        
        
        
        fechaCol.setCellValueFactory(new PropertyValueFactory<Registro, Date>("fechahora"));
        horaCol.setCellValueFactory(new PropertyValueFactory<Registro, String>("hora"));
        tipoRegCol.setCellValueFactory(new PropertyValueFactory<Registro, String>("tipo"));
        imgRegCol.setCellValueFactory(new PropertyValueFactory<Registro, String>("tipo"));        
        modifCol.setCellValueFactory(new PropertyValueFactory<Registro, String>("modificado"));
        
        fechaCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(3/8f));
        horaCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/8f));
        imgRegCol.prefWidthProperty().set(30);
        imgRegCol.maxWidthProperty().set(30);
        tipoRegCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/4f));
        modifCol.prefWidthProperty().bind(tablaRegistros.widthProperty().multiply(1/4f).subtract(30));
        
    }

    private void crearListeners() {
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Usuario>() {
                    @Override
                    public void changed(ObservableValue<? extends Usuario> observable,
                        Usuario oldValue, Usuario newValue) {                         
                        if (newValue!=null){                            
                            currentUser = newValue;
                            infoNombre.setText(currentUser.getNombre());
                            loadRegistros();
                        }
                    }
                });
    }
    
    @SuppressWarnings("unchecked")
    private void crearFancyCells(){
        tablaRegistros.setEditable(true);
        horaCol.setEditable(true);
        horaCol.setCellFactory(TextFieldTableCell.forTableColumn());
        horaCol.setOnEditCommit(new EventHandler<CellEditEvent<Registro, String>>() {
            @Override public void handle(CellEditEvent<Registro, String> t) {                  
                updateRegistro(t.getTablePosition().getRow(),t.getNewValue());                 
            }
        });
        
        final ObservableMap<String,Image> imagenes = FXCollections.observableHashMap();
        
        imgRegCol.setCellFactory(new Callback<TableColumn<Registro, String>, TableCell<Registro, String>>() {
            @Override public TableCell<Registro, String> call(TableColumn<Registro, String> p) {
                return new TableCell<Registro, String>(){
                    @Override protected void updateItem(String t, boolean bln) {  
                        super.updateItem(t, bln);
                        if (!bln && t!=null && t.equals("teclado")){
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

                                ImageView img = ImageViewBuilder.create()
                                        .image(imgIcono)
                                        .fitHeight(25)
                                        .fitWidth(25)                                        
                                        .build();          

                                setGraphic(LabelBuilder.create().graphic(img).tooltip(tooltip).build());                                
                            }   
                            
                        } else {
                            setGraphic(null);
                        } 
                        
                    }
                                     
                };
            }
        });
        
        fechaCol.setCellFactory(new Callback<TableColumn<Registro, Date>, TableCell<Registro, Date>>() {
            @Override public TableCell<Registro, Date> call(TableColumn<Registro, Date> param) {
                return new TableCell<Registro, Date>() {
                    @Override protected void updateItem(Date item, boolean empty) {
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
    
    @SuppressWarnings("unchecked")
    private void crearDatePickers() {
        
        desdeFecha = Utils.newDatePicker("Fecha inicial");        
        desdeFechaPanel.getChildren().addAll(desdeFecha);
        desdeFecha.selectedDateProperty().addListener(new ChangeListener(){
            @Override
            public void changed (ObservableValue ov, Object t, Object t1) {
                tablaRegistros.getItems().clear();
                tablaUsuarios.getSelectionModel().clearSelection();
            }
        });
        
        hastaFecha = Utils.newDatePicker("Fecha final");        
        hastaFechaPanel.getChildren().addAll(hastaFecha);
        hastaFecha.selectedDateProperty().addListener(new ChangeListener(){
            @Override
            public void changed (ObservableValue ov, Object t, Object t1) {                
                tablaRegistros.getItems().clear();
                tablaUsuarios.getSelectionModel().clearSelection();
            }
        });
        
        addFecha = Utils.newDatePicker("Seleccionar fecha");
        addFechaPanel.getChildren().add(addFecha);
        
        
    }
}
