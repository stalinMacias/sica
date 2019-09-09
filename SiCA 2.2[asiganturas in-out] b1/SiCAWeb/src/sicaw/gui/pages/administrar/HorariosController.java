package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Duration;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.horarios.HorarioCrn;
import sica.common.objetos.Crn;
import sica.common.objetos.MateriaSimple;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;

public class HorariosController implements Initializable {

    private ObservableList <Usuario> usuarios;
    private ObservableList <MateriaSimple> materias;
    private ChangeListener changeCrnsShowed;
    private ChangeListener changeValCrn;
    private Crn currentCrn;    
    private boolean escuchando;
    
    @FXML private Button addNew;
    @FXML private SplitPane split1;
    @FXML private TextField crnCode;
    @FXML private ChoiceBox <Integer> anioBox;
    @FXML private ChoiceBox <String> cicloBox;
    @FXML private ComboBox <String> materiaBox;
    @FXML private ComboBox <String> profesorBox;
    @FXML private Button saveNewCrn;
    @FXML private Label infoNewCrn;
    @FXML private Label horarioInfo;
    
    @FXML private ChoiceBox <Integer> anioCrns;
    @FXML private ChoiceBox <String> cicloCrns;
    @FXML private TableView <Crn> tablaCrns;
    @FXML private TableColumn <Crn, String> crnCol;
    @FXML private TableColumn <Crn, String> matCol;
    @FXML private TableColumn <Crn, String> profCol;
    
    @FXML private TableView <HorarioCrn> tablaHorario;
    @FXML private TableColumn <HorarioCrn, String> bloqueCol;
    @FXML private TableColumn <HorarioCrn, String> diaCol;
    @FXML private TableColumn <HorarioCrn, String> horaCol;
    @FXML private TableColumn <HorarioCrn, String> aulaCol;
    @FXML private TableColumn <HorarioCrn, String> elimCol;
    
    @FXML private HBox addNewHor;
    @FXML private ChoiceBox <String> bloqueNew;
    @FXML private ChoiceBox <String> diaNew;
    @FXML private ChoiceBox <String> horaNew;
    @FXML private TextField aulaNew;
    @FXML private Button anadirNew;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        usuarios = FXCollections.observableArrayList();
        materias = FXCollections.observableArrayList();
        
        inicializarVistaTablas();        
        populateBoxes();        
        createFancyCells();        
        crearListeners();        
        loadCrns();
        
        escuchando = false;
        
        if (Autenticator.getCurrentUser().getPrivilegios() != Privilegios.ADMINISTRADOR){
            addNew.setVisible(false);
            addNewHor.setVisible(false);
        }
        
        saveNewCrn.setVisible(false);
        infoNewCrn.setVisible(false);
    } 
    
    private void loadCrns(){
        int a = anioCrns.getSelectionModel().getSelectedItem();
        String c = cicloCrns.getSelectionModel().getSelectedItem();
        
        tablaCrns.setItems( DBQueries.getCrns(a,c) );     
        currentCrn = null;
        
    }
    
    @SuppressWarnings("unchecked")  
    private void showDatosCrn(){
        
        stopListeners();
        
        crnCode.setText(currentCrn.getCrn());
        crnCode.disableProperty().set(true);
        materiaBox.getSelectionModel().select(currentCrn.getMateria());
        profesorBox.getSelectionModel().select(currentCrn.getProfesor());  
        anioBox.getSelectionModel().select(currentCrn.getAnio());
        cicloBox.getSelectionModel().select(currentCrn.getCiclo());
        cicloBox.setDisable(true);
        anioBox.setDisable(true);
        showHorario();
        saveNewCrn.setVisible(false);
        infoNewCrn.setVisible(false);
        anadirNew.disableProperty().set(false);
        horarioInfo.setText("Horario "+currentCrn.getCrn());
        startListeners();
    }
    
    public void showHorario(){
        Crn crn = currentCrn;
        
        ObservableList<HorarioCrn> rs = DBQueries.getHorarioCrn(
                crn.getCrn(),crn.getAnio(),crn.getCiclo(),crn.getCodProf());

        tablaHorario.setItems(rs);            
        
    }
    
    @FXML protected void nuevoCrn(){
        
        cancelarNuevoCrn();
        tablaCrns.getSelectionModel().clearSelection();
        crnCode.disableProperty().set(false);
        crnCode.requestFocus();
        anadirNew.disableProperty().set(true);
        
    }
    
    private void cancelarNuevoCrn(){
        
        stopListeners();
        crnCode.clear();
        materiaBox.getSelectionModel().clearSelection();
        profesorBox.getSelectionModel().clearSelection();
        anioBox.getSelectionModel().clearSelection();
        cicloBox.getSelectionModel().clearSelection();
        cicloBox.setDisable(false);
        anioBox.setDisable(false);
        tablaHorario.getItems().clear();
        saveNewCrn.setVisible(true);
        infoNewCrn.setVisible(true);
        anadirNew.disableProperty().set(false);
        horarioInfo.setText("Horario");
        
    }
    
    @FXML protected void guardarCrn(){
        if ( !crnCode.getText().isEmpty()
                && crnCode.getText().matches("[0-9]*")
                && !profesorBox.getSelectionModel().isEmpty() 
                && !materiaBox.getSelectionModel().isEmpty() 
                && !anioBox.getSelectionModel().isEmpty() 
                && !cicloBox.getSelectionModel().isEmpty()){
            
            
            Crn crn = new Crn();
            crn.setCrn(crnCode.getText());
            crn.setProfesor(profesorBox.getSelectionModel().getSelectedItem());
            for (Usuario us :usuarios){
                if (us.getNombre().equals(crn.getProfesor()))
                    crn.setCodProf(us.getCodigo());
            }
            crn.setMateria(materiaBox.getSelectionModel().getSelectedItem());
            for (MateriaSimple ms : materias){
                if (ms.getNombre().equals(crn.getMateria()))
                    crn.setCodMat(ms.getCodigo());
            }
            
            crn.setAnio(anioBox.getSelectionModel().getSelectedItem());
            
            crn.setCiclo(cicloBox.getSelectionModel().getSelectedItem());
                        
            boolean res = DBQueries.insertCrn(crn.getCrn(),
                    crn.getCodProf(), crn.getCodMat(), crn.getAnio(),
                    crn.getCiclo());
            
            if (res){                
                loadCrns();
                cancelarNuevoCrn();
                currentCrn = crn;
                showDatosCrn();
                return;
                
            }
        }
        Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis(50),
                new KeyValue (crnCode.rotateProperty(), 3),
                new KeyValue (profesorBox.rotateProperty(), -3),
                new KeyValue (materiaBox.rotateProperty(), 3),
                new KeyValue (anioBox.rotateProperty(), -3),
                new KeyValue (cicloBox.rotateProperty(), 3)),
            new KeyFrame(
                Duration.millis(100),
                new KeyValue (crnCode.rotateProperty(), -3),
                new KeyValue (profesorBox.rotateProperty(), 3),
                new KeyValue (materiaBox.rotateProperty(), -3),
                new KeyValue (anioBox.rotateProperty(), 3),
                new KeyValue (cicloBox.rotateProperty(), -3)),
            new KeyFrame(
                Duration.millis(150),
                new KeyValue (crnCode.rotateProperty(), 0),
                new KeyValue (profesorBox.rotateProperty(), 0),
                new KeyValue (materiaBox.rotateProperty(), 0),
                new KeyValue (anioBox.rotateProperty(), 0),
                new KeyValue (cicloBox.rotateProperty(), 0)      
            ));
        timeline.setCycleCount(2);    
        timeline.play();
        
    }
            
    private void updateCrn(){
        
        currentCrn.setProfesor(profesorBox.getSelectionModel().getSelectedItem());
            for (Usuario us :usuarios){
                if (us.getNombre().equals(currentCrn.getProfesor()))
                    currentCrn.setCodProf(us.getCodigo());
            }
            currentCrn.setMateria(materiaBox.getSelectionModel().getSelectedItem());
            for (MateriaSimple ms : materias){
                if (ms.getNombre().equals(currentCrn.getMateria()))
                    currentCrn.setCodMat(ms.getCodigo());
            }
        
        boolean res = DBQueries.updateCrn(currentCrn.getCrn(), currentCrn.getAnio(), 
                currentCrn.getCiclo(), currentCrn.getCodMat(), currentCrn.getCodProf());
        
        if (res){
            Crn temp = currentCrn;
            loadCrns();
            currentCrn = temp;
            showDatosCrn();
        }
        
    } 
    
    @FXML protected void addHorario(){
        
        if ( !bloqueNew.getSelectionModel().isEmpty() && 
                !diaNew.getSelectionModel().isEmpty() &&
                !horaNew.getSelectionModel().isEmpty()){
        
            boolean res = DBQueries.insertHorario(
                currentCrn.getCrn(), 
                currentCrn.getAnio(),
                currentCrn.getCiclo(),
                bloqueNew.getSelectionModel().getSelectedIndex() , 
                diaNew.getSelectionModel().getSelectedItem(), 
                horaNew.getSelectionModel().getSelectedItem()+":00", 
                aulaNew.getText().toUpperCase());
        
                if (res){                    
                    bloqueNew.getSelectionModel().clearSelection();
                    diaNew.getSelectionModel().clearSelection();
                    horaNew.getSelectionModel().clearSelection();
                    aulaNew.clear();
                    
                    showHorario();
                    
                    return;
                }
        
        } 
            
        Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis(50),
                new KeyValue (bloqueNew.rotateProperty(), 3),
                new KeyValue (diaNew.rotateProperty(), -3),
                new KeyValue (horaNew.rotateProperty(), 3)),
            new KeyFrame(
                Duration.millis(100),
                new KeyValue (bloqueNew.rotateProperty(), -3),
                new KeyValue (diaNew.rotateProperty(), 3),
                new KeyValue (horaNew.rotateProperty(), -3)),
            new KeyFrame(
                Duration.millis(150),
                new KeyValue (bloqueNew.rotateProperty(), 0),
                new KeyValue (diaNew.rotateProperty(), 0),
                new KeyValue (horaNew.rotateProperty(), 0)
            ));
        timeline.setCycleCount(2);    
        timeline.play();
    }
    
    private void deleteHorario(int line){
        HorarioCrn crn = tablaHorario.getItems().get(line);
        
        boolean res = DBQueries.deleteHorario(
                currentCrn.getCrn(),
                currentCrn.getAnio(),
                currentCrn.getCiclo(),
                crn.getBloque().equals("1y2")? "0":crn.getBloque() ,
                crn.getDia(),
                crn.getHora()
                );
        
        if (res){
            showHorario();
        } else {
            System.out.println(res);
        }
        
    }
    
    private void inicializarVistaTablas() {
        tablaCrns.prefWidthProperty().set(split1.widthProperty().multiply(0.3f).doubleValue());
        tablaCrns.prefWidthProperty().bind(split1.widthProperty().multiply(0.3f));
                        
        crnCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        matCol.setCellValueFactory(new PropertyValueFactory<>("materia"));
        profCol.setCellValueFactory(new PropertyValueFactory<>("profesor"));        
        crnCol.prefWidthProperty().bind(tablaCrns.widthProperty().multiply(1/10f));
        matCol.prefWidthProperty().bind(tablaCrns.widthProperty().multiply(4/10f));
        profCol.prefWidthProperty().bind(tablaCrns.widthProperty().multiply(5/10f));        
        
        bloqueCol.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        diaCol.setCellValueFactory(new PropertyValueFactory<>("dia"));
        horaCol.setCellValueFactory(new PropertyValueFactory<>("hora"));
        aulaCol.setCellValueFactory(new PropertyValueFactory<>("aula"));
        elimCol.setCellValueFactory(new PropertyValueFactory<>("crn"));
        
        bloqueCol.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f));        
        diaCol.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f));
        horaCol.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f));        
        aulaCol.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f));        
        elimCol.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f));        
        
        bloqueNew.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f).subtract(10));        
        diaNew.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f).subtract(10));
        horaNew.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f).subtract(10));        
        aulaNew.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f).subtract(10));        
        anadirNew.prefWidthProperty().bind(tablaHorario.widthProperty().multiply(2/10f).subtract(10));        
        
        bloqueNew.setTooltip(new Tooltip("4x4"));
        diaNew.setTooltip(new Tooltip("Dia"));
        horaNew.setTooltip(new Tooltip("Hora"));
        aulaNew.setPromptText("Aula");
    }

    @SuppressWarnings("unchecked")
    private void crearListeners() {
        
        changeCrnsShowed  = new ChangeListener(){
            @Override
            public void changed(ObservableValue ov, Object oldVal, Object newVal) {                
                if (newVal != null){
                    cancelarNuevoCrn();
                    loadCrns();
                }                
            }            
        };
        
        anioCrns.getSelectionModel().selectedItemProperty().addListener(changeCrnsShowed);
        cicloCrns.getSelectionModel().selectedItemProperty().addListener(changeCrnsShowed);        
        
        tablaCrns.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Crn> observable, Crn oldValue, Crn newValue) -> {
            cancelarNuevoCrn();
            if (newValue!=null){
                currentCrn = newValue;
                showDatosCrn();
            }
        });
        
        changeValCrn  = (ChangeListener)(ObservableValue ov, Object oldVal, Object newVal) -> {
            if (newVal != null){
                updateCrn();
            }            
        };       
        
    }
    
    @SuppressWarnings("unchecked")
    private void startListeners(){
        if(!escuchando) {
            materiaBox.getSelectionModel().selectedItemProperty().addListener(changeValCrn);
            profesorBox.getSelectionModel().selectedItemProperty().addListener(changeValCrn);
            escuchando = true;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void stopListeners(){
        if (escuchando) {
            materiaBox.getSelectionModel().selectedItemProperty().removeListener(changeValCrn);
            profesorBox.getSelectionModel().selectedItemProperty().removeListener(changeValCrn);
            escuchando = false;
        }
    }
   
    private void populateBoxes() {
        
        usuarios = DBQueries.getUsuariosNoAdministrativos();

        for (Usuario user : usuarios){
            profesorBox.getItems().add(user.getNombre());
        }   

        materias = DBQueries.getMaterias();

        for (MateriaSimple mat : materias){             
            materiaBox.getItems().add(mat.getNombre());
        }         
        
        Calendar cal = Calendar.getInstance();
        Integer val = cal.get(Calendar.YEAR);
        
        anioCrns.getItems().clear();
        anioBox.getItems().clear();
        
        for (Integer i=val-1 ; i<val+2; i++ ){
            anioCrns.getItems().add(i);
            anioBox.getItems().add(i);        
        }
        
        anioCrns.getSelectionModel().select(val);
        
        String c = (cal.get(Calendar.MONTH) < Calendar.AUGUST) ? "A": "B";
        cicloCrns.getSelectionModel().select(c);
        
        
    }

    @SuppressWarnings("unchecked")  
    private void createFancyCells() {      
        final Callback cb = (Callback<ListView<String>,ListCell<String>>) 
                (ListView<String> p) -> new ListCell<String>() {
                    
            @Override protected void updateItem(String item, boolean empty) {
                if (item!= null || !empty) {
                    setText(item);
                    setTextFill(Color.BLACK);
                } else {
                    setText(null);
                }
                
            }
        };    
    
        materiaBox.setButtonCell((ListCell)cb.call(null)); 
        profesorBox.setButtonCell((ListCell)cb.call(null));
        
        elimCol.setCellFactory(new Callback<TableColumn<HorarioCrn,String>,TableCell<HorarioCrn,String>>(){   
            @Override public TableCell<HorarioCrn, String> call(final TableColumn<HorarioCrn, String> param) {   
                return new TableCell<HorarioCrn, String>(){
                    @Override public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item!=null){                                 
                            Hyperlink h = new Hyperlink("Eliminar");
                            h.setTextFill(Color.DARKBLUE);
                            h.setOnAction(e -> deleteHorario(getIndex()));                                              
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
