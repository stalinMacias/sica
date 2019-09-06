package sica.screen;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import sica.Configs;
import sica.Main;
import sica.Screen;
import sica.Screens;
import sica.ScreenManager;
import sica.common.usuarios.Usuario;

public class Menu extends Screen implements Initializable{

    private Usuario usuario;
    private boolean forced;
        
    @FXML private HBox atrasBtn;
    @FXML private Label atrasInfo;
    @FXML private StackPane container;    
    @FXML private ImageView imagen;
    @FXML private Label infoUsr;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        forced = false;
        usuario = null;
        imagen.setVisible(false);
        infoUsr.setVisible(false);
        atrasBtn.setCursor(Cursor.HAND);       
    }          
    
    @FXML public void goBack(){
        if (container.getChildren().size() == 1){
            if (usuario!=null) {
                usuario = null;
            }   
            ScreenManager.principal().goTo(Screens.HUELLATECLADO);            
        }         
        if (container.getChildren().size() >= 1 ){
            container.getChildren().remove(container.getChildren().size()-1);             
        } 
        
        atrasInfo.setText(container.getChildren().size()>1? "Atras": "Salir");
    }       
    
    public void addScreen(Screens sc){ 
        Screen s = ScreenManager.getScreen(sc);
        if (!container.getChildren().contains(s.getParent()))
            container.getChildren().add(s.getParent());
        
        atrasInfo.setText(container.getChildren().size()>1? "Atras": "Salir");        
    }
    
    public void setUsuario(Usuario u){
        usuario = u;        
        
        if (usuario!=null){                         
            //imagen.setImage(new Image(Configs.SERVER_FOTOS()+usuario.getCodigo()+".jpg"));
            if (imagen.getImage().isError()){
                  imagen.setImage(new Image(Main.class.getResource("images/error.jpg").toExternalForm()));
            }
            
            String usr = "";
            Scanner scan = new Scanner(usuario.getNombre());
            while (scan.hasNext()){
                String tmp = scan.next();
                if (tmp.length() >= 1)
                    usr+=tmp.toUpperCase().charAt(0)+tmp.toLowerCase().substring(1)+" ";
            }
            
            infoUsr.setText(usr.concat("\n\nTipo: "+usuario.getTipo()));
            
            imagen.setVisible(true);
            infoUsr.setVisible(true);
            
            container.getChildren().clear();        
            addScreen(usuario.isAdmin()? Screens.MENU_OPC_ADMIN: Screens.MENU_OPC_USER);      
            
        } else {
            imagen.setVisible(false);
            infoUsr.setVisible(false);
            goBack(); 
            ScreenManager.principal().avisar("Usuario y/o contraseña invalida");
        }      
    }

    public Usuario getUsuario() {
        return usuario;
    }    
    
    public void setForceCodigo(){
        forced = true;
    }
    
    @Override public void start() {        
        imagen.setVisible(false);
        infoUsr.setVisible(false);    
        addScreen(sica.Scanner.connectedProperty().get() && !forced?                
            Screens.MENU_BLOQUEO2:
            Screens.MENU_BLOQUEO);           
        if (forced) forced = false;
    }
    
}
