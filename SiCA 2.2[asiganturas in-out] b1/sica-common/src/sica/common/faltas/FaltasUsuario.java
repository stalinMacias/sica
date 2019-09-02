package sica.common.faltas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FaltasUsuario {

    private String usuario;
    private String nombre;
    private final ObservableList <Falta> faltas;
    
    public FaltasUsuario(String usr,String nom){
        this();
        usuario = usr;
        nombre = nom;
    }
    
    public FaltasUsuario(){
        faltas = FXCollections.observableArrayList();
    }    

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }    

    public ObservableList<Falta> getFaltas() {
        return faltas;
    }

    public void addFalta(Falta f) {
        faltas.add(f);
    }    

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    
}
