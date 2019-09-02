package sica.common.justificantes;

import javafx.collections.ObservableList;

public class TipoJustificante {
    private String id;
    private String nombre;
    private String descripcion_gral;
    private String descripcion;
    private String documentos;
    private String tipousuario;
    private ObservableList<Fraccion> fracciones;

    public String getTipousuario() {
        return tipousuario;
    }
    
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion_gral() {
        return descripcion_gral;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getDocumentos() {
        return documentos.equals("1");
    }

    public void setFracciones(ObservableList<Fraccion> fracciones) {
        this.fracciones = fracciones;
    }
    
    public ObservableList<Fraccion> getFracciones() {
        return fracciones;
    }
    
    
    
}
