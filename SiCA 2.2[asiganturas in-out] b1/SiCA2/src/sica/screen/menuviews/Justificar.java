package sica.screen.menuviews;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import sica.Screen;
import sica.ScreenManager;
import sica.common.DBQueries;
import sica.common.Utils;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.faltas.FaltaClase;
import sica.common.justificantes.Folio;
import sica.common.justificantes.Fraccion;
import sica.common.justificantes.ReportesJustificantes;
import sica.common.justificantes.TipoJustificante;

public class Justificar extends Screen implements Initializable {

    private boolean enviado;
    private AsistenciaUsuario asistencia;
    private FaltaClase falta;
    private Service<ObservableList<TipoJustificante>> getJustificantes;
    private ObjectProperty<TipoJustificante> justificante;
    private ObjectProperty<Fraccion> fraccion;
    private File archivo;    
    
    @FXML private Label titulo;
    @FXML private Label fecha;    
    @FXML private Label justifnombre;
    @FXML private HBox fraccBox;
    @FXML private Label fraccnombre;
    @FXML private HBox archivoBox;
    @FXML private Hyperlink archivoSelect;
    @FXML private Label archivonombre;
    @FXML private ScrollPane opcPane;
    @FXML private FlowPane opc;
    @FXML private TextArea comentario;
    @FXML private Button enviar;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {   
        justificante = new SimpleObjectProperty<>(null);
        fraccion = new  SimpleObjectProperty<>(null);        
        
        getJustificantes = new Service<ObservableList<TipoJustificante>>() {
            @Override protected Task<ObservableList<TipoJustificante>> createTask() {
                return ReportesJustificantes.getListaJustificantesTipo(
                        ScreenManager.menu().getUsuario().getTipo()
                );
            }
        };
        
        getJustificantes.valueProperty().addListener(
                (ObservableValue<? extends ObservableList<TipoJustificante>> o, 
                        ObservableList<TipoJustificante> ov, 
                        ObservableList<TipoJustificante> nv) -> {
            opc.getChildren().clear();
            if (nv != null){
                nv.forEach((j)->{
                    Label l = new Label(j.getNombre());
                    l.setPadding(new Insets(15,15,15,15)); 
                    l.setStyle("-fx-background-color: rgb(240,240,240)");
                    l.getStyleClass().addAll("font-size-20","clickeable");
                    l.setOnMouseClicked(e -> {
                        justificante.set(j);
                    });
                    opc.getChildren().add(l);
                });
            }
        });
        
        justificante.addListener((ObservableValue<? extends TipoJustificante> o, TipoJustificante ov, TipoJustificante nv) -> {
            opc.getChildren().clear();
            justifnombre.setText(nv!=null? nv.getNombre():"");
            if (nv != null){
                fraccBox.setVisible(!nv.getFracciones().isEmpty());
                if (nv.getFracciones().isEmpty() && nv.getDocumentos()){
                    archivoBox.setVisible(true);
                    selectFile();
                } else if (nv.getFracciones().isEmpty()){
                    opcPane.setVisible(false);
                } else {                
                    nv.getFracciones().forEach(f -> {
                        Label l = new Label(f.getCategoria());
                        l.setPadding(new Insets(15,15,15,15)); 
                        l.setStyle("-fx-background-color: rgb(240,240,240)");
                        l.getStyleClass().addAll("font-size-20","clickeable");     
                        l.setOnMouseClicked(e -> {
                            fraccion.set(f);
                        });
                        opc.getChildren().add(l);
                    });
                }                
            }
        });        
        
        fraccion.addListener((ObservableValue<? extends Fraccion> o, Fraccion ov, Fraccion nv) -> {
            fraccnombre.setText(nv!=null? nv.getCategoria() : "");
            opc.getChildren().clear();
            if (nv != null && nv.getDocumentos()){
                archivoBox.setVisible(true);
                selectFile();
            } else {
                opcPane.setVisible(false); 
                
            }
        });
    }   
    
    public void setDiaJustificando(AsistenciaUsuario au){
        asistencia = au;
        falta = null;
        titulo.setText("Justificación de Falta a Jornada Laboral");
        fecha.setText("Fecha: "+Utils.formatfullDate(asistencia.getFecha()).toUpperCase());        
    }
    
    public void setClaseJustificando(FaltaClase f){
        asistencia = null;
        falta = f;
        titulo.setText("Justificación de Falta a Asignatura");
        fecha.setText("Fecha: "+Utils.formatfullDate(falta.getFecha()).toUpperCase()
                +" Materia: "+falta.getCrn().getMateria());        
    }
    
    @FXML protected void sendJustificante(){        
        Folio folio = null;
        
        if (!enviado && asistencia!=null){            //
            folio = DBQueries.insertJustificantePeriodo(
                    asistencia.getUsuario(), justificante.get().getId(), fraccion.get()!=null? fraccion.get().getFraccion():"", 
                    Utils.formatDate(asistencia.getFecha()), Utils.formatDate(asistencia.getFecha()), comentario.getText());
            
        } else if (!enviado && falta!=null){
            folio = DBQueries.insertJustificanteClase(
                    falta.getCrn().getCodProf(), justificante.get().getId(), fraccion.get()!=null? fraccion.get().getFraccion():"", 
                    Utils.formatDate(falta.getFecha()), falta.getCrn().getCrn(), comentario.getText());            
        }
        
        if (!enviado && folio != null){
            enviar.setText("Justificante enviado, Folio: "+folio.getFolio());        
            comentario.setDisable(true);
            archivoSelect.setDisable(true);
            ScreenManager.principal().avisar("Justificante enviado a autorización");
            //sendArchivos(folio);
            enviado = true;
            
        }
    }
    
    @FXML private void selectFile(){        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo del justificante");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );        
        archivo = fileChooser.showOpenDialog(this.getParent().getScene().getWindow());
        
        if (archivo != null){
            archivoSelect.setText("Archivo:");
            if (archivo.length() > (2 * 1024 * 1024)){ // 2Mb
                ScreenManager.principal().avisar("No se permiten archivos superiores a los "+2+"Mb");
                archivo = null;
            } else {
                archivonombre.setText(archivo.getName());
                opcPane.setVisible(false);
                comentario.requestFocus();
            }
        } 
        if (archivo == null){
            opcPane.setVisible(true);
            archivonombre.setText(null);
            archivoSelect.setText("Click aquí para seleccionar archivo");
            ScreenManager.principal().avisar("Es necesario seleccionar un archivo");
        }
    }

    @Override public void start() {
        justificante.set(null);
        fraccion.set(null);
        comentario.clear();
        fraccBox.setVisible(false);
        archivoBox.setVisible(false);
        enviar.setText("Enviar justificante");
        if (enviar.isDisabled()) enviar.setDisable(false);
        if (comentario.isDisabled()) comentario.setDisable(false);
        if (!opcPane.isVisible()) opcPane.setVisible(true);
        if (archivoSelect.isDisabled()) archivoSelect.setDisable(false);
            
        if (getJustificantes.isRunning()) getJustificantes.cancel();
        getJustificantes.reset();
        getJustificantes.start();
        
        enviado = false;
    }
    
}
