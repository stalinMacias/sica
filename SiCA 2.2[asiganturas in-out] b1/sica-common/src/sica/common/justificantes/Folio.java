package sica.common.justificantes;

import java.util.Date;
import javafx.collections.ObservableList;
import sica.common.Utils;

public class Folio implements JustificanteInterface {
    protected String folio;
    protected String fechayhora;
    protected String usuario;
    protected String nombreusuario;
    protected String justificante;
    protected String nombrejustificante;
    protected String descripcion_gral;
    protected String fraccion;
    protected String nombrefraccion;
    protected ObservableList<Comentario> comentarios;        
    protected String aceptado;
    protected String aceptadonombre;
    protected String aprobado;
    protected String aprobadonombre;
    
    public String getFolio() {
        return folio;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getFechayhora() {
        return fechayhora;
    }

    public Date getFechayHora(){
        return Utils.parseFullDate(fechayhora);
    }
    
    @Override
    public String getNombrejustificante() {
        return nombrejustificante;
    }

    @Override
    public String getDescripcionJustificante() {
        return descripcion_gral;
    }

    public String getNombrefraccion() {
        return nombrefraccion;
    }

    public ObservableList<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(ObservableList<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public Boolean isAprobado(){
        return aprobado!=null && aprobado.equals("1");
    }
    
    public Boolean isAceptado(){
        return aceptado!=null && aceptado.equals("1");
    }
    
    public Boolean isPendiente(){         
        return ((aceptado==null||aceptado.isEmpty()) && (aprobado==null||aprobado.isEmpty()))               
            || ((aceptado!=null&&!aceptado.isEmpty()) && isAceptado() && (aprobado==null||aprobado.isEmpty()));
            //|| (aprobado==null||aprobado.isEmpty());
    }

    public String getAprobadonombre() {
        return aprobadonombre;
    }

    public String getAceptadonombre() {
        return aceptadonombre;
    }
    

    public String getNombreusuario() {
        return nombreusuario;
    }

    public String getJustificante() {
        return justificante;
    }

    public String getFraccion() {
        return fraccion;
    }

    public void setAceptado(String aceptado) {
        this.aceptado = aceptado;
    }

    public void setAceptadonombre(String aceptadonombre) {
        this.aceptadonombre = aceptadonombre;
    }

    public void setAprobado(String aprobado) {
        this.aprobado = aprobado;
    }

    public void setAprobadonombre(String aprobadonombre) {
        this.aprobadonombre = aprobadonombre;
    }
    
    
    
}
