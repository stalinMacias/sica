package sicaw.gui.pages.justificacion;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
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
import sica.common.DBQueries;
import sica.common.justificantes.Folio;
import sica.common.justificantes.Fraccion;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.justificantes.ReportesJustificantes;
import sica.common.justificantes.TipoJustificante;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;
import sicaw.gui.Principal;
import sicaweb.Utils;

public class TCController implements Initializable {

    private DatePicker desdeFecha;
    private DatePicker hastaFecha;
    private ObservableMap<String, ObservableList<TipoJustificante>> nuevoJustificante;
        
    @FXML private SplitPane split1;
    @FXML private HBox fechasPanel;
    @FXML private Label title;
    
    @FXML private TableView <Usuario> tablaUsuarios;
    @FXML private TableColumn <Usuario, String> codigoCol;
    @FXML private TableColumn <Usuario, String> nombreCol;
    @FXML private TableColumn <Usuario, String> tipoCol;
    
    @FXML private TableView <JustificantePeriodo> tablaJustificantes;
    @FXML private TableColumn <JustificantePeriodo, JustificantePeriodo> folioCol;
    @FXML private TableColumn <JustificantePeriodo, String> justCol;
    @FXML private TableColumn <JustificantePeriodo, String> desdeCol;
    @FXML private TableColumn <JustificantePeriodo, String> hastaCol;
    @FXML private TableColumn <JustificantePeriodo, JustificantePeriodo> estatusCol;    
    @FXML private TableColumn <JustificantePeriodo, String> elimCol;    
    
    @FXML private HBox addPanel;
    @FXML private ComboBox <TipoJustificante> justNew;
    @FXML private ComboBox <Fraccion> fraccNew;
    @FXML private Button anadirNew;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        inicializarVistaTablas();     
        createFancyCells();        
        crearDatePickers();        
        loadUsuarios();        
        
        anadirNew.setDisable(true);
        
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR)
            addPanel.setVisible(false);
                   
        nuevoJustificante = ReportesJustificantes.getListaJustificantes();
        crearListeners(); 
                
    }    
    
    private void loadUsuarios(){
        tablaUsuarios.setItems(DBQueries.getTodosUsuarios());          
    }    
    
    private void loadJustificaciones(String codigo){
        title.setText("Justificantes de "+codigo);        
        anadirNew.setDisable(false);            
        tablaJustificantes.setItems(DBQueries.getJustificantesPeriodoTodosUsuario(codigo));        
    }
    
    @FXML protected void anadirJustificante(){
        
        if ( !tablaUsuarios.getSelectionModel().isEmpty() &&
                !justNew.getSelectionModel().isEmpty() && 
                !(justNew.getSelectionModel().getSelectedItem().getFracciones().size()>0 &&                
                 fraccNew.getSelectionModel().isEmpty()) &&
                desdeFecha.getSelectedDate() != null &&
                hastaFecha.getSelectedDate() != null &&
                desdeFecha.getSelectedDate().compareTo(hastaFecha.getSelectedDate()) <=0 ){
        
            Folio folio = DBQueries.insertJustificantePeriodo(
                    tablaUsuarios.getSelectionModel().getSelectedItem().getCodigo(),
                    justNew.getSelectionModel().getSelectedItem().getId(),
                    fraccNew.getSelectionModel().isEmpty()?"":fraccNew.getSelectionModel().getSelectedItem().getFraccion(),
                    desdeFecha.getDateFormat().format(desdeFecha.getSelectedDate()),
                    hastaFecha.getDateFormat().format(hastaFecha.getSelectedDate()),
                    "");
        
                if (folio != null){     
                    Principal.avisar("Justificante creado, folio: "+folio.getFolio(),5000);
                    boolean aprobado = DBQueries.aprobarJustificanteFolio(folio.getFolio());                    
                    justNew.getSelectionModel().clearSelection();
                    desdeFecha.setSelectedDate(null);
                    hastaFecha.setSelectedDate(null);                    
                    loadJustificaciones(tablaUsuarios.getSelectionModel().getSelectedItem().getCodigo());                    
                    Principal.avisar(aprobado?"Justificante aprobado correctamente"
                            :"Falló aprobación de justificante!\nutilizar menu folios pendientes para aprobar");
                    
                }else {
                    Principal.avisar("Error enviando justificante");
                }                   
        
        } else {
            Principal.avisar("Error con los datos del justificante");
        }
    }
    
    private void deleteJustificante(String folio){        
        boolean res = DBQueries.deleteJustificanteFolio(folio);
        
        if (res){
            Principal.avisar("Justificante eliminado");
            for (int i = 0; i<tablaJustificantes.getItems().size();i++){
                if (tablaJustificantes.getItems().get(i).getFolio().equals(folio)){
                    tablaJustificantes.getItems().remove(i);
                    
                    break;
                }
            }
        } else {
            Principal.avisar("Error eliminando justificante");
        }
        
    }
    
    private void inicializarVistaTablas() {
    
        tablaUsuarios.prefWidthProperty().set(split1.widthProperty().multiply(0.35f).doubleValue());
        tablaUsuarios.prefWidthProperty().bind(split1.widthProperty().multiply(0.35f));
                
        estatusCol.setCellValueFactory(
                (TableColumn.CellDataFeatures<JustificantePeriodo, JustificantePeriodo> p) -> 
                        new SimpleObjectProperty<>(p.getValue()));
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tipoCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        
        codigoCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(2/10f).subtract(3));
        nombreCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(5/10f).subtract(3));
        tipoCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(3/10f).subtract(3));   
        
        folioCol.setCellValueFactory((TableColumn.CellDataFeatures<JustificantePeriodo, JustificantePeriodo> p) -> 
                new SimpleObjectProperty<>(p.getValue()));
        
        justCol.setCellValueFactory(new PropertyValueFactory<>("nombrejustificante"));
        desdeCol.setCellValueFactory(new PropertyValueFactory<>("fecha_inicial"));
        hastaCol.setCellValueFactory(new PropertyValueFactory<>("fecha_final"));        
        elimCol.setCellValueFactory(new PropertyValueFactory<>("folio"));        
        
        folioCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(1/10f).subtract(5));
        justCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(4/10f));
        desdeCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(3/20f));
        hastaCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(3/20f));
        elimCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(1/10f));
        estatusCol.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(1/10f));
        
        justNew.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(3/10f).subtract(40));
        anadirNew.prefWidthProperty().bind(tablaJustificantes.widthProperty().multiply(2/10f));
               
    }
     
    private void crearListeners() {        
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Usuario> observable, Usuario t, Usuario t1) -> {
            if (!justNew.getSelectionModel().isEmpty())
                justNew.getSelectionModel().clearSelection();
            
            if (t1!=null){
                loadJustificaciones(t1.getCodigo());
                justNew.setItems(nuevoJustificante.get(t1.getTipo()));
            } else {
                anadirNew.setDisable(true);
            }
        });        
        justNew.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TipoJustificante> ov, TipoJustificante t, TipoJustificante t1) -> {
            if (!fraccNew.getSelectionModel().isEmpty())
                fraccNew.getSelectionModel().clearSelection();
            
            if (t1 != null && t1.getFracciones() != null && t1.getFracciones().size() > 0 ){
                fraccNew.setItems(t1.getFracciones());
                fraccNew.setDisable(false);
            } else {
                fraccNew.setDisable(true);
            }
        });
        
    }
    
    private void crearDatePickers() {        
        desdeFecha = Utils.newDatePicker("Fecha inicial", (ObservableValue <? extends Date> ov, Date t, Date t1) -> {
            if (t1 != null){
                if (hastaFecha.getSelectedDate() == null){
                    hastaFecha.setSelectedDate(desdeFecha.getSelectedDate());
                } else if ( hastaFecha.getSelectedDate() != null &&
                        desdeFecha.getSelectedDate().compareTo(hastaFecha.getSelectedDate()) > 0){
                    hastaFecha.setSelectedDate(null);
                }
            }
        });   
        
        hastaFecha = Utils.newDatePicker("Fecha final", (ObservableValue <? extends Date> ov, Date t, Date t1) -> {
            if (t1 != null){
                if ( desdeFecha.getSelectedDate() != null &&
                        desdeFecha.getSelectedDate().compareTo(hastaFecha.getSelectedDate()) > 0){
                    
                    desdeFecha.setSelectedDate(null);
                }
            }
        });
        
        fechasPanel.getChildren().addAll(desdeFecha,hastaFecha);
    }
     
    private void createFancyCells() {     
        folioCol.setCellFactory(new Callback<TableColumn<JustificantePeriodo, JustificantePeriodo>, TableCell<JustificantePeriodo, JustificantePeriodo>>() {
            @Override public TableCell<JustificantePeriodo, JustificantePeriodo> call(TableColumn<JustificantePeriodo, JustificantePeriodo> p) {
                return new TableCell<JustificantePeriodo,JustificantePeriodo>(){
                    @Override protected void updateItem(final JustificantePeriodo t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            Hyperlink h = new Hyperlink(t.getFolio());
                            h.setOnAction(e -> Principal.showJustificante(t));
                            setGraphic(h);
                        } else {
                            setGraphic(null);
                        }
                    }
                    
                };
            }
        });
        estatusCol.setCellFactory(new Callback<TableColumn<JustificantePeriodo, JustificantePeriodo>, TableCell<JustificantePeriodo, JustificantePeriodo>>() {
            @Override public TableCell<JustificantePeriodo, JustificantePeriodo> call(TableColumn<JustificantePeriodo, JustificantePeriodo> p) {
                return new TableCell<JustificantePeriodo,JustificantePeriodo>(){
                    @Override protected void updateItem(JustificantePeriodo t, boolean bln) {
                        super.updateItem(t, bln); 
                        if (t!=null && !bln){
                            Text l = new Text(t.isPendiente()? "Pendiente": t.isAprobado()? "Aprobado" : "Rechazado");                        
                            l.setFill(t.isPendiente()? Color.ORANGE: t.isAprobado()? Color.GREEN : Color.RED);
                            if (!t.isPendiente()) {
                                setTooltip(new Tooltip(t.getAprobadonombre()));
                            }
                            setGraphic(l);
                        } else {
                            setGraphic(null);
                        }
                    }
                    
                };
            }
        });
        
        elimCol.setCellFactory(new Callback<TableColumn<JustificantePeriodo,String>,TableCell<JustificantePeriodo,String>>(){        
            @Override public TableCell<JustificantePeriodo, String> call(final TableColumn<JustificantePeriodo, String> param) {   
                return new TableCell<JustificantePeriodo, String>(){
                    @Override public void updateItem(final String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item!=null){                                 
                            Button btn = new Button("Eliminar");
                            btn.setCursor(Cursor.HAND);
                            btn.setScaleX(0.8);
                            btn.setScaleY(0.8);
                            btn.setOnAction(e -> deleteJustificante(item));
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        } 
                    }
                };          
            }	
        });
        
        justNew.setCellFactory(new Callback<ListView<TipoJustificante>, ListCell<TipoJustificante>>() {
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
        });
        justNew.setButtonCell((ListCell<TipoJustificante>)justNew.getCellFactory().call(null));
        
        fraccNew.setCellFactory(new Callback<ListView<Fraccion>, ListCell<Fraccion>>() {
            @Override public ListCell<Fraccion> call(ListView<Fraccion> p) {
                return new ListCell<Fraccion>(){
                    @Override protected void updateItem(Fraccion t, boolean bln) {
                        super.updateItem(t, bln);                        
                        if (t!=null){
                            Label l = new Label(t.getCategoria());
                            l.setTooltip(new Tooltip(t.getDescripcion()));                            
                            setGraphic(l);
                        } else {
                            setGraphic(null);
                        }
                    }                    
                };
            }
        });
        fraccNew.setButtonCell(new ListCell<Fraccion>(){
            @Override protected void updateItem(Fraccion t, boolean bln) {
                super.updateItem(t, bln);
                setText((t!=null)? t.getFraccion(): null);
            }
            
        });
    }
    
}
