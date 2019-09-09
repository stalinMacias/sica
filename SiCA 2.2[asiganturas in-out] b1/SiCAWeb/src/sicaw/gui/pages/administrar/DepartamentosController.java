package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sica.common.Autenticator;
import sica.common.DBQueries;
import sica.common.objetos.Departamento;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;

public class DepartamentosController implements Initializable {

    private ObservableList <Usuario> usuarios;
        
    @FXML private TableView <Departamento>tablaDeptos;
    @FXML private TableColumn <Departamento, String> nombreCol;
    @FXML private TableColumn <Departamento, String> jefeCol;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        inicializarTablas();        
        loadUsuarios();        
        createFancyCell();        
        loadDepartamentos();
        
    }    
    
    public void loadUsuarios(){
        usuarios = DBQueries.getTodosUsuarios();
        
    }
    
    private void loadDepartamentos(){        
        tablaDeptos.setItems(DBQueries.getDepartamentos());
        
    }
    
    private void updateDepartamento(int line, String newVal){
        
        Departamento temp = new Departamento();                
        Departamento d = tablaDeptos.getItems().get(line);
          
        temp.setCodigo(d.getCodigo());
        temp.setNombre(d.getNombre());

        for (Usuario stemp : usuarios){
            if (stemp.getNombre().equals(newVal)){
                temp.setJefe(stemp.getCodigo());                
                break;
            }
        }
        
        DBQueries.updateDepartamento(temp.getCodigo(), temp.getJefe());                        
        
        tablaDeptos.getItems().set(line, temp);
    }

    private void inicializarTablas() {
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        jefeCol.setCellValueFactory(new PropertyValueFactory<>("jefe"));
        
        nombreCol.prefWidthProperty().bind(tablaDeptos.widthProperty().multiply(1/2f).subtract(15));
        jefeCol.prefWidthProperty().bind(tablaDeptos.widthProperty().multiply(1/2f).subtract(15));  
    }

    @SuppressWarnings("unchecked")    
    private void createFancyCell() {      
        final Callback cb = (Callback<ListView<String>,ListCell<String>>) (ListView<String> p) -> new ListCell<String>() {            
            @Override protected void updateItem(String item, boolean empty) {
                if (item!= null || !empty) {
                    setText(item);
                    setTextFill(Color.BLACK);
                    
                } else {
                    setText(null);
                }
                
            }
        };    
        
        if (Autenticator.getCurrentUser().getPrivilegios() == Privilegios.ADMINISTRADOR)
        jefeCol.setCellFactory(new Callback<TableColumn<Departamento,String>,TableCell<Departamento,String>>(){        
                @Override
                public TableCell<Departamento, String> call(final TableColumn<Departamento, String> param) {   
                    return new TableCell<Departamento, String>(){
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (this.getIndex() < tablaDeptos.getItems().size()){
                                ComboBox <String> choice = new ComboBox();   

                                usuarios.forEach(us -> {
                                    choice.getItems().addAll(us.getNombre());
                                    if (us.getCodigo().equals(item))
                                        choice.getSelectionModel().select(us.getNombre());
                                });

                                choice.setButtonCell((ListCell)cb.call(null));                               
                                choice.getSelectionModel().selectedItemProperty().addListener(crearListener(this.getIndex()));
                                choice.prefWidthProperty().bind(jefeCol.widthProperty().subtract(15));

                                setGraphic(choice);
                            } else {
                                setText(null);
                            }

                        };
                    };          
                }	
        });
    }

    private ChangeListener<String> crearListener(final int i) {
        return (ObservableValue<? extends String> ov, String t, String t1) -> {
            if (t1 != null){
                updateDepartamento(i,t1); 
            }
        };
    }

    
    
}
