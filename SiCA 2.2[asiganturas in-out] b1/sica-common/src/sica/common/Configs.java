package sica.common;

public class Configs{
    
    public static ConfigProperty<String> SERVER = new ConfigProperty<>("hostserver", "http://127.0.0.1/sica");  //148.202.89.3  http://localhost/sica
    
    public static final long TOLERANCIA_CLASE = 20*60*1000; //millis
    public static final long TOLERANCIA_ADMINISTRATIVOS= 40*60*1000; //millis
    
    public static String SERVER_PHP(){
        return SERVER.get()+"/php/";
    }   
    public static String SERVER_JUSTIF(){
        return SERVER.get()+"/justificantes/";
    }   
    public static String SERVER_CAPTURAS(){
        return SERVER.get()+"/capturas/";
    }   
    public static String SERVER_FOTOS(){
        return SERVER.get()+"/Fotos/";
    }   
    public static String SERVER_IMGS(){
        return SERVER.get()+"/imgs/";
    }
    
    public static String PUSH_NOTIF(){
        return SERVER_PHP()+"pushnotif.html";
    }   
    public static String PHP_UPLOAD(){
        return SERVER_PHP()+"filemanager.php";
    }   
    public static String EMAIL_SENDER(){
        return SERVER_PHP()+"correoincidencias2.php";
    }   
    public static String SIIAU_LOGIN(){
        return SERVER_PHP()+"siiaulogin.php";
    }   
     
}
