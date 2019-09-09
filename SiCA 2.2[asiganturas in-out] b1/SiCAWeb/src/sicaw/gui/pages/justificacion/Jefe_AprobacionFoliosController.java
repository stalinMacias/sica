package sicaw.gui.pages.justificacion;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.Configs;
import sica.common.DBQueries;
import sica.common.justificantes.JustificanteFolio;
import sica.common.justificantes.ReportesJustificantes;
import sica.common.usuarios.Privilegios;
import sicaw.gui.Principal;
import sicaweb.FileDownloadTask;
import sicaweb.PdfToImage;
import sicaweb.Utils;

public class Jefe_AprobacionFoliosController implements Initializable {
    
    private Service<File> loadFileService;
    private PdfToImage pdfViewer;    
    private ObjectProperty<ImageView> imageView;
    private ObservableList<Image> images;
        
    @FXML private ListView<JustificanteFolio> lista;
    @FXML private ProgressIndicator loadingJustificantes;
    @FXML private ScrollPane scroller;
    @FXML private Pagination pagination;
    @FXML private Slider zoom;
    @FXML private ProgressIndicator loading;

    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        createCellFactorys();
        
        imageView = new SimpleObjectProperty<>(new ImageView());
        pdfViewer = new PdfToImage();        
        images = FXCollections.observableArrayList();
        
        
        loadFileService = new Service<File>() {
            @Override protected Task<File> createTask() {                               
                String fileName = lista.getSelectionModel().getSelectedItem().getFolio()
                        + "_"
                        + lista.getSelectionModel().getSelectedItem().getUsuario()
                        + ".pdf";
                
                String url = Configs.SERVER_JUSTIF() + fileName;                                
                imageView.get().setImage(new Image(getClass().getResource("sinArchivos.jpg").toExternalForm()));  
                images.clear();
                pagination.setCurrentPageIndex(0);
                return new FileDownloadTask(url,Utils.getTempFile(fileName));                
            }
        };
        
        loadFileService.valueProperty().addListener((ObservableValue<? extends File> ov, File t, File t1) -> {
            if (t1 != t){
                pdfViewer.fileProperty().set(loadFileService.getValue());
            }
        });
        
        pdfViewer.imagesProperty().addListener(
            (ObservableValue<? extends ObservableList<Image>> ov, ObservableList<Image> t, ObservableList<Image> t1) -> {
                pagination.setCurrentPageIndex(0);
                pagination.setPageCount(t1!=null? t1.size(): 0);
                if (t1!=null){
                    images.setAll(t1);
                    if (images.size()>0)
                        imageView.get().setImage(images.get(0));
            }
        });
        
        scroller.contentProperty().bind(imageView);        
        imageView.getValue().setPreserveRatio(true);       
        imageView.getValue().setStyle("-fx-background-color: white;");
        imageView.get().fitHeightProperty().bind(scroller.heightProperty().multiply(zoom.valueProperty().divide(100)));
        imageView.get().fitWidthProperty().bind(scroller.widthProperty().multiply(zoom.valueProperty().divide(100)));
        
        pagination.currentPageIndexProperty().addListener((Observable o) -> {
            if (pagination.getCurrentPageIndex() < images.size())
                imageView.get().setImage(images.get(pagination.getCurrentPageIndex()));
        });                
        loading.visibleProperty().bind(
                Bindings.or(pdfViewer.loadingProperty(), loadFileService.runningProperty()));
        loading.progressProperty().bind(
                Bindings.when(loadFileService.runningProperty()).then(loadFileService.progressProperty()).otherwise(-1));
        
        Task<ObservableList<JustificanteFolio>>  t = 
            Autenticator.getCurrentUser().getPrivilegios().equals(Privilegios.ADMINISTRADOR)? 
                ReportesJustificantes.getJustificantesPendientesAprobar():
                ReportesJustificantes.getJustificantesPendientesAprobarJefe(Autenticator.getCurrentUser().getCodigo());
        
        lista.itemsProperty().bind(t.valueProperty());      
        loadingJustificantes.visibleProperty().bind(t.runningProperty());
        
        new Thread(t).start();
              
    }   
    
    private void aprobarJustificante(JustificanteFolio f){
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR){
            if (!f.isAprobado() && DBQueries.aprobarJustificanteFolio(f.getFolio())){
                f.setAprobado("1");
                f.setAprobadonombre(Autenticator.getCurrentUser().getNombre());
                Principal.avisar("Justificante aprobado");
            } 
        } else {
            if (!f.isAceptado() && DBQueries.aceptarJustificanteFolio(f.getFolio())){
                f.setAceptado("1");
                f.setAceptadonombre(Autenticator.getCurrentUser().getNombre());                
                Principal.avisar(f.isAprobado()?"Justificante marcado como visto"
                        :"Justificante aprobado por jefe de instancia");
            } 
        }
    }
    
    private void rechazarJustificante(JustificanteFolio f){
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR){
            if (!f.isAprobado() && DBQueries.noAprobarJustificanteFolio(f.getFolio())){
                f.setAprobado("0");
                f.setAprobadonombre(Autenticator.getCurrentUser().getNombre());               
                Principal.avisar("Justificante rechazado");
            } 
        } else {
             if (!f.isAceptado() && DBQueries.noAceptarJustificanteFolio(f.getFolio())){
                f.setAceptado("0");
                f.setAceptadonombre(Autenticator.getCurrentUser().getNombre());                               
                Principal.avisar("Justificante rechazado por jefe de instancia");
            } 
        }
        
    }
    
    private void createCellFactorys(){        
        lista.setCellFactory(new Callback<ListView<JustificanteFolio>, ListCell<JustificanteFolio>>() {
            @Override public ListCell<JustificanteFolio> call(ListView<JustificanteFolio> p) {
                return new ListCell<JustificanteFolio>(){
                    @Override protected void updateItem(final JustificanteFolio t, boolean bln) {
                        super.updateItem(t, bln); 
                        
                        if (!bln && t!=null){                            
                            
                            Label lfolio = new Label("Folio:");
                            Hyperlink hfolio = new Hyperlink(t.getFolio());
                            hfolio.setOnAction(e -> Principal.showJustificante(t));
                            
                            Label lnombre = new Label("Usuario: "+t.getNombreusuario()+" ("+t.getUsuario()+")");
                            
                            Label ljustif = new Label("Justificante: "+t.getNombrejustificante()
                                    +(t.getNombrefraccion()!=null?", "+t.getNombrefraccion():null));                            
                            ljustif.setWrapText(true);
                            ljustif.setMaxWidth(600);
                            
                            Label linfo = new Label();
                            Text lstatus = new Text();
                            Label linfo1 = new Label();
                            Label linfo2 = new Label();
                            
                            if (t.isAsignatura()){
                                linfo.setText(linfo.getText()+"\t\t\tTipo:\tAsignatura\t\t\tEstatus:");
                                linfo1.setText("Fecha: "+t.getFecha());
                                linfo2.setText("Materia y CRN: "+t.getNombremateria()+" ("+t.getCrn()+") ");
                                
                            } else if (t.isPeriodo()){
                                linfo.setText(linfo.getText()+"\t\t\tTipo:\tPeriodo\t\t\tEstatus:");
                                linfo1.setText("Fecha inicial: "+Utils.formatDate(t.getFecha_inicial())
                                        +" Fecha final: "+Utils.formatDate(t.getFecha_final()));
                            }
                            
                            if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR){
                                lstatus.setText(t.isAceptado()?"ACEPTADO":"PENDIENTE");
                                lstatus.setFill(t.isAceptado()?Color.GREEN:Color.ORANGERED);
                            } else {
                                lstatus.setText(t.isAprobado()?"APROBADO":"PENDIENTE");
                                lstatus.setFill(t.isAprobado()?Color.GREEN:Color.ORANGERED);
                            }                                          
                            
                            HBox info = new HBox(10);
                            info.setAlignment(Pos.CENTER_LEFT);
                            info.getChildren().addAll(lfolio, hfolio, linfo, lstatus);
                            
                            final VBox vbox1 = new VBox(5);                                          
                            vbox1.setPadding(new Insets(5));
                            vbox1.getChildren().addAll(info, lnombre);
                                                        
                            final VBox vbox2 = new VBox(5);
                            vbox2.setPadding(new Insets(0,5,0,5));                            
                            
                            final HBox buttonBox = new HBox(20);
                            buttonBox.setAlignment(Pos.CENTER_RIGHT);
                            VBox.setMargin(buttonBox, new Insets(5,5,0,5));                            
                            
                            if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.JEFE && t.isAprobado()){
                                Label l = new Label("- Justificante aprobado por Coordinación de Personal -");
                                buttonBox.getChildren().add(l);
                                
                            } else {
                                if (!t.isAceptado() && Autenticator.getCurrentUser().getPrivilegios() != Privilegios.JEFE){
                                    Label l = new Label("El jefe de instancia aún no ha aprobado este justificante");
                                    buttonBox.getChildren().add(l);
                                }
                                
                                Button aprobarBtn = new Button("Aprobar");
                                Button rechazarBtn = new Button("Rechazar");                           
                                buttonBox.getChildren().addAll(aprobarBtn,rechazarBtn);
                                
                                aprobarBtn.setOnAction((ActionEvent ae) -> {
                                    aprobarJustificante(t);
                                    buttonBox.getChildren().clear();
                                    buttonBox.getChildren().add(new Label("- Aprobado -"));
                                });

                                rechazarBtn.setOnAction((ActionEvent ae) -> {
                                    rechazarJustificante(t);
                                    buttonBox.getChildren().clear();
                                    buttonBox.getChildren().add(new Label("- Rechazado -"));
                                });
                            
                            }
                            
                            vbox2.getChildren().addAll(ljustif,linfo1);
                            if (!linfo2.getText().isEmpty()){
                                vbox2.getChildren().addAll(linfo2);
                            }
                                                      
                            setGraphic(vbox1);
                            this.focusedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t0, Boolean t1) -> {
                                if (t1 && !(vbox1.getChildren().contains(vbox2)&&vbox1.getChildren().contains(buttonBox))){
                                    vbox1.getChildren().addAll(vbox2,buttonBox);
                                    
                                } else if (vbox1.getChildren().contains(vbox2)&&vbox1.getChildren().contains(buttonBox)) {
                                    vbox1.getChildren().removeAll(vbox2,buttonBox);                                        
                                }
                            });                                                           
                            
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        lista.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends JustificanteFolio> ov, JustificanteFolio t, JustificanteFolio t1) -> {
                zoom.setValue(100);
                searchFiles();
                if (t1 != null && Autenticator.getCurrentUser().getPrivilegios() == Privilegios.JEFE 
                        && t1.isAprobado() && !t1.isAceptado()){
                    aprobarJustificante(t1);
                }
        });        
    }    
    
    protected void searchFiles(){
        if (loadFileService.isRunning()) loadFileService.cancel();
        loadFileService.reset();
        loadFileService.start();        
    }
}