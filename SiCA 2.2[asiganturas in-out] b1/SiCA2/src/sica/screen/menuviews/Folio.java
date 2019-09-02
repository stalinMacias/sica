package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sica.Screen;
import sica.ScreenManager;
import sica.common.DBQueries;
import sica.common.Utils;
import sica.common.justificantes.JustificanteFolio;

public class Folio extends Screen implements Initializable {

    private JustificanteFolio jFolio;
    
    @FXML private Label folio;
    @FXML private Label estatus;
    @FXML private Label tipo;
    @FXML private Label fechayhora;
    @FXML private Label fecha;
    @FXML private Label fechafinal;
    @FXML private Label aceptado;
    @FXML private Label aprobado;
    @FXML private Label justificante;
    @FXML private Label fraccion;
    @FXML private VBox vcomnt;
    @FXML private TextArea coment;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    public void setFolio(JustificanteFolio jFolio){
        this.jFolio = jFolio;
        folio.setText("Consulta de folio No. "+jFolio.getFolio());
        estatus.setText(
            jFolio.isPendiente()? "Pendiente":                                
            jFolio.isAprobado()? "Aprobado":
            "Rechazado");
        estatus.setTextFill(
            jFolio.isPendiente()? Color.ORANGERED:                                
            jFolio.isAprobado()? Color.GREEN:
            Color.RED );            
        fechayhora.setText(Utils.formatTime(jFolio.getFechayHora())+", "+Utils.fullDateFormat(jFolio.getFechayHora()));
        justificante.setText(jFolio.getNombrejustificante());
        fraccion.setText(jFolio.getNombrefraccion()!=null? jFolio.getNombrefraccion() : "-");
        aceptado.setText(
                jFolio.isPendiente()? 
                    jFolio.isAceptado()? "Aprobado " :
                    "Pendiente aprobación" :
                !jFolio.isAceptado() && !jFolio.isAprobado()? "Rechazado":
                    jFolio.isAceptado()? "Aprobado por "+jFolio.getAceptadonombre() : "-");
        aprobado.setText( 
                jFolio.isPendiente()? "Pendiente aprobación":
                !jFolio.isAceptado() && !jFolio.isAprobado()? "-":
                jFolio.isAprobado()? "Aprobado por "+jFolio.getAprobadonombre():
                "Rechazado");
        
        coment.setDisable(!jFolio.isPendiente());
                                   
        fecha.setText(jFolio.isPeriodo()? Utils.formatDate(jFolio.getFecha_inicial()): Utils.formatDate(jFolio.getFecha()));
        
        if (jFolio.isAsignatura()){
            tipo.setText("Asignatura - "+jFolio.getNombremateria());
            fechafinal.setText("-");
        } else if (jFolio.isDia()){
            tipo.setText("Día");
            fechafinal.setText("-");
        } else {
            tipo.setText("Periodo");
            fechafinal.setText(Utils.formatDate(jFolio.getFecha_final()));
        }
        
        vcomnt.getChildren().clear();        
        
        jFolio.setComentarios(DBQueries.getComentariosFolio(jFolio.getFolio()));
        jFolio.getComentarios().stream().forEach(c -> {
            Label l = new Label(" "+c.getComentario());
            l.setWrapText(true);  
            l.maxWidthProperty().bind(vcomnt.widthProperty());
            Label l2 = new Label("\t-"+c.getNombreusuario()+ " ("+Utils.formatFullDate(c.getHorayfecha())+")");
            VBox.setMargin(l2, new Insets(0, 0, 10, 0));
            vcomnt.getChildren().addAll(l,l2);
        });
    }
    
    @FXML protected void agregarComentario(){        
        if (!jFolio.isPendiente()){
            ScreenManager.principal().avisar("Folio cerrado");
            
        } else if (!coment.getText().isEmpty()){
            Boolean resp = DBQueries.addComentarioAFolio(jFolio.getFolio(), coment.getText(),
                   ScreenManager.menu().getUsuario().getCodigo());

            if (resp){
                Label l = new Label(" "+coment.getText());
                l.setWrapText(true);  
                l.setMaxWidth(vcomnt.getWidth());

                Label l2 = new Label("\t-"+ScreenManager.menu().getUsuario().getNombre()+ " ( Ahora )");                
                VBox.setMargin(l2, new Insets(0, 0, 10, 0));
                
                vcomnt.getChildren().addAll(0,FXCollections.observableArrayList(l,l2));  
                coment.clear();
            }
        } 
    }
    
    @Override
    public void start() {
        coment.clear();
    }
    
}
