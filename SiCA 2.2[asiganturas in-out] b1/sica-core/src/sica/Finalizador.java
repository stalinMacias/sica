package sica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import sica.objetos.Huella;
import sica.objetos.HuellaUbicacion;

/**
 * Clase usada para finalizar la aplicacion
 * @author Cuvalles.SicaTeam
 */
public class Finalizador {
    
    /**
     * Este metodo se elaboro para que cuando se cierra la aplicacion, primeramente se guarde la lista de huellas actual en la DB local,
     * ya que ahora se guarda un orden especial en el que el ultimo usuario que registra con huella se coloca adelanta de la fila de huellas. 
     * Esto con la idea de agilizar la busqueda de huellas.
     */
    public static void finalizar(){
        //Se finalizara la aplicacion
        System.out.println("Finalizando la aplicacion . . .");
        //guardar las huellas en la DB local, esto para no perder el orden nuevo que se ha generado
        List<Huella> lstHuellas = ScannerValidator_Frec.getHuellas();
        if( lstHuellas != null && !lstHuellas.isEmpty() ){
            //LocalDB.deleteHuellasAll();
            //System.out.println("Finalizador:: Se guardaran " + lstHuellas.size() + " huellas");
            int contador = 0;
            
            //no se guardaran las huellas, solo la lista de Ubicaciones
            
            /*
            for(Huella hr: lstHuellas){
                contador++;
                
                Huella h = new Huella();
                h.setUsuario( hr.getUsuario() );
                h.setId( hr.getId() );                               
                h.setHuella( hr.getHuella() );
                h.setPosicion( hr.getPosicion() );  
                
                //funcionara?
                //System.out.println("Guardando huella de: " + h.getUsuario());
                LocalDB.saveHuella(h);
            } */
            

            HuellaUbicacion ubi = LocalDB_Frec.getUbicacion(0); //para comprabar si exiten ubicaciones guardadas
            if( ubi != null ){ 
                System.out.println("[Finalizador] se eliminaran las ubicaciones de la LocalDB");
                    LocalDB_Frec.deleteUbicacionesAll();
            } else { System.out.println("[Finalizador] no existen las ubicaciones guardadas en la LocalDB"); }
            
            //-------------------------------------
            System.out.println("[Finalizador] Guardando lista de Ubicaciones en LocalDB");
            LocalDB_Frec.saveUbicaciones(ScannerValidator_Frec.getUbicaciones());
            //--------------------------------------

            //borrame
            System.out.println("Ahora se volveran a cargar la lista de ubicaciones");
            List<HuellaUbicacion> nubs = new ArrayList<>();
            nubs = LocalDB_Frec.getUbicaciones();
            System.out.println("Tamaño lista ubicaciones vuelta a cargar: " + nubs.size());
            
        } else {
            System.out.println("Error! huellas vacias al querer guardarlas en la DB local");
        }
        
        Platform.exit();
    }
    

    
    public static void imprimirHuellas(List<Huella> lista){
        /*
        System.out.println("=======================================");
        System.out.println("                  imprimiendo huellas      ");
        for( Huella h: lista ){
            System.out.println("huella id: " + h.getId());
        }
        System.out.println("=======================================");
        */
        //System.out.println("buscando la huella id 587");
        int size = lista.size();
        size -= 1;
        if( size > 0){
            for(int i = size; i >= 0;  i--){
                if (lista.get(i).getId() == 587) {
                    System.out.println("Huella encontrada: ");
                    System.out.println("usuario: " + lista.get(i).getUsuario() );
                    System.out.println("posicion de la huella: " + i);
                }
            }
        }
        
        
        
    }
    
}
