package sicaw.gui.pages.mensajes;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import sica.common.DBQueries;
import sica.common.objetos.Departamento;
import sica.common.objetos.Mensaje;
import sica.common.usuarios.Usuario;

public class GeneralController implements Initializable {

    private ObservableList <String> usuariosDestino;
    private ObservableList <Mensaje> usuariosRepetidos;
    
    @FXML private SplitPane split1;
    @FXML private VBox destinos;
    @FXML private CheckBox todosChk;
    @FXML private TextArea mensaje;
    @FXML private Text info;
    @FXML private VBox precaucion;
    @FXML private Label tamMsj;
    
    @FXML private TableView <Mensaje> tablaUsuarios;    
    @FXML private TableColumn <Mensaje,String> nomCol;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        usuariosRepetidos = FXCollections.observableArrayList();
        usuariosDestino = FXCollections.observableArrayList();
        
        inicializarVistaTablas();
        
        crearListeners();
        
        split1.getItems().remove(precaucion);
        mensaje.setEditable(false);
        
        todosChk.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                for (Node n : destinos.getChildren()){
                    if (n instanceof CheckBox){
                        CheckBox ch = (CheckBox) n;
                        ch.setSelected(todosChk.isSelected());                                                
                    }                    
                }
                for (Usuario u : DBQueries.getTodosUsuarios()){
                    if (todosChk.isSelected()){
                        usuariosDestino.add(u.getCodigo());
                    } else {
                        usuariosDestino.clear();
                    }
                } 
                buscarMensajes();
            }
        });
        
    }    
    
    @FXML protected void limpiarMensaje(){
        
        todosChk.setSelected(false);
        for (Node n : destinos.getChildren()){
            if (n instanceof CheckBox){
                CheckBox ch = (CheckBox) n;
                ch.setSelected(false);                                                
            }                    
        }
        
        usuariosDestino.clear();
        usuariosRepetidos.clear();
        
        if (split1.getItems().contains(precaucion))
            split1.getItems().remove(precaucion);
        
        mensaje.clear();
        mensaje.setEditable(false);
        
    }
    
    @FXML protected void guardarMensaje(){
        info.setText(null);
        
        if (mensaje.getText().length() > 0){
            
            for (Mensaje user : usuariosRepetidos){
                DBQueries.elimMensaje(user.getUsuario());  
            }

            for (String user : usuariosDestino){
                DBQueries.guardarMensaje(user, mensaje.getText());

            }

            info.setFill(Color.BLACK);
            info.setText("Mensaje sera entregado la proxima vez que los usuarios chequen");
        
        } else {
            info.setFill(Color.RED);
            info.setText("Favor de introducir el mensaje");
            
        }
        
        info.setVisible(true);
    }
    
    private void updateEspacioLibre(){
        info.setVisible(false);
        
        if (mensaje.getText().length() > 100 )            
            mensaje.setText(mensaje.getText().substring(0, 100));
                
        tamMsj.setText(mensaje.getText().length()+" de 100");
        
    }

    private void buscarMensajes() {
                    
            usuariosRepetidos.clear();
            ObservableList<Mensaje> rs2 = DBQueries.getAllMensajes();
            
            try{
                
                for (String usr : usuariosDestino){                    
                    for (Mensaje m : rs2 ) {
                        if (m.getUsuario().equals(usr))
                            usuariosRepetidos.add(m);
                    }                

                }

                if (usuariosRepetidos.size() > 0){
                    tablaUsuarios.setItems(usuariosRepetidos);
                    if (!split1.getItems().contains(precaucion))
                        split1.getItems().add(precaucion);

                } else {
                    split1.getItems().remove(precaucion);   
                }

                mensaje.setEditable(true);

            } catch (Exception e){
                e.printStackTrace(System.out);
            }
  
    }
    
    private void inicializarVistaTablas() {
        
        nomCol.prefWidthProperty().bind(tablaUsuarios.widthProperty().subtract(15));
        nomCol.setCellValueFactory(new PropertyValueFactory<Mensaje,String>("usuario"));
        
        final ObservableList<Departamento> departamentos = DBQueries.getDepartamentos();
        
        EventHandler<ActionEvent> ev = new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                if ( t.getTarget() instanceof CheckBox){
                    CheckBox ch = (CheckBox) t.getTarget();                    
                    String depto = "";
                    for (Departamento d : departamentos){                        
                        if (d.getNombre().equals(ch.getText())){
                            depto = d.getCodigo();
                            break;
                        }
                    }
                    
                    for (Usuario u : DBQueries.getAlgunosUsuarios(depto)){
                        if (ch.isSelected()){
                            usuariosDestino.add(u.getCodigo());
                        } else if (usuariosDestino.contains(u.getCodigo())){
                            usuariosDestino.remove(u.getCodigo());
                        }
                    } 
                     
                    buscarMensajes();
                }
            }
            
        };
        
        
        for (Departamento d : departamentos){
            destinos.getChildren().add(
                    CheckBoxBuilder.create().text(d.getNombre()).onAction(ev).build()                   
            );
        }
        
    }

    private void crearListeners() {
    
        mensaje.textProperty().addListener(                
            new ChangeListener <String>(){
                @Override
                public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                    updateEspacioLibre();
                }  
            });
    }
    
}
