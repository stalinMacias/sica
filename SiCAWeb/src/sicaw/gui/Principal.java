package sicaw.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import sica.common.Autenticator;
import sica.common.Utils;
import sica.common.justificantes.Folio;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificanteFolio;
import sica.common.justificantes.JustificantePeriodo;
import static sica.common.usuarios.Privilegios.ADMINISTRADOR;
import static sica.common.usuarios.Privilegios.JEFE;
import static sica.common.usuarios.Privilegios.USUARIO;
import sicaw.Screens;   
import sicaw.ScreenInterface;
import sicaw.ScreenManager;
import sicaw.gui.menus.MenuController;
import sicaweb.gui.FolioInfo;

public class Principal implements Initializable, ScreenInterface {
    private static Principal principal;
    
    private ScreenManager mainManager;
    private FolioInfo folioInfo;
    
    @FXML private StackPane mainContainer;
    @FXML private VBox container;
    @FXML private VBox avisos;    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        principal = this;
        
        avisos.setVisible(false);
        avisos.getChildren().addListener((Observable o) -> {
            avisos.setVisible(avisos.getChildren().size()!=0);
        });
        folioInfo = new FolioInfo();
        mainContainer.getChildren().add(folioInfo);

        folioInfo.visibleProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mainContainer.getChildren().get(0).setEffect(t1? Utils.getBlurEffect():null);
        });
    }  
   
    public static void showJustificante(Folio justificante){
        principal.folioInfo.show(justificante);
    }
    public static void showJustificante(JustificanteFolio justificante){
        principal.folioInfo.show(justificante);
    }
    public static void showJustificante(JustificantePeriodo justificante){
        principal.folioInfo.show(justificante);
    } 
    public static void showJustificante(JustificanteAsignatura justificante){
        principal.folioInfo.show(justificante);
    }
    
    public static void avisar(String txt){
         avisar(txt, 3500);
    }
    
    public static void avisar(String txt, int time){
               
        final HBox h = new HBox(10);
        h.setPadding(new Insets(10));
        h.setStyle("-fx-background-color: rgba(1,1,1,0.85); -fx-background-radius: 10;");
        h.setAlignment(Pos.CENTER_LEFT);
        
        ProgressIndicator p = new ProgressIndicator(-1);
        p.setPrefHeight(30);
        p.setMinWidth(30);
        
        Label l = new Label(txt);
        l.getStyleClass().add("h2");
        l.setTextFill(Color.WHITE);
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
                
        h.getChildren().addAll(p,l);
        HBox.setHgrow(l, Priority.ALWAYS);
        
        Task<Void> t = newAvisoTask(time); 
        t.setOnScheduled(e -> principal.avisos.getChildren().add(h));
        t.setOnSucceeded(e -> principal.avisos.getChildren().remove(h));
        
        new Thread(t).start();
    }    

    private static Task<Void> newAvisoTask(final int time){
        return new Task<Void>(){ 
            @Override protected Void call() throws Exception {
                long start = System.currentTimeMillis();                
                while ( System.currentTimeMillis() - start < time ){
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        System.out.println(e.getMessage());
                    }
                }                
                return null;
            }           
        };
    }
    
    @Override
    public void setScreenParent(ScreenManager screenP) {
        mainManager = screenP;
    }
    
    public void loadMenu(){
        container.getChildren().clear();
        
        try{            
            FXMLLoader myLoader = null;
            
            switch(Autenticator.getCurrentUser().getPrivilegios()){
                case ADMINISTRADOR:
                    myLoader = new FXMLLoader(Screens.class.getResource(Screens.MENU_ADMIN));
                    break;

                //case DIRECTIVO:
                //    myLoader = new FXMLLoader(Screens.class.getResource(Screens.MENU_DIRECTIVO));
                //    break;
                    
                case JEFE:
                    myLoader = new FXMLLoader(Screens.class.getResource(Screens.MENU_JEFE));
                    break;
                    
                case USUARIO: //case DIRECTIVO:
                    myLoader = new FXMLLoader(Screens.class.getResource(Screens.MENU_PROFESOR));
                    break;
            }
            
            
            if (myLoader != null){                
                Parent topMenu = (Parent) myLoader.load();                 
                MenuController mc = (MenuController) myLoader.getController();
                mc.setScreenParent(mainManager);
                mc.setContainer(container);                
                container.getChildren().add(topMenu);                
                mc.setScreen("Mi perfil");
            }            
             
        }catch (IOException e){
            e.printStackTrace(System.out);
        }
    }    
    
    
}
