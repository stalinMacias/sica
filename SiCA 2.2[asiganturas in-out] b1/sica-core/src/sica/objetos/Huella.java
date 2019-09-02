package sica.objetos;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Huella implements Serializable{
    
    @Id private Integer id;
    private String usuario;
    private byte[] huella;
//    private Integer posicion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public byte[] getHuella(){
        return huella;
    }

    public void setHuella(byte[] huella) {
        this.huella = huella;
    }       

    /*
    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
    */
    
    //@Override
    /*
    public int compareTo(Huella o) {
        
        Integer a = posicion;
        Integer b = o.getPosicion();
        return a.compareTo(b);
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    
}
