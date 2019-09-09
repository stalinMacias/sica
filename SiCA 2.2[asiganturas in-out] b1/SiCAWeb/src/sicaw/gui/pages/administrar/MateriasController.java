package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.objetos.Departamento;
import sica.common.objetos.MateriaSimple;
import sica.common.usuarios.Privilegios;

public class MateriasController implements Initializable {

    private ChangeListener <Boolean> changelistener;
    private ChangeListener <Departamento> changelistener2;
    private boolean listening;
        
    @FXML private Button addNew;
    @FXML private SplitPane split;
    @FXML private HBox botonesNuevaMateria;
    
    @FXML private TableView <MateriaSimple> tablaMaterias;
    @FXML private TableColumn <MateriaSimple, String> codigoColUs; 
    @FXML private TableColumn <MateriaSimple, String> nombreColUs;
    @FXML private TableColumn <MateriaSimple, String> depColUs;
    
    @FXML private TextField infoCodigo;
    @FXML private TextField infoNombre;
    @FXML private ComboBox <Departamento> infoDepto;    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        inicializarVistaTablas();
        
        crearListeners();
        
        loadMaterias();
        
        infoDepto.getItems().clear();
        for (Departamento d : DBQueries.getDepartamentos()){
            if (d.getCodigo().matches("[A-Z]*") && !d.getCodigo().equals("NA")){
                infoDepto.getItems().add(d);
            }
        }
        
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR){
            infoNombre.focusedProperty().addListener(changelistener);
            infoDepto.getSelectionModel().selectedItemProperty().addListener(changelistener2);            
            listening = true;
            System.out.println("listeners añadidos");
        } else {
            addNew.setVisible(false);
            infoNombre.setEditable(false);
            infoCodigo.setEditable(false); 
            listening = false;
        }
        
        
    }    
    
    private void showDatosMateria(MateriaSimple mat){
        
        stopListeners();
        
        infoCodigo.setText(mat.getCodigo());
        infoNombre.setText(mat.getNombre());
        
        if (!infoDepto.getSelectionModel().isEmpty()){
            infoDepto.getSelectionModel().clearSelection();
        }
        
        for (Departamento d: infoDepto.getItems()){
            if (d.getCodigo().equals(mat.getDepartamento())){
                infoDepto.getSelectionModel().select(d);
                break;
            }
        }
        startListeners();
        
    }

    @FXML protected void updateValMateria(){
        
        if (listening){
            System.out.println("updating materia");
            if (!infoCodigo.getText().isEmpty() && !infoNombre.getText().isEmpty() 
                    && !infoDepto.getSelectionModel().isEmpty()){
                
                int s = tablaMaterias.getSelectionModel().getSelectedIndex();

                String cod = infoCodigo.getText();
                String nom = infoNombre.getText().toUpperCase();
                String dept = infoDepto.getSelectionModel().getSelectedItem().getCodigo();

                boolean result = DBQueries.updateMateria(cod, nom, dept);

                if (result){
                    loadMaterias();
                    tablaMaterias.getSelectionModel().select(s);
                    tablaMaterias.requestFocus();
                } else {
                    System.out.println("Error actualizando materia");
                }

            } 
        }
    }
         
    @FXML protected void crearMateria(){
        
        stopListeners();
        infoNombre.clear();        
        tablaMaterias.getSelectionModel().clearSelection();
        botonesNuevaMateria.setVisible(true);
        infoCodigo.clear();
        infoCodigo.setDisable(false);
        infoCodigo.requestFocus();
    }
    
    @FXML protected void cancelarNuevaMateria(){
        infoCodigo.setDisable(true);
        infoCodigo.clear();
        infoNombre.clear();
        botonesNuevaMateria.setVisible(false);
    }
    
    @FXML protected void guardarNuevaMateria(){
        
        if (!infoCodigo.getText().isEmpty() && !infoNombre.getText().isEmpty()) {            
            
            String cod = infoCodigo.getText();
            String nom = infoNombre.getText();
            String dept = infoDepto.getSelectionModel().getSelectedItem().getCodigo();

            boolean result = DBQueries.insertMateria(cod, nom,dept);            
            if (result){
                cancelarNuevaMateria();
                loadMaterias();
            } else {
                System.out.println("Error guardando materia");
            }   
        }    
    }
    
    private void inicializarVistaTablas() {
        
        tablaMaterias.prefWidthProperty().set(split.widthProperty().multiply(0.5f).doubleValue());
        tablaMaterias.prefWidthProperty().bind(split.widthProperty().multiply(0.5f));
        
        codigoColUs.setCellValueFactory(new PropertyValueFactory<MateriaSimple, String>("codigo"));
        nombreColUs.setCellValueFactory(new PropertyValueFactory<MateriaSimple, String>("nombre"));
        
        Callback <CellDataFeatures<MateriaSimple,String>,ObservableValue<String>> cb = 
                new Callback<CellDataFeatures<MateriaSimple,String>,ObservableValue<String>>(){
            @Override
            public ObservableValue<String> call(CellDataFeatures<MateriaSimple, String> p) {
               ObservableValue<String> o = new SimpleStringProperty();
               for (Departamento d : infoDepto.getItems()){
                   if (d.getCodigo().equals(p.getValue().getDepartamento())){
                       o = new SimpleStringProperty(d.getNombre());
                       break;
                   }
               }
               return o;
            }
        };
        depColUs.setCellValueFactory(cb);        
        
        
        codigoColUs.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(1/10f));
        nombreColUs.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(5/10f));
        depColUs.prefWidthProperty().bind(tablaMaterias.widthProperty().multiply(4/10f));
        
        botonesNuevaMateria.setVisible(false);
        
    }

    private void crearListeners() {
        
        tablaMaterias.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<MateriaSimple>() {
                    @Override
                    public void changed(ObservableValue<? extends MateriaSimple> observable,
                        MateriaSimple oldValue, MateriaSimple newValue) {
                        cancelarNuevaMateria();                        
                        
                        if (newValue!=null){                            
                            showDatosMateria(newValue);
                        }
                    }
                });
        
        changelistener = new ChangeListener<Boolean>(){
            @Override public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                if (newVal != null && !newVal){
                    updateValMateria();
                }
            }
            
        };
        
        changelistener2 = new ChangeListener<Departamento>(){
            @Override
            public void changed(ObservableValue ov, Departamento oldVal, Departamento newVal) {
                if (newVal!= null){ 
                    updateValMateria();
                }
            }
            
        };
        
        infoDepto.setCellFactory(new Callback<ListView<Departamento>, ListCell<Departamento>>(){
            @Override
            public ListCell<Departamento> call(ListView<Departamento> p) {                
                return new ListCell<Departamento>(){
                    @Override
                    protected void updateItem(Departamento t, boolean empty) {
                        super.updateItem(t,empty);
                        if (!empty){
                            setText(t.getNombre());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
               
        infoDepto.setButtonCell( (ListCell<Departamento>) infoDepto.getCellFactory().call(null) );
        
    }
    
    private void startListeners(){   
        listening = (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR);
    }
    
    private void stopListeners(){
        listening = false;
    }


    private void loadMaterias() {        
        
        tablaMaterias.setItems(DBQueries.getMaterias());
        
    }
}
