package sica.common;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.text.Font;
import javafx.util.Duration;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.objetos.Registro;

public class Utils {    
    private static final SimpleDateFormat datefformat = new SimpleDateFormat("EEEE d 'de' MMMM 'de' y",Locale.getDefault());
    private static final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");    
    private static final SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");

    public static SimpleDateFormat getSimpleTimeFormat() {
        return timef;
    }       
    
    public static Date parseDate(String s){
        try{
            return formatDate.parse(s);
        } catch (ParseException e){
            return new Date();
        }
    }
    
    /**
     * Cuidado, este metodo es con f minuscula
     * @param d
     * @return un String con el formato extenso: EEEE d 'de' MMMM 'de' y, Locale.getDefault()
     */
    public static String formatfullDate(Date d){
        return datefformat.format(d);
    }
    
    
    
    public static String formatDate(Date d){
        return formatDate.format(d);
    }
    
    public static SimpleDateFormat getDateFormat(){
        return formatDate;
    }
    
    public static Date parseFullDateTime(String s){
        try{
            return datef.parse(s);
        } catch (ParseException e){
            return new Date();
        }
    }
    
    public static Date parseTime(String s){
        try{
            return timef.parse(s);
        } catch (ParseException e){
            e.printStackTrace(System.out);
            return new Date();
        }
    }
     
    /**
     * Cuidado: este metodo es con F mayuscula. 
     * @param d
     * @return un String con el formato yyyy-MM-dd HH:mm:ss
     */
    public static String formatFullDate(Date d){
        return datef.format(d);
    }
    
    public static String formatTime(Date d){
        return timef.format(d);
    }
        
    public static boolean dentroDeHorarioAdmin(String hora, String entrada){       
        try{        
            long dif = timef.parse(hora).getTime() - timef.parse(entrada).getTime() ;            
            return ( dif < Configs.TOLERANCIA_ADMINISTRATIVOS);

        } catch (ParseException e){
            e.printStackTrace(System.out);
            return false;
        }        
    }

    public static boolean horarioTerminado(String hora, String salida) {
        try{        
            long dif = timef.parse(hora).getTime() - timef.parse(salida).getTime();            
            return ( dif > -(5*60*1000) ); //5 min antes OK!

        } catch (ParseException e){
            e.printStackTrace(System.err);
            return false;
        }       
    }
    
    public static long getTimeDiff(String entrada, String salida){
        Date d1 = parseTime(entrada);
        Date d2 = parseTime(salida);
        return d2.getTime()-d1.getTime();
    }
    
    public static void updateSistemTime(String hora){        
        if (hora == null) return;
        
        System.out.println("Actualizando hora del equipo a "+hora);
        
        try {
            String comando = "cmd";
            String entrada = "time" + " " + hora;
        
            Process proceso = Runtime.getRuntime().exec(comando);
            try (BufferedOutputStream out = new BufferedOutputStream(proceso.getOutputStream())) {
                out.write(entrada.getBytes());
                out.write("\r\n".getBytes());
                out.flush();
            }
            proceso.waitFor();
            System.out.println("hora actualizada");
            
        } catch (IOException | InterruptedException ex) {
            System.out.println("Actualizacion de hora fallida");
        }
    }
    
    public static Boolean urlExist(String url){
        HttpURLConnection httpUrlConn;
        try {
            httpUrlConn = (HttpURLConnection) new URL(url).openConnection();
            httpUrlConn.setRequestMethod("HEAD");
            
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(2000);
            httpUrlConn.setReadTimeout(3000);
 
            // Print HTTP status code/message for your information.
            System.out.println("URL "+url+" response: "+ httpUrlConn.getResponseCode());
 
            if (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK){
                return true;
            }
            
        } catch (IOException e){
            System.out.println(e.getClass().getName()+" "+e.getMessage());
        }
        
        System.out.println("No se encontro la pagina "+url);
        return false;
    }
    
    public static void loadFonts(Class T, String ... fonts){
        System.out.println("Cargando fuentes");
        //deben encontrarse en la carpeta fonts/
        for (String s : fonts){
            Font.loadFont(T.getResource("fonts/"+s).toExternalForm(),14);
        }        
    }
    
    
    public static Registro findRegistro(String usr, Date fecha, String hora, ObservableList<Registro> regs){        
        for (Registro r : regs ){
            if (r.getUsuario().equals(usr)){
                if ( formatDate(fecha).equals(formatDate(r.getFechahora())) ){
                    if ( dentroDeHorario(hora, formatTime(r.getFechahora()))){
                        return r;
                    }
                }
            }
        }
        return null;
    }
    
    public static Registro findAndDeleteRegistro(Date fecha, String hora, ObservableList<Registro> regs){        
        for (Registro r : regs ){
            if ( formatDate(fecha).equals(formatDate(r.getFechahora())) ){
                if ( dentroDeHorario(hora, formatTime(r.getFechahora()))){
                    regs.remove(r);
                    return r;
                }
            }
        }
        return null;
    }
        
    public static Registro findPrimerRegistro(String usr, Date fecha,  ObservableList<Registro> regs){
        Registro re = null;
        for (Registro r : regs ){
            if (r.getUsuario().equals(usr) && formatDate(fecha).equals(formatDate(r.getFechahora())) ){                
                re = re==null? r: r.getFechahora().before(re.getFechahora())? r: re;
            }            
        }
        return re;
    }
    public static Registro findUltimoRegistro(String usr, Date fecha,  ObservableList<Registro> regs){
        Registro re = null;
        for (Registro r : regs ){
            if (r.getUsuario().equals(usr) && formatDate(fecha).equals(formatDate(r.getFechahora())) ){                
                re = re==null? r: r.getFechahora().after(re.getFechahora())? r: re;
            }            
        }
        return re;
    }
    public static Registro findAndDeletePrimerRegistro(Date fecha,  ObservableList<Registro> regs){
        Registro re = null;
        for (Registro r : regs ){
            if (formatDate(fecha).equals(formatDate(r.getFechahora())) ){                
                re = re==null? r: r.getFechahora().before(re.getFechahora())? r: re;
            }            
        }
        if (re!=null) regs.remove(re);
        return re;
    }
    
    public static Registro findAndDeleteUltimoRegistro(Date fecha,  ObservableList<Registro> regs){
        Registro re = null;
        for (Registro r : regs ){
            if (formatDate(fecha).equals(formatDate(r.getFechahora())) ){                
                re = re==null? r: r.getFechahora().after(re.getFechahora())? r: re;
            }            
        }
        if (re!=null) regs.remove(re);
        return re;
    }
    
    
    public static String fullDateFormat(Date d){
        return d==null? null: DateFormat.getDateInstance(DateFormat.FULL).format(d);
    }
       
    public static Date parseFullDate(String s){
        try{
            return datef.parse(s);
        } catch (ParseException | NumberFormatException e){
            e.printStackTrace(System.out);
            return new Date();
        }
    }
    
    public static String millisToTime(long millis){
        return String.format("%02d:%02d:%02d", 
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
    
    public static boolean dentroDeHorario(String a, String b){       
        try{        
            long dif = Math.abs( timef.parse(a).getTime() - timef.parse(b).getTime() );            
            return ( dif < Configs.TOLERANCIA_CLASE );

        } catch (ParseException e){
            e.printStackTrace(System.out);
            return false;
        }        
    }
    
    public static boolean horarioTerminadoAdmin(String hora, String salida) {
        try{        
            long dif = timef.parse(hora).getTime() - timef.parse(salida).getTime();            
            return ( dif > 0 );

        } catch (ParseException e){
            e.printStackTrace(System.out);
            return false;
        }
    }
    
    public static int diaDeLaSemana(String day){
        switch(day){
            case "DOMINGO": return Calendar.SUNDAY; 
            case "LUNES": return Calendar.MONDAY;
            case "MARTES": return Calendar.TUESDAY;
            case "MIERCOLES": return Calendar.WEDNESDAY;
            case "JUEVES": return Calendar.THURSDAY;
            case "VIERNES": return Calendar.FRIDAY;
            case "SABADO": return Calendar.SATURDAY;
            default: return 0;
        }
    }
    
    public static String diaDeLaSemana(int day){
        switch(day){
            case Calendar.SUNDAY: return "DOMINGO"; 
            case Calendar.MONDAY: return "LUNES";
            case Calendar.TUESDAY: return "MARTES";
            case Calendar.WEDNESDAY: return "MIERCOLES";
            case Calendar.THURSDAY: return "JUEVES";
            case Calendar.FRIDAY: return "VIERNES";
            case Calendar.SATURDAY: return "SABADO";
            default: return "";
        }
    }
    
    public static String diaDeLaSemana(Date dia){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dia);
        return diaDeLaSemana(calendar.get(Calendar.DAY_OF_WEEK));
    }
    
    public static boolean cercaEntrada(String hora, String entrada, String salida){        
        long ent = parseTime(entrada).getTime();
        long sal = parseTime(salida).getTime();
        
        return  (parseTime(hora).getTime()-ent) <= ( (sal-ent)/2 );
    }
    
    public static Evento findEvento(Date fecha, ObservableList<Evento> eventos){
        
        for (Evento e: eventos){
            if (fecha.compareTo(e.getInicio())>=0 && fecha.compareTo(e.getFin())<=0){
                return e;
            }
        }
        return null;
    }
    
    public static JustificantePeriodo findJustificanteDia(String usr, Date fecha, ObservableList<JustificantePeriodo> justif){
        
        for (JustificantePeriodo j: justif){
            if (j.getUsuario().equals(usr) &&
                (fecha.compareTo(j.getFechaInicial())>=0 && fecha.compareTo(j.getFechaFinal())<=0) ){
                
                return j;                
            }
        }
        
        return null;
    }
        
    public static JustificantePeriodo findJustificanteDia(Date fecha, ObservableList<JustificantePeriodo> justif){
        
        for (JustificantePeriodo j: justif){
            if (fecha.compareTo(j.getFechaInicial())>=0 && fecha.compareTo(j.getFechaFinal())<=0 ){                
                return j;                
            }
        }
        
        return null;
    }
    
    public static JustificanteAsignatura findAndDeleteJustificanteClase(String crn, Date fecha, ObservableList<JustificanteAsignatura> justif){
        
        for (JustificanteAsignatura j: justif){
            if (j.getCrn().equals(crn) && formatDate(fecha).equals(j.getFecha())){
                justif.remove(j);
                return j;                
            }
        }
        
        return null;
    }
                
    @SuppressWarnings("unchecked")
    public static void configureTooltipDuration(int openDelay, int visibleDuration, int closeDelay){
        try {  
              
            Class TTBehaviourClass = null;  
            Class<?>[] declaredClasses = Tooltip.class.getDeclaredClasses();              
            for (Class c:declaredClasses) {  
                if (c.getCanonicalName().equals("javafx.scene.control.Tooltip.TooltipBehavior")) {  
                    TTBehaviourClass = c;  
                    break;  
                }  
            }    
            if (TTBehaviourClass == null){
                throw new NullPointerException("TooltipBehavior.Class no encontrada");
            }
            Constructor constructor = TTBehaviourClass.getDeclaredConstructor(  
                    Duration.class, Duration.class, Duration.class, boolean.class);  
            
            constructor.setAccessible(true);  
            Object newTTBehaviour = constructor.newInstance(  
                    new Duration(openDelay), new Duration(visibleDuration),   
                    new Duration(closeDelay), false);  
              
            Field ttbehaviourField = Tooltip.class.getDeclaredField("BEHAVIOR");  
             
            ttbehaviourField.setAccessible(true);                
            ttbehaviourField.set(Tooltip.class, newTTBehaviour);  
              
        } catch (SecurityException | NoSuchMethodException | InstantiationException 
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException 
                | NoSuchFieldException | NullPointerException e) {  
            System.out.println(e.getMessage());  
        }          
    }
    
    public static Effect getBlurEffect(){
        BoxBlur bb = new BoxBlur();
        bb.setWidth(6);
        bb.setHeight(6);
        bb.setIterations(2);
        return bb;
    }
      
    public static File getTempFile(String filename){
        return new File(System.getProperty("java.io.tmpdir")+filename);
    }
    
    @Deprecated
    public static void openOSK(){
        //
        new Thread( () -> {
            try {
                System.out.println("launching osk");
                new ProcessBuilder("cmd", "/c", System.getenv("SystemRoot") + "/system32/osk.exe").start();
                Thread.sleep(300);

            } catch (IOException | InterruptedException e){ e.printStackTrace(System.err);}
        }).start();//*/
    }
    
    @Deprecated
    public static void openOSK(final int posX, final int posY, final int tamX, final int tamY){
        //*        
        new Thread( () -> {            
            try {
                System.out.println("launching osk");
                new ProcessBuilder("cmd", "/c", System.getenv("SystemRoot") + "/system32/osk.exe").start();
                Thread.sleep(300);
                
            } catch (IOException | InterruptedException e){ e.printStackTrace(System.err);}
            
            HWND hwnd = User32.INSTANCE.FindWindow(null, "Teclado en pantalla");
            if (hwnd != null){                
                User32.INSTANCE.SetWindowPos(hwnd, null, posX+2, posY+2, tamX, tamY, User32.SWP_NOZORDER);
            }
        }).start();//*/
    }
    
    @Deprecated
    public static void closeOSK(){
        System.out.println("Attempting close osk");
        try {  
            Runtime.getRuntime().exec("taskkill /f /im osk.exe"); 
        } catch (IOException e){
            e.printStackTrace(System.err);
        }
    }
}
