package sica;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import sica.common.usuarios.Usuario;
import sica.objetos.Huella;

public final class ScannerValidator extends Scanner {
    private static final Logger log = Logger.getLogger(ScannerValidator.class);
    
    private ChangeListener<UserData> listener;
    private final Service<UserData> buscarTask;
    private final DPFPVerification verificador;
    private DPFPDataEvent dataEvent;    
    
    public ScannerValidator(final boolean makeRegistro) {  
        
        //Agregado para maximizar la seguridad de la varificacion -> un far muy bajo = alta securidad de FAR -> FAR -> False  Acceptance Rate y , FRR -> False Rejection Rate
        verificador = DPFPGlobal.getVerificationFactory().createVerification(); 
        verificador.setFARRequested( DPFPVerification.HIGH_SECURITY_FAR );
        
        buscarTask = new Service<UserData>() {
            @Override protected Task<UserData> createTask() {
                return new Task<UserData>() {
                    @Override protected UserData call() throws Exception {
                        updateMessage(null);
                        if ( dataEvent == null ) return null;
                        String usr = buscarHuella();
                        if ( usr == null) {
                            updateMessage("Huella no reconocida");
                            return null;
                        }
                        Usuario u = LocalDB.getUsuario(usr);
                        if ( u == null ){
                            updateMessage("Usuario no reconocido");
                            return null;
                        }
                        
                        UserData data = Autenticator.getData(u);
                        
                        if (makeRegistro && !Autenticator.makeRegistro(data, "huella")){
                            updateMessage("Error realizando registro");
                            return null;
                        }
                        
                        return data;
                    }
                };
            }
        };    
    }
    
    @Override public void processData(DPFPDataEvent event) {
        dataEvent = event;
        Platform.runLater(new Runnable() { @Override public void run() {
            if (!buscarTask.isRunning()){
                buscarTask.reset();
                buscarTask.start();
            }
        }});
    }
        
    
    public String buscarHuella() {     
        
        try {
            DPFPFeatureSet featureSet = extractor.createFeatureSet(dataEvent.getSample(), DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);            
            for ( Huella h : LocalDB.getHuellas()){ 
                DPFPTemplate reference = DPFPGlobal.getTemplateFactory().createTemplate(h.getHuella());
                DPFPVerificationResult result = verificador.verify(featureSet, reference);
                if (result.isVerified()){ 
                    return h.getUsuario();
                }
            }
            
        } catch (DPFPImageQualityException e){
            log.error(e.getMessage());
        }            
        
        return null;    
    } 
    
    public void clearListener(){
        if (listener != null){
            buscarTask.valueProperty().removeListener(listener);
        }            
    }
    
    public void addListener(ChangeListener<UserData> ch){
        clearListener();
        listener = ch;        
        if (listener != null) {
            buscarTask.valueProperty().addListener(listener);
        }
    }
    public ReadOnlyBooleanProperty runningProperty(){
        return buscarTask.runningProperty();
    }

    public ReadOnlyStringProperty estatusProperty() {
        return buscarTask.messageProperty();
    }
    
    
    
}
