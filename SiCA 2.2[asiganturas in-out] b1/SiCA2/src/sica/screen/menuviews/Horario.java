package sica.screen.menuviews;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import sica.Screen;
import sica.ScreenManager;
import sica.common.DBQueries;
import sica.common.horarios.HorarioCrn;
import sica.common.horarios.HorarioUsuario;

public class Horario extends Screen implements Initializable {

    @FXML private ToggleButton asignaturasBtn;
    @FXML private ToggleButton jornadaBtn;
    
    @FXML private TableView<HorarioCrn> asignaturas;
    @FXML private TableColumn <HorarioCrn, String> bloqueCol;
    @FXML private TableColumn <HorarioCrn, String> crnCol;
    @FXML private TableColumn <HorarioCrn, String> materiaCol;
    @FXML private TableColumn <HorarioCrn, String> diaCol;
    @FXML private TableColumn <HorarioCrn, String> horarioCol;
    @FXML private TableColumn <HorarioCrn, String> aulaCol;

    @FXML private TableView<HorarioUsuario> jornada;
    @FXML private TableColumn <HorarioUsuario, String> diaJCol;
    @FXML private TableColumn <HorarioUsuario, String> entradaJCol;
    @FXML private TableColumn <HorarioUsuario, String> salidaJCol;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        initTables();
        asignaturas.visibleProperty().bind(asignaturasBtn.selectedProperty());
        jornada.visibleProperty().bind(jornadaBtn.selectedProperty());
        
    }  
    
    public void initTables(){
        bloqueCol.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        materiaCol.setCellValueFactory(new PropertyValueFactory<>("materia"));
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        diaCol.setCellValueFactory(new PropertyValueFactory<>("dia"));
        horarioCol.setCellValueFactory(new PropertyValueFactory<>("horario"));
        aulaCol.setCellValueFactory(new PropertyValueFactory<>("aula"));
        
        bloqueCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(1/20f));
        crnCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(1/10f));
        materiaCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(5/10f));
        diaCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(3/20f));
        horarioCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(1/10f));
        aulaCol.prefWidthProperty().bind(asignaturas.widthProperty().multiply(1/10f).subtract(3));
        
        diaJCol.setCellValueFactory(new PropertyValueFactory<>("dias"));
        entradaJCol.setCellValueFactory(new PropertyValueFactory<>("entrada"));
        salidaJCol.setCellValueFactory(new PropertyValueFactory<>("salida"));
        
        diaJCol.prefWidthProperty().bind(jornada.widthProperty().multiply(1/3f));
        entradaJCol.prefWidthProperty().bind(jornada.widthProperty().multiply(1/3f));
        salidaJCol.prefWidthProperty().bind(jornada.widthProperty().multiply(1/3f).subtract(3));        
    }    
    
    @Override public void start() {
        ObservableList<HorarioCrn> list = 
                DBQueries.getAsignaturasActuales(ScreenManager.menu().getUsuario().getCodigo());
        
        asignaturas.setItems(list);
        
        HorarioUsuario dom = new HorarioUsuario("DOMINGO", "--:--:--", "--:--:--");
        HorarioUsuario lun = new HorarioUsuario("LUNES", "--:--:--", "--:--:--");
        HorarioUsuario mar = new HorarioUsuario("MARTES", "--:--:--", "--:--:--");
        HorarioUsuario mie = new HorarioUsuario("MIERCOLES", "--:--:--", "--:--:--");
        HorarioUsuario jue = new HorarioUsuario("JUEVES", "--:--:--", "--:--:--");
        HorarioUsuario vie = new HorarioUsuario("VIERNES", "--:--:--", "--:--:--");
        HorarioUsuario sab = new HorarioUsuario("SABADO", "--:--:--", "--:--:--");
        
        ObservableList<HorarioUsuario> h = 
                DBQueries.getHorarioActualUsuario(ScreenManager.menu().getUsuario().getCodigo());
                
        if (!h.isEmpty()) {        
            for (HorarioUsuario hu : h){                
                for (char c : hu.getDias().toCharArray()){
                    switch(c){                        
                        case '1': 
                            dom.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                dom.setSalida(hu.getSalida());
                            } else {
                                lun.setSalida(hu.getSalida());
                            }
                            break;
                        case '2':
                            lun.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                lun.setSalida(hu.getSalida());
                            } else {
                                mar.setSalida(hu.getSalida());
                            }
                            break;
                        case '3':
                            mar.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                mar.setSalida(hu.getSalida());
                            } else {
                                mie.setSalida(hu.getSalida());
                            }
                            break;
                        case '4': 
                            mie.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                mie.setSalida(hu.getSalida());
                            } else {
                                jue.setSalida(hu.getSalida());
                            }
                            break;
                        case '5': 
                            jue.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                jue.setSalida(hu.getSalida());
                            } else {
                                vie.setSalida(hu.getSalida());
                            }
                            break;
                        case '6':
                            vie.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                vie.setSalida(hu.getSalida());
                            } else {
                                sab.setSalida(hu.getSalida());
                            }
                            break;
                        case '7': 
                            sab.setEntrada(hu.getEntrada());
                            if (!hu.getDiasig()){
                                sab.setSalida(hu.getSalida());
                            } else {
                                dom.setSalida(hu.getSalida());
                            }
                            break;
                    }
                }
            }
        }
        jornada.setItems(FXCollections.observableArrayList(dom,lun,mar,mie,jue,vie,sab));
        
        if (!list.isEmpty()){
            asignaturasBtn.setSelected(true);
        } else {
            jornadaBtn.setSelected(true);
        }
    }
    
    
}
