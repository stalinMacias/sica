package sicaw.gui.pages;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.Configs;
import sica.common.DBQueries;
import sica.common.horarios.HorarioCrn;
import sica.common.horarios.HorarioUsuario;
import sica.common.justificantes.JustificanteFolio;
import sica.common.usuarios.CorreoUsuario;
import sicaw.gui.Principal;
import sicaweb.Utils;

public class Bienvenida implements Initializable {

        
    @FXML private ImageView imageUsr;
    @FXML private Text nombre;
    @FXML private Label nombre2;
    @FXML private Label codigo;
    @FXML private Label tipo;
    @FXML private Label instancia;
    @FXML private ListView<CorreoUsuario> correos;
    @FXML private Label telefono;
    
    @FXML private SplitPane split;
    @FXML private TitledPane jornadaPane;
    @FXML private TitledPane materiasPane;
    @FXML private TitledPane justifPane;
    
    @FXML private Label domEnt; @FXML private Label domSal;
    @FXML private Label lunEnt; @FXML private Label lunSal;
    @FXML private Label marEnt; @FXML private Label marSal;
    @FXML private Label mieEnt; @FXML private Label mieSal;
    @FXML private Label jueEnt; @FXML private Label jueSal;
    @FXML private Label vieEnt; @FXML private Label vieSal;
    @FXML private Label sabEnt; @FXML private Label sabSal;
    
    @FXML private TableView <HorarioCrn> tablaMaterias;
    @FXML private TableColumn <HorarioCrn, String> bloqueCol;
    @FXML private TableColumn <HorarioCrn, String> crnCol;
    @FXML private TableColumn <HorarioCrn, String> materiaCol;
    @FXML private TableColumn <HorarioCrn, String> diaCol;
    @FXML private TableColumn <HorarioCrn, String> horarioCol;
    @FXML private TableColumn <HorarioCrn, String> aulaCol;
    
    @FXML private TableView <JustificanteFolio> tablaJustif;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> folioCol;
    @FXML private TableColumn <JustificanteFolio, String> justifCol;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> fechaCol;
    @FXML private TableColumn <JustificanteFolio, JustificanteFolio> estatusCol;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        inicializarVista();
        createCellFactorys();
        
        nombre.setText(Autenticator.getCurrentUser().getNombre());          
        nombre2.setText(Autenticator.getCurrentUser().getNombre());    
        codigo.setText(Autenticator.getCurrentUser().getCodigo());
        tipo.setText(Autenticator.getCurrentUser().getTipo().toUpperCase());
        instancia.setText(Autenticator.getCurrentUser().getDepartamento());        
        telefono.setText(Autenticator.getCurrentUser().getTelefono());
        
        correos.getItems().setAll(DBQueries.getCorreosUsurio(Autenticator.getCurrentUser().getCodigo()));
        correos.setPrefHeight(correos.getItems().size()*30);
        correos.setVisible(!correos.getItems().isEmpty());        
        
        String host = Configs.SERVER_FOTOS()+ Autenticator.getCurrentUser().getCodigo()+".jpg";
        
        Image imagen = new Image(host);
            
        if (imagen.isError()){
            host = Configs.SERVER_FOTOS()+"error.jpg";
            imagen = new Image(host);
        }
        
        if (!imagen.isError()){
            imageUsr.setPreserveRatio(true);
            imageUsr.setImage(imagen);
            
        } else {
            System.out.println("Error estableciendo imagenes");
        }
               
        loadHorarioJornada();
        loadHorarioAsignaturas();
        loadUltimosJustificantes();
        
        if (split.getItems().isEmpty())
            split.setVisible(false);
         
       
    }    
    
    private void loadHorarioJornada(){
        ObservableList<HorarioUsuario> h = 
                DBQueries.getHorarioActualUsuario(Autenticator.getCurrentUser().getCodigo());
            
        if (h.isEmpty()){            
            if (split.getItems().contains(jornadaPane))
                split.getItems().remove(jornadaPane);
            
        } else {
        
            for (HorarioUsuario hu : h){                
                for (char c : hu.getDias().toCharArray()){
                    switch(c){                        
                        case '1': 
                            domEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                domSal.setText(hu.getSalida());
                            } else {
                                lunSal.setText(hu.getSalida());
                            }
                            break;
                        case '2':
                            lunEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                lunSal.setText(hu.getSalida());
                            } else {
                                marSal.setText(hu.getSalida());
                            }
                            break;
                        case '3':
                            marEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                marSal.setText(hu.getSalida());
                            } else {
                                mieSal.setText(hu.getSalida());
                            }
                            break;
                        case '4': 
                            mieEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                mieSal.setText(hu.getSalida());
                            } else {
                                jueSal.setText(hu.getSalida());
                            }
                            break;
                        case '5': 
                            jueEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                jueSal.setText(hu.getSalida());
                            } else {
                                vieSal.setText(hu.getSalida());
                            }
                            break;
                        case '6':
                            vieEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                vieSal.setText(hu.getSalida());
                            } else {
                                sabSal.setText(hu.getSalida());
                            }
                            break;
                        case '7': 
                            sabEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                sabSal.setText(hu.getSalida());
                            } else {
                                domSal.setText(hu.getSalida());
                            }
                            break;
                    }
                }
            }
        }
    }
    
    private void loadHorarioAsignaturas(){
        Calendar c = Calendar.getInstance();          
        tablaMaterias.setItems(
            DBQueries.getHorarioCrnsUsuario(
                Autenticator.getCurrentUser().getCodigo(),
                c.get(Calendar.YEAR), 
                (c.get(Calendar.MONTH)<6)? "A":"B" ));
        if (tablaMaterias.getItems().isEmpty() && split.getItems().contains(materiasPane))
            split.getItems().remove(materiasPane);
            
    }
    
    private void loadUltimosJustificantes(){
        tablaJustif.setItems(
            DBQueries.getJustificantesUltimosUsuario(
                    Autenticator.getCurrentUser().getCodigo(), 
                    10));
        
        if (tablaJustif.getItems().isEmpty() && split.getItems().contains(justifPane))
            split.getItems().remove(justifPane);
    }
    
    private void createCellFactorys(){
        horarioCol.setCellFactory(new Callback<TableColumn<HorarioCrn, String>, TableCell<HorarioCrn, String>>() {
            @Override public TableCell<HorarioCrn, String> call(TableColumn<HorarioCrn, String> p) {
                return new TableCell<HorarioCrn,String>(){
                    @Override protected void updateItem(String t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null && t.length()>5){
                            setText(t.substring(0,5));
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });
        fechaCol.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
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
        estatusCol.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
            @Override public TableCell<JustificanteFolio, JustificanteFolio> call(TableColumn<JustificanteFolio, JustificanteFolio> p) {
                return new TableCell<JustificanteFolio,JustificanteFolio>(){
                    @Override protected void updateItem(JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            Text text = new Text(
                                t.isPendiente()? "Pendiente":                                
                                t.isAprobado()? "Aprobado":
                                "Rechazado");
                            
                            text.setFill(
                                t.isPendiente()? Color.ORANGERED:                                
                                t.isAprobado()? Color.GREEN:
                                Color.RED );                            
                            
                            if (!t.getAprobadonombre().isEmpty())
                                setTooltip(new Tooltip(t.getAprobadonombre()));
                            
                            setGraphic(text);
                        } else {
                            setGraphic(null);
                        }                      
                    }                    
                };
            }
        });
        folioCol.setCellFactory(new Callback<TableColumn<JustificanteFolio, JustificanteFolio>, TableCell<JustificanteFolio, JustificanteFolio>>() {
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
        
    }
            
    private void inicializarVista() {
        
        bloqueCol.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        materiaCol.setCellValueFactory(new PropertyValueFactory<>("materia"));
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        diaCol.setCellValueFactory(new PropertyValueFactory<>("dia"));
        horarioCol.setCellValueFactory(new PropertyValueFactory<>("horario"));
        aulaCol.setCellValueFactory(new PropertyValueFactory<>("aula"));
        
        bloqueCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(1/20f));
        crnCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(1/10f));
        materiaCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(5/10f));
        diaCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(3/20f));
        horarioCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(1/10f));
        aulaCol.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(1/10f));
        
        correos.setCellFactory(new Callback<ListView<CorreoUsuario>, ListCell<CorreoUsuario>>() {
            @Override public ListCell<CorreoUsuario> call(ListView<CorreoUsuario> p) {
                return new ListCell<CorreoUsuario>(){
                    @Override protected void updateItem(CorreoUsuario t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t!=null && !bln){
                            setText(t.getCorreo() + (t.getPrincipal()?" (Principal)":""));
                        } else {
                            setText(null);
                        }
                    }
                    
                };
            }
        });        
        
        Callback<TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio>, ObservableValue<JustificanteFolio>> cb = 
            new Callback<TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio>, ObservableValue<JustificanteFolio>>() {
            
            @Override public ObservableValue<JustificanteFolio> call(TableColumn.CellDataFeatures<JustificanteFolio, JustificanteFolio> p) {
                return new SimpleObjectProperty<>(p.getValue());
            }
        };
        
        folioCol.setCellValueFactory(cb);        
        justifCol.setCellValueFactory(new PropertyValueFactory<JustificanteFolio, String>("nombrejustificante"));
        fechaCol.setCellValueFactory(cb);
        estatusCol.setCellValueFactory(cb);
        
        folioCol.prefWidthProperty().bind(tablaJustif.widthProperty().multiply(2/10f));
        fechaCol.prefWidthProperty().bind(tablaJustif.widthProperty().multiply(2/10f));
        justifCol.prefWidthProperty().bind(tablaJustif.widthProperty().multiply(4/10f));        
        estatusCol.prefWidthProperty().bind(tablaJustif.widthProperty().multiply(2/10f));
        
    }
   
}
