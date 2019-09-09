package sicaw.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import sica.common.Autenticator;
import sicaw.Screens;
import sicaw.ScreenInterface;
import sicaw.ScreenManager;

public class LoginController implements Initializable, ScreenInterface{

    private Service<Boolean> loginService;
    private ScreenManager mainManager;
    
    @FXML private HBox loadingBox;
    @FXML private ProgressIndicator loading;    
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Button button;
    @FXML private Label fail;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fail.setVisible(false);
        
        loginService = new Service<Boolean>() {
            @Override protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override protected Boolean call() throws Exception {
                        return Autenticator.autenticate(txtUser.getText(), txtPass.getText())!=null;
                    }
                };
            }
        };
        loginService.valueProperty().addListener((ov,t,t1) -> {
            if (fail.isVisible()) fail.setVisible(false);
            
            if (t1 != null && t1){
                ((Principal)mainManager.getController(Screens.MAIN_PRINCIPAL)).loadMenu();
                mainManager.setScreen(Screens.MAIN_PRINCIPAL);
                txtUser.clear();
                txtPass.clear();
                
            } else if (t1!=null) {
                Timeline tl = new Timeline();
                tl.getKeyFrames().addAll(
                        new KeyFrame(
                                Duration.millis(50),
                                new KeyValue (txtPass.rotateProperty(), 3),
                                new KeyValue (txtUser.rotateProperty(), -3)
                        ),
                        new KeyFrame(
                                Duration.millis(100),
                                new KeyValue (txtPass.rotateProperty(), -3),
                                new KeyValue (txtUser.rotateProperty(), 3)
                        ),
                        new KeyFrame(
                                Duration.millis(150),
                                new KeyValue (txtPass.rotateProperty(), 0),
                                new KeyValue (txtUser.rotateProperty(), 0)
                        )
                );
                tl.setOnFinished(e -> txtPass.clear());
                tl.setCycleCount(2);
                fail.setVisible(true);
                tl.play(); 
            }
        });
        
        txtUser.editableProperty().bind(loginService.runningProperty().not());
        txtPass.editableProperty().bind(loginService.runningProperty().not());     
        button.disableProperty().bind(loginService.runningProperty());
        loadingBox.visibleProperty().bind(loginService.runningProperty());
        loading.progressProperty().bind(loginService.progressProperty());
    }    

    @Override
    public void setScreenParent(ScreenManager screenP) {
        mainManager = screenP;
    }
    
    @FXML protected void focusPass(){
        txtPass.requestFocus();
    }
    
    @FXML protected void login(){
        txtUser.requestFocus();  
        if (loginService.isRunning()) loginService.cancel();                
        if (loginService.getState() != State.READY) loginService.reset();
        loginService.start();
    }
    
}
