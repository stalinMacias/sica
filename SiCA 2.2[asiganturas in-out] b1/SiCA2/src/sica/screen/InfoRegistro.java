package sica.screen;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.ConnectionServer;
import sica.Configs;
import sica.Main;
import sica.Scanner;
import sica.Screen;
import sica.ScreenManager;
import sica.Screens;
import sica.TipoRegistro;
import sica.UserData;
import sica.common.Utils;

public class InfoRegistro  extends Screen implements Initializable{
    private final static Logger log = LoggerFactory.getLogger(InfoRegistro.class);
    private UserData data;
    
    private Service<Void> progressService;
    @FXML private Label nombreTxt;
    @FXML private Text horaTxt;
    @FXML private Text horaSalTxt;
    @FXML private Text info;
    @FXML private Label infoReg;
    @FXML private Text messageTxt;
    @FXML private ProgressBar progress;
    @FXML private ImageView fotoUser;
    @FXML private ImageView entradaOK;
    @FXML private ImageView salidaOK;
    @FXML private HBox messagePanel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messagePanel.setVisible(false);
        
        progressService = new Service<Void>() {
            @Override protected Task<Void> createTask() {
                return new Task<Void>(){
                    @Override protected Void call() {  
                        updateProgress(0, 1);
                        int time = Configs.DATATIME.get()*1000;  
                        if (data!=null && data.getMensaje()!=null){
                            time += 2000;
                        }
                        long ini = System.currentTimeMillis();
                       
                        for (long act = ini; act-ini <= time ; act = System.currentTimeMillis() ) {                            
                            updateProgress(act-ini, time);
                            
                            try { Thread.sleep(10); } 
                            catch (InterruptedException e) { 
                                log.error(e.getMessage());
                                if (isCancelled()) break;
                            }
                        }
                        return null;
                    }   

                    @Override protected void failed() {
                        super.failed();
                        returnToHome();
                    }

                    @Override protected void cancelled() {
                        super.cancelled(); 
                        returnToHome();
                    }

                    @Override protected void succeeded() {
                        super.succeeded(); 
                        returnToHome();
                    }
                    
                    
                };           
            }
        };
        progress.progressProperty().bind(progressService.progressProperty());
    } 
    
    private void returnToHome(){
        ScreenManager.principal().goTo(Screens.HUELLATECLADO);
    }

    @Override
    public void start() {   
        Scanner.stopAllScanners();
        if (data!=null) {        
            load();
            if (progressService.getState() != Worker.State.READY){
                progressService.reset();
            }
            progressService.start();
        }        
    }
    
    public void setUserData(UserData ud){
        data = ud;
    }
    
    public void load(){        
        
        nombreTxt.setText(data.getUsuario().getNombre());
        
                 
        if (ConnectionServer.isConnected()){
            log.info("Descargando foto: " + data.getFoto());  
            //System.out.println("foto: "data.getFoto(fotoUser));
            try {
                
            } catch (Exception e) {
            }
 
           Image imagen;
            //Image imagen = new Image(data.getFoto()+"");//data.getFoto());
            //Image imagen = new Image("http://148.202.119.37/sica/Fotos/+"colecion"+.jpg");
            try{
                String newUrl = "" + data.getFoto() + "";
                System.out.println("///////////////////////////");
                System.out.println("La foto a mostrar es:" + newUrl);
                imagen = new Image(newUrl);
                
                
                log.info("1) se creo objeto image: ");
                System.out.println("");
                if (!imagen.isError()){
                    log.info("2) no error con imagen");
                    fotoUser.setImage(imagen);
                    fotoUser.setPreserveRatio(true);

                } else {
                    log.info("Error descargando foto ");
                    fotoNoDisponible();            
                }  
                
                
            } catch(NullPointerException | IllegalArgumentException e){
                System.out.println("Error al generar la imagen!!!: " + e.getMessage());
            }
            
            
        } else {
            fotoNoDisponible();            
        }
        
        infoReg.setText("");
        info.setText("");
        horaTxt.setText("");            
        horaSalTxt.setText("");
        entradaOK.setVisible(false);
        salidaOK.setVisible(false);        
        
        if ( data.getTipoRegistroMat() == null ) {
           // System.out.println("TipoRegistroMat resulto = null, se mostrara info para solo entrada");
            
            switch (data.getTipoReg()){
                case ENTRADA:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Entrada de "+data.getUsuario().getTipo());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;

                case ENTRADAYMATERIA:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                            + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                            + "\nAula: "+ data.getMateriaActual().getAula());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);   
                    break;

                case ENTRADATARDE:
                    infoReg.setText("Registro realizado con errores");                
                    info.setText("Ha registrado su asistencia después del tiempo"
                            + " de tolerancia, se ha creado un incidente en el"
                            + " sistema. Puede revisar el caso con su Jefe inmediato");

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));                
                    break;

                case ENTRADATARDEYMATERIA:
                    if(data.getUsuario().getTipo().toLowerCase() != "Profesor de tiempo completo"){
                    infoReg.setText("Registro realizado con errores");
                    info.setText("No se ha registrado entrada a jornada laboral!"
                            + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                            + "\nAula: "+ data.getMateriaActual().getAula());

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;
                    }
                    else if(data.getUsuario().getTipo().toLowerCase() == "Profesor de tiempo completo"){
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Se ha registrado entrada a jornada laboral!"
                            + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                            + "\nAula: "+ data.getMateriaActual().getAula());

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;
                    }

                case ENTRADATARDEYFUERATOLERANCIA:
                    
                    infoReg.setText("Registro realizado con errores");
                    info.setText("No hay registrado de entrada a jornada laboral"
                            + "\nRegistro de actividad académica fuera del "
                            + "rango de tolerancia.");

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;
                    
                        
                case MATERIA:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Entrada a materia: "+data.getMateriaActual().getMateria()
                            + "\nAula: "+ data.getMateriaActual().getAula());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;

                case MATERIAFUERATOLERANCIA: 
                    
                    infoReg.setText("Registro realizado con errores");
                    info.setText("Registro de materia fuera del tiempo de tolerancia. "
                            + "Recuerde que la tolerancia es de 20 minutos antes y "
                            + "despues de la hora en que esta programada su asignatura");

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;

                case MATERIAREGISTRADA:
                    infoReg.setText("Registro duplicado");
                    info.setText("Materia: "+data.getMateriaActual().getMateria()
                                + "\nAula: "+ data.getMateriaActual().getAula());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getMateriaActual()
                                    .getRegistroEntrada().getFechahora()));
                    horaSalTxt.setText("Hora de este registro: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);


                    break;

                case SALIDA:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                            + "");

                    if (data.getHorario().getRegistroEntrada() != null){
                        horaTxt.setText("Hora de entrada: "+Utils.formatTime(
                                data.getHorario().getRegistroEntrada().getFechahora()));
                        entradaOK.setVisible(true);                
                    }
                    horaSalTxt.setText("Hora de salida: "+Utils.formatTime(data.getRegistro().getFechahora()));                
                    salidaOK.setVisible(true);                
                    break;

                case SALIDAANTES:
                    infoReg.setText("Registro realizado con errores");
                    info.setText("Ha registrado salida antes de cubrir su jornada "
                            + "laboral, se ha creado un incidente en el sistema. "
                            + "Puede revisar el caso con su Jefe inmediato");
                    if (data.getHorario().getRegistroEntrada() != null){
                        horaTxt.setText("Hora de entrada: "+Utils.formatTime(
                                data.getHorario().getRegistroEntrada().getFechahora()));
                        entradaOK.setVisible(true);                
                    }
                    horaSalTxt.setText("Hora de salida: "+Utils.formatTime(data.getRegistro().getFechahora()));                
                    salidaOK.setVisible(true);  
                    break;                

                case SINACTIVIDAD:
                    infoReg.setText("Registro realizado con errores");                
                    info.setText(                        
                            data.getTipoJornada().equals("sinjornada")?// De asignatura?
                                "No se tiene actividad programada, se ha creado "
                            + "un incidente en el sistema. Recuerde que tiene una "
                            + "tolerancia de 20 minutos antes y despues de la "
                            + "hora en que esta programada su asignatura":
                                "No se tiene actividad programada, se ha creado "
                            + "un incidente en el sistema.");
                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;

                case OFFLINE:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Registro realizado fuera de linea (No hay internet)");
                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;            
            }
          
           
     } else { 
             // ---------------------------------------------------------------------------------------------------------------------------------------------------- //
            // ---------------------------------------------------------------------------------------------------------------------------------------------------- //
            //                                          para el caso que sea Entrada y Salida
            // ---------------------------------------------------------------------------------------------------------------------------------------------------- //
            if(data.getTipoReg() != null){
                //System.out.println("tipoRegistro = " + data.getTipoReg().toString());
            }else{
                log.error("data.getTipoReg es NULL");
            }
            
            switch (data.getTipoReg()){
                case ENTRADA:
                    //si fuera Entrada sin asignaturas, de eso ya se encarga la seccion anterior
                    switch(data.getTipoRegistroMat()){
                        //Entrada Jornada + entrada clase + salida clase
                        case EYS_MATERIA: case EYS_MATERIA_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                     + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                        case EYS_MATERIA_E: case EYS_MATERIA_E_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                        case EYS_MATERIA_S: case EYS_MATERIA_S_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                     + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                        case EYS_MATERIA_E_FUERATOLERANCIA:
                            infoReg.setText("Registro realizado con errores");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nEntrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                        /*  
                        case EYS_MATERIA_E_REGISTRADA: //se deshabilito la opcion de mostrar registro duplicado, ahora muestra regisro exitoso
                            infoReg.setText("Registro duplicado");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break; */
                            
                        case EYS_MATERIA_FUERATOLERANCIA:
                             infoReg.setText("Registro realizado ");
                            info.setText("Entrada exitosa de " + data.getUsuario().getTipo().toLowerCase());
                                   // + "\nEntrada Fuera de Tolerancia a materia: "
                                  //  + "\nAula: "+ data.getMateriaActual().getAula()
                                     //+ "\nSalida Fuera de Tolerancia de materia: "+ data.getMateriaAnterior().getMateria()
                                    //+ "\nAula: "+ data.getMateriaAnterior().getAula());
                                   // + "\nRegistro a materia Fuera de Tolerancia" );
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                            /*
                        case EYS_MATERIA_REGISTRADA:
                             infoReg.setText("SRegistro duplicado");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break; */
                            
                        case EYS_MATERIA_S_ANTES:
                                              
                            infoReg.setText("Registro realizado con errores");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nSalida Anticipada de materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break;
                            /*
                        case EYS_MATERIA_S_REGISTRADA:
                            infoReg.setText("Registro duplicado");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase()
                                    + "\nSalida Duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);   
                            break; */
                            
                        case EYS_MATERIA_SINACTIVIDAD:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Entrada de " + data.getUsuario().getTipo().toLowerCase() );
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true); 
                            break;
                    }
                    break;

                case ENTRADATARDE:
                    infoReg.setText("Registro realizado con errores");                
                    info.setText("No se ha registrado entrada a jornada laboral ");
                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));        
                    
                    switch(data.getTipoRegistroMat()){
                       
                        case EYS_MATERIA: case EYS_MATERIA_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida a materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break;
                        case EYS_MATERIA_E: case EYS_MATERIA_E_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                            break;
                        case EYS_MATERIA_S: case EYS_MATERIA_S_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nSalida a materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break;
                        case EYS_MATERIA_FUERATOLERANCIA:
                            info.setText( info.getText()
                                    + "\nRegistro de materia fuera del tiempo de tolerancia. " );
                            break;
                        case EYS_MATERIA_S_ANTES:
                            info.setText( info.getText()
                                    + "\nSalida anticipada de materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                            break;
                        case EYS_MATERIA_E_FUERATOLERANCIA:
                            info.setText( info.getText()
                                    + "\nEntrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                            break;
                            
                            
                        /*
                        case EYS_MATERIA_E_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                            break; 
                        case EYS_MATERIA_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida duplicada a materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break;
                        case EYS_MATERIA_S_REGISTRADA:
                            info.setText( info.getText()
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break;
                            */
                        case EYS_MATERIA_SINACTIVIDAD:
                            //No se agrega nada porque al inicio ya se había asigando la informacion "superior" de entrada tarde a jornada
                            break;
                    }
                    break;
                    

                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
                /*
                case MATERIA:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Entrada a materia: "+data.getMateriaActual().getMateria()
                            + "\nAula: "+ data.getMateriaActual().getAula());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;

                case MATERIAFUERATOLERANCIA: 
                    infoReg.setText("Registro realizado con errores");
                    info.setText("Registro de materia fuera del tiempo de tolerancia."
                            + "Recuerde que la tolerancia es de 20 minutos antes y "
                            + "despues de la hora en que esta programada su asignatura");

                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    break;

                case MATERIAREGISTRADA:
                    infoReg.setText("Registro duplicado");
                    info.setText("Materia: "+data.getMateriaActual().getMateria()
                                + "\nAula: "+ data.getMateriaActual().getAula());
                    horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getMateriaActual()
                                    .getRegistroEntrada().getFechahora()));
                    horaSalTxt.setText("Hora de este registro: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;
                    */
                    ///--------------------------------------------------------------------------------------------------------------------------------------------------------

                    
                case SALIDA:
                    switch(data.getTipoRegistroMat()){
                        case EYS_MATERIA: case EYS_MATERIA_REGISTRADA:
                              infoReg.setText("Su registro ha sido exitoso");
                              info.setText("Se ha registrado salida (Se tomará en cuenta el último registro del día como salida) " 
                                    + ""
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            break;
                        case EYS_MATERIA_E: case EYS_MATERIA_E_REGISTRADA:
                              infoReg.setText("Su registro ha sido exitoso");
                              info.setText("Entrada a materia: "+ data.getMateriaActual().getMateria()
                                      + "\nAula: "+ data.getMateriaActual().getAula()                                     
                                     );   
                            break;
                        case EYS_MATERIA_S: case EYS_MATERIA_S_REGISTRADA:
                              infoReg.setText("Su registro ha sido exitoso");
                              info.setText( 
                                    "Salida de materia: "+ data.getMateriaAnterior().getMateria()
                                     + "\nAula: "+ data.getMateriaAnterior().getAula()
                                    +"\n(Se tomará en cuenta el último registro del día como salida)"
                              );
                            break;
                        case EYS_MATERIA_E_FUERATOLERANCIA:
                              infoReg.setText("Registro realizado con errores");
                              info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                                    + ""
                                    + "\nEntrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria() );
                            break;
                            /*
                        case EYS_MATERIA_E_REGISTRADA:
                              infoReg.setText("Registro duplicado");
                              info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                                    + " (Se tomará en cuenta el último registro del día como salida)"
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                            break; */
                        case EYS_MATERIA_S_ANTES:
                            String tipo = data.getUsuario().getTipo();
                            
                                                                           
                              infoReg.setText(" Registro realizado");
                              info.setText("Se ha registrado salida (Se tomará en cuenta el último registro del día como salida) "                                   
                                    + "\nSalida Fuera de Tolerancia de materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                            break;
                           
                        /*
                        case EYS_MATERIA_S_REGISTRADA:
                              infoReg.setText("Registro duplicado");
                              info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                                    + " (Se tomará en cuenta el último registro del día como salida)"
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break; */
                        case EYS_MATERIA_FUERATOLERANCIA:
                              infoReg.setText("Registro realizado con errores");
                              info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase());
                                   /* + " (Se tomará en cuenta el último registro del día como salida)"
                                    + "\nEntrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida Fuera de Tolerancia de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());/*
                            break;
                            /*
                        case EYS_MATERIA_REGISTRADA:
                              infoReg.setText("Registro duplicado");
                              info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                                    + " (Se tomará en cuenta el último registro del día como salida)"
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            break; */
                        case EYS_MATERIA_SINACTIVIDAD:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Se ha registrado salida de " + data.getUsuario().getTipo().toLowerCase()
                                    + "" );
                            break;
                    }
                    
                    if (data.getHorario().getRegistroEntrada() != null) {
                        horaTxt.setText("Hora de entrada: " + Utils.formatTime(
                                data.getHorario().getRegistroEntrada().getFechahora()));
                        entradaOK.setVisible(true);
                    }
                    horaSalTxt.setText("Hora de salida: " + Utils.formatTime(data.getRegistro().getFechahora()));
                    salidaOK.setVisible(true);        
                    break;

                    
                case SALIDAANTES:
                    infoReg.setText("Registro exitoso"); //es que como se manejan 2 registros: tipo y tipom, por eso puede que sea salida antes pero en realidad haya algo de materias
                    info.setText("");
                    if (data.getHorario().getRegistroEntrada() != null){
                        horaTxt.setText("Hora de entrada: "+Utils.formatTime(
                                data.getHorario().getRegistroEntrada().getFechahora()));
                        entradaOK.setVisible(true);                
                    }
                    horaSalTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));                
                    salidaOK.setVisible(true);  
                    
                    switch(data.getTipoRegistroMat()){
                        case EYS_MATERIA: case EYS_MATERIA_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                               entradaOK.setVisible(false); 
                               salidaOK.setVisible(false);
                            break;
                        case EYS_MATERIA_E: case EYS_MATERIA_E_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nEntrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );   
                               entradaOK.setVisible(false); 
                            break;
                        case EYS_MATERIA_S: case EYS_MATERIA_S_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                              entradaOK.setVisible(false); 
                            break; 
                        case EYS_MATERIA_E_FUERATOLERANCIA:
                            infoReg.setText("Registro realizado con errores");
                              info.setText( info.getText()
                                    + "\nEntrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                              entradaOK.setVisible(false); 
                            break;
                       /*
                        case EYS_MATERIA_E_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                            break; */
                        case EYS_MATERIA_S_ANTES:
                            infoReg.setText("Registro realizado con errores");
                              info.setText( info.getText()
                                    + "\nSalida anticipada de materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                              entradaOK.setVisible(false); 
                            break;
                            /*
                        case EYS_MATERIA_S_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                            break; */
                        case EYS_MATERIA_FUERATOLERANCIA:
                            infoReg.setText("Registro realizado con errores");
                              info.setText( info.getText()
                                    + "\nRegistro Fuera de Tolerancia"   );
                              entradaOK.setVisible(false); 
                            break;
                            /*
                        case EYS_MATERIA_REGISTRADA:
                              info.setText( info.getText()
                                    + "\nEntrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            break; */
                        default:
                            infoReg.setText("Registro realizado con errores");
                            info.setText("Ha registrado salida antes de cubrir su jornada "
                            + "laboral, se ha creado un incidente en el sistema. "
                            + "Puede revisar el caso con su Jefe inmediato");
                            break;
                    }
                    break;         
                
                case SINACTIVIDAD:


                    switch(data.getTipoRegistroMat()){
                        case EYS_MATERIA: case EYS_MATERIA_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                            info.setText("Entrada a materia: "+data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula()
                                    + "\nSalida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break;
                        case EYS_MATERIA_E: case EYS_MATERIA_E_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                              info.setText( "Entrada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );   
                              horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break;
                        case EYS_MATERIA_S: case EYS_MATERIA_S_REGISTRADA:
                            infoReg.setText("Su registro ha sido exitoso");
                              info.setText( "Salida de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula());
                              horaTxt.setText("Hora de Salida: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break;
                        case EYS_MATERIA_E_FUERATOLERANCIA:
                             infoReg.setText("Registro realizado con errores");
                              info.setText( "Entrada Fuera de Tolerancia a materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                              horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break;
                            /*
                        case EYS_MATERIA_E_REGISTRADA:
                            infoReg.setText("Registro duplicado");
                              info.setText( "Entrada duplicada a materia: "+ data.getMateriaActual().getMateria()
                                    + "\nAula: "+ data.getMateriaActual().getAula() );
                              horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break; */
                        case EYS_MATERIA_S_ANTES:
                            infoReg.setText("Registro realizado con errores");
                              info.setText( "Salida anticipada de materia: "+ data.getMateriaEnCurso().getMateria()
                                    + "\nAula: "+ data.getMateriaEnCurso().getAula() );
                              horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break;
                            /*
                        case EYS_MATERIA_S_REGISTRADA:
                            infoReg.setText("Registro duplicado");
                              info.setText( "Salida duplicada de materia: "+ data.getMateriaAnterior().getMateria()
                                    + "\nAula: "+ data.getMateriaAnterior().getAula() );
                              horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break; */
                        case EYS_MATERIA_FUERATOLERANCIA:
                            infoReg.setText("Registro realizado con errores");
                            info.setText("Registro de materia fuera del tiempo de tolerancia. "
                                    + "Recuerde que la tolerancia es de 20 minutos antes y "
                                    + "despues tanto a la hora de entrada como a la de salida en que esta programada su asignatura");
                            horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            break;
                       /*
                        case EYS_MATERIA_REGISTRADA:
                            infoReg.setText("Registro duplicado");
                            info.setText("Entrada a materia: "+data.getMateriaActual().getMateria()
                                        + "\nAula: "+ data.getMateriaActual().getAula()
                                        + "\nSalida de materia: "+data.getMateriaAnterior().getMateria()
                                        + "\nAula: "+ data.getMateriaAnterior().getAula());
                            horaTxt.setText("Hora de entrada: "+Utils.formatTime(data.getMateriaActual()
                                            .getRegistroEntrada().getFechahora()));
                            horaSalTxt.setText("Hora de este registro: "+Utils.formatTime(data.getRegistro().getFechahora()));
                            entradaOK.setVisible(true);
                            break; */
                        case EYS_MATERIA_SINACTIVIDAD: //en el caso que no haya ninguna actividad
                            infoReg.setText("Registro realizado con errores");                
                            info.setText(                        
                            data.getTipoJornada().equals("sinjornada")?// De asignatura?
                                "No se tiene actividad programada, se ha creado "
                            + "un incidente en el sistema. Recuerde que tiene una "
                            + "tolerancia de 20 minutos antes y despues de la "
                            + "hora en que esta programada la entrada y salida de su asignatura":
                                "No se tiene actividad programada, se ha creado "
                            + "un incidente en el sistema.");
                            horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    }
                    break;
                    
                case OFFLINE:
                    infoReg.setText("Su registro ha sido exitoso");
                    info.setText("Registro realizado fuera de linea (No hay internet)");
                    horaTxt.setText("Hora registrada: "+Utils.formatTime(data.getRegistro().getFechahora()));
                    entradaOK.setVisible(true);
                    break;    
                    
                    //falta revisar detalladamente si se registran adecuadamente en el superUser las materiasAnterior, actual y otra
                    // !!! falta revisar cuando le poenen un cierto dato y cuando otro en "horaTxt"
            }
        }
        
        if ( data.getMensaje()!= null ){
            messagePanel.setVisible(true);
            messageTxt.setText(data.getMensaje());
            
            Timeline time = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(messageTxt.scaleXProperty(),1),
                    new KeyValue(messageTxt.scaleYProperty(),1)),                        
                new KeyFrame(new Duration(300),
                    new KeyValue(messageTxt.scaleXProperty(),2),
                    new KeyValue(messageTxt.scaleYProperty(),2)),
                new KeyFrame(new Duration(500),
                    new KeyValue(messageTxt.scaleXProperty(),1),
                    new KeyValue(messageTxt.scaleYProperty(),1)));
            time.setCycleCount(1);
            time.play();
            
        } else {
            messagePanel.setVisible(false);
            messageTxt.setText("");
        }
        
        data = null;
    }
    
    private void fotoNoDisponible(){        
        Image imageMuestra = new Image(Main.class.getResource("images/error.jpg").toExternalForm());
        fotoUser.setImage(imageMuestra);
        log.info("Foto no disponible establecida");        
    }
     
}
