package sicaw;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import sica.common.Configs;
import sica.common.DBGetterFromJSON;
import sica.common.Utils;

public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {      
        System.out.println("SicaWeb - Version 2.5.1");
                
        String server = getParameters().getNamed().get("host");
        Configs.SERVER.set(server!=null? server : "http://127.0.0.1/sica");// http://148.202.89.3/sica http://localhost/sica
        sica.common.DBQueries.setDBGetter(new DBGetterFromJSON(Configs.SERVER.get()));
        System.out.println("Conectando a "+Configs.SERVER.get());
        
        String h = getParameters().getNamed().get("screenH");
        String w = getParameters().getNamed().get("screenW");
        
        ScreenManager mainContainer = new ScreenManager();
        mainContainer.loadScreen(Screens.MAIN_LOGIN);
        mainContainer.loadScreen(Screens.MAIN_ERROR);
        mainContainer.loadScreen(Screens.MAIN_PRINCIPAL);        
        
        HBox root = new HBox();
        root.setFillHeight(true);
        root.getChildren().addAll(mainContainer);        
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: white");
        
        int hh = h!=null && h.matches("[0-9]*")? Integer.parseInt(h): 768;
        int ww = w!=null && w.matches("[0-9]*")? Integer.parseInt(w): 1280;
        
        System.out.println("Dimenciones recibidas: "+w+"x"+h+", utilizadas: "+ww+"x"+hh);
        Scene scene = new Scene(root, ww, hh);           
        scene.getStylesheets().add(getClass().getResource("gui/tableTheme.css").toExternalForm());        
        
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("SiCA - Administrador");
        stage.show();
        
        if (true){//DBQueries.isConnected()){
            Utils.loadFonts(getClass(), 
                    "TrajanPro-Bold.otf",
                    "TrajanPro-Regular.otf",
                    "AvenirLTStd-Light.otf",
                    "AvenirLTStd-Heavy.otf");
            mainContainer.setScreen(Screens.MAIN_LOGIN);         
        } else {
            mainContainer.setScreen(Screens.MAIN_ERROR);
        }   
        
        //Utils.configureTooltipDurations(100, 30000, 100);
        
    }
            
    public static void main(String[] args) {
        Application.launch(args);
    }
}