package sica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static sica.TipoRegistro.EYS_MATERIA_SINACTIVIDAD;
import sica.common.Utils;

public class EmailSender extends Thread implements Runnable{    
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    
    public static void send(TipoRegistro tipoReg, UserData usr){
        if ( Utils.urlExist(Configs.EMAIL_SENDER()) )        
            new EmailSender(tipoReg,usr).start();
    }
    
    public static void sendEyS(TipoRegistro tipoRegJornada, TipoRegistro tipoRegMateria, UserData usr){
        if ( Utils.urlExist(Configs.EMAIL_SENDER()) )        
            new EmailSender(tipoRegJornada,tipoRegMateria,usr).start();
    }
    
    //private final String opcion;
    private final String opcion;
    private final UserData usuario;   
    private final TipoRegistro tipoRegistro;
    private TipoRegistro tipoRegistroMat;
    
    /**
     * Constructor exclusivo para el caso de registrar solo entrada a asignaturas
     * @param tipoReg
     * @param usr 
     */
    private EmailSender(TipoRegistro tipoReg, UserData usr){        
        log.info("Verificando envio de correo");        
        tipoRegistro = tipoReg;
        tipoRegistroMat = null;
        usuario = usr;
        
        switch (tipoRegistro){
            case ENTRADATARDE: case ENTRADATARDEYMATERIA:
                opcion = "jornada_retardo";
                break;
            
            case ENTRADATARDEYFUERATOLERANCIA:
                opcion = "jornada_retardo_fuera_tolerancia";
                break;
                
            case MATERIAFUERATOLERANCIA:
                opcion = "fuera_tolerancia";
                break;
                
            case SALIDAANTES:
                opcion = "jornada_anticipada";
                break;
                
            case SINACTIVIDAD:
                opcion = usr.getTipoJornada().equals("sinjornada")?
                        "sin_actividad_academica":
                        "sin_actividad_laboral";
                break; 
                
            default:
                opcion = null;
                break;           
        }
    }
    
    /**
     * Constructor especifico para el caso de registrar EntradaYSalida a asignaturas.
     * @param tipoRegJornada
     * @param tipoRegMateria
     * @param usr 
     */    
    private EmailSender(TipoRegistro tipoRegJornada, TipoRegistro tipoRegMateria, UserData usr){        
        log.info("Verificando envio de correo");        
        tipoRegistro = tipoRegJornada;
        tipoRegistroMat = tipoRegMateria;
        usuario = usr;
        
        if(tipoRegJornada != null && tipoRegMateria != null){
        
        switch (tipoRegJornada){
            case ENTRADA:
                    switch(tipoRegMateria){
                        case EYS_MATERIA_E_FUERATOLERANCIA: case EYS_MATERIA_S_ANTES:
                            opcion = "fuera_tolerancia";
                             break;
                        case EYS_MATERIA_SINACTIVIDAD:
                            opcion = usr.getTipoJornada().equals("sinjornada") ? 
                            "sin_actividad_academica"
                            : "sin_actividad_laboral";
                            break;
                        default:
                            opcion = null;
                    }
                break;
            case ENTRADATARDE: 
                //opcion = "jornada_retardo";
                switch(tipoRegMateria){
                    
                    case EYS_MATERIA: case EYS_MATERIA_E: case EYS_MATERIA_S:
                        opcion = "jornada_retardo";
                        break;
                     case EYS_MATERIA_S_ANTES:
                        opcion = "jornada_retardo_fuera_tolerancia_EYS";
                        break;
                    default:
                        opcion = "jornada_retardo";
                        break;
                }
                break;
                
            case SALIDAANTES:
                //solo en el caso que de materias no se tenga actividad, entonces se cosideraría salida anticipada
                switch(tipoRegMateria){
                    case EYS_MATERIA_SINACTIVIDAD:
                        opcion = "jornada_anticipada";
                        break;
                    case EYS_MATERIA_S_ANTES:  
                        opcion = "fuera_tolerancia";
                        break;
                    default:
                        opcion = null;
                        break; 
                        
                }
                
                //opcion = "jornada_anticipada";
                break;
                
            case SALIDA:
                switch(tipoRegMateria){
                        case EYS_MATERIA_S_ANTES:
                            opcion = "fuera_tolerancia";
                             break;
                        case EYS_MATERIA_SINACTIVIDAD:
                            //En este caso es una salida normal, por ello no se crea incidente para correo.
                            opcion = null;
                            break;
                        default:
                            opcion = null;
                    }
                break;

            case SINACTIVIDAD:
                switch(tipoRegMateria){
                     case EYS_MATERIA_E_FUERATOLERANCIA: case EYS_MATERIA_S_ANTES:
                        opcion = "fuera_tolerancia";
                        break;
                    case EYS_MATERIA_SINACTIVIDAD:
                        opcion = usr.getTipoJornada().equals("sinjornada") ? 
                        "sin_actividad_academica"
                        : "sin_actividad_laboral";
                        break;
                    default:
                            opcion = null;
                            break;
                }
                break;
            default:
                opcion = null;
                break;
        }
        }else{
            opcion = null;
            log.error("Error!! tipoRegJornada o tipoRegMateria son null");
        }
        
        if(opcion == null){
            log.error("Error!!!! sucedió un error al elegir la OPCION de correo en el metodo EmaiSender para EyS: la opcion resulto = null");
        }
    }
        
    @Override
    public void run() {
        if (opcion == null || usuario.getUsuario().getCorreo() == null) {
            if(usuario.getUsuario().getCorreo() == null){ //por seguridad
                usuario.getUsuario().setCorreo("sin correo");
            }
            log.info("Envio de correo no necesario, {} , {} ",opcion,usuario.getUsuario().getCorreo());
            return;        
        }
        
        log.info("Enviando correo de incidencia");
        
        StringBuilder response = new StringBuilder();
        
        try {                       
            URL url = new URL(Configs.EMAIL_SENDER());            
                
            StringBuilder postData = new StringBuilder();                       
            postData.append("opcion=").append(URLEncoder.encode(opcion, "UTF-8"));
            postData.append("&nombre=").append(URLEncoder.encode(usuario.getUsuario().getNombre(), "UTF-8"));
            postData.append("&correo=").append(URLEncoder.encode(usuario.getUsuario().getCorreo(), "UTF-8"));
            postData.append("&tipousuarios=").append(URLEncoder.encode(usuario.getUsuario().getTipo(), "UTF-8"));
            System.out.println(usuario.getUsuario().getTipo());
            switch (tipoRegistro) {
                case SALIDAANTES:
                    postData.append("&hora=").append(
                            usuario.getHorario().getSalida());
                    break;
                case SINACTIVIDAD:
                    postData.append("&hora=").append(Utils.formatTime(
                            usuario.getRegistro().getFechahora()));
                    break;
                
            }
            
            if( tipoRegistroMat != null ){
                switch(tipoRegistroMat){
                    case EYS_MATERIA_E_FUERATOLERANCIA: case EYS_MATERIA_S_ANTES:  // entrada o salida fuera de tolerancia
                    // para proporcionar la hora del registro, la cual se asigna cuando se genera el registro.
                    postData.append("&hora=").append(Utils.formatTime(
                            usuario.getRegistro().getFechahora()));
                    if ( usuario.getMateriaEnCurso() != null && usuario.getMateriaEnCurso().getCrn() != null ) {
                        postData.append("&clase=").append( URLEncoder.encode(usuario.getMateriaEnCurso().getMateria(), "UTF-8") );
                    } else {
                        System.out.println("Error, no se encontro la materia en curso para enviar correo de FUERATOLERANCIA");
                    }
                        break;
                 }
            }
            
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            try (Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                int c; 
                while ((c = in.read()) >= 0)
                    response.append((char)c);                
            }
            
            log.info(response.toString());
            
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace(System.out);
            log.error(ex.getMessage());
        }
    }    
    
    
}
