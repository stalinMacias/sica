package sica;

import java.net.URISyntaxException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;
import sica.objetos.Huella;

public class LocalDB {

    private static final Logger log = LoggerFactory.getLogger(LocalDB.class);
    
    private static EntityManagerFactory emf;        
    private static EntityManager em;
    
    public static String getLocalDBPath(){
        String path;
        try {
            path = LocalDB.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart();
        } catch (URISyntaxException ex) {
            log.error(ex.getMessage());
            path = LocalDB.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        }        
        if (log.isDebugEnabled()) log.debug("Path original: {}",path);
        String name= path.substring(0, path.lastIndexOf("/") + 1)+"db/database.odb"; 
        
        if (log.isDebugEnabled()) log.debug("Path: {}",name);
         
        return name;
    }
        
    public static boolean initialize(){
        try {
            emf = Persistence.createEntityManagerFactory(getLocalDBPath());
            em = emf.createEntityManager();
            return true;
            
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
    
    public static void closeLocalDB(){
        if (log.isDebugEnabled()) log.debug("Cerrando base de datos local");
        emf.close();
    }
    
    public static void deleteAll(){
        if (log.isDebugEnabled()) log.debug("Limpiando base de datos local");
        
        em.getTransaction().begin();            
        int he = em.createQuery("DELETE FROM Huella").executeUpdate();
        int ue = em.createQuery("DELETE FROM Usuario").executeUpdate();
        em.getTransaction().commit();   
        
        if (log.isDebugEnabled()){
            log.debug("{} huellas eliminadas",he);
            log.debug("{} usuarios eliminados",ue);
        }
    }
    
    public static List<Huella> getHuellas(){        
        TypedQuery<Huella> query =  em.createQuery("SELECT h FROM Huella h", Huella.class);        
        List<Huella> results = query.getResultList();        
        return results;        
    }
    
    public static void saveHuella(Huella h){   
                
        em.getTransaction().begin();
        em.persist(h);
        em.getTransaction().commit();        

    }
    
    public static void deleteHuella(Integer id){
        Huella hue= em.find(Huella.class, id);        
        if ( hue != null ){
            em.getTransaction().begin();
            em.remove(hue);
            em.getTransaction().commit();  
        }
    }
    
    public static List<Registro> getRegistrosOffline(){        
        TypedQuery<Registro> query =  em.createQuery("SELECT r FROM Registro r", Registro.class);        
        List<Registro> results = query.getResultList();        
        return results;        
    }
    
    public static void saveRegistroOffline(Registro r){           
        em.getTransaction().begin();
        em.persist(r);
        em.getTransaction().commit();                
    }
    
    public static void deleteRegistroOffline(Registro r){
        if (em.contains(r)){
            em.getTransaction().begin();
            em.remove(r);
            em.getTransaction().commit();  
        }
    }
    
    public static List<Usuario> getUsuarios(){        
        TypedQuery<Usuario> query =  em.createQuery("SELECT u FROM Usuario u", Usuario.class);        
        return query.getResultList();                
    }
    
    public static Usuario getUsuario(String usr){                        
        for ( Usuario u : getUsuarios() )
            if (u.getCodigo().equals(usr))
                return u;
        
        return em.find(Usuario.class, usr);
    }
     
    public static void saveUsuario(Usuario u){

        em.getTransaction().begin();
        em.persist(u);
        em.getTransaction().commit();

    }
    
    public static void deleteUsuario(String usr){
        Usuario user = em.find(Usuario.class, usr);        
        if ( user!=null ){
            em.getTransaction().begin();
            em.remove(user);
            em.getTransaction().commit();  
        }
    }
    
}
