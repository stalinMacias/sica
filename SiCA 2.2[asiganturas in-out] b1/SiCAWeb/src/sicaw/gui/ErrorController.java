package sicaw.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sica.common.Configs;
import sicaw.ScreenInterface;
import sicaw.ScreenManager;

public class ErrorController implements Initializable, ScreenInterface {

    @FXML private ImageView imgError;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            String host = Configs.SERVER_IMGS()+"nohayconexion.jpg";
            
            Image imagen = new Image(host);

            if (!imagen.isError()){                
                imgError.setImage(imagen);
                imgError.setPreserveRatio(true);                

            } else {
                System.out.println("Error estableciendo imagen de error XD");
            }

        } catch (Exception e){
            e.printStackTrace(System.out);
        }
    }    

    @Override
    public void setScreenParent(ScreenManager screenP) {        
    }
}
