package sica.common.objetos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import sica.common.Utils;

@Entity
public class Registro implements Serializable {

    @Id private String usuario;
    @Id private String fechahora;
    private String tipo;
    private String modificado;
    private String equipo;

    public String getUsuario() {
        return usuario;
    }    
    
    public Date getFechahora() {
        return Utils.parseFullDate(fechahora);        
    }

    public String getHora(){
        return Utils.formatTime(getFechahora());
    }
    
    public String getFecha(){
        return Utils.formatDate(getFechahora());
    }
    
    public String getFechahoraS() {
        return fechahora;        
    }
    
    public void setFechahora(String fechahora) {
        this.fechahora = fechahora;
    }

    public void setFechahora(Date fechahora) {
        this.fechahora = Utils.formatFullDate(fechahora);
    }    
    
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getModificado() {
        return modificado;
    }

    public void setModificado(String modificado) {
        this.modificado = modificado;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }
    
    
    
}
