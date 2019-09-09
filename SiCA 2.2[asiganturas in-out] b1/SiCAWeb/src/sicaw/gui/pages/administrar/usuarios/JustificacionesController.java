package sicaw.gui.pages.administrar.usuarios;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.DBQueries;
import sica.common.justificantes.JustificanteFolio;
import sica.common.usuarios.Usuario;
import sicaw.gui.Principal;

public class JustificacionesController implements Initializable {

    private Usuario currentUser;
    
    @FXML private ChoiceBox<Integer> cantidad;
    @FXML private TableView <JustificanteFolio> tabla;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> folio;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> tipo;
    @FXML private TableColumn <JustificanteFolio, String> justificante;    
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> estatus;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarVistaTablas();
        createCellFactorys();
        cantidad.getItems().addAll(10,30,50,100,1000);    
        cantidad.getSelectionModel().selectFirst();
    }    
    
    public void setUser(Usuario usr){
        currentUser = usr;
        if (tabla.getItems()!=null)
            tabla.getItems().clear();
    }
    
    @FXML protected void update(){   
        if (currentUser!=null)
            tabla.setItems(DBQueries.getJustificantesUltimosUsuario(currentUser.getCodigo(),cantidad.getValue()));
        
    }
    
    private void inicializarVistaTablas() {          
        
        justificante.setCellValueFactory(new PropertyValueFactory<JustificanteFolio, String>("nombrejustificante"));
        
        Callback<TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio>, ObservableValue<JustificanteFolio>> callback 
            = new Callback<TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio>, ObservableValue<JustificanteFolio>>() {
                @Override public ObservableValue<JustificanteFolio> call(TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio> p) {
                    return new SimpleObjectProperty<>(p.getValue());
                }
        };
        folio.setCellValueFactory(callback);
        tipo.setCellValueFactory(callback);
        estatus.setCellValueFactory(callback);
        
        folio.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        justificante.prefWidthProperty().bind(tabla.widthProperty().multiply(4/10f).subtract(3));
        estatus.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f).subtract(3));
        tipo.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f).subtract(3));
      
    }
    
    private void createCellFactorys(){
        folio.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
            @Override public TableCell<JustificanteFolio, JustificanteFolio> call(TableColumn<JustificanteFolio, JustificanteFolio> p) {
                return new TableCell<JustificanteFolio,JustificanteFolio>(){
                    @Override protected void updateItem(final JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            Hyperlink h = new Hyperlink(t.getFolio());
                            h.setOnAction(new EventHandler<ActionEvent>() {
                                @Override public void handle(ActionEvent a) {
                                    Principal.showJustificante(t);
                                }
                            });
                            setGraphic(h);
                        } else {
                            setGraphic(null);
                        }
                    }
                    
                };
            }
        });
        
        tipo.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
            @Override public TableCell<JustificanteFolio, JustificanteFolio> call(TableColumn<JustificanteFolio, JustificanteFolio> p) {
                return new TableCell<JustificanteFolio,JustificanteFolio>(){
                    @Override protected void updateItem(final JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            setText(t.isAsignatura()? "Asignatura":
                                    t.isDia()? "Dia":
                                    "Periodo");                                   
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        
        estatus.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
            @Override public TableCell<JustificanteFolio, JustificanteFolio> call(TableColumn<JustificanteFolio, JustificanteFolio> p) {
                return new TableCell<JustificanteFolio,JustificanteFolio>(){
                    @Override protected void updateItem(final JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            Text text = new Text(t.isPendiente()? "Pendiente":
                                                t.isAprobado()? "Aceptado":
                                                "Rechazado");
                            setGraphic(text);
                            if (!t.isPendiente()){
                                setTooltip(new Tooltip(t.getAprobadonombre()));
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                    
                };
            }
        });
    }
}
