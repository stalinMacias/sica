package sica;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.LoggerFactory;
import sica.common.Utils;

public class Log {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Log.class);    
    private static volatile boolean opened = false;
    
    public static void initialize(boolean debug){
        
        Properties p = new Properties();        
        p.setProperty("log4j.rootLogger", debug? "DEBUG, A1":"INFO, A1");             
        p.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        p.setProperty("log4j.appender.A1.Target","System.out");        
        p.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        p.setProperty("log4j.appender.A1.layout.ConversionPattern", "[%d{HH:mm:ss,SSS}] %-10c{1} - %m%n");                
        p.setProperty("log4j.logger.com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver", "INFO");
        p.setProperty("log4j.logger.org.apache.http", "WARN");
        p.setProperty("log4j.logger.org.apache.http.wire", "WARN");
        
        
        PropertyConfigurator.configure(p);
        OutputStream os = new OutputStream() {
            private final StringBuilder line = new StringBuilder();
            @Override public void write(int b) throws IOException {
                if (b != '\n') line.append((char) b);
                else {                    
                    log.info(line.toString());
                    line.setLength(0);
                } 
            }
        };
        OutputStream os2 = new OutputStream() {
            private final StringBuilder line = new StringBuilder();
            @Override public void write(int b) throws IOException {
                if (b != '\n') line.append((char) b);
                else {                    
                    log.error("ERR - "+line.toString());
                    line.setLength(0);
                } 
            }
        };
        System.setOut(new PrintStream(os,true));
        //System.setErr(new PrintStream(os2,true));
    }
    
    public static void show(){
        
        if (!opened){
            Stage stage = new Stage();            

            final TextArea text = new TextArea();
            text.setEditable(false);
            
            final Appender ap = getAppender(text);
            Logger.getRootLogger().addAppender( ap );
            
            VBox vbox = new VBox();
            vbox.getChildren().add(text);
            VBox.setVgrow(text, Priority.ALWAYS);
            
            Scene scene = new Scene(vbox,500,350);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Log");
            
            stage.setOnCloseRequest((WindowEvent t) -> {
                ap.close();
                log.info("Cerrando consola");
            });
            
            stage.addEventFilter(KeyEvent.KEY_PRESSED, (final KeyEvent event) -> {              
                if (event.getCode() == KeyCode.U && event.isControlDown()){
                    log.info("Solicitando actualización");
                    Updater.update(true);
                }
            });        
            log.info("Consola iniciada");
            stage.show();            
            opened = true;
            
        }
        
    }
    
    private static Appender getAppender(final TextArea text){
        return new AppenderSkeleton() {
            @Override protected void append(final LoggingEvent le) {
                if (opened){                    
                    String s = "["+Utils.formatTime(new Date(le.getTimeStamp()))+"]   "
                            +le.getLoggerName().substring(le.getLoggerName().lastIndexOf('.')+1,
                                    le.getLoggerName().length())
                            +"  -  "
                            +le.getRenderedMessage()+"\n";
                    Platform.runLater(() -> text.appendText(s));    
                }
            }

            @Override
            public void close() {
                opened = false;
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
            
        };
    }
}
