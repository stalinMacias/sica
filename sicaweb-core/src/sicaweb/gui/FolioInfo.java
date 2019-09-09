package sicaweb.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import sica.common.Autenticator;
import sica.common.Configs;
import sica.common.DBQueries;
import sica.common.Utils;
import sica.common.justificantes.Comentario;
import sica.common.justificantes.Folio;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificanteFolio;
import sica.common.justificantes.JustificantePeriodo;
import sicaweb.FileDownloadTask;

public class FolioInfo extends VBox {
    private final Service<File> loadFileService;
    
    @FXML private VBox container;
    
    @FXML private ImageView close;
    @FXML private Label folio;
    @FXML private Text estatus;
    @FXML private Label tipo;
    @FXML private Label fechayhora;
    @FXML private Label fecha;
    @FXML private Label fechafinal;
    @FXML private Label lfechafinal;
    @FXML private Label aceptado;
    @FXML private Label aprobado;
    @FXML private Label materia;
    @FXML private Label justificante;
    @FXML private Label descripcion;
    @FXML private Label fraccion;
    @FXML private Hyperlink archivo;
    @FXML private ProgressIndicator downloading;
    @FXML private VBox vcomnt;
    
    @FXML private TextField coment;
    
    public FolioInfo() {
        super.setVisible(false);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FolioInfo.fxml"));        
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);        
        
        try {
            fxmlLoader.load();            
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
        
        container.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent t) {
                if (t.getTarget().equals(t.getSource()))
                    hide();
            }
        });        
        close.setCursor(Cursor.HAND);
        loadFileService = new Service<File>() {
            @Override protected Task<File> createTask() {     
                String url = Configs.SERVER_JUSTIF() + archivo.getText();                                
                return new FileDownloadTask(url,Utils.getTempFile(archivo.getText())){
                    public void openFile(File file){
                        if (file!=null){
                            if (Desktop.isDesktopSupported()){
                                try {
                                    Desktop.getDesktop().open(file);
                                    Thread.sleep(3000);
                                } catch (IOException | InterruptedException e){
                                    e.printStackTrace(System.out);
                                }
                            }                    
                        }
                    }
                    @Override protected File call() throws Exception {                        
                        File call = super.call(); 
                        openFile(call);
                        return call;
                    }
                
                };                
            }
        };
        downloading.visibleProperty().bind(loadFileService.runningProperty());
    }
        
    private void loadJustificanteInfo(Folio f){
        
        folio.setText(f.getFolio());
        fechayhora.setText(Utils.formatTime(f.getFechayHora())+", "+Utils.fullDateFormat(f.getFechayHora()));
        estatus.setText(f.isPendiente()? "PENDIENTE": f.isAprobado()?"APROBADO": "RECHAZADO");
        estatus.setFill(f.isPendiente()? Color.ORANGERED: f.isAprobado()?Color.GREEN: Color.RED);
        justificante.setText(f.getNombrejustificante());
        descripcion.setText(f.getDescripcionJustificante());
        descripcion.setTooltip(new Tooltip(f.getDescripcionJustificante()));
        fraccion.setText(f.getNombrefraccion()!=null? f.getNombrefraccion():"-");
        aceptado.setText(
                f.isPendiente()? 
                    f.isAceptado()? "Aprobado por "+f.getAceptadonombre() :
                    "Pendiente aprobación" :
                !f.isAceptado() && !f.isAprobado()? "Rechazado por "+f.getAceptadonombre():
                    f.isAceptado()? "Aprobado por "+f.getAceptadonombre() : "-");
        aprobado.setText( 
                f.isPendiente()? "Pendiente aprobación":
                !f.isAceptado() && !f.isAprobado()? "-":
                f.isAprobado()? "Aprobado por "+f.getAprobadonombre():
                "Rechazado por "+f.getAprobadonombre());
        
        coment.setDisable(!f.isPendiente());
                                   
        String name = Integer.parseInt(f.getFolio())+"_"+f.getUsuario()+".pdf";
        String url = Configs.SERVER_JUSTIF() + name;  
        archivo.setText(Utils.urlExist(url)? name: "-");
        
        if (f instanceof JustificanteFolio){
            JustificanteFolio j = (JustificanteFolio) f;
            tipo.setText(j.isAsignatura()? "Asignatura": j.isDia()? "Dia": "Periodo");
            materia.setText(j.isAsignatura()? j.getNombremateria()+" ("+j.getCrn()+")":"-");
            fecha.setText(j.isPeriodo()? Utils.formatDate(j.getFecha_inicial()): Utils.formatDate(j.getFecha()));

            if (j.isDia() || j.isAsignatura()){
                lfechafinal.setVisible(false);
                fechafinal.setText(null);
            } else {
                lfechafinal.setVisible(true);
                fechafinal.setText(Utils.formatDate(j.getFecha_final()));
            }
        } else if (f instanceof JustificantePeriodo){
            JustificantePeriodo j = (JustificantePeriodo) f;
            tipo.setText( j.isDia()? "Dia": "Periodo");
            materia.setText("-");
            fecha.setText( Utils.formatDate(j.getFechaInicial()));

            if (j.isDia()){
                lfechafinal.setVisible(false);
                fechafinal.setText(null);
            } else {
                lfechafinal.setVisible(true);
                fechafinal.setText(Utils.formatDate(j.getFechaFinal()));
            }
        } else if (f instanceof JustificanteAsignatura){
            JustificanteAsignatura j = (JustificanteAsignatura) f;
            tipo.setText("Asignatura");
            materia.setText(j.getNombremateria()+" ("+j.getCrn()+")");
            fecha.setText( Utils.formatDate(j.getFechaD()));
            lfechafinal.setVisible(false);
            fechafinal.setText(null);
        }
        
        vcomnt.getChildren().clear();
        f.setComentarios(DBQueries.getComentariosFolio(f.getFolio()));
        for (Comentario c: f.getComentarios()){
            Label l = new Label(" "+c.getComentario());
            l.setWrapText(true);  
            l.setMaxWidth(vcomnt.getWidth());
            Label l2 = new Label("\t-"+c.getNombreusuario()+ " ("+Utils.formatFullDate(c.getHorayfecha())+")");
            
            vcomnt.getChildren().addAll(l,l2);
        }
    }
    
    @FXML private void agregarComentario(){
        if (!coment.getText().isEmpty()){
            Boolean resp = DBQueries.addComentarioAFolio(folio.getText(), coment.getText(),
                    Autenticator.getCurrentUser().getCodigo());

            if (resp){
                Label l = new Label(coment.getText());
                l.setWrapText(true);  
                l.setMaxWidth(vcomnt.getWidth());

                Label l2 = new Label("\t\t-"+Autenticator.getCurrentUser().getNombre()+ " ( Ahora )");                
                
                vcomnt.getChildren().addAll(0,FXCollections.observableArrayList(l,l2));  
                coment.clear();
            }
        }
    }
    
    @FXML private void hide(){
        this.setVisible(false);        
    }
    public void show(Folio justificante){
        loadJustificanteInfo(justificante);
        this.setVisible(true);
    }
    public void show(JustificanteFolio justificante){
        loadJustificanteInfo(justificante);
        this.setVisible(true);
    }
    public void show(JustificantePeriodo justificante){
        loadJustificanteInfo(justificante);
        this.setVisible(true);
    } 
    public void show(JustificanteAsignatura justificante){
        loadJustificanteInfo(justificante);
        this.setVisible(true);
    }
    
    @FXML private void downloadFile(){
        if (!archivo.getText().equals("-")){
            if (loadFileService.isRunning()) loadFileService.cancel();
            loadFileService.reset();
            loadFileService.start();        
        }
    }
}
