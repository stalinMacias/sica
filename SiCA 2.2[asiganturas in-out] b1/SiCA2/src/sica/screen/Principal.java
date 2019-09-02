package sica.screen;

import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.ConnectionServer;
import sica.NotificationReceiver;
import sica.Screen;
import sica.Screens;
import sica.ScreenManager;
import sica.SiCA;
import sica.common.Utils;

public class Principal extends Screen implements Initializable{

    private static final Logger log = LoggerFactory.getLogger(Principal.class);    
    private Service<Void> clockService;
    private Screen actualScreen;
    
    
    @FXML private VBox topContainer;
    @FXML private StackPane container;   
    @FXML private Label fechahora;
    @FXML private Label titulo;
    int contador = 0;
    int contadorGC = 0;
    int desfase = 0; //desfase en segundos
    int iniciar = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Calendar serverCalendar = Calendar.getInstance();
        
        clockService = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);
                return new Task<Void>() {
                    @Override protected Void call() throws Exception {
                        while(SiCA.isRunning()){
                            //.getInstance() genera instancia de Calendar, en lugar de usar new Calendar
                            Calendar myCalendar = Calendar.getInstance();
                            
                            boolean conectado = false;
                            
                            
                            if(contador > 4260 || iniciar < 5){  //71 minutos -> Actualizar calendario y desfase
                                if(iniciar < 5){
                                    iniciar++;
                                }
                                contador = 0;
                                
                                //=================================================
                                //ACTUALIZAR CALENDARIO DEL SERVIDOR
                                //=================================================
                                String getedTiempo = "8:8:8";
                                String getedFecha = "8-8-8";
                                
                                
                                if(HuellaTeclado.disponible){
                                log.info("DISPONIBLE");
                                //conexion con el servidor
                                if(ConnectionServer.isConnected()){
                                    try{
                                        getedTiempo = ConnectionServer.getDBTime();
                                        getedFecha = ConnectionServer.getDBDate();
                                        conectado = true;
                                    }catch (Exception e){            
                                        log.error(e.getMessage());          
                                    }  
                                }
                                }else{
                                log.info("NO NO NO DISPONIBLE");
                                }
                                
                                if(conectado){
                                    //Agrega fecha-tiempo del servidor a un Calendar
                                    //log.info("Tiempo del servidor " + getedTiempo);
                                    //log.info("Fecha del servidor " + getedFecha);
                                    String[] seccionesTiempo = getedTiempo.split(":");
                                    String[] seccionesFecha = getedFecha.split("-");
                                    int horas, minutos, segundos, años, meses, dias;

                                    horas = Integer.parseInt(seccionesTiempo[0]);
                                    minutos = Integer.parseInt(seccionesTiempo[1]);
                                    segundos = Integer.parseInt(seccionesTiempo[2]);

                                    años = Integer.parseInt(seccionesFecha[0]);
                                    meses = Integer.parseInt(seccionesFecha[1]);
                                    dias = Integer.parseInt(seccionesFecha[2]);

                                    // por algun extraño motivo requiero restar -1 a meses para que se setee bien el calendario
                                    serverCalendar.set(años, meses - 1, dias, horas, minutos, segundos);
                                    // ---------------------------------------------
                                    // ---------------------------------------------



                                    //==============================================
                                    //          Calcular DESFASE
                                    //==============================================                    
                                    long desfaseMilis = serverCalendar.getTimeInMillis() - myCalendar.getTimeInMillis();
                                    //log.info("milis Servidor = " + Principal.serverCalendar.getTimeInMillis());
                                    //log.info("milisegu Local = " + myCalendar.getTimeInMillis());
                                    try{
                                        desfase =  (int) desfaseMilis / 1000;
                                    }catch(Exception e){
                                        log.error("Error!, demasiada diferencia entre hora del servidor y local");
                                        log.error(e.getMessage());
                                    }
                                    //log.info("DesMili = " + desfaseMilis);
                                    //log.info("Desfase = " + desfase);

                                    if(desfase <= 1800 && desfase >= -1800 ){ //30 minutos - 17 min
                                        desfase = desfase + 1; //segundo de adelanto para mejor funcionamiento
                                    }else if(desfase <= 20000 && desfase >= -20000){ 
                                        //codigo
                                        log.info("ADVERTENCIA!!!!!  VERIFICAR hora local y hora del servidor");
                                    }else if(desfase <= 30000 && desfase >= -30000){ 
                                        //codigo
                                        log.info("ADVERTENCIA!!!!!  VERIFICAR hora local y hora del servidor");}
                                    else if(desfase <= 604800 && desfase >= -604800){ 
                                        //codigo
                                        log.info("ADVERTENCIA!!!!!  VERIFICAR hora local y hora del servidor");}
                                    else{ 
                                        log.info("Error!!! Demasiada diferencia entre hora Local y del Servidor");
                                        desfase = 0;
                                        // nota: limite del int 1 semana en segundos
                                    }                               
                                }else { desfase = 0; }
                                
                                
                                
                                //Setear la hora del equipo
                                String hrSimple = getedTiempo.replace(".0", "");
                                Utils.updateSistemTime(hrSimple);
                                
                            }
                            
                            
                            //para sumar unidades al calendario existen add() y roll(). Mas info en internet xD
                            myCalendar.add(Calendar.SECOND, desfase);
                            updateMessage(df.format(myCalendar.getTime()).toUpperCase());
                            

                            
                            //El original era lo de abajo
                            //updateMessage(df.format(Calendar.getInstance().getTime()).toUpperCase());
                            try {
                                Thread.sleep(100);
                                contador++;
                                //contadorGC++;
                            } catch (InterruptedException e){                                
                                if (isCancelled())
                                    break;                                
                                log.error(e.getMessage());                                
                            }
                            
                            //inecesario controlarlo de esta manera
                            //Limpieza de memoria
                            //if(contadorGC > 7860 ){ //131min
                                //System.out.println("Ejecutando el Garbage Collector ##########");
                                //System.gc();
                                //contadorGC = 0;
                            //}
                            
                        }
                        return null;
                    }
                };
            }
        };
        
        /*
        //Intentar crear un servicio que actualice a la hora del servidor cada 77min
        clockServiceUpdate = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                //final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM);
                return new Task<Void>() {
                    @Override protected Void call() throws Exception {
                        
                        while(SiCA.isRunning()){
                            String getedTiempo = "8:8:8";
                            String getedFecha = "8-8-8";
                            
                            //conexion con el servidor
                            if(ConnectionServer.isConnected()){
                                try{
                                    getedTiempo = ConnectionServer.getDBTime();
                                    getedFecha = ConnectionServer.getDBDate();
                                }catch (Exception e){            
                                    log.error(e.getMessage());          
                                }  
                            }
                            
                            //Agregat fecha-tiempo del servidor a un Calendar
                            log.info("Tiempo del servidor " + getedTiempo);
                            log.info("Fecha del servidor " + getedFecha);
                            String[] seccionesTiempo = getedTiempo.split(":");
                            String[] seccionesFecha = getedFecha.split("-");
                            int horas, minutos, segundos, años, meses, dias;

                            horas = Integer.parseInt(seccionesTiempo[0]);
                            minutos = Integer.parseInt(seccionesTiempo[1]);
                            segundos = Integer.parseInt(seccionesTiempo[2]);
                            
                            años = Integer.parseInt(seccionesFecha[0]);
                            meses = Integer.parseInt(seccionesFecha[1]);
                            dias = Integer.parseInt(seccionesFecha[2]);
                            
                            // por algun extraño motivo requiero restar -1 a meses para que se setee bien el calendario
                            Share.interCalendar.set(años, meses - 1, dias, horas, minutos, segundos);
                            // --------------------------------------------
                            updateMessage(null); 
                            
                            try {
                                if(x<3){
                                    Thread.sleep(1000);
                                    x++;
                                }else{
                                    Thread.sleep(5000); //77 minutos para que no se actualize a horas en punto
                                }
                            } catch (InterruptedException e){                                
                                if (isCancelled())
                                    break;                                
                                log.error(e.getMessage());                                
                            }
                        }
                        return null;
                    }
                };
            }
        };
        */
        
        clockService.start();
        fechahora.textProperty().bind(clockService.messageProperty());
        //titulo.textProperty().bind(clockServiceUpdate.messageProperty());
        
        topContainer.getChildren().add(NotificationReceiver.getNotifReceiver());
                
    }
    
    public void timeToInt(String tiempo, String fecha){

        
        
    }
    
    public void goTo(Screens screen){        
        if (actualScreen != null && container.getChildren().contains(actualScreen.getParent())){
            container.getChildren().remove(actualScreen.getParent()); 
        }
        actualScreen = ScreenManager.getScreen(screen);
        
        if (actualScreen!=null && !container.getChildren().contains(actualScreen.getParent())){
            container.getChildren().add(actualScreen.getParent());   
        }              
    }
    
    public void avisar(String txt){
               
        final HBox h = new HBox(20);
        h.setPrefHeight(60);
        h.setPadding(new Insets(8));
        h.setStyle("-fx-background-color: rgba(1,1,1,.99); -fx-background-radius: 10;");
        h.setAlignment(Pos.CENTER);
        h.setMaxHeight(35);
        h.setMaxWidth(620);
        StackPane.setMargin(h, new Insets(0, 0, 120, 0));
        
        ProgressIndicator p = new ProgressIndicator(-1);
        p.setPrefHeight(26);        
        p.setMaxHeight(26);
        
        Label l = new Label(txt);
        l.getStyleClass().add("label2");
        l.setStyle("-fx-font-size: 25");
        l.setTextFill(Color.WHITE);
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
                
        h.getChildren().addAll(p,l);
        HBox.setHgrow(l, Priority.ALWAYS);
               
        avisar(2000,h);
    }    

    public void avisar(int time, Node node){        
        Task<Void> t = newAvisoTask(time,node);         
        new Thread(t).start();
    }
    
    private Task<Void> newAvisoTask(final int time, final Node node){
        return new Task<Void>(){ 
            @Override protected Void call() throws Exception {
                long start = System.currentTimeMillis();                
                while ( System.currentTimeMillis() - start < time ){
                    try{
                        Thread.sleep(10);
                    } catch (InterruptedException e){
                        System.out.println(e.getMessage());
                    }
                }                
                return null;
            }      

            @Override protected void scheduled() {
                super.scheduled();  
                container.getChildren().add(node);
            }
            
            @Override protected void succeeded() {
                super.succeeded();
                container.getChildren().remove(node);
            }

        };
    }
    
    @Override public void start() {
        goTo(Screens.HUELLATECLADO);
    }

    
}
