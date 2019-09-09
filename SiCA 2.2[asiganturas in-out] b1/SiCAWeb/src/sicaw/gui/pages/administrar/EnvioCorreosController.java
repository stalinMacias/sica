package sicaw.gui.pages.administrar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import sicaweb.AdminSicaClient;

public class EnvioCorreosController implements Initializable {

    @FXML private WebView correosWeb;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AdminSicaClient.loadPaginaCorreosProgramados(correosWeb);
    }    
    
}
