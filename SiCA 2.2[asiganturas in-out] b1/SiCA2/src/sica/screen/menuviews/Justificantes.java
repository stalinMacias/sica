package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.common.DBQueries;
import sica.common.Utils;
import sica.common.justificantes.JustificanteFolio;

public class Justificantes extends Screen implements Initializable {

    @FXML private TableView <JustificanteFolio> tabla;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> folio;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> justif;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> fecha;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> tipo;    
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> estatus;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTable();
        createCellsFactories();
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, ov, nv) -> {            
            if (nv != null){
                startConsultaFolio(nv);                
            }
        });
    }    

    private void startConsultaFolio(JustificanteFolio folio){
        ScreenManager.folio().setFolio(folio);
        ScreenManager.menu().addScreen(Screens.MENU_FOLIO);
        Platform.runLater(()->tabla.getSelectionModel().clearSelection());
    }       
    
    @Override
    public void start() {        
        tabla.setItems(
            DBQueries.getJustificantesUltimosUsuario(
                    ScreenManager.menu().getUsuario().getCodigo(), 
                    10));
        tabla.getSelectionModel().select(null);
    }
    
    private void initTable(){
        Callback<TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio>, ObservableValue<JustificanteFolio>> 
                cb = p -> new SimpleObjectProperty<>(p.getValue());
        
        folio.setCellValueFactory(new PropertyValueFactory<>("folio"));        
        justif.setCellValueFactory(new PropertyValueFactory<>("nombrejustificante"));
        fecha.setCellValueFactory(cb);
        estatus.setCellValueFactory(cb);
        tipo.setCellValueFactory(cb);
        
        folio.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
        fecha.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f));
        justif.prefWidthProperty().bind(tabla.widthProperty().multiply(4/10f));        
        estatus.prefWidthProperty().bind(tabla.widthProperty().multiply(2/10f).subtract(3));
        tipo.prefWidthProperty().bind(tabla.widthProperty().multiply(1/10f));
    }
    
    private void createCellsFactories(){
        fecha.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
            @Override public TableCell<JustificanteFolio, JustificanteFolio> call(TableColumn<JustificanteFolio, JustificanteFolio> p) {
                return new TableCell<JustificanteFolio,JustificanteFolio>(){
                    @Override protected void updateItem(JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);                        
                        if (!bln && t!=null){
                            setAlignment(Pos.CENTER);
                            setText(Utils.formatDate(t.getFechayHora()));
                            setTooltip(new Tooltip(
                                Utils.fullDateFormat(t.getFechayHora())+", "+Utils.formatTime(t.getFechayHora())));
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
                    @Override protected void updateItem(JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            Text text = new Text(
                                (t.isPendiente())? "Pendiente":                                
                                t.isAprobado()? "Aprobado":
                                "Rechazado");
                            
                            text.setFill(
                                t.isPendiente()? Color.ORANGERED:                                
                                t.isAprobado()? Color.GREEN:
                                Color.RED );                            
                            
                            if (t.getAprobadonombre() != null && !t.getAprobadonombre().isEmpty())
                                setTooltip(new Tooltip(t.getAprobadonombre()));
                            
                            setGraphic(text);
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
    }    
}
