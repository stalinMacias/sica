package sica;

import java.util.Arrays;
import java.util.EmptyStackException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.Utils;

public class Main extends Application { 
    private static final Logger log = LoggerFactory.getLogger(Main.class);    
    private static final String WINDOWS_TITLE = "SiCA - Sistema de Control de Asistencias";
    private static final int WINDOWS_WIDTH = 1015;
    private static final int WINDOWS_HEIGTH = 652;
    
    @Override public void start(final Stage primaryStage){ 
        /** VM options for embed devices  -Dcom.sun.javafx.isEmbedded=true -Dcom.sun.javafx.virtualKeyboard=javafx -Dcom.sun.javafx.vk.adjustwindow=true
         * -Dcom.sun.javafx.isEmbedded=true
         * -Dcom.sun.javafx.virtualKeyboard=javafx
         * -Dcom.sun.javafx.vk.adjustwindow=true
         */
        
        SiCA.initialize(
                getParameters().getRaw().contains("-debug"),
                getParameters().getRaw().contains("-reset"));                     
        
        final Rectangle2D tam = Screen.getPrimary().getBounds();              
        log.info("Resolucion pantalla: {} ",tam);    

        Utils.loadFonts(getClass(),
                "TrajanPro-Regular.otf",
                "AvenirLTStd-Light.otf",
                "AvenirLTStd-Heavy.otf");

        //iniciamos el panel principal
        final Parent parent = ScreenManager.getScreen(Screens.PRINCIPAL).getParent();
        
        //Se crea la ventana, se aplican las configuraciones   
        primaryStage.setTitle(WINDOWS_TITLE);
        primaryStage.setScene(new Scene(parent,WINDOWS_WIDTH,WINDOWS_HEIGTH));
        primaryStage.setResizable(false);
        //primaryStage.setAlwaysOnTop(true); 
        
        if (Configs.UNDECORATED.get()){
            primaryStage.initStyle(StageStyle.UNDECORATED);     
        }                                

        primaryStage.setFullScreen(Configs.FULLSCREEN.get()); 
        
        Configs.FULLSCREEN.addListener((ChangeListener<Boolean>) (ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) -> {
            if (Platform.isFxApplicationThread()){
                primaryStage.setFullScreen(Configs.FULLSCREEN.get());
            } else {
                Platform.runLater(() -> primaryStage.setFullScreen(Configs.FULLSCREEN.get()));     
            }            
        });
        
        // Prevenir cierre de aplicacion con alt f4
        primaryStage.setOnCloseRequest((final WindowEvent event) -> {
            if (Configs.PREVENT_ALTF4.get()){
                log.info("Ignorando peticion de cerrado");
                event.consume();
            } else {
                // #HuellasFrec Finalizador.finalizar();
                Platform.exit();
            }
        });

        // Establece los atajos de teclado        
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, (final KeyEvent event) -> {              
            if (event.getCode() == KeyCode.Q && event.isControlDown() && !event.isAltDown() && !event.isShiftDown()){
                event.consume();
                // #HuellasFrec Finalizador.finalizar();
                Platform.exit();
                
            }else if (event.getCode() == KeyCode.Q && event.isControlDown() && event.isShiftDown() && !event.isAltDown()){
                event.consume();
                //Cerrar la aplicacion forzadamente:  sin que se guarden el orden actual de las huellas
                Platform.exit();
                
            }else if (event.getCode() == KeyCode.ESCAPE){
                event.consume(); 
                if (Configs.PREVENT_ESC.get()){
                    Configs.FULLSCREEN.set(!Configs.FULLSCREEN.get());
                } 
                
            } else if (event.getCode() == KeyCode.R && event.isControlDown()){
                event.consume();
                if (log.isDebugEnabled()) log.debug("Reconectando a BD");
                ConnectionServer.startConnection();

            } else if (event.getCode() == KeyCode.F && event.isControlDown()){
                event.consume();
                Configs.FULLSCREEN.set(!Configs.FULLSCREEN.get());
                
            } else if (event.getCode() == KeyCode.S && event.isControlDown()){
                log.info("Abriendo menu");
                ScreenManager.menu().setForceCodigo();
                ScreenManager.principal().goTo(Screens.MENU);

            } else if (event.getCode() == KeyCode.U && event.isControlDown() && event.isShiftDown() ){
                log.info("Solicitando actualización simple");
                //Forzar que verifique si hay actualizaciones independientes de huellas,
                //se puede utilizar en el caso que se agregue una huella o usuario y se desee actualizar solo ello y no toda la DB local.
                Updater.update(false);

            }  else if (event.getCode() == KeyCode.U && event.isControlDown()){
                log.info("Solicitando actualización");
                Updater.update(true);

            } else if (event.getCode() == KeyCode.P && event.isControlDown()){
                System.out.println("****************************************");
                log.info("Resetando configuraciones - propiedasdes");
                Configs.loadLocalConfig(true);

            } else if (event.getCode() == KeyCode.L && event.isControlDown() && event.isShiftDown()){
                Log.show();
            }
        });           
        
        primaryStage.show(); 
               
    }   
    /*---------------------------------------------------------------
------------------------------mi codigo-------ALAN-----------------------
---------------------------------------------------------------------*/
    public class Stock {

        private Object[] elements;
        private int size = 0;
        private static final int DEFAULT_INITIAL_CAPACITY = 16;

        public Stock() {
            elements = new Object[DEFAULT_INITIAL_CAPACITY];
        }

        public void push(Object e) {
            ensureCapacity();
            elements[size++] = e;
        }

        public Object pop() {
            if (size == 0) {
                throw new EmptyStackException();
            }
            return elements[--size];
        }

        private void ensureCapacity() {
            if (elements.length == size) {
                elements = Arrays.copyOf(elements, 2 * size + 1);
            }
        }
    }

    public Object pop(int size, Object[] elements) {
        if (size == 0) {
            log.equals(log);
            log.info(WINDOWS_TITLE, log);
            System.out.println("borrando objetos obsoletos");
            
        }else{
            System.err.println("no se a podido realizar la operacion");
            log.error(WINDOWS_TITLE, log);
            
        }
        Object result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    /*---------------------------------------------------------------
------------------------------mi codigo----------ALAN--------------------
---------------------------------------------------------------------*/   
 
     
    
    @Override public void stop(){ 
        log.info("Iniciando cerrado de aplicación");                
        SiCA.stop();        
        log.info("Saliendo ahora");     
    }    
        
    public static void main(String[] args) {
        launch(args);
    }
    

    
}
