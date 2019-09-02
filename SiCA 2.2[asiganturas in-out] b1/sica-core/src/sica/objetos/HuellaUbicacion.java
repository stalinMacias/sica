package sica.objetos;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Cuvalles.SicaTeam
 */
@Entity
public class HuellaUbicacion implements Serializable {
    
        @Id private Integer indice;
        private Integer huellaID;
       // private Integer ubicado; // debido a que no se debe modificar el id, es por eso que se crea otro atributo

    public Integer getIndice() {
        return indice;
    }

    public void setIndice(Integer indice) {
        this.indice = indice;
    }
    
    public Integer getHuellaID() {
        return huellaID;
    }

    public void setHuellaID(Integer huellaID) {
        this.huellaID = huellaID;
    }


    
    
        
        
    
}
