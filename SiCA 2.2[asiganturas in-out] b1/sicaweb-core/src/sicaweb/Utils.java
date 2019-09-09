package sicaweb;

import eu.schudt.javafx.controls.calendar.DatePicker;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.swing.JOptionPane;
import sica.common.justificantes.Evento;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.objetos.Registro;

public class Utils extends sica.common.Utils{ 
    
    public static String fullDateFormat(Date d){
        return d==null? null: DateFormat.getDateInstance(DateFormat.FULL).format(d);
    }
    
    public static long getTimeDiff(String entrada, String salida){
        Date d1 = parseTime(entrada);
        Date d2 = parseTime(salida);
        return d2.getTime()-d1.getTime();
    }
    
    public static String millisToTime(long millis){
        return String.format("%02d:%02d:%02d", 
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
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
     
    public static DatePicker newDatePicker(String text){                          
         DatePicker d = new DatePicker();
         d.setPromptText(text);
         d.getCalendarView().setTodayButtonText("Hoy");
         d.setDateFormat(getDateFormat());        
         d.getStylesheets().add(Utils.class.getResource("DatePicker.css").toString());
         d.getCalendarView().getCalendar().setFirstDayOfWeek(Calendar.SUNDAY);
         return d;

    }
    public static DatePicker newDatePicker(String text, ChangeListener<Date> ch){
        DatePicker d = newDatePicker(text);
        d.selectedDateProperty().addListener(ch);
        return d;
    }
    
    public static void loadFonts(Class T, String ... fonts){
        //deben encontrarse en la carpeta fonts/
        for (String s : fonts){
            Font.loadFont(T.getResource("fonts/"+s).toExternalForm(),14);
        }        
    }
    
    public static File getArchivo(Window stage, int MBSize){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JPG, PDF", "*.jpg", "*.pdf"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg"),
            new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );        
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null){
            if (file.length() > (MBSize * 1024 * 1024)){ // 2Mb
                JOptionPane.showConfirmDialog(null,
                        "No se permiten archivos superiores a los "+MBSize+"Mb", 
                        "Error", 
                        JOptionPane.CLOSED_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                
            } else if (file.getName().toLowerCase().endsWith(".pdf")) {                
                if (PDFJustificantes.validate(file)){
                    return file;
                    
                } else {
                    JOptionPane.showConfirmDialog(null,
                        "No se permiten archivos con más de 10 paginas", 
                        "Error", 
                        JOptionPane.CLOSED_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } else {
                return file;
            }
        }
        return null;
    }
        
    public static Effect getBlurEffect(){
        BoxBlur bb = new BoxBlur();
        bb.setWidth(6);
        bb.setHeight(6);
        bb.setIterations(2);
        return bb;
    }
    
    public static Boolean urlExist(String url){
        HttpURLConnection httpUrlConn;
        try {
            httpUrlConn = (HttpURLConnection) new URL(url).openConnection();
            httpUrlConn.setRequestMethod("HEAD");
            
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(2000);
            httpUrlConn.setReadTimeout(3000);
 
            if (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK){
                return true;
            }
            
        } catch (IOException e){
           
        }        
        return false;
    }
    
    public static File getTempFile(String filename){
        return new File(System.getProperty("java.io.tmpdir")+filename);
    }
}
