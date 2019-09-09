package sicaw.gui.pages.calendario;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sica.common.DBQueries;
import sica.common.justificantes.TipoEvento;

public class AgregarEventoController implements Initializable {

    @FXML private TableView <TipoEvento> tablaEventos;
    @FXML private TableColumn <TipoEvento, String> eventoCol;
    @FXML private TableColumn <TipoEvento, TipoEvento> colorCol;
    
    @FXML private TextField eventoNew;
    @FXML private ColorPicker colorNew;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {           
        inicializarVistaTablas();        
        createFancyCells();        
        loadEventos();        
    }    
    
    public void loadEventos(){
        tablaEventos.setItems(DBQueries.getTipoEventos());
        
    }
    
    @FXML protected void addEvento(){
        if (colorNew.getValue()!=null && !eventoNew.getText().isEmpty()){            
            Color c = colorNew.getValue();
            int red = Double.valueOf(c.getRed() * 255).intValue();
            int green = Double.valueOf(c.getGreen() * 255).intValue();
            int blue = Double.valueOf(c.getBlue() * 255).intValue();
        
            DBQueries.insertTipoEvento(red+","+green+","+blue,eventoNew.getText());
            loadEventos();
        }
    }
    
    private void updateEvento(int linea, Color color){
        
        int red = Double.valueOf(color.getRed() * 255).intValue();
        int green = Double.valueOf(color.getGreen() * 255).intValue();
        int blue = Double.valueOf(color.getBlue() * 255).intValue();
        
        DBQueries.updateTipoEvento( linea, red+","+green+","+blue);
        
    }

    private void inicializarVistaTablas() {
        
        colorCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TipoEvento, TipoEvento>, ObservableValue<TipoEvento>>() {
            @Override public ObservableValue<TipoEvento> call(TableColumn.CellDataFeatures<TipoEvento, TipoEvento> p) {
                return new SimpleObjectProperty<>(p.getValue());
            }
        });
        eventoCol.setCellValueFactory(new PropertyValueFactory<TipoEvento, String>("nombre"));
        
        colorCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(3/10f).subtract(5));
        eventoCol.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(7/10f).subtract(5));
        
        colorNew.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(3/10f).subtract(5));
        eventoNew.prefWidthProperty().bind(tablaEventos.widthProperty().multiply(5/10f).subtract(5));
        
    }
    
     private void createFancyCells() {      
        
        colorCol.setCellFactory(new Callback<TableColumn<TipoEvento,TipoEvento>,TableCell<TipoEvento,TipoEvento>>(){        
                @Override public TableCell<TipoEvento, TipoEvento> call(final TableColumn<TipoEvento, TipoEvento> param) {   
                    return new TableCell<TipoEvento, TipoEvento>(){
                        @Override public void updateItem(TipoEvento item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item!=null && !empty){         
                                StringTokenizer st = new StringTokenizer(item.getColor(),",",false);
                                Color c = Color.rgb(
                                        Integer.parseInt(st.nextToken()),
                                        Integer.parseInt(st.nextToken()),
                                        Integer.parseInt(st.nextToken()));
                                                                        
                                ColorPicker btn = new ColorPicker();
                                btn.setValue(c);
                                btn.cursorProperty().set(Cursor.HAND);
                                btn.scaleXProperty().set(0.9);
                                btn.scaleYProperty().set(0.9);
                                
                                btn.valueProperty().addListener(actionBtn(item.getTipo()));                                
                                setGraphic(btn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    };          
                }	
        });
    }     
      
    private ChangeListener<Color> actionBtn(final String linea){
        return new ChangeListener <Color> (){
            @Override public void changed(ObservableValue<? extends Color> ov, Color t, Color t1) {
                updateEvento(Integer.valueOf(linea), t1);
            }
            
        };  
    }
}
