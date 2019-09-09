package sicaw.gui.pages.ayuda;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sica.common.Configs;
import sicaw.gui.menus.MenuController;

public class Ayuda implements Initializable {

    @FXML private ScrollPane scroll;
    @FXML private ImageView ayuda;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Image image = 
                MenuController.titulo.equals("  Ayuda enviar justificantes")?
                    new Image(Configs.SERVER_IMGS()+"Enviar.jpg"):
                    new Image(Configs.SERVER_IMGS()+"Aprobar.jpg");
        
        ayuda.fitWidthProperty().bind(scroll.widthProperty());
        ayuda.setImage(image);
    }    
    
}
