package sica;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import java.io.ByteArrayInputStream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScannerCapturator extends Scanner {
    private final static Logger log = LoggerFactory.getLogger(ScannerCapturator.class);

    private final DPFPEnrollment reclutador;
    private final IntegerProperty huellasNecesarias;
    
    private EventHandler<Event> onFailed;
    private EventHandler<Event> onSuccess;
    private String user;
    
    public ScannerCapturator(){
        reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment(); 
        huellasNecesarias = new SimpleIntegerProperty(4);       
    }
    
    public ScannerCapturator(String user){
        this();
        this.user = user;        
    }
    
    public DoubleBinding progressHuellasNecesariasProperty(){
        return Bindings.subtract(4d, huellasNecesarias).divide(4d);
    }    
    
    public void cleanCapturador(){
        reclutador.clear();
        huellasNecesarias.set(4);
    }
    
    public void setUser(String user){
        this.user = user;
        huellasNecesarias.set(4);
        reclutador.clear();
    }
    
    @Override
    public void processData(DPFPDataEvent event) {        
        if (user != null){
            try {
                DPFPFeatureSet set = extractor.createFeatureSet(event.getSample(), DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
                if (set != null){
                    try{
                        reclutador.addFeatures(set);
                    } catch (DPFPImageQualityException e){
                        if (log.isDebugEnabled()) log.debug(e.getMessage());
                    }
                    switch(reclutador.getTemplateStatus()){                    
                        case TEMPLATE_STATUS_READY:	// informe de éxito y reinicia la captura de huellas
                            byte[] serialize = reclutador.getTemplate().serialize();
                            log.info("Guardando huella");
                            ConnectionServer.guardarHuella(user, new ByteArrayInputStream(serialize), serialize.length);                            
                            onSuccess.handle(new Event(EventType.ROOT));                            
                            reclutador.clear();                            
                            break;

                        case TEMPLATE_STATUS_FAILED: // informe de fallas y reiniciar la captura de huellas
                            onFailed.handle(new Event(EventType.ROOT));
                            reclutador.clear();
                            break;
                    }                    
                    huellasNecesarias.set(reclutador.getFeaturesNeeded());
                }

            } catch (DPFPImageQualityException e) {
                log.error(e.getMessage());
            }
        } else {
            log.info("Huella ignorada");
        }
    }    

    public void setOnFailed(EventHandler<Event> onFailed) {
        this.onFailed = onFailed;
    }

    public void setOnSuccess(EventHandler<Event> onSuccess) {
        this.onSuccess = onSuccess;
    }
        
}
