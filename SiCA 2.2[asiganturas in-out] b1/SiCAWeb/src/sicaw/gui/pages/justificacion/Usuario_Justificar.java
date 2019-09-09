package sicaw.gui.pages.justificacion;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javax.swing.JOptionPane;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.asistencias.AsistenciaClase;
import sica.common.asistencias.ReportesAsistencias;
import sica.common.justificantes.Folio;
import sica.common.justificantes.Fraccion;
import sica.common.justificantes.ReportesJustificantes;
import sica.common.justificantes.TipoJustificante;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;
import sicaw.gui.Principal;
import sicaweb.FileManagerTask;
import sicaweb.PDFJustificantes;
import sicaweb.Utils;

public class Usuario_Justificar implements Initializable {

    private BooleanProperty enviado;
    private Service<ObservableList<AsistenciaClase>> loadMateriasService;
    private FileManagerTask uploadFileTask;
    
    @FXML private VBox container;
    @FXML private ToggleGroup tgroup;
    @FXML private ToggleButton asignatura;
    @FXML private ToggleButton dia;
    @FXML private ToggleButton periodo;
    
    @FXML private HBox fechasBox;
    @FXML private Label fecha2;    
    private DatePicker fecha;
    private DatePicker hasta;
    
    @FXML private HBox materiasBox;
    @FXML private ComboBox<AsistenciaClase> materias;
    
    @FXML private HBox fraccBox;
    @FXML private ComboBox<TipoJustificante> motivosFalta;
    @FXML private ComboBox<Fraccion> fraccJustif;
    
    @FXML private TextArea comentario;
    @FXML private VBox archivosBox;
    @FXML private ListView<File> archivos;
    
    @FXML private VBox buttonBox;
    @FXML private Button sendButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        enviado = new SimpleBooleanProperty(false);
        uploadFileTask = new FileManagerTask(null, FileManagerTask.Type.UPLOAD, FileManagerTask.Option.JUSTIFICANTE);
        createDatePickers();
        createCellFactory();
        
        loadMateriasService = new Service<ObservableList<AsistenciaClase>>()  {
            @Override protected Task<ObservableList<AsistenciaClase>> createTask() {                
                return ReportesAsistencias.getAsistenciaClasesPorDia(
                        fecha.getSelectedDate(), 
                        Autenticator.getCurrentUser(), 
                        Privilegios.USUARIO);
            }
        };
        loadMateriasService.valueProperty().addListener(
                (ObservableValue<? extends ObservableList<AsistenciaClase>> ov, 
                        ObservableList<AsistenciaClase> t, 
                        ObservableList<AsistenciaClase> t1) -> {
            if (t1 != null)
                materias.setItems(t1);
            else
                materias.getItems().clear();
        });
        
        tgroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
            loadView();
        });
        container.getChildren().removeAll(materiasBox,fraccBox,archivosBox);
        
        Task<ObservableList<TipoJustificante>> tipos = ReportesJustificantes.getListaJustificantesTipo(Autenticator.getCurrentUser().getTipo());                
        tipos.valueProperty().addListener(
                (ObservableValue<? extends ObservableList<TipoJustificante>> ov, 
                        ObservableList<TipoJustificante> t,
                        ObservableList<TipoJustificante> t1) -> {                    
            if (t1 != null)
                motivosFalta.setItems(t1);
        });
        new Thread(tipos).start();        
                
        createListeners();                
        tgroup.selectToggle(tgroup.getToggles().get(0));
        
        asignatura.disableProperty().bind(enviado);
        dia.disableProperty().bind(enviado);
        fecha.disableProperty().bind(enviado.and(tgroup.selectedToggleProperty().isNotNull()));
        hasta.disableProperty().bind(enviado);
        periodo.disableProperty().bind(enviado);
        motivosFalta.disableProperty().bind(enviado);
        fraccJustif.disableProperty().bind(enviado);
        comentario.disableProperty().bind(enviado);
        materias.disableProperty().bind(enviado);
        
    }       
    
    @FXML protected void sendJustificante(){    
        if (enviado.get()){
            clear();
        } else {        
            Usuario usr = Autenticator.getCurrentUser();        
            TipoJustificante justif = motivosFalta.getSelectionModel().getSelectedItem();
            Fraccion fracc = fraccJustif.getSelectionModel().getSelectedItem();
            Date fech = fecha.getSelectedDate();
            Date hast = hasta.getSelectedDate();
            AsistenciaClase crn = materias.getSelectionModel().getSelectedItem();
            String cmt = comentario.getText();

            if (fech == null){
                Principal.avisar("Error! Seleccionar fecha a justificar");
            } else if (periodo.isSelected() && (hast==null || fech.compareTo(hast)>=0)){
                Principal.avisar("Error! Fecha final incorrecta");
            } else if (asignatura.isSelected() && crn==null){
                Principal.avisar("Error! Seleccionar materia a justificar");
            } else if (asignatura.isSelected() && (crn.getRegistro()!=null || crn.getJustificante()!=null)){
                Principal.avisar("Error! No es necesario justificar la materia seleccionada");
            } else if (justif == null){
                Principal.avisar("Error! Seleccionar justificante");
            } else if (!justif.getFracciones().isEmpty() && fracc == null){
                Principal.avisar("Error! Seleccionar fraccion del justificante");
            }  else if ((fracc!=null && (fracc.getDocumentos() && archivos.getItems().isEmpty()))
                    || (justif.getDocumentos() && justif.getFracciones().isEmpty() && archivos.getItems().isEmpty())) {           
                Principal.avisar("Error! El justificante actual requiere que se envien archivos");
            } else {     

                if (dia.isSelected()) {
                    sendJustificantePeriodo(usr, justif, fracc, fech, fech, cmt);
                } else if(asignatura.isSelected()){
                    sendJustificanteAsignatura(usr, justif, fracc, fech, crn, cmt);
                } else if (periodo.isSelected()){
                    sendJustificantePeriodo(usr, justif, fracc, fech, hast, cmt);
                } else {
                    Principal.avisar("Seleccionar una opcion de justificante");
                }
            }
        }
    }
    
    private void sendJustificantePeriodo(Usuario usr, TipoJustificante justif, Fraccion fracc, Date fecha, Date hasta, String cmt){
        Folio folio = DBQueries.insertJustificantePeriodo(
                usr.getCodigo(), justif.getId(), fracc!=null? fracc.getFraccion():"", 
                Utils.formatDate(fecha), Utils.formatDate(hasta), cmt);

        if (folio !=null){
            sendArchivos(folio, usr);
            clearButton(folio);
        } else {
            Principal.avisar("Error enviando justificante");
        }        
    }
    
    private void sendJustificanteAsignatura(Usuario usr, TipoJustificante justif, Fraccion fracc, Date fecha, AsistenciaClase crn, String cmt){
        Folio folio = DBQueries.insertJustificanteClase(
                usr.getCodigo(), justif.getId(), fracc!=null? fracc.getFraccion():"", 
                Utils.formatDate(fecha), crn.getCrn(), cmt);

        if (folio !=null){
            sendArchivos(folio, usr);
            clearButton(folio);
        } else {
            Principal.avisar("Error enviando justificante");
        }        
    }
    
    private void sendArchivos(Folio folio, Usuario usr){        
        
        if (archivos.getItems().size() >= 1){
            uploadFileTask.setFile(PDFJustificantes.concatenateFiles(archivos.getItems()));
            
        } else {
            uploadFileTask.setFile(null);
        }
        
        if (uploadFileTask.getFile() != null){
            uploadFileTask.setName(folio.getFolio()+"_"+usr.getCodigo()+".pdf");            
            new Thread(uploadFileTask).start();            
        }
         
    }
    
    private void clearButton(Folio folio){
        enviado.set(true);
        sendButton.setText("Crear otro justificante");
        Label l = new Label("Justificante enviado, Folio: "+folio.getFolio());        
        buttonBox.getChildren().add(0,l);
        Principal.avisar("Justificante enviado, Folio: "+folio.getFolio());
        
    }
    
    private void clear(){
        enviado.set(false);
        buttonBox.getChildren().remove(0);
        sendButton.setText("Enviar justificante");       
        fecha.setSelectedDate(null);
        hasta.setSelectedDate(null);
        motivosFalta.getSelectionModel().clearSelection();
        comentario.clear();
        archivos.getItems().clear();
    }
        
    private void loadView(){
        if (asignatura.isSelected() && !container.getChildren().contains(materiasBox)){
            container.getChildren().add(2,materiasBox);
            if (fecha.getSelectedDate()!=null){
                if (loadMateriasService.getState()!=Service.State.READY)
                        loadMateriasService.reset();
                    loadMateriasService.start();
            }
                
        } else {
            container.getChildren().remove(materiasBox);
        }
        if (periodo.isSelected()){
            hasta.setSelectedDate(fecha.getSelectedDate());
        }
    }
    
    @FXML protected void addArchivo(){        
        File file = Utils.getArchivo(container.getScene().getWindow(), 5);        
        if (file != null){             
            if (file.getName().toLowerCase().endsWith("pdf") && archivos.getItems().size()>0){
                for (File f : archivos.getItems()){
                    if (f.getName().toLowerCase().endsWith("pdf")){
                        JOptionPane.showConfirmDialog(null,
                            "Solo se puede enviar un archivo en formato PDF", 
                            "Error", 
                            JOptionPane.CLOSED_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
            }
            archivos.getItems().add(file);            
        }
    }
    
    private void createDatePickers(){
        fecha = Utils.newDatePicker("Seleccionar...");
        hasta = Utils.newDatePicker("Seleccionar...");
        fecha.setStyle("-fx-font-size: 14px;");
        hasta.setStyle("-fx-font-size: 14px;");
        fechasBox.getChildren().add(1, fecha);
        fechasBox.getChildren().add(hasta);        
        fecha2.visibleProperty().bind(periodo.selectedProperty());
        hasta.visibleProperty().bind(periodo.selectedProperty());      
    }
    
    private void createListeners(){
        fecha.selectedDateProperty().addListener((ObservableValue<? extends Date> ov, Date t, Date t1) -> {
            if (asignatura.isSelected()){
                if (loadMateriasService.getState()!=Service.State.READY)
                    loadMateriasService.reset();
                loadMateriasService.start(); 
            }
        });  
        motivosFalta.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TipoJustificante> ov, TipoJustificante t, TipoJustificante t1) -> {
            fraccJustif.getSelectionModel().clearSelection();
            fraccJustif.getItems().clear();
            
            if (t1!=null && !t1.getFracciones().isEmpty()){
                fraccJustif.getItems().addAll(t1.getFracciones());
                if (!container.getChildren().contains(fraccBox)){
                    container.getChildren().add(asignatura.isSelected()? 4:3, fraccBox);
                }
                if (container.getChildren().contains(archivosBox)){
                    container.getChildren().remove(archivosBox);
                }
            } else {
                if (container.getChildren().contains(fraccBox)){
                    container.getChildren().remove(fraccBox);
                }
                if (t1!=null && (t1.getDocumentos() || t1.getNombre().equals("Otro"))){
                    if (!container.getChildren().contains(archivosBox))
                        container.getChildren().add(container.getChildren().size()-2, archivosBox);
                } else if (container.getChildren().contains(archivosBox)){
                    container.getChildren().remove(archivosBox);
                }
            }
        });
        fraccJustif.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends Fraccion> ov, Fraccion t, Fraccion t1) -> {
                if (t1!=null && t1.getDocumentos()){
                    if (!container.getChildren().contains(archivosBox))
                        container.getChildren().add(container.getChildren().size()-2, archivosBox);
                } else if (container.getChildren().contains(archivosBox)){
                    container.getChildren().remove(archivosBox);
                }
        });
    }
    
    private void createCellFactory(){
        materias.setCellFactory(new Callback<ListView<AsistenciaClase>, ListCell<AsistenciaClase>>() {
            @Override public ListCell<AsistenciaClase> call(ListView<AsistenciaClase> p) {
                return new ListCell<AsistenciaClase>(){
                    @Override protected void updateItem(AsistenciaClase t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            VBox v = new VBox(3);
                            v.setAlignment(Pos.TOP_CENTER);
                            v.setPadding(new Insets(5));
                            v.getChildren().addAll(
                                new Label(t.getMateria()+" ("+t.getCrn()+") "),
                                new Label("Horario: "+t.getHorario()),
                                new Label("Estatus: "+(t.getRegistro()!=null? t.getRegistro().getHora().substring(0, 5) :
                                        t.getJustificante()!=null? t.getJustificante().getNombrejustificante():
                                        fecha.getSelectedDate()!=null &&
                                            fecha.getSelectedDate().compareTo(Calendar.getInstance().getTime())>0? "Fecha futura":
                                        "Falta"))
                                );
                            setGraphic(v);     
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        materias.setButtonCell(new ListCell<AsistenciaClase>(){
            @Override protected void updateItem(AsistenciaClase t, boolean bln) {
                super.updateItem(t, bln);
                if (!bln && t!=null){
                    this.setText(t.getMateria()+" ("+t.getCrn()+") - "+t.getHorario().substring(0, 5) );                    
                } else {
                    setText(null);
                }
            }
        });
        
        motivosFalta.setCellFactory(new Callback<ListView<TipoJustificante>, ListCell<TipoJustificante>>() {
            @Override public ListCell<TipoJustificante> call(ListView<TipoJustificante> p) {
                return new ListCell<TipoJustificante>(){
                    @Override protected void updateItem(TipoJustificante t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t!=null && !bln){
                            VBox v = new VBox();
                            Label l = new Label(t.getNombre());
                            Label l2 = new Label(" - "+t.getDescripcion_gral());
                            l2.setWrapText(true);   
                            l2.setMaxWidth(450);
                            l2.setStyle("-fx-text-fill: gray");
                            l.setTooltip(new Tooltip(t.getDescripcion()));
                            l2.setTooltip(l.getTooltip());
                            
                            v.getChildren().addAll(l,l2);
                            setGraphic(v);
                        } else {
                            setTooltip(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        motivosFalta.setButtonCell(new ListCell<TipoJustificante>(){
            @Override protected void updateItem(TipoJustificante t, boolean bln) {
                super.updateItem(t, bln); 
                if (!bln && t!=null){
                    setText(t.getNombre());   
                } else {
                    setText(null);
                }
            }
        });
        
        fraccJustif.setCellFactory(new Callback<ListView<Fraccion>, ListCell<Fraccion>>() {
            @Override public ListCell<Fraccion> call(ListView<Fraccion> p) {
                return new ListCell<Fraccion>(){
                    @Override protected void updateItem(Fraccion t, boolean bln) {
                        super.updateItem(t, bln);
                        if (!bln && t!=null){
                            VBox v = new VBox();
                            Label l = new Label(t.getCategoria());
                            Label l2 = new Label(" - "+t.getDescripcion());
                            l2.setWrapText(true);         
                            l2.setMaxWidth(400);
                            l2.setStyle("-fx-text-fill: gray");
                            
                            v.getChildren().addAll(l,l2);
                            setGraphic(v);
                        } else {
                            setGraphic(null);
                        }
                    }                    
                };
            }
        });
        fraccJustif.setButtonCell(new ListCell<Fraccion>(){
            @Override protected void updateItem(Fraccion t, boolean bln) {
                super.updateItem(t, bln);
                if (!bln && t!=null){
                    setText(t.getCategoria());
                } else {
                    setText(null);
                }
            }            
        });        
        
        archivos.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override public ListCell<File> call(ListView<File> p) {
                return new ListCell<File>(){
                    @Override protected void updateItem(File f, boolean empty){
                        super.updateItem(f,empty);
                        if (f!=null && !empty){
                            HBox h = new HBox();
                            Label l = new Label(f.getName());
                            
                            Hyperlink hi = new Hyperlink("Eliminar");
                            hi.setOnAction(e -> archivos.getItems().remove(getIndex()));
                            hi.visibleProperty().bind(uploadFileTask.runningProperty().not()
                                        .and(uploadFileTask.stateProperty().isEqualTo(Task.State.READY)));
                            
                            ProgressIndicator p = new ProgressIndicator();
                            p.setPrefHeight(23);
                            p.visibleProperty().bind(uploadFileTask.runningProperty());
                            
                            HBox h2 = new HBox();
                            h.getChildren().addAll(l,h2,p,hi);
                            h.setAlignment(Pos.CENTER_LEFT);
                            HBox.setHgrow(h2, Priority.ALWAYS);
                            
                            archivos.setPrefHeight(archivos.getItems().size()*30+4);
                            setGraphic(h);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });        
    }    
}
