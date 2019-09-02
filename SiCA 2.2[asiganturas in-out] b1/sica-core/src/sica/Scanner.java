package sica;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Scanner {
    private final static Logger log = LoggerFactory.getLogger(Scanner.class);
    private static final List<Scanner> scannerList = new ArrayList<>();    
    private final static BooleanProperty conectedProperty = new SimpleBooleanProperty(false);;
    
    private final DPFPCapture capturer;
    protected final DPFPFeatureExtraction extractor;
    
    public abstract void processData(DPFPDataEvent event);
    
    protected Scanner() {            
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction(); 
        
        capturer.addDataListener( new DPFPDataAdapter() {
            @Override public void dataAcquired(DPFPDataEvent e) {
                if (log.isDebugEnabled()) log.debug("Huella capturada");
                processData(e);
            }
        });
        
        capturer.addReaderStatusListener(new DPFPReaderStatusAdapter(){
            @Override public void readerConnected(DPFPReaderStatusEvent s) {
                Platform.runLater(new Runnable() { @Override public void run() {
                    conectedProperty.set(true);
                }});
            }
            @Override public void readerDisconnected(DPFPReaderStatusEvent s) {
                Platform.runLater(new Runnable() { @Override public void run() {
                    conectedProperty.set(false);
                }});
            }
        });
        addScannerToList();
    }
        
    public static ReadOnlyBooleanProperty connectedProperty(){
        return conectedProperty;
    }
   
    public void startCapturing(){
        if (!capturer.isStarted())
            capturer.startCapture();
    }
    
    public void stopCapturing(){
        if (capturer.isStarted())
            capturer.stopCapture();
    }
    
    private void addScannerToList(){
        scannerList.add(this);
    }
    
    public static void stopAllScanners(){
        for (Scanner s : scannerList){
            s.stopCapturing();
        }
    }
}  
      
     