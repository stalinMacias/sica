package sicaw.gui.pages.calendario;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import sica.common.DBQueries;
import sica.common.justificantes.Evento;
import sica.common.justificantes.TipoEvento;
import sicaw.gui.Principal;
import sicaweb.Utils;

public class CalendarioController implements Initializable {

    private DatePicker calendar;
    private DatePicker desdeFecha;
    private DatePicker hastaFecha;
    
    @FXML private SplitPane split1;
    @FXML private VBox vbox;
    @FXML private ChoiceBox <Integer> anioBox;
    
    @FXML private TableView <Evento> tablaEventos;
    @FXML private TableColumn <Evento, String> colorCol;
    @FXML private TableColumn <Evento, String> tipoCol;
    @FXML private TableColumn <Evento, String> eventoCol;
    @FXML private TableColumn <Evento, Date> desdeCol;
    @FXML private TableColumn <Evento, Date> hastaCol;
    @FXML private TableColumn <Evento, String> elimCol;  
    
    @FXML private ComboBox <TipoEvento> eventoNew;
    @FXML private Button anadirNew;
    @FXML private HBox fechasPanel;
    @FXML private CheckBox soloAsignaturas;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {            
        inicializarVistaTablas();        
        crearDatePickers();        
        crearCalendario();        
        createCellFactories();
        populateBoxes();        
        createListener();              
        loadEventos();        
        updateView();
    }
    
    private void loadEventos(){
        tablaEventos.setItems(DBQueries.getEventosTodos(
                anioBox.getSelectionModel().getSelectedItem()));                     
    }
    
    @FXML protected void anadirEvento(){
        if (!eventoNew.getSelectionModel().isEmpty()
                && desdeFecha.getSelectedDate() != null
                && hastaFecha.getSelectedDate() != null){
            
            String evento = eventoNew.getSelectionModel().getSelectedItem().getTipo();
            
            boolean res = DBQueries.insertEvento(
                    desdeFecha.getDateFormat().format(desdeFecha.getSelectedDate()),
                    hastaFecha.getDateFormat().format(hastaFecha.getSelectedDate()),
                    evento,
                    soloAsignaturas.isSelected());
            
            if (res){                
                desdeFecha.setSelectedDate(null);
                hastaFecha.setSelectedDate(null);
                eventoNew.getSelectionModel().clearSelection();
                
                loadEventos();
                updateView();
            }
        }
    }
    
    private void deleteEvento(int linea){
        Evento ev = tablaEventos.getItems().get(linea);
        
        boolean res = DBQueries.deleteEvento(
                ev.getTipo(),
                desdeFecha.getDateFormat().format(ev.getInicio()),
                desdeFecha.getDateFormat().format(ev.getFin()),
                ev.isAsignatura());
        
        if (res) {                
            loadEventos();
            updateView();
        }
        
    }
    
    private String getColor(int dia){
        
        int mes = calendar.getCalendarView().getCalendar().get(Calendar.MONTH);
        int año = calendar.getCalendarView().getCalendar().get(Calendar.YEAR);
        
        Calendar actual = Calendar.getInstance();
        actual.set(año, mes, dia, 12, 0);
        
        for (Evento ev : tablaEventos.getItems()){         
            Calendar inicioEv = Calendar.getInstance();
            Calendar finEv = Calendar.getInstance();
            
            inicioEv.setTime(ev.getInicio());            
            inicioEv.set(Calendar.HOUR_OF_DAY,0);
            
            finEv.setTime(ev.getFin());               
            finEv.set(Calendar.HOUR_OF_DAY,23);
            
            
            if (  actual.compareTo(inicioEv) >=0 && actual.compareTo(finEv)<=0 ) {                
                    return "rgba("+ev.getColor()+",0.5) ";                    
                
            } 
        }
        return "null";
        
    }
    
    private void createListener(){    
        Pane p1 = (Pane)calendar.getCalendarView().getChildren().get(0);
        
        for (Node t : p1.getChildren()){                
            if (t instanceof Button){                
                Button b = (Button)t;                
                b.addEventHandler(MouseEvent.MOUSE_CLICKED,new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                            updateView();
                        }
                    });
                
            } else if (t instanceof HBox){
                HBox hbox = (HBox)t;
                for (Node n : hbox.getChildren()){
                    if (n instanceof Button){                
                        Button b = (Button)n;  
                        b.setOnAction(null);
                        b.setCursor(Cursor.NONE);
                    }
                }
            }            
        }
        
        anioBox.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener <Integer>(){
                    @Override
                    public void changed(ObservableValue<? extends Integer> ov, Integer t, Integer t1) {
                        loadEventos();
                        updateView();
                    }                    
                } );     
        
        
        soloAsignaturas.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                Principal.avisar(t1? "Solo se justificaran faltas a asignaturas":
                        "Se justificará faltas en jornada laboral y asignaturas");
            }
        });
        
    }
    
    private void updateView(){        
        Pane p1 = (Pane)calendar.getCalendarView().getChildren().get(1);        
        Pane p3 = (Pane)p1.getChildren().get(0);        
        Pane p4 = (Pane)p3.getChildren().get(1);
            
        for (Node t4 : p4.getChildren()){ 
            if (t4 instanceof Button){                
                Button b = (Button)t4;
                b.setOnAction(null);
                b.setCursor(Cursor.NONE);
                for (String s : b.getStyleClass()){
                    if (s.equals("calendar-cell-current-month")){                        
                        b.setStyle("-fx-background-color: "
                                +getColor(Integer.parseInt(b.getText()))+" ; ");                         
                    } else {
                        b.setStyle("-fx-background-color: null; ");                        
                    }
                    
                    if (s.equals("calendar-cell-today")){
                        b.setStyle("-fx-background-color: -fx-accent;");
                    }
                }
            }
        }
    }
    
    private void inicializarVistaTablas() {        
        vbox.prefWidthProperty().bind(split1.widthProperty().multiply(0.4f));
        
        colorCol.setCellValueFactory(new PropertyValueFactory<Evento, String>("color"));
        eventoCol.setCellValueFactory(new PropertyValueFactory<Evento, String>("nombre"));
        desdeCol.setCellValueFactory(new PropertyValueFactory<Evento, Date>("inicio"));
        hastaCol.setCellValueFactory(new PropertyValueFactory<Evento, Date>("fin"));
        elimCol.setCellValueFactory(new PropertyValueFactory<Evento, String>("tipo"));
        tipoCol.setCellValueFactory(new PropertyValueFactory<Evento, String>("asignaturas"));
        
        colorCol.prefWidthProperty().set(30);
        eventoCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(3/10f).subtract(30));
        tipoCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(2/10f));
        desdeCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(3/20f));
        hastaCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(3/20f));
        elimCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(2/10f));
        
        anadirNew.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(2/10f));
        
    }
    
    private void crearDatePickers() {        
        desdeFecha = Utils.newDatePicker("Fecha inicial");        
        hastaFecha = Utils.newDatePicker("Fecha final");        
        fechasPanel.getChildren().addAll(desdeFecha, hastaFecha);                
    }

    private void crearCalendario() {        
        calendar = Utils.newDatePicker(null);
        calendar.getCalendarView().showTodayButtonProperty().set(false);
        
        vbox.getChildren().addAll(calendar.getCalendarView());
        VBox.setMargin(vbox, new Insets(20));
        
        DoubleBinding zoom = vbox.widthProperty()
                .divide(calendar.getCalendarView().widthProperty())
                .subtract(.2);
        
        calendar.getCalendarView().scaleYProperty().bind(zoom);
        calendar.getCalendarView().scaleXProperty().bind(zoom);        
        
    }
    
    private void createCellFactories() {  
        
        Callback <TableColumn<Evento, Date>, TableCell<Evento, Date>> cb 
          = new Callback<TableColumn<Evento, Date>, TableCell<Evento, Date>>() {
            @Override
            public TableCell<Evento, Date> call(TableColumn<Evento, Date> p) {
                return new TableCell<Evento, Date>(){
                    @Override
                    protected void updateItem(Date t, boolean bln) {
                        super.updateItem(t, bln);    
                        if (!bln && t!=null){
                            setText(Utils.formatDate(t));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
        desdeCol.setCellFactory(cb);
        hastaCol.setCellFactory(cb);
        
        elimCol.setCellFactory(new Callback<TableColumn<Evento,String>,TableCell<Evento,String>>(){        
                @Override public TableCell<Evento, String> call(final TableColumn<Evento, String> param) {   
                    return new TableCell<Evento, String>(){
                        @Override public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item!=null){               
                                Button btn = new Button("ELIMINAR");
                                    btn.setCursor(Cursor.HAND);
                                    btn.setScaleX(0.85);
                                    btn.setScaleY(0.85);
                                    btn.setOnAction(actionBtn(this.getIndex()));                                     
                                                                
                                setGraphic(btn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    };          
                }	
        });
    
        colorCol.setCellFactory(new Callback<TableColumn<Evento,String>,TableCell<Evento,String>>(){        
                @Override public TableCell<Evento, String> call(final TableColumn<Evento, String> param) {   
                    return new TableCell<Evento, String>(){
                        @Override public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item!=null && !empty){               
                                StringTokenizer st = new StringTokenizer(item,",",false);
                                Color c = Color.rgb(
                                        Integer.parseInt(st.nextToken()),
                                        Integer.parseInt(st.nextToken()),
                                        Integer.parseInt(st.nextToken()));
                                
                                Rectangle re = new Rectangle();
                                    re.setFill(c);
                                    re.setWidth(30);
                                    re.setHeight(30);
                                    re.setArcHeight(5);
                                    re.setArcWidth(5);
                                    re.setOpacity(0.7);                                    
                                
                                setGraphic(re);
                            } else {
                                setGraphic(null);
                            }
                        }
                    };          
                }	
        });
        tipoCol.setCellFactory(new Callback<TableColumn<Evento, String>, TableCell<Evento, String>>() {
            @Override public TableCell<Evento, String> call(TableColumn<Evento, String> p) {
                return new TableCell<Evento,String>(){
                    @Override protected void updateItem(String t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t!=null & !bln){
                            if (t.equals("1")){
                                setText("Solo asignaturas");
                            } else {
                                setText("Jornada y asignaturas");
                            }
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        eventoNew.setCellFactory(new Callback<ListView<TipoEvento>, ListCell<TipoEvento>>() {
            @Override public ListCell<TipoEvento> call(ListView<TipoEvento> p) {
                return new ListCell<TipoEvento>(){
                    @Override protected void updateItem(TipoEvento t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t!=null && !bln){
                            setText(t.getNombre());
                        } else {
                            setText(null);
                        }
                    }                     
                };
            }
        });
        
    }
    
    private EventHandler actionBtn(final int linea){
        return new EventHandler(){            
            @Override
            public void handle(Event t) {
                deleteEvento(linea);
            }
        };       
    }

    private void populateBoxes() {
        anioBox.getItems().clear();        
        eventoNew.setItems(DBQueries.getTipoEventos());
                    
        Calendar cal = Calendar.getInstance();
        Integer val = cal.get(Calendar.YEAR);
                
        for (Integer i=val-1 ; i<val+2; i++ ){
            anioBox.getItems().add(i);        
        }
        
        anioBox.getSelectionModel().select(val);
        
    }



}
