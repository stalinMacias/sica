package sica;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sica.common.usuarios.Usuario;

public class CodigoValidator extends Service<UserData> {
    private final StringProperty usr;
    private ChangeListener<UserData> listener;

    public CodigoValidator(StringProperty s) {
        usr = new SimpleStringProperty();
        usr.bind(s);
    }   
        
    @Override
    protected Task<UserData> createTask() {        
        return new Task<UserData>() {
            @Override protected UserData call() throws Exception {
                updateMessage(null);
                if ( usr.get() == null) return null;
                
                Usuario u = LocalDB.getUsuario(usr.get());
                
                if ( u == null ){
                    updateMessage("Usuario no encontrado");
                    return null;
                }
                
                if( u.getNombre().equals("MANTENIMIENTO") ) {
                    System.out.println("Usuario MANTENIMIENTO: saliendo de la apliacion! ... ");
                    //updateMessage("Saliendo...");
                    //salir de la aplicacion.
                    // #HuellasFrec Finalizador.finalizar();
                    return null;
                }               
                
                UserData data = Autenticator.getData(u);
               
                if (!Autenticator.makeRegistro(data, "teclado")) {
                    updateMessage("Error realizando registro");
                    return null;
                }

                return data;
            }
        };  
    }

    public ReadOnlyStringProperty estatusProperty() {
        return messageProperty();
    }
    
    public void clearListener(){
        if (listener != null){
            this.valueProperty().removeListener(listener);
        }            
    }
    
    public void addListener(ChangeListener<UserData> ch){
        clearListener();
        listener = ch;        
        if (listener != null) {
            this.valueProperty().addListener(listener);
        }
    }
    
}
