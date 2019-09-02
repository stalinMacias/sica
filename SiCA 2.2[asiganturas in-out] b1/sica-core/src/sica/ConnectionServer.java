package sica;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionServer {
    
    private static final Logger log = LoggerFactory.getLogger(ConnectionServer.class);
    private static volatile SimpleBooleanProperty conectado;
    private static ObjectProperty<Connection> con;    
    private static Service<Boolean> conAttempts;
    
    /** Constructor por defecto, privado para evitar varias instancias */
    public static void initialize(){
        con = new SimpleObjectProperty<>();
        try {      
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            log.error("Error obteniendo driver de mySql!");
            ex.printStackTrace(System.out);
        }
        conectado = new SimpleBooleanProperty();
                
        conAttempts = new Service<Boolean>() {
            @Override protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override protected Boolean call() throws Exception {    
                        set(Boolean.FALSE);
                        while (true){                                                        
                            if (!startDBConnection()){
                                try{ 
                                    Thread.sleep(3000); 
                                } catch (InterruptedException e){ 
                                    if (!isCancelled())
                                        log.info(e.getMessage()); 
                                } 
                            } else break;
                        }
                        return Boolean.TRUE;
                    }
                };
            }           
        };
        
        conectado.bind(conAttempts.valueProperty());
        
        startConnection();
    }    
    
    public static ReadOnlyObjectProperty<Connection> connectionProperty(){
        return con;
    }
    
    private static boolean startDBConnection() {     
        try { 
            if (con.get()!=null){
                con.get().close();
            }
            log.info("Iniciando conexion a la base de datos");
            String h = "jdbc:mysql:"
                    + "//" + Configs.HOST.get()
                    + ":" + Configs.PORT.get()
                    + "/" + Configs.BASEDEDATOS.get()
                    + "?autoReconnect=true";
            log.debug(h+Configs.USER.get());
            con.set(DriverManager.getConnection(h,Configs.USER.get(),Configs.PASSWORD.get()));

            con.get().createStatement();     
            log.info("Conexion exitosa");
            return true;

        } catch (SQLException e){              
            log.error("Error conectando a base de datos");   
            con.set(null);
            return false;
        }         
    }
        
    public static boolean isConnected(){
        try {
            if (con.get() != null && !con.get().isClosed()){
                con.get().prepareStatement("SELECT TRUE").execute();
                return true;
            }                         
        } catch (SQLException e){
            log.error(e.getMessage());
        } 
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(() -> startConnection());            
        } else {
            startConnection();
        }
        return false;
    }
    
    public static SimpleBooleanProperty conectedProperty(){
        return conectado;       
    }
    
    public static void startConnection(boolean force) { 
        if (force && conAttempts.isRunning())
            conAttempts.cancel();
        
        if (!conAttempts.isRunning()){
            conAttempts.reset();
            conAttempts.start();
        }        
    }
        
    public static void startConnection() {        
        startConnection(false);
    }
    
    public static void closeConectionDaemon(){
        if (log.isDebugEnabled())
            log.debug("Deteniendo hilo de conexion");
        
        if (conAttempts.isRunning()) conAttempts.cancel();
    }
    
    public static int eliminarHuella(String usr) {
        try{            
            return con.get().prepareStatement("DELETE FROM usuarios_huellas WHERE usuario = "+usr+" ").executeUpdate();
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return 0;
        }        
    }
    
    public static void guardarHuella(String codigo, ByteArrayInputStream huella, int tam) {   
        try {                  
            PreparedStatement actualizarStmt = con.get().prepareStatement(""
                    + "INSERT INTO usuarios_huellas(usuario, huella) values(?,?)");

            actualizarStmt.setString(1,codigo);               
            actualizarStmt.setBinaryStream(2, huella, tam);  
            actualizarStmt.execute();
            
        } catch (SQLException e) {   
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
        } 
    }
      
    public static ResultSet getUsuario(String usr){
        try {            
            return con.get().prepareStatement(
                "SELECT get_usuario.*, IF(administradores.codigo IS NULL, '0','1') AS isadmin "
                    + "FROM get_usuario LEFT JOIN administradores "
                    + "ON get_usuario.usuario = administradores.codigo WHERE usuario = "+usr).executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }

    public static ResultSet getTipoJornadaUsuario(String usr){
        try {            
            return con.get().prepareStatement("SELECT jornada "
                    + "FROM usuarios INNER JOIN tipousuarios "
                    + "ON usuarios.tipo = tipousuarios.tipo "
                    + "WHERE usuarios.usuario = '"+usr+"' ").executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
    public static ResultSet getHorarioUsuario(String usr){
        try {            
            return con.get().prepareStatement("CALL get_horario_hoy_usuario('"+usr+"')").executeQuery();            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    //agregado 2016-07-22 por Diego
    //ELIMINADO 2016-08-17 por Diego, debido a cambio de requerimientos. Esto ya no se requiere y el procedimiento get_bloque_actual no existe ya en la DB, aunque si existe una "funcion" similar.
    /*
     *  Obtiene el bloque en el que nos encontramos en este momento. De no estar dentro del periodo de un bloque retornaría vacio. 
     * @return un ResultSet con el bloque en el que nos encontramos ahora. De no estar dentro de un bloque retorna null o vacio.
     */
    /*
    public static ResultSet getBloqueActual( ){
        try {            
            return con.get().prepareStatement("CALL get_bloque_actual").executeQuery();            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            System.out.println("Error al llamar el procedimiento get_bloque_actual");
            return null;           
        }
    }
    */
    

    public static ResultSet getClaseActual(String usr){
        try {            
            return con.get().prepareStatement("CALL get_clase_actual_usuario('"+usr+"')").executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
    /**
     *  Manda llamar el proceidmiento get_clase_anterior_usuario();, <br>
     *  dicho prodecimiento obtiene la clase anterior de un profesor. Por ejemplo, 
     *  si son las 10:05 am, y tuvo una clase a las 8:00 (de duración 2hrs), retornará dicha clase.
     * @param usr es el código de usuario
     * @return un ResultSet con la clase anterior.
     */
    public static ResultSet getClaseAnterior(String usr){
        try {            
            return con.get().prepareStatement("CALL get_clase_anterior_usuario('"+usr+"')").executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
     /**
     *  Manda llamar el proceidmiento get_clase_encurso_usuario();, <br>
     *  dicho prodecimiento obtiene la clase que se encuentra en curso, si es que la hay. Esto sería: "hora actual" &gt; [clase + 20min] Y "hora actual" &lt; [clase + duracion - 20min].
     * <i> Por ejemplo: 
     *  si son las 10:30 am, y se tiene una clase a las 10:00 (de duración 2hrs), retornará dicha clase. </i>
     * @param usr es el código de usuario
     * @return un ResultSet con la clase actual si es que la hay, si no: retorna el resultset sin resultados o eseo creo jeje.
     */
    public static ResultSet getClaseEnCurso(String usr){
        try {            
            return con.get().prepareStatement("CALL get_clase_encurso_usuario('"+usr+"')").executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
    public static ResultSet getClasesFueraTolerancia(String usr){
        try {
            
            return con.get().prepareStatement("CALL get_clases_fuera_tolerancia_usuario('"+usr+"')").executeQuery();

        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
    /**
     * Metodo especifico para el caso de registro de entrada y salida de clase [EYS]. 
     * Busca si hay alguna clase que se encuentre de entre [20 a 60] minutos antes de la clase o entre [20 a 60] minutos después de la <b>salida</b> de clase.
     * @param usr códgio de usuario
     * @return las clases que se encuentre de entre [20 a 60] minutos antes de la clase o entre [20 a 60], de no encontrarse retorna un null.
     */
        public static ResultSet getClasesEYSFueraTolerancia(String usr){
        try {
            
            return con.get().prepareStatement("CALL get_clases_eys_fueratolerancia_usuario('"+usr+"')").executeQuery();

        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    
    
    /**
     * Según entiendo, este metodo básado en el procedimiento de la DB 'get_clases_pendietes_usuarios'  
     * obtiene una tabla de todas las asignaturas pendientes que se tienen en el día. <b>Por ejemplo:</b> 
     * Si son las 10:15 am y tengo una clase a las 12:00 y otra a las 16:00, me listrara la de las 12_oo y la de las 16:00.
     * Debido a que se basa a <b> hora_clase &gt; tiempo_actual + 99min </b> (1:39 Hrs), en el mismo caso, pero que fueran las 10:22 ya solo se mostraria la clase de las 16:00.. 
     * PERO este metodo solo retorna TRUE o FALSE si encuentra o no materias pendientes.
     * 
     * @param usr
     * @return  true si hay materias pendietes o false si no.
     */
    public static Boolean getClasesPendientes(String usr){
        try {
            
            return con.get().prepareStatement("CALL get_clases_pendientes_usuario('"+usr+"')")
                    .executeQuery().next();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return false;            
        } 
    }
    
    public static ResultSet getRegistroEntrada(String usr, int day){        
        try{            
            
            return con.get().prepareStatement(
                    "SELECT * FROM registrosfull WHERE DATE(fechahora) = DATE(SUBDATE(NOW(),"+day+")) "
                    + "AND usuario = '"+usr+"' ORDER BY fechahora ASC").executeQuery();
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;
        }
        
    }
    
    public static ResultSet getRegistroClase(String usuario, String horario){        
        try{            
            return con.get().prepareStatement(
                    "SELECT * FROM registrosfull WHERE DATE(fechahora) = DATE(NOW()) "
                    + "AND TIME(fechahora) > TIME(CONCAT(DATE(NOW()),' ','"+horario+"') - INTERVAL 20 MINUTE)"
                    + "AND TIME(fechahora) < TIME(CONCAT(DATE(NOW()),' ','"+horario+"') + INTERVAL 20 MINUTE)"
                    + "AND usuario = '"+usuario+"' "
            ).executeQuery();
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;
        }
        
    }  
    
    /**
     * Metodo que llama el procedimiento "get_registros_clase_salida" de la DB, el cual funciona para buscar los
     * registros de asistenica de salida de materia que se hayan hecho en el "periodo" correpsondiente: algo como materiaHora + duracion +- 20min.
     * @param hora la hora de la clase
     * @param duracion el tiempo de duracion de la clase
     * @param usuario codigo de usuario
     * @return Los registros que encuentre como salida de clase. De no encontrar nada retornara vacio.
     */
    public static ResultSet getRegistrosClaseSalida(String hora, String duracion, String usuario){        
        try{            
            System.out.println("CALL get_registros_clase_salida(  '"+ hora  +"', '"+  duracion  +"', '"+  usuario +"'  )");
            return con.get().prepareStatement(
                    "CALL get_registros_clase_salida(  '"+ hora  +"', '"+  duracion  +"', '"+  usuario +"'  )"
            ).executeQuery();
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;
        }
        
    }  

    public static ResultSet getMessage(String usr) {
        try{
            return con.get().prepareStatement("CALL get_mensaje('"+usr+"') ").executeQuery();
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ResultSet newRegistro(String usr, String tipo) {
        try{
            return con.get().prepareStatement("CALL make_registro_usuario('"+usr+"','"+tipo+"') ").executeQuery();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    /**
     * Agrega un registro en el servidor, incluyendo el nombre del equipo donde se ejecuta el sica.
     * @param usr usuario
     * @param tipo tipo de registro [huella, codigo,..]
     * @param equipo donde se ejecuta SiCA. NOTA: maximo 7 caractares.
     * @return 
     */
    public static ResultSet newRegistro(String usr, String tipo, String equipo) {
        try{
            System.out.println("Equipo = " + equipo);
            System.out.println("Se ejecutara " + "CALL make_registro_usuario_equipo('"+usr+"','"+tipo+"','"+equipo+"') ");
            return con.get().prepareStatement("CALL make_registro_usuario_equipo('"+usr+"','"+tipo+"','"+equipo+"') ").executeQuery();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    
    public static ResultSet getHuella(Integer id){
        try {            
            return con.get().prepareStatement("SELECT * FROM usuarios_huellas WHERE id = "+id).executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }  
    }
    public static ResultSet getHuellas(){        
        try {            
            return con.get().prepareStatement("SELECT * FROM usuarios_huellas").executeQuery();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }        
    }
    
    public static ResultSet getAllUsuarios(){        
        try {            
            return con.get().prepareStatement(
                "SELECT get_usuario.*, IF(administradores.codigo IS NULL, '0','1') AS isadmin "
                        + "FROM get_usuario LEFT JOIN administradores "
                        + "ON get_usuario.usuario = administradores.codigo").executeQuery();

        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }          
    
    public static String getDBTime(){
        try{            
            ResultSet rs = con.get().prepareStatement("SELECT TIME(NOW())").executeQuery();            
            rs.next();
            return rs.getString(1);
            
        } catch (SQLException | NullPointerException e){
            log.error("{} - {}",e.getMessage(),e.getLocalizedMessage());
            return null;
        }
    }

    public static String getDBDate(){
        try{
            
            ResultSet rs = con.get().prepareStatement("SELECT DATE(NOW())").executeQuery();
            rs.next();
            return rs.getString(1);
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;
        }
    }    
    
    public static int insertUsuario(String cod, String nom, int tipo, int status, String depto, String telefono){
        try{
            PreparedStatement p = con.get().prepareStatement(
                    "INSERT INTO usuarios (usuario, nombre, tipo, status, departamento, telefono) "
                    + " VALUES (?,?,?,?,?,?) ");
            
            p.setString(1, cod);
            p.setString(2, nom);
            p.setInt(3, tipo);
            p.setInt(4, status);
            p.setString(5, depto);
            p.setString(6, telefono);
            
            
            return p.executeUpdate();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return 0;
        }
    }
     public static int addCorreoUsuario(String usr, String correo){        
         try{
            PreparedStatement p = con.get().prepareStatement(
                    "CALL add_correo_usuario(?,?)");
            
            p.setString(1, usr);
            p.setString(2, correo);           
            
            return p.executeUpdate();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return 0;
        }
    }
    
    public static int updateUsuario(String cod, String nom, int tipo, int status, String depto, String telefono){
     
        try{
            PreparedStatement p = con.get().prepareStatement(
                    " UPDATE usuarios "
                    + " SET nombre = ?, tipo = ?, status = ?, departamento = ?, telefono = ? "
                    + " WHERE usuario = '"+cod+"' ");
            p.setString(1, nom);
            p.setInt(2,tipo);
            p.setInt(3, status);
            p.setString(4, depto);
            p.setString(5, telefono);
            
            return p.executeUpdate();
            
        }catch (SQLException e){
            
            return 0;
        }
        
    }
     
    public static int cantidadHuellas(String codigo){
        int cant = 0;
        try {
            
            ResultSet rs = con.get().prepareStatement("SELECT COUNT(1) AS cant "
                    + " FROM usuarios_huellas WHERE usuario = '"+codigo+"' ").executeQuery();
            
            if (rs.next()){
                cant = rs.getInt("cant");
            }
            
        } catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());   
        }
        return cant;
    }
        
    public static int insertRegistroOffline(String usuario, String fechahora, String tipo){
        try{
            
            PreparedStatement p = con.get().prepareStatement("INSERT INTO registrosfull "
                    + " (usuario, fechahora, tipo) values (?,?,?) ");
            
            p.setString(1, usuario);
            p.setString(2, fechahora);
            p.setString(3, tipo);
            
            return p.executeUpdate();
            
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return 0;
        }
    }
    
        public static int insertRegistroOffline(String usuario, String fechahora, String tipo, String equipo){
        try{
            if(equipo == null){
                equipo = "Error";
            }
            
            PreparedStatement p = con.get().prepareStatement("INSERT INTO registrosfull "
                    + " (usuario, fechahora, tipo, equipo) values (?,?,?,?) ");
            
            p.setString(1, usuario);
            p.setString(2, fechahora);
            p.setString(3, tipo);
            p.setString(4, equipo);
            
            return p.executeUpdate();
            
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return 0;
        }
    }
   
    public static ResultSet getLastUpdate(){
        try{
            return con.get().prepareStatement("SELECT * FROM actualizaciones ORDER BY actualizado DESC LIMIT 1 ").executeQuery();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }

    public static ResultSet getUpdatedUsers(String date){
        try{
            return con.get().prepareStatement("SELECT * FROM actualizaciones WHERE actualizado > '"+date+"'").executeQuery();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    
    /**
     * Metodo que obtiene el lapaso actual en el que nos encontramos. Dichos lapsos son usados para
     * indicar si en este momento las asignaturas son para registrar asistencia solo la entrada o entrada y salida.
     * En el ResultSet se incluyen <b>2 atributos:</b> "fecha_inicial" de tipo date, y "checar" que puede puede incluir: "entrada", "entysal" u "otro".
     * @return un ResultSet con el lapso actual para saber si se debe checar asignatura solo entrad o entrda y salida.
     */
    public static ResultSet getLapsoActual(){
        try{
            return con.get().prepareStatement("CALL get_lapso_actual").executeQuery();
            
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    
    public static ResultSet executeQuery(String query){
        try {            
            return con.get().prepareStatement(query).executeQuery();            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        } 
    }
    public static int executeUpdate(String query){
        try {            
            return con.get().prepareStatement(query).executeUpdate();
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return 0;            
        } 
    }
    
   
    
}
