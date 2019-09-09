package sicaw.gui.pages.administrar.usuarios;

import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sica.common.DBQueries;
import sica.common.horarios.HorarioUsuario;
import sica.common.horarios.ReportesHorarios;
import sica.common.usuarios.Usuario;
import sicaweb.Utils;
import sicaw.gui.Principal;

public class HorarioController implements Initializable {

    private Usuario currentUser;    
    
    @FXML private VBox panel;
    @FXML private Label info;
    
    @FXML private CheckBox diaSig;
    @FXML private CheckBox dom;
    @FXML private CheckBox lun;
    @FXML private CheckBox mar;
    @FXML private CheckBox mier;
    @FXML private CheckBox jue;
    @FXML private CheckBox vier;
    @FXML private CheckBox sab;
    @FXML private TextField lunEnt;
    @FXML private TextField domEnt;
    @FXML private TextField marEnt;
    @FXML private TextField mierEnt;
    @FXML private TextField jueEnt;
    @FXML private TextField vierEnt;
    @FXML private TextField sabEnt;
    @FXML private TextField domSal;
    @FXML private TextField lunSal;
    @FXML private TextField marSal;
    @FXML private TextField mierSal;
    @FXML private TextField jueSal;
    @FXML private TextField vierSal;
    @FXML private TextField sabSal;
    
    @FXML private Label cargaHoraria;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        crearListeners();        
    }    
    
    public void setUser(Usuario usr){
        currentUser = usr;
        
        if (usr.getTipo().equals("Asignatura")){
            panel.setDisable(true);
            info.setText("Este tipo de usuario no tiene horario");
            
        } else {
            
            lun.setSelected(false);
            mar.setSelected(false);
            mier.setSelected(false);
            jue.setSelected(false);
            vier.setSelected(false);
            sab.setSelected(false);
            dom.setSelected(false);
            
            panel.setDisable(false);
            info.setText("Mostrando horario actual");            
            ObservableList<HorarioUsuario> h = DBQueries.getHorarioActualUsuario(usr.getCodigo());
            
            for (HorarioUsuario hu : h){                
                diaSig.setSelected(hu.getDiasig());
                
                for (char c : hu.getDias().toCharArray()){
                    switch(c){                        
                        case '1': dom.setSelected(true);
                            domEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                domSal.setText(hu.getSalida());
                            } else {
                                lunSal.setText(hu.getSalida());
                            }
                            break;
                        case '2': lun.setSelected(true);
                            lunEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                lunSal.setText(hu.getSalida());
                            } else {
                                marSal.setText(hu.getSalida());
                            }
                            break;
                        case '3': mar.setSelected(true);
                            marEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                marSal.setText(hu.getSalida());
                            } else {
                                mierSal.setText(hu.getSalida());
                            }
                            break;
                        case '4': mier.setSelected(true);
                            mierEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                mierSal.setText(hu.getSalida());
                            } else {
                                jueSal.setText(hu.getSalida());
                            }
                            break;
                        case '5': jue.setSelected(true);
                            jueEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                jueSal.setText(hu.getSalida());
                            } else {
                                vierSal.setText(hu.getSalida());
                            }
                            break;
                        case '6': vier.setSelected(true);
                            vierEnt.setText(hu.getEntrada());
                            if (!hu.getDiasig()){
                                vierSal.setText(hu.getSalida());
                            } else {
                                sabSal.setText(hu.getSalida());
                            }
                            break;
                        case '7': sab.setSelected(true);
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
            
            cargaHoraria.setText(Utils.millisToTime(ReportesHorarios.getCargaHorariaSemanal(h)));
        }
    }   
        
    
    
    @FXML protected void guardar(){
        ObservableList<HorarioUsuario> hors2 = getHorariosSeleccionados();
        if (hors2.size()>0){
            DBQueries.deleteHorarios(currentUser.getCodigo());
            for (HorarioUsuario h : hors2){
                boolean saveH = DBQueries.saveHorario(currentUser.getCodigo(),h.getDias(),
                        h.getEntrada(),h.getSalida(),diaSig.isSelected());
                
                if (!saveH){
                    Principal.avisar("Error guardando horarios");
                }
            }
            
        } else {
            Principal.avisar("Introducir horario");
        }
        
    }
    
    @FXML protected void nuevo(){
        ObservableList<HorarioUsuario> hors2 = getHorariosSeleccionados();
        if (hors2.size()>0){
            DBQueries.endHorarios(currentUser.getCodigo());
            for (HorarioUsuario h : hors2){
                boolean saveH = DBQueries.saveHorario(currentUser.getCodigo(),h.getDias(),
                        h.getEntrada(),h.getSalida(),diaSig.isSelected());
                
                if (!saveH){
                    Principal.avisar("Error guardando horarios");
                }
            }
            
        } else {
            Principal.avisar("Introducir horario");
        }
    }
    
    private ObservableList<HorarioUsuario> getHorariosSeleccionados(){
        
        ObservableList<HorarioUsuario> hors = FXCollections.observableArrayList();
      
        if (dom.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("1");
            h.setEntrada(domEnt.getText());
            h.setSalida(diaSig.isSelected()? lunSal.getText() : domSal.getText());
            hors.add(h);
        }
              
        if (lun.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("2");
            h.setEntrada(lunEnt.getText());
            h.setSalida(diaSig.isSelected()? marSal.getText() : lunSal.getText());
            hors.add(h);
        }        
              
        if (mar.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("3");
            h.setEntrada(marEnt.getText());
            h.setSalida(diaSig.isSelected()? mierSal.getText() : marSal.getText());
            hors.add(h);
        }        
              
        if (mier.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("4");
            h.setEntrada(mierEnt.getText());
            h.setSalida(diaSig.isSelected()? jueSal.getText() : mierSal.getText());
            hors.add(h);
        }       
              
        if (jue.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("5");
            h.setEntrada(jueEnt.getText());
            h.setSalida(diaSig.isSelected()? vierSal.getText() : jueSal.getText());
            hors.add(h);
        }        
              
        if (vier.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("6");
            h.setEntrada(vierEnt.getText());
            h.setSalida(diaSig.isSelected()? sabSal.getText() : vierSal.getText());
            hors.add(h);
        }
        
              
        if (sab.isSelected()){
            HorarioUsuario h = new HorarioUsuario();
            h.setDias("7");
            h.setEntrada(sabEnt.getText());
            h.setSalida(diaSig.isSelected()? domSal.getText() : sabSal.getText());
            hors.add(h);
        }
        
        ObservableList<HorarioUsuario> hors2 = FXCollections.observableArrayList();
        
        if (hors.size()>0){
            hors2.add(hors.get(0));
            hors.remove(0);
            
            while (hors.size()>0){
                boolean f = false;
                for (HorarioUsuario h : hors2 ){
                    if (h.getEntrada().equals(hors.get(0).getEntrada()) 
                            && h.getSalida().equals(hors.get(0).getSalida())){
                        
                        h.addDia(hors.get(0).getDias());                        
                        f = true;
                    } 
                }
                if (!f){
                    hors2.add(hors.get(0));
                }
                
                hors.remove(0);
            }
        }
        
        cargaHoraria.setText(Utils.millisToTime(ReportesHorarios.getCargaHorariaSemanal(hors2)));
        
        return hors2;
        
    }
    public void crearListeners(){
        
        domEnt.disableProperty().bind( dom.selectedProperty().or(diaSig.selectedProperty().not().and(dom.selectedProperty())).not() );
        lunEnt.disableProperty().bind( lun.selectedProperty().or(diaSig.selectedProperty().not().and(lun.selectedProperty())).not() );
        marEnt.disableProperty().bind( mar.selectedProperty().or(diaSig.selectedProperty().not().and(mar.selectedProperty())).not() );
        mierEnt.disableProperty().bind( mier.selectedProperty().or(diaSig.selectedProperty().not().and(mier.selectedProperty())).not() );
        jueEnt.disableProperty().bind( jue.selectedProperty().or(diaSig.selectedProperty().not().and(jue.selectedProperty())).not() );
        vierEnt.disableProperty().bind( vier.selectedProperty().or(diaSig.selectedProperty().not().and(vier.selectedProperty())).not() );
        sabEnt.disableProperty().bind( sab.selectedProperty().or(diaSig.selectedProperty().not().and(sab.selectedProperty())).not() );
                
        domSal.disableProperty().bind( dom.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(sab.selectedProperty())).not() );
        lunSal.disableProperty().bind( lun.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(dom.selectedProperty())).not() );
        marSal.disableProperty().bind( mar.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(lun.selectedProperty())).not() );
        mierSal.disableProperty().bind( mier.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(mar.selectedProperty())).not() );
        jueSal.disableProperty().bind( jue.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(mier.selectedProperty())).not() );
        vierSal.disableProperty().bind( vier.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(jue.selectedProperty())).not() );
        sabSal.disableProperty().bind( sab.selectedProperty().and(diaSig.selectedProperty().not()).or(diaSig.selectedProperty().and(vier.selectedProperty())).not() );
        
        dom.disableProperty().bind( diaSig.selectedProperty().and(sab.selectedProperty()) );
        lun.disableProperty().bind( diaSig.selectedProperty().and(dom.selectedProperty()) );
        mar.disableProperty().bind( diaSig.selectedProperty().and(lun.selectedProperty()) );
        mier.disableProperty().bind( diaSig.selectedProperty().and(mar.selectedProperty()) );
        jue.disableProperty().bind( diaSig.selectedProperty().and(mier.selectedProperty()) );
        vier.disableProperty().bind( diaSig.selectedProperty().and(jue.selectedProperty()) );
        sab.disableProperty().bind( diaSig.selectedProperty().and(vier.selectedProperty()) );
        
        domEnt.focusedProperty().addListener(ch(domEnt));
        lunEnt.focusedProperty().addListener(ch(lunEnt));
        marEnt.focusedProperty().addListener(ch(marEnt));
        mierEnt.focusedProperty().addListener(ch(mierEnt));
        jueEnt.focusedProperty().addListener(ch(jueEnt));
        vierEnt.focusedProperty().addListener(ch(vierEnt));
        sabEnt.focusedProperty().addListener(ch(sabEnt));
        
        domSal.focusedProperty().addListener(ch(domSal));
        lunSal.focusedProperty().addListener(ch(lunSal));
        marSal.focusedProperty().addListener(ch(marSal));
        mierSal.focusedProperty().addListener(ch(mierSal));
        jueSal.focusedProperty().addListener(ch(jueSal));
        vierSal.focusedProperty().addListener(ch(vierSal));
        sabSal.focusedProperty().addListener(ch(sabSal));
        
        ChangeListener <Boolean> ch = (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (!t1){
                sab.setSelected(false);vier.setSelected(false);
                jue.setSelected(false);mier.setSelected(false);
                mar.setSelected(false);lun.setSelected(false);
                dom.setSelected(false);
                
            }            
        };
        diaSig.selectedProperty().addListener(ch);
        
    }
    
    public ChangeListener<Boolean> ch (final TextField tf){
        return  (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (!t1){
                String text = tf.getText();
                try {
                    Utils.getSimpleTimeFormat().parse(text);
                    
                } catch (ParseException e){
                    text = "";
                }
                tf.setText(text);
            }
        };
    }
}
