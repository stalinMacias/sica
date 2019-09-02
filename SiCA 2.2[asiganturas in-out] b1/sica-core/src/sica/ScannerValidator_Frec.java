package sica;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import sica.common.usuarios.Usuario;
import sica.objetos.Huella;
import sica.objetos.HuellaUbicacion;

     /* Modificaciones del SiCA para tratart de imeplementar un algritmo que haga
     * mas rapido el la lectura de las huellas ordenando la lista de huellas poniendo
     * al principio las huellas más usadas. 
     * El algoritmo tiene ciertas fallas po lo que a partir de la version sica 2.2 b1 se 
     * deshabilito regresando al metodo anterior, pero no se elimina para poder utilizarlo en un futuro.
     * 
     * Hay varios atributos y etodos que se regresaron a su forma aterior, por ejemplo
     * las listas de huellas o referencas que se intento que fueran abtractas ahora vuelven a ser
     * normales.
     * 
     * Se usara una nota hastag #HuellasFrec ára identificar que era refete a ello. 
*/

public final class ScannerValidator_Frec extends Scanner {
    private static final Logger log = Logger.getLogger(ScannerValidator_Frec.class);
    
    private ChangeListener<UserData> listener;
    private final Service<UserData> buscarTask;
    private final DPFPVerification verificador;
    private DPFPDataEvent dataEvent;    
    
    private static List<Huella> huellas;
    private static List<HuellaUbicacion> ubicaciones;
    private static List<DPFPTemplate> referencias;

    
    public ScannerValidator_Frec(final boolean makeRegistro) {  
        
        //Agregado para maximizar la seguridad de la varificacion -> un far muy bajo = alta securidad de FAR -> FAR -> False  Acceptance Rate y , FRR -> False Rejection Rate
        verificador = DPFPGlobal.getVerificationFactory().createVerification(); 
        verificador.setFARRequested( DPFPVerification.HIGH_SECURITY_FAR );
        
        buscarTask = new Service<UserData>() {
            @Override protected Task<UserData> createTask() {
                return new Task<UserData>() {
                    @Override protected UserData call() throws Exception {
                        updateMessage(null);
                        if ( dataEvent == null ) return null;
                        String usr = buscarHuella();
                        if ( usr == null) {
                            updateMessage("Huella no reconocida");
                            return null;
                        }
                        Usuario u = LocalDB.getUsuario(usr);
                        if ( u == null ){
                            updateMessage("Usuario no reconocido");
                            return null;
                        }
                        
                        UserData data = Autenticator.getData(u);
                        
                        if (makeRegistro && !Autenticator.makeRegistro(data, "huella")){
                            updateMessage("Error realizando registro");
                            return null;
                        }
                        
                        return data;
                    }
                };
            }
        };    
    }
    
    @Override public void processData(DPFPDataEvent event) {
        dataEvent = event;
        Platform.runLater(new Runnable() { @Override public void run() {
            if (!buscarTask.isRunning()){
                buscarTask.reset();
                buscarTask.start();
            }
        }});
    }
    
    /**
     * Modificaciones del SiCA para tratart de imeplementar un algritmo que haga
     * mas rapido el la lectura de las huellas ordenando la lista de huellas poniendo
     * al principio las huellas más usadas. 
     * El algoritmo tiene ciertas fallas po lo que a partir de la version sica 2.2 b1 se 
     * deshabilito regresando al metodo anterior, pero no se elimina para poder utilizarlo en un futuro.
     * 
     * Hay varios atributos y etodos que se regresaron a su forma aterior, por ejemplo
     * las listas de huellas o referencas que se intento que fueran abtractas ahora vuelven a ser
     * normales.
     * 
     * Se usara una nota hastag #HuellasFrec ára identificar que era refete a ello.
     * 
     * @return 
     */
    public String buscarHuella() {     
        /*
         ideas para mejrorar este algoritmo
        
        !! Se está reaciendo el código para corregir problemas
        Version A5
        
        1.- Hacer que la ultima huella usada sea posicionada al principio
        2.- Crear un metodo Static que mantenga en memoria ram una lista las plantillas de todas las huellas, asi solo se 
            consultarian las plantillas de la lista en lugar de generarlas. Solo verificar el metodo actualizar para cuando se acutalice
            tambien lo haga esta lista.
        LISTO!
        
        */
        
        
        try {
            
            
            //Verificador de que es lo que falta para mostrarlo en pantalla
            if( huellas != null){
                if( !huellas.isEmpty()  ){
                    if( referencias != null ){
                        if( !referencias.isEmpty() ){
                            System.out.println("!!!! Configs.HUELLAS_UPDATE.get() = " + Configs.HUELLAS_UPDATE.get() );
                        } else { System.out.println("!!! Lst referencias -> vacia pero no null"); }
                    } else { System.out.println("!!!! Lst referencias -> null"); }
                } else { System.out.println("!!!! Huellas -> vacias, pero no Null"); }
            } else { System.out.println("!!!! Huellas -> null"); }
            
            
            
            //generar las listas de huellas y referencias de ser necesario
            if( huellas == null || huellas.isEmpty() || referencias == null || referencias.isEmpty()  || Configs.HUELLAS_UPDATE.get()  ){//huellas == null || huellas.isEmpty() || referencias == null || referencias.isEmpty()){
                recargarHuellas();
            }

            
            // nuevo algortimo ===========================================
            System.out.println("Creando un FeatureSet");
            DPFPFeatureSet featureSet = extractor.createFeatureSet(dataEvent.getSample(), DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);            
            int tam = huellas.size() - 1;
            System.out.println("tamaño de las huellas: " + tam);
            System.out.println("Comenzara la verificacion de huellas");
            for ( int i = tam ; i >= 0 ; i--){ 
                //DPFPTemplate reference = DPFPGlobal.getTemplateFactory().createTemplate(huellas.get(i).getHuella());
                DPFPTemplate reference = referencias.get(i);
                DPFPVerificationResult result = verificador.verify(featureSet, reference);
                if (result.isVerified()){ 
                    System.out.println(">> Posicion de la huella encontrada: " + i);
                    System.out.println("numero ID de huella: " + huellas.get(i).getId());
                    //este algoritmo va agregando al tope de la pila-lista las ultimas huellas usadas, por lo que la mas reicentemente usada, sera la primera en ser encontrada.
                    //pero esto solo sucede en la ram. cuando se reinicie el sistema iniciara como default
                    System.out.println("removiendo referencia");
                    DPFPTemplate rx = referencias.remove(i);
                    System.out.println("agregando referencia al final");
                    referencias.add(rx);
                    
                    System.out.println("removiendo huella");
                    Huella aux = huellas.remove(i);
                    System.out.println("colocando la huella al final");
                    huellas.add(aux);
                    
                    /*
                    HuellaUbicacion ux = ubicaciones.remove(i);
                    ux.setIndice(ubicaciones.size());
                    reordenarUbicaciones();
                    ubicaciones.add(ux); */
                    
                    System.out.println("Algo de infromacion::::::::::::::::::::::::");
                    System.out.println("size of lista ubicaciones: " + ubicaciones.size());
                    
                    System.out.println("rotando las ubicaciones");
                    System.out.println("valor del indice i: " + i);
                    //hay que borrar esto borrame
                    if(i >= ubicaciones.size()){
                        System.out.println("Error!!!!!!!!!!!!!!! el indici i es mas grande que el indice de ubicaciones");
                    }
                    System.out.println("-- se mandara ejecutar el metodo rotaUbicaciones con indice: " + i + ", y huellaID: " + ubicaciones.get(i).getHuellaID());
                    rotarUbicacionesHuellasID(i, ubicaciones.get(i).getHuellaID());
                    
                    
                    //ya solo falta verifcar la de Updater()
                    
                    /*
                    Huella aux = huellas.remove(i);
                    System.out.println("Se reordenaran las posiciones de la lista de huellas");
                    reordenarPosiciones(huellas);
                    //se agrega nueva posicon a la huella encontrada para mandarla al principio de la busqueda
                    aux.setPosicion( huellas.size() ); //no es necario sumar +1 debido a que la posicion de un arreglo incia en 0...
                    System.out.println("se ejecutara: huellas.add(aux)");
                    //agregamos la huella al final de la lista
                    if( !huellas.add(aux) ){
                        System.out.println("Error!!! al agregar aux a la lista huellas");
                    } */
                    
                    System.out.println("return aux.getUsuario() = " + aux.getUsuario());
                    return aux.getUsuario();
                }
            }
            // =======================================================
            
            
            
            /* Metodo Original
            DPFPFeatureSet featureSet = extractor.createFeatureSet(dataEvent.getSample(), DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);            
            for ( Huella h : LocalDB.getHuellas()){ 
                DPFPTemplate reference = DPFPGlobal.getTemplateFactory().createTemplate(h.getHuella());
                DPFPVerificationResult result = verificador.verify(featureSet, reference);
                if (result.isVerified()){ 
                    return h.getUsuario();
                }
            }
            */
            
            
            
        } catch (DPFPImageQualityException e){
            log.error(e.getMessage());
        }            
        
        return null;    
    } 
    
    public void clearListener(){
        if (listener != null){
            buscarTask.valueProperty().removeListener(listener);
        }            
    }
    
    public void addListener(ChangeListener<UserData> ch){
        clearListener();
        listener = ch;        
        if (listener != null) {
            buscarTask.valueProperty().addListener(listener);
        }
    }
    public ReadOnlyBooleanProperty runningProperty(){
        return buscarTask.runningProperty();
    }

    public ReadOnlyStringProperty estatusProperty() {
        return buscarTask.messageProperty();
    }
    
      public static List<Huella> getHuellas() {
        return huellas;
    }

    public static List<HuellaUbicacion> getUbicaciones() {
        return ubicaciones;
    }

    //recarga las huellas y las referencias en ram.
      
      /**
       * Vuelve a cargar la lista de huellas de la DB local, las reordena, y genera la lista de referencias (DPFPTemplate)
       */
      public static void recargarHuellas(){
                //ScreenManager.principal().avisar(t1);
                
                
                System.out.println("recargarHuellas(): RECARGANDO LISTADO DE HUELLAS EN RAM");
                
                //existen huellas en localDB?, 
                huellas = LocalDB.getHuellas();
                if(huellas == null || huellas.isEmpty()){ 
                    log.error("Error! no se pudo obtener ninguna lista de huellas de la LocalDB"); 
                    log.info("Se buscara actualizar todo de nuevo desde el servidor");
                    if(Configs.HUELLAS_UPDATE.get()){  //para evitar recursividad infinita si este metodo se está llamando desde la clase Updater
                        Updater.update(true); 
                    }
                    System.out.println("[ScannerValidator] Cargando huellas de la locaDB");
                    huellas = LocalDB.getHuellas();
                    //como no existian huellas en RAM, señal que es la primera vez que se está iniciando el programa, por lo tanto hay que tratar de cargar las ubicaciones de la LocalDB
                    
                    if(huellas == null || huellas.isEmpty()){
                        log.error("Continua el Error! no se pudo obtener por segunda ocasion ninguna lista de huellas de la LocalDB"); 
                    }
                } 
                
                if(huellas != null && !huellas.isEmpty()){
                    ubicaciones = LocalDB_Frec.getUbicaciones();
                }
                
                System.out.println("[ScannerValidator] Cargando ubicaciones de la locaDB");
                ubicaciones = LocalDB_Frec.getUbicaciones();

                System.out.println("###############################################");
                System.out.println("Se cargaron ubicaciones, se mostraran:");
                System.out.println("Se imprimirarn la lista de ubicaciones");
                if (ubicaciones != null && !ubicaciones.isEmpty()) {
                  //  for (HuellaUbicacion u : ubicaciones) {
                   //     System.out.println("huella id: " + u.getHuellaID() + ", indice: " + u.getIndice());
                  //  }
                } else {
                    System.out.println("!!!! ERROR: la lista UBICACIONES esta vacia o es nula");
                }
                
                //analizar el error que sale tras cerrar y abrir y volver a cerar la aplicacion 
                 //       el erorro al parecer es al querer elimianr las HulleaUbicaciones d ela LocalDB
                 
                
                
                
                if( ubicaciones == null || ubicaciones.isEmpty() ){
                    System.out.println("[ScannerValidator] No hay ubicaciones ni la localDB ni en RAM, procediendo a generar nuevas ubicaciones");
                    generarUbicaciones();
                }
                
                
                //Aqui voy;
                //Ordenar listaHuellas
                System.out.println("Se ordenaran las huellas con lista de ubicaciones");
                System.out.println("tamaño lista huellas antes = " + huellas.size());
                if(!ordenarHuellas()){
                    log.error("Error ordenando huellas usando las ubicaciones");
                }
                System.out.println("tamaño lista huellas despues = " + huellas.size());

                //se me había pasado crear la lista de referencias antes de agregarle elementos
                referencias = new ArrayList<DPFPTemplate>();
                log.info("Se hara la lista de referencias");
                for(Huella h2 : huellas){
                    DPFPTemplate reference = DPFPGlobal.getTemplateFactory().createTemplate(h2.getHuella());
                    referencias.add(reference);
                }
       
                Configs.HUELLAS_UPDATE.set(false); //listo, actulizando ram
            
      }
      
      /**
       * Permite agregar una huella en las listas huellas y referencias sin necesidad de recargar todo. Especialmente usado
       * para la clase Updater. El metodo incluye la aliminacion de huellas si estas ya existen. 
       * Asi como tambien guarda la Ubicacion en la LocalDB.
       * <b>Nota:</b> es necesario que previamente ya existan las listas 'huellas' (con posiciones) y 'referencias' .
       * @param huella
       * @return true si todo salio bien, false si sucedió algun problema. 
       */
      public static boolean agregarHuellaRam(Huella huella){
          boolean ok = false;
          log.info("agregando una huella a la RAM");
          
          //si la lista huellas no está vacia
          if(huellas != null && !huellas.isEmpty()){
              if( ubicaciones != null && !ubicaciones.isEmpty() ){ //verificar que las posiciones esten inicializadas
                  
                  //si ya existe la huella: eliminarla de la ram -> esto se queda por seguridad, por si se llama desde algun otro metodo que no elimine previamente las huellas a sustituir.
                  for(int n=0 ; n<huellas.size() ; n++ ){
                      if( huellas.get(n).getId().intValue() == huella.getId().intValue() ){
                          eliminarHuellaRam( huellas.get(n).getId() );
                      }
                  }
                  
                  //agregarle la posicion a la nueva huella
                  //huella.setPosicion( huellas.size() );
        
                  //agregar la ubicacion de la nueva huella
                  System.out.println(">> instanciando huellaUbicacion");
                  HuellaUbicacion ubi = new HuellaUbicacion();
                  System.out.println(">> agregando ID a la ubi: " + huella.getId());
                  ubi.setHuellaID(huella.getId());
                  System.out.println(">> agregando indice a ubi: (huellas.size) " + huellas.size());
                  ubi.setIndice(huellas.size());
                  
                  System.out.println(">> agregando la huella a lista huellas");
                  //agregar la huella y la ubicacion en la base de la lista, que como se leerá al revez estaría en realidad al final
                  huellas.add(0, huella); //este metodo desplaza los indices, por lo que no hay de que preocuparnos, se supone jeje.
                  
                  //guardar la nueva Ubicacion
                  System.out.println("!!!!!! Verificar si funciona esto de guardar una sola ubicacion, esn ESPECIAL si se actualiza una huella");
                  //ubicaciones.add(0, ubi);  //debido a que habria que rotar el indice de ubicaciones, mejor solo agregaro al final
                  ubicaciones.add(ubi);
                  LocalDB_Frec.saveUbicacion(ubi); // funcionara esto?
                  System.out.println(">> se supone ya se guardo la ubicacion");
                  
                  //generar referencia de huella
                  DPFPTemplate reference = DPFPGlobal.getTemplateFactory().createTemplate(huella.getHuella());
                  //agregarla a la lista de referencias, con el mismo indice que la huella
                  referencias.add(0, reference);
                  
                  //todo salio bien al parecer
                  ok = true;
              }else{
                  log.info("No se pudo agregar huella " + huella.getId() + ": La lista ubicaciones es null o vacia");
                  //agregarPosiciones(huellas);
              }
              
              
              
          }else{
              log.info("No se pudo agregar huella " + huella.getId() + ": Lista huellas es vacía o null");
          }
          
          return ok;
      }
      
      public static boolean eliminarHuellaRam(int idHuella){
          
          if (huellas != null && !huellas.isEmpty()) {
              if(ubicaciones != null && !ubicaciones.isEmpty()){
              //falta asegurarse que existan posiciones o ubicaciones
                  boolean existe = false;
                  
                  //Eliminando la huella de las listas de huellas y de referencias
                  for( int i = 0; i < huellas.size(); i++ ){
                      if( huellas.get(i).getId() == idHuella ){ //si existe en RAM la huella con ese id
                          existe = true;
                          try{
                              
                              System.out.println("size of huellas:: " + huellas.size());
                              huellas.remove(i);
                              System.out.println("DESPUES size of huellas:: " + huellas.size());

                              
                              System.out.println("size of ubicaciones:: " + ubicaciones.size());
                              removerUbicacion(i);
                              System.out.println("DESPUES size of ubicaciones:: " + ubicaciones.size());

                              
                              System.out.println("size of referencias:: " + referencias.size());
                              referencias.remove(i);
                              System.out.println("DESPUES size of referencias:: " + referencias.size());
                            
                            
                          } catch (Exception e) {
                              log.error("Error al eliminar una huella de la RAM: " + e.getMessage() );
                          }
                          break; //se supone que cada huella es unica, por lo que no ocupamos seguir buscando mas
                      }
                  }
                  
                  if(!existe){
                      log.info("La huella con id: " + idHuella + " no existe en RAM");
                  }
                  
                  
             } else { log.error("No se pudo ELIMIANR huella " + idHuella + ": Lista ubicaciones es vacía o null"); }
          } else { log.error("No se pudo ELIMIANR huella " + idHuella + ": Lista huellas es vacía o null"); }

          return true;
      }
      
      


      
      /**
       * <b>Nuevo:</b> este metodo reordena los indices de la lista ubicaciones, por ejempo para cuando se elimina un huella de RAM.
       * <b>Antiguamente</b> para reordenarPosiciones: 
     * Solo la primera vez cuando se carga la lista de huellas y no se tenian posiciones se asignan las posciones a la inversa, 
     * para poder invertir toda lista, pero enseguida se ordene la lista y las "posicones" quedan en "orden" normal de menor a mayor.
     * Es entonces cuando funciona este metodo, para reordenar posciones de "menor a mayor" ya que la lista ya tiene dicho orden.
     * Por lo tanto, el metodo "agregarPosicones()" solo debe usarse cuando las huellas son "nuevas" y no se tienen posicones.
     * @param lstHuellas 
     * @deprecated debido a que no se deben modificar el ID de una entity
     */
      private static void reordenarUbicaciones(){
          /*
          if( ubicaciones != null && !ubicaciones.isEmpty() ){
            for (int i = 0; i < ubicaciones.size(); i++) {
                ubicaciones.get(i).setIndice(i);
            }
          } else { log.error("Error al querer reordenar las ubicaciones: la lista ubicaciones es null o está vacía"); }
          */
      }
    
    /**
     * Verifica si se encuentran cargadas en RAM las listas de huellas y referencias.
     * @return true si existen, false si no.
     */
    public static boolean existenListas(){
        if( huellas == null || huellas.isEmpty() || ubicaciones == null || ubicaciones.isEmpty() ||referencias == null || referencias.isEmpty() ){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Metodo que genera una nueva lista de Ubicaciones en base a la lista de huellas, solo 
     * que de manera inversa, la cual será utilizada por ordenarHuellas()
     * @return 
     */
    public static boolean generarUbicaciones(){
        boolean correcto = false;
        log.info("Generando una nueva lista de ubicaciones");
        if(huellas != null || huellas.isEmpty()){
            ubicaciones = new ArrayList<>();
            int desc = huellas.size() -1;
            //se agregan las ubicaciones, pero de manera inversa, para eso es desc
            for(int i = 0; i < huellas.size() ; i++){
                HuellaUbicacion ubi = new HuellaUbicacion();
                ubi.setIndice(i);
                ubi.setHuellaID(huellas.get(desc).getId());
                ubicaciones.add(ubi);
                desc--;
            }
            correcto = true;
        }
        return correcto;
    }
    
    /**
     * Metodo que en realidad hace una rotacion de las HuellasID del objeto HuellaUbicacion, ya que no se deben
     * modificar el ID de una entity que si no da error; en el caso de HuellaID el ID es el indice.
     * Automaticamente ya posiciona el huellaID al final de la lista ubicaciones.
     * ¿Quien lo usa? Está enfocado para ser usado cada vez que una huella es posicionada al final de la lista
     * @param indice  de la Ubicacion extraida
     * @param huellaID correspondiente a la Ubicacion extraida 
     */
    public static void rotarUbicacionesHuellasID(int indice, int huellaID){
        System.out.println("!!!!!!!!!!!l comienza el metodo rotarUbicacionesHuellasID");
        System.out.println("!!!!!!!!!!!! valores:: indice = " + indice + ", huellaID = " + huellaID);
        boolean a = ubicaciones == null;
        System.out.println("la lista ubicaciones es null? " + a );
        boolean b = ubicaciones.isEmpty();
        System.out.println("la lista ubicaciones está vacía? " + b);
        if(ubicaciones != null && !ubicaciones.isEmpty()){
            for(int i = indice; i < (ubicaciones.size() - 1 ) ; i++){
                ubicaciones.get(i).setHuellaID( ubicaciones.get(i+1).getHuellaID() );
            }
            ubicaciones.get(ubicaciones.size()-1).setHuellaID(huellaID);
            
        } else { System.out.println("[ScannerValidator] (rotarUbicaciones()) Error!! la lista ubicaciones está vacía"); }
    }
    
    /**
     * Metodo especial si se necesita removeruna ubicacon de la lista
     * @param indice
     */
    public static void removerUbicacion(int indice){
        System.out.println("[ScannerValidator] Removiendo la ubicacion: " + indice);
        if(ubicaciones != null && !ubicaciones.isEmpty()){
            LocalDB_Frec.deleteUbicacionesAll();
            
            for(int i = indice; i < (ubicaciones.size() - 1)  ; i++){
                ubicaciones.get(i).setHuellaID( ubicaciones.get(i+1).getHuellaID() );
            }
            //ubicaciones.get(ubicaciones.size()-1).setHuellaID(huellaID);
            ubicaciones.remove(ubicaciones.size()-1); 
            System.out.println("!! revisando: ubicaciones despues de remover una, size = " + ubicaciones.size());
            LocalDB_Frec.saveUbicaciones(ubicaciones);
            
        }
    }
        /**
     * 
     */
    public static void guardarUbicaciones(){
        if(ubicaciones != null && !ubicaciones.isEmpty()){
            LocalDB_Frec.deleteUbicacionesAll();
            
            
            
            //ubicaciones.get(ubicaciones.size()-1).setHuellaID(huellaID);
            ubicaciones.remove(ubicaciones.size()-1); 
            LocalDB_Frec.saveUbicaciones(ubicaciones);
            
        }
    }
    
    
    /** 
     * Debido a que se eliminan huellas y ubicaciones, tambien se deben ACTUALIZAR las ubicaciones en la LocalDB, para ello se debe 
     * 1) elimianar toda lista en la localDB  y 
     * 2) volvr a guardarla de nuevo.
     * Todo ello lo hace este metodo.
     */
    public static void acutalizarUbicacionesLocalDB(){
        System.out.println("[Scanner Validator] Actualizando ubicaciones en LocalDB");
        if( ubicaciones != null &&  !ubicaciones.isEmpty()){
            //debido a que se eliminan huellas y ubicaciones, tambien se deben elimianr las ubicaciones
            //de la LocalDB, para ello se debe 1) elimianar toda lista en la localDB  y 2) volvr a guardarla de nuevo
            LocalDB_Frec.deleteUbicacionesAll();
            LocalDB_Frec.saveUbicaciones(ubicaciones);
        } else { System.out.println("Error!! [ScannerValidator] acutalizarUbicacionesLocalDB(): no existen las ubicaciones en ram"); } 
    }
    
    /**
     * Metodo que utiliza la lista de ubicaciones como guía para ordenar la lista de huellas
     * @return true si todo funciono bien, false si sucedio algun error
     */
    public static boolean ordenarHuellas(){
        boolean correcto = false;
                if( ubicaciones != null && !ubicaciones.isEmpty() ){
                    if(ubicaciones.size() == huellas.size()){
                        
                        List<Huella> haux = new ArrayList<>();
                        for ( int i = 0; i < ubicaciones.size() ; i++) {
                            for (int j = 0; j < huellas.size(); j++) {
                                if (ubicaciones.get(i).getHuellaID().intValue() == huellas.get(j).getId().intValue() ) {
                                    //cuidado: aqui se supone que el int i corresponder a ubicacion.indice, por lo que al add agrega las huellas de forma
                                    //adecuada (digamos que automáticamente ya los estaría acomodando en su lugar correpondiente segun el indice).
                                    haux.add(huellas.get(j)); 
                                    //System.out.println("Agregando huella a haux, ID: " + huellas.get(j).getId());
                                    break;
                                }
                            }
                        }
                        huellas = haux;
                        correcto = true;

                        
                    } else {log.error("la lista ubicaciones tiene tamaño diferente a la de huellas");}
                } else {log.error("La lista de ubicaciones sigue siendo null o empty");}
                
                return correcto;
                
    }

}
