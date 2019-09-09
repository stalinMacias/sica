package sicaweb;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLInputElement;

public class AdminSicaClient {
    
    private static final String MAIL_PROVIDER = "http://atrigger.com";
    private static final String MAIL_PROVIDER_USER = "sica@valles.udg.mx";
    private static final String MAIL_PROVIDER_PASSWORD = "sica1234";
    
    public static void loadPaginaCorreosProgramados(WebView w){
        final BooleanProperty loginAttempted = new SimpleBooleanProperty();
        loginAttempted.setValue(false);
        final WebEngine engine = w.getEngine();
        
        engine.documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> ov, Document oldDoc, Document doc) {
                if (doc != null && !loginAttempted.get()) {
                    if (doc.getElementsByTagName("form").getLength() > 0) {
                        HTMLFormElement form = (HTMLFormElement) doc.getElementsByTagName("form").item(0);
                        if ("/user/login".equals(form.getAttribute("action"))) {
                            HTMLInputElement username = null;
                            HTMLInputElement password = null;
                            NodeList nodes = form.getElementsByTagName("input");
                            for (int i = 0; i < nodes.getLength(); i++) {
                                HTMLInputElement input = (HTMLInputElement) nodes.item(i);
                                if (input.getId()!=null)
                                switch (input.getId()) {
                                    case "_username":
                                        username = input;
                                        break;
                                    case "_password":
                                        password = input;
                                        break;
                                }
                            }
 
                            if (username != null && password != null) {
                                loginAttempted.set(true);
                                username.setValue(MAIL_PROVIDER_USER);
                                password.setValue(MAIL_PROVIDER_PASSWORD);
                                form.submit();
                            }
                        }
                    }
                }
            }
        });
        
        final BooleanProperty logged = new SimpleBooleanProperty();
        logged.setValue(false);
        
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) {
                if (t1 == Worker.State.SUCCEEDED && !logged.get()){
                    logged.set(true);
                    engine.load(MAIL_PROVIDER+"/panel/list/upcoming");
                } else {
                    
                }
            }
        });
        
        engine.load(MAIL_PROVIDER+"/user/login");
        
        
    }
    
}
