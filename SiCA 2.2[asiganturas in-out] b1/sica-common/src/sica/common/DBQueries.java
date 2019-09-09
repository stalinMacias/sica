package sica.common;

import java.util.Calendar;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import sica.common.asistencias.AsistenciaClase;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.faltas.FaltaClase;
import sica.common.faltas.FaltasUsuario;
import sica.common.horarios.HorarioCrn;
import sica.common.horarios.HorarioUsuario;
import sica.common.justificantes.Comentario;
import sica.common.justificantes.Evento;
import sica.common.justificantes.Folio;
import sica.common.justificantes.Fraccion;
import sica.common.justificantes.JustificanteAsignatura;
import sica.common.justificantes.JustificanteFolio;
import sica.common.justificantes.JustificantePeriodo;
import sica.common.justificantes.TipoEvento;
import sica.common.justificantes.TipoJustificante;
import sica.common.objetos.Bloque;
import sica.common.objetos.Configuracion;
import sica.common.objetos.Crn;
import sica.common.objetos.Departamento;
import sica.common.objetos.Documento;
import sica.common.objetos.EntradaLog;
import sica.common.objetos.MateriaSimple;
import sica.common.objetos.Mensaje;
import sica.common.objetos.Registro;
import sica.common.usuarios.CorreoUsuario;
import sica.common.usuarios.StatusUsuario;
import sica.common.usuarios.TipoUsuario;
import sica.common.usuarios.Usuario;

public class DBQueries {    
    protected static DBGetter dbGetter;
    
    public static void setDBGetter(DBGetter dbgetter){
        DBQueries.dbGetter = dbgetter;
    }
    
    public static ObservableList<AsistenciaUsuario> getAsistenciaUsuarios (String fecha, int tipo){   
        return dbGetter.getList(
                "CALL getall_usuarios_para_asistencia("+tipo+",'"+fecha+"')", 
                AsistenciaUsuario.class);                 
    } 
        
    public static ObservableList<AsistenciaUsuario> getAsistenciaUsuarios (String fecha, int tipo, String jefe){
        return dbGetter.getList(
                "CALL getsome_usuarios_para_asistencia("+tipo+",'"+jefe+"','"+fecha+"')",  
                AsistenciaUsuario.class);        
                
    }    
    
    public static ObservableList<AsistenciaUsuario> getAsistenciaUsuario (String fecha,  String cod){
        return dbGetter.getList(
                "CALL get_horario_fecha_usuario("+cod+",'"+fecha+"')", 
                AsistenciaUsuario.class);      
        
    }
    
    public static ObservableList<Registro> getRegistrosFecha(String fecha){
        return dbGetter.getList(
                "CALL get_registros_fecha('"+fecha+"')", 
                Registro.class);      
    }
    
    public static ObservableList<Registro> getRegistrosPeriodo(String desde, String hasta){
        return dbGetter.getList(
                "CALL get_registros_periodo('"+desde+"','"+hasta+"')", 
                Registro.class);      
    }
    
    public static ObservableList<Registro> getRegistrosPeriodoTipousuarios(String desde, String hasta, int tipo){
        return dbGetter.getList(
                "CALL get_registros_periodo_tipo_usuarios('"+desde+"','"+hasta+"',"+tipo+")", 
                Registro.class);      
    }
    
    public static ObservableList<Registro> getRegistrosUsuarioFecha(String cod,String fecha){
        return dbGetter.getList(
                "CALL getsome_registros_usuario("+cod+",'"+fecha+"')", 
                Registro.class);      
    }
    
    public static ObservableList<Registro> getRegistrosUsuarioPeriodo(String usr, String desde, String hasta){        
        return dbGetter.getList(
                "CALL get_registros_usuario_periodo("+usr+",'"+desde+"','"+hasta+"')",
                Registro.class);         
    }
    
    public static ObservableList<Registro> getRegistrosUsuarioPeriodoJefe(String jefe, String desde, String hasta){        
        return dbGetter.getList(
                "CALL get_registros_jefe_periodo("+jefe+",'"+desde+"','"+hasta+"')",
                Registro.class);         
    }
    
    public static ObservableList<HorarioUsuario> getHorariosUsuarioPerdiodo(String usr, String desde, String hasta){
        return dbGetter.getList(
                "CALL get_horarios_usuario_periodo("+usr+",'"+desde+"','"+hasta+"')", 
                HorarioUsuario.class);        
    }            
    //*/
    public static Usuario getAdmin(String usr){
        ObservableList<Usuario> list = dbGetter.getList(
                "SELECT get_usuario.* FROM get_usuario INNER JOIN administradores "
                        + "ON get_usuario.usuario = administradores.codigo"
                        + " WHERE get_usuario.usuario = '"+usr+"' " ,
                Usuario.class);      
        return list.isEmpty()? null : list.get(0);
    }   
    public static Usuario getAdminView(String usr){
        ObservableList<Usuario> list = dbGetter.getList(
                "SELECT get_usuario.* FROM get_usuario INNER JOIN us_vis "
                        + "ON get_usuario.usuario = us_vis.codigo"
                        + " WHERE get_usuario.usuario = '"+usr+"' " ,
                Usuario.class);      
        return list.isEmpty()? null : list.get(0);
    }   
    public static Usuario getDirectivo(String usr){
        ObservableList<Usuario> list = dbGetter.getList(
                "SELECT get_usuario.* FROM get_usuario INNER JOIN directivos "
                        + "ON get_usuario.usuario = directivos.codigo"
                        + " WHERE get_usuario.usuario = '"+usr+"' " ,
                Usuario.class); 
        return list.isEmpty()? null : list.get(0);        
    }
    
    public static Usuario getJefeDepartamento(String usr) {
        ObservableList<Usuario> list = dbGetter.getList(
                "SELECT get_usuario.* FROM get_usuario INNER JOIN instancias "
                        + "ON get_usuario.usuario = instancias.jefe "
                        + "WHERE usuario = '"+usr+"' ",
                Usuario.class); 
        return list.isEmpty()? null : list.get(0);        
    }
    
    public static ObservableList<Usuario> getTodosUsuarios() {        
        return dbGetter.getList(
                "SELECT * FROM get_usuario ORDER BY nombre ASC " , 
                Usuario.class);        
    }

     public static ObservableList<Usuario> getAlgunosUsuarios(int tipo){         
         return dbGetter.getList(
                 "SELECT * FROM get_some_usuarios WHERE codtipo = '"+tipo+"' "
                 + " ORDER BY nombre ASC " , 
                 Usuario.class);    
       
    }
     
    public static ObservableList<Usuario> getAlgunosUsuarios(int tipo, String usuario){
        return dbGetter.getList(
                "SELECT * FROM get_some_usuarios"
                + " WHERE codtipo = '"+tipo+"' "
                + " AND departamento = (SELECT instancias.`nombre` FROM instancias WHERE jefe = "+usuario+")"
                + " ORDER BY usuario ASC " , 
                Usuario.class);         
    }
    
    public static ObservableList<Usuario> getAlgunosUsuarios(String usuario){
        return dbGetter.getList(
                "SELECT * FROM get_some_usuarios"
                + " WHERE departamento = (SELECT instancias.`nombre` FROM instancias WHERE jefe = "+usuario+")"
                + " ORDER BY usuario ASC " , 
                Usuario.class);         
    }
    
    public static Usuario getUsuario(String user) {
        ObservableList<Usuario> list = dbGetter.getList(
                "SELECT * FROM get_usuario WHERE usuario = '"+user+"' " ,
                Usuario.class); 
        return list.isEmpty()? null : list.get(0);
           
    }
    
    public static ObservableList<CorreoUsuario> getCorreosUsurio(String user){
        return dbGetter.getList(
                "SELECT * FROM correosusuarios WHERE usuario = '"+user+"' ORDER BY principal DESC",
                CorreoUsuario.class);
    }
    
    public static boolean addCorreoUsuario(String usr, String correo){
        log("Añade correo: "+correo+", al usuario "+usr);
         
        String query  = String.format(
                "CALL add_correo_usuario('%s','%s')",
                usr,
                correo);
        return new PHPExecuter(query).getResponse();
    }
    
    public static boolean updateCorreoPrincipal(String usr, String correo){
        log("Actualiza correo como principal: "+correo+", del usuario "+usr);
        String query  = String.format(
                "UPDATE correosusuarios SET principal = FALSE WHERE usuario = '%s' ",
                usr);
         boolean flag = new PHPExecuter(query).getResponse();
         
         if (flag){
            query = String.format(
                "UPDATE correosusuarios SET principal = TRUE WHERE usuario = '%s' AND correo = '%s' ",
                usr,
                correo);            
            flag = new PHPExecuter(query).getResponse();
        }
        return flag;
    }
    
    public static boolean deleteCorreoUsuario(String usr, String correo, boolean principal){
        log("Elimina correo "+correo+" del usuario "+usr);
        String query  = String.format(
                "DELETE FROM correosusuarios WHERE usuario = '%s' AND correo = '%s' ",
                usr,
                correo);
        boolean flag = new PHPExecuter(query).getResponse();
         
         if (flag && principal){
            query = String.format(
                "UPDATE correosusuarios SET principal = TRUE WHERE usuario = '%s' LIMIT 1 ",
                usr);            
            flag = new PHPExecuter(query).getResponse();
        }
        return flag;
    }
    
    public static boolean updatePass(String userCode, String text) {        
        String query  = String.format(
                "UPDATE usuarios SET pass = '%s' WHERE usuario = '%s' ",
                text,
                userCode);
        
        return new PHPExecuter(query).getResponse();
        
    }
    
    public static ObservableList<Usuario> getUsuariosNoAdministrativos() {
                
        return dbGetter.getList(                
                "SELECT * FROM get_some_usuarios WHERE codtipo != '3' "
                + " ORDER BY nombre ASC ",
                Usuario.class); 
       
    }
    
    public static ObservableList<MateriaSimple> getMaterias() {
        
        return dbGetter.getList(
                "SELECT * FROM materias ORDER BY nombre ASC " , 
                MateriaSimple.class);
        
    }
    
    public static boolean insertMateria(String cod, String nom, String dept){
        log("Añade materia: "+cod+", "+nom);

        String query  = String.format(
            "INSERT INTO materias (codigo, nombre, departamento) VALUES ('%s','%s','%s')",
            cod,
            nom,
            dept);

        return new PHPExecuter(query).getResponse();
        
    }
    
    public static boolean updateMateria(String cod, String nom,String depto){     
        
        log("Actualiza materia: "+cod+", "+nom);
        
        String query  = String.format(
            "UPDATE materias SET nombre = '%s', departamento = '%s' WHERE codigo = '%s' ",
            nom,
            depto,
            cod);

        return new PHPExecuter(query).getResponse();        
        
    }
        
    public static ObservableList<Bloque> getBloques() {
        
        return dbGetter.getList(
                "SELECT * FROM bloques ORDER BY anio DESC, ciclo DESC, bloque ASC" , 
                Bloque.class);
        
    }
    
    public static boolean insertBloque(int anio, String ciclo, int bloque, String inicio, String fin){
        log("Añade bloque: "+bloque+", "+anio+ciclo);

        String query  = String.format(
            "INSERT INTO bloques (anio, ciclo, bloque, inicio, fin) VALUES (%d,'%s',%d,'%s','%s')",
            anio,
            ciclo,
            bloque,
            inicio,
            fin);

        return new PHPExecuter(query).getResponse();
        
    }
    
    public static boolean updateBloque(int anio, String ciclo, int bloque, String inicio, String fin){
        
        log("Actualiza bloque: "+bloque+", "+anio+ciclo);
        
        String query  = String.format(
            "UPDATE bloques SET inicio = '%s', fin = '%s' WHERE bloque = %d AND anio = %d AND ciclo = '%s' ",
            inicio,
            fin,
            bloque,
            anio,
            ciclo);

        return new PHPExecuter(query).getResponse();        
        
    }
    
    public static boolean insertUsuario(String cod, String nom, int tipo, 
            int status, String depto, String telefono, String coment){
        
        log("Crea nuevo usuario: "+cod+", "+nom);
        
        
        String query  = String.format(
            "INSERT INTO usuarios (usuario, nombre, tipo, status, departamento,"
            + "telefono,comentario) VALUES ('%s','%s','%d','%d','%s','%s','%s')",
            cod,
            nom,
            tipo,
            status,
            depto,
            telefono,
            coment);

        return new PHPExecuter(query).getResponse();        
        
    }
    
    public static boolean updateUsuario(String cod, String nom, int tipo, int status, 
            String depto, String telefono, String coment){
     
        log("Actualiza los datos del usuario: "+cod);
        
        String query  = String.format(
            "UPDATE usuarios SET nombre = '%s', tipo = '%d', status = '%d', "
            + "departamento = '%s', telefono = '%s', comentario = '%s' "
            + "WHERE usuario = '%s' ",
            nom,
            tipo,
            status,
            depto,
            telefono,
            coment,
            cod);

        return new PHPExecuter(query).getResponse();        
      
    }
    
    public static boolean deleteUsuario(String codigo){
        log("ELIMINA usuario: "+codigo);
        
        String query  = String.format(
            "CALL delete_usuario('%s') ",
            codigo);

        return new PHPExecuter(query).getResponse();      
    }
    
    public static ObservableList<Departamento> getDepartamentos(){
        
        return dbGetter.getList(
                "SELECT * FROM instancias ORDER BY (codigo = 'NA') ASC, nombre DESC",
                Departamento.class);
        
    }
    public static ObservableList<TipoUsuario> getTipoUsuarios() {
        return dbGetter.getList(
                "SELECT * FROM tipousuarios ORDER BY orden ASC ",
                TipoUsuario.class);
       
    }
    public static ObservableList<StatusUsuario> getStatusUsuarios() {
        return dbGetter.getList(
                "SELECT * FROM statususuarios ORDER BY 1 ASC ",
                StatusUsuario.class);        
    }

    public static boolean updateDepartamento(String codigo, String jefe) {       
        log("Establece nuevo jefe de instancia: "+codigo+","+jefe);       
        String query  = String.format(
                "UPDATE instancias SET jefe = '%s' WHERE codigo = '%s'",
                jefe,
                codigo);
        
        return new PHPExecuter(query).getResponse();                
    }

    public static ObservableList<Crn> getCrns(int anio, String ciclo) {
        return dbGetter.getList(
                "SELECT * FROM get_crns WHERE anio = '"+anio+"' "
                + " AND ciclo = '" +ciclo +"' ",
                Crn.class);        
    }
    
    public static ObservableList<Crn> getCrn(String crn,int anio,String ciclo) {
        return dbGetter.getList(
                "SELECT * FROM get_crns WHERE anio = '"+anio+"' "
                + " AND ciclo = '" +ciclo +"' AND crn = '"+crn+"' ",
                Crn.class);
        
    }

    public static ObservableList<HorarioCrn> getHorarioCrn(String crn, Integer anio, String ciclo, String codProf) {
        
        return dbGetter.getList(
                "SELECT * FROM get_horario_crn "
                    + " WHERE crn = '"+crn+"' "
                    + " AND anio = '" +anio +"' "
                    + " AND ciclo = '" +ciclo +"' "
                    + " AND usuario = '" +codProf +"' ",
                HorarioCrn.class);

    }
    
    public static ObservableList<HorarioCrn> getHorarioCrnsUsuario(String codProf, Integer anio, String ciclo ) {
        
        return dbGetter.getList(
                "SELECT * FROM get_horario_crn "
                    + " WHERE usuario = '" +codProf +"'  "
                    + " AND anio = '" +anio +"' "
                    + " AND ciclo = '" +ciclo +"' ",
                HorarioCrn.class);

    }

    public static boolean insertHorario(String crn, int anio, String ciclo, 
            int bloque, String dia, String hora, String aula){
        
        log("Inserta horario a crn: "+crn+", "+ciclo+", "+dia+", "+hora);
        
        String query  = String.format(
                " INSERT INTO horarioscrn (crn, anio, ciclo, bloque, dia, hora, aula) "
                + " VALUES ('%s',%d,'%s',%d,'%s','%s','%s')",
                crn,
                anio,
                ciclo,
                bloque,
                dia,
                hora,
                aula);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static boolean deleteHorario(String crn, Integer anio, String ciclo, String bloque, String dia, String hora) {
        
        log("Elimina horario de crn: "+crn+", "+ciclo+", "+dia+", "+hora);
        
        String query  = String.format(
                " DELETE FROM horarioscrn WHERE crn = '%s' AND anio = %d "
                    + " AND ciclo = '%s' AND bloque = '%s' AND dia = '%s' AND hora = '%s' ",
                crn,
                anio,
                ciclo,
                bloque,
                dia,
                hora);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static boolean insertCrn(String crn, String usuario, String materia, 
            int anio, String ciclo ){
        
        log("Crea de nuevo crn: "+crn+", "+usuario+", "+materia+", "+anio+ciclo);
            
        String query  = String.format(
                " INSERT INTO crn (crn, usuario, materia, anio, ciclo) "
                + " VALUES ('%s','%s','%s',%d,'%s')",
                crn,
                usuario,
                materia,
                anio,
                ciclo);
        
        return new PHPExecuter(query).getResponse();
        
    }
    
    public static boolean updateCrn(String crn, int anio, String ciclo, String materia, String usuario) {
        
        log("Actualiza crn: "+crn+", "+usuario+", "+materia+", "+anio+ciclo);
        
        String query  = String.format(
                " UPDATE crn SET materia = '%s' , usuario = '%s' "
                + " WHERE crn = '%s' AND anio = %d AND ciclo = '%s'",
                materia,
                usuario,
                crn,
                anio,
                ciclo);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static ObservableList<Mensaje> getMensaje(String codigo) {
    
        return dbGetter.getList(
                "SELECT * FROM mensajes WHERE usuario = '"+codigo+"' " ,
                Mensaje.class);
        
    }
    
    public static ObservableList<Mensaje> getAllMensajes() {
    
        return dbGetter.getList(
                "SELECT * FROM mensajes ",
                Mensaje.class);
        
    }

    public static boolean elimMensaje(String codigo) {
        
        String query  = String.format(
                "DELETE FROM mensajes WHERE usuario = '%s' " ,
                codigo);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static boolean guardarMensaje(String codigo, String text) {
        
        log("Crea mensaje para usuario: "+codigo);
        
        String query  = String.format(
                " INSERT INTO mensajes (usuario, mensaje) VALUES ('%s','%s')",
                codigo,
                text);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static ObservableList<Registro> registros(String usr, String desde, String hasta) {
        return dbGetter.getList(
                "SELECT * FROM registrosfull WHERE usuario = '"+usr+"' "
                    + "AND DATE(fechahora) BETWEEN '"+desde+"' AND '"+hasta+"' "
                    + "ORDER BY fechahora DESC ",
                Registro.class);
       
    }
    
    
    public static ObservableList<Evento> getEventosTodos(int anio) {        
        return dbGetter.getList(
                "SELECT * FROM get_eventos "
                    + " WHERE inicio BETWEEN '"+anio+"-01-01' AND '"+anio+"-12-31' "
                    + " OR fin BETWEEN '"+anio+"-01-01' AND '"+anio+"-12-31' "                    
                     + " ORDER BY inicio DESC ",
                Evento.class);
    }

    public static ObservableList<TipoEvento> getTipoEventos() {        
        return dbGetter.getList(
                "SELECT * FROM eventos_tipos ",
                TipoEvento.class);
        
    }
    
    public static boolean insertTipoEvento(String rgb, String evento){
         String query  = String.format(
                "INSERT INTO eventos_tipos (color,nombre) VALUES ('%s','%s')",
                rgb,
                evento);
        
        return new PHPExecuter(query).getResponse();       
    }
    
    public static ObservableList<Evento> getEventoJornadaFecha(String fecha) {        
        return dbGetter.getList(
                "SELECT * FROM get_eventos WHERE asignaturas = FALSE "
                        + " AND ('"+fecha+"' BETWEEN inicio AND fin ) ",
                Evento.class);        
    }
    
    public static ObservableList<Evento> getEventoAsignaturasFecha(String fecha) {        
        return dbGetter.getList(
                "SELECT * FROM get_eventos WHERE '"+fecha+"' BETWEEN inicio AND fin ",
                Evento.class);        
    }
    
    public static ObservableList<Evento> getEventosJornadaFechas(String desde, String hasta) {        
        return dbGetter.getList(
                "SELECT * FROM get_eventos WHERE asignaturas = FALSE "
                        + "AND ((inicio BETWEEN '"+desde+"' AND '"+hasta+"') "
                        + "OR fin BETWEEN  '"+desde+"' AND '"+hasta+"') ",
                Evento.class);        
    }
    
    public static ObservableList<Evento> getEventosAsignaturasFechas(String desde, String hasta) {        
        return dbGetter.getList(
                "SELECT * FROM get_eventos WHERE (inicio BETWEEN '"+desde+"' AND '"+hasta+"') "
                        + "OR fin BETWEEN  '"+desde+"' AND '"+hasta+"'",
                Evento.class);        
    }

    public static boolean updateTipoEvento(int tipo, String color) {        
        String query  = String.format(
                " UPDATE eventos_tipos SET color = '%s' WHERE tipo = %d ",
                color,
                tipo);
        
        return new PHPExecuter(query).getResponse();
    }

    public static boolean insertEvento(String desde, String hasta, String evento, boolean asignaturas) {        
        log("Crea nuevo evento: "+evento+", "+desde+", "+hasta);
        
        String query  = String.format(
                " INSERT INTO eventos (inicio, fin, tipo, asignaturas) "
                + " VALUES ('%s','%s',%s,%d)",
                desde,
                hasta,
                evento,
                asignaturas? 1:0);        
        return new PHPExecuter(query).getResponse();
      
    }

    public static boolean deleteEvento(String evento, String desde, String hasta, boolean asignaturas) {        
        log("Elimina evento: "+evento+", "+desde+", "+hasta);
            
        String query  = String.format(
                " DELETE FROM eventos WHERE tipo = '%s' AND inicio = '%s' "
                + " AND fin = '%s' AND asignaturas = %d",
                evento,
                desde,
                hasta,
                asignaturas? 1:0);        
        return new PHPExecuter(query).getResponse();        
    }

    public static ObservableList<AsistenciaClase> getClasesDia(String txtDia, String nomDia) {
        return dbGetter.getList(
                "SELECT * FROM get_horario_para_asistencia "
                + " WHERE ('"+ txtDia + "' BETWEEN inicio AND fin) "
                + " AND dia = '" + nomDia + "' ORDER BY horario ASC ",
                AsistenciaClase.class);        
    }
    
    public static ObservableList<AsistenciaClase> getClasesDia(String txtDia, String nomDia, String jefe) {
        return dbGetter.getList(
                "CALL get_horario_asignaturas_dia_departamento('"+txtDia+"','"+nomDia+"', "+jefe+")",
                AsistenciaClase.class);        
    }
    
    public static ObservableList<AsistenciaClase> getClasesDia(String txtDia, String nomDia, String depto, String usr) {
        return dbGetter.getList(
                "SELECT * FROM get_horario_para_asistencia "
                    + " WHERE ('"+ txtDia + "' BETWEEN inicio AND fin) "
                    + " AND dia = '" + nomDia + "' AND usuario = '"+usr+"' "
                    + " ORDER BY horario ASC ",
                AsistenciaClase.class);        
    }

    public static ObservableList<Documento> getDocumentos(String codigo) {
        return dbGetter.getList(
                "SELECT * FROM documentos WHERE usuario = '" + codigo + "' "
                + " ORDER BY fecha DESC ",
                Documento.class);                
    }

    public static boolean updateRegistro(String codigo, String previa, String hora) {         
        log("Modifica registro: "+codigo+", "+previa+", "+hora);
            
        String query  = String.format(
                " UPDATE registrosfull SET fechahora = CONCAT(DATE(fechahora), ' %s'), modificado = '%s' "
                + " WHERE usuario = '%s' AND fechahora = '%s'",
                hora,
                Autenticator.getCurrentUser().getCodigo(),
                codigo,
                previa);
        
        return new PHPExecuter(query).getResponse();        
    }
   
    

    /* Por que estara comentado? */
    
    /*
    public static void log (String desc){    
        String query  = String.format(
                "INSERT INTO log (usuario,fecha,descripcion) VALUES ('%s',NOW(),'%s')",
                Autenticator.getCurrentUser().getCodigo(),
                desc);

        return new PHPExecuter(query).getResponse();    
        
    }    */
    
    
    public static void log (String desc){    
        String query  = String.format(
                "INSERT INTO log (usuario,fecha,descripcion) VALUES ('%s',NOW(),'%s')",
                Autenticator.getCurrentUser().getCodigo(),
                desc);

        boolean respuesta = new PHPExecuter(query).getResponse();    
        if(respuesta){
            //System.out.println("Resultado: TRUE!");
        }else{
            System.out.println("Problema ejecutando el Query: " + query);
            System.out.println("Resultado: FALSE!!!");
        }
        
        
    }
    

    public static ObservableList<EntradaLog> getLog() {        
        return dbGetter.getList(
                "SELECT * FROM get_log ",
                EntradaLog.class);        
    }
        
    public static ObservableList<TipoJustificante> getJustificantesListaTipoUsuario(String tipo){
        return dbGetter.getList(
                "CALL get_lista_justificantes_tipousuario('"+tipo+"')",
                
                TipoJustificante.class);
    }
    
    public static ObservableList<TipoJustificante> getJustificantesListaCompleta(){
        return dbGetter.getList(
                "CALL get_lista_justificantes()",
                TipoJustificante.class);
    }
    
    public static ObservableList<Fraccion> getFraccionesJustificante(String id){
        return dbGetter.getList(
                "SELECT * FROM justificantes_fracciones WHERE justificante_id = "+id,
                Fraccion.class);
    }
    
    public static Folio insertJustificanteClase(String usr, String justif, String fracc, String fecha, String crn, String cmnt){
        log("Inserta justificante de clase: "+crn+", "+usr+", "+fecha+", "+justif);
            
        String query  = String.format(
                "CALL insert_justificante_asignatura(%s,%s,'%s','%s','%s','')",
                usr,
                justif,
                fracc,
                fecha,
                crn);
        
        ObservableList<Folio> list = dbGetter.getList(query,Folio.class);
        Folio folio = (list.size() > 0)? list.get(0): null;
        if (folio!=null && cmnt !=null && !cmnt.isEmpty())
            DBQueries.addComentarioAFolio(folio.getFolio(), cmnt, usr);
        return folio;
    }
    
    public static Folio insertJustificantePeriodo(String usr, String justif, String fracc, String fecha, String hasta, String cmnt){
        log("Inserta justificante periodo: "+usr+", "+fecha+"-"+hasta+", "+justif);
            
        String query  = String.format(
                "CALL insert_justificante_periodo(%s,%s,'%s','%s','%s','')",
                usr,
                justif,
                fracc,
                fecha,
                hasta
                );
        
        ObservableList<Folio> list = dbGetter.getList(query,Folio.class);
        Folio folio = (list.size() > 0)? list.get(0): null;
        if (folio!=null && cmnt !=null && !cmnt.isEmpty())
            DBQueries.addComentarioAFolio(folio.getFolio(), cmnt, usr);
        return folio;
    }
    
    public static ObservableList<JustificanteFolio> getJustificantesParaAprobacionJefe(String jefe){
        return dbGetter.getList(
                "CALL get_justificantes_pendientes_jefe("+jefe+")",
                JustificanteFolio.class);               
    }
    
    public static ObservableList<JustificanteFolio> getJustificantesParaAprobacion(){
        return dbGetter.getList(
                "CALL get_justificantes_pendientes",
                JustificanteFolio.class);               
    }
    
    public static ObservableList<Comentario> getComentariosFolio(String folio){
        return dbGetter.getList(
                "SELECT justificantes_comentarios.*, usuarios.nombre as nombreusuario "
                + "FROM justificantes_comentarios INNER JOIN usuarios USING (usuario) "
                + "WHERE folio = "+folio+" ORDER BY horayfecha DESC",
                Comentario.class);    
    }
    
    public static Boolean addComentarioAFolio(String folio, String comentario, String usr){
        return new PHPExecuter( String.format(
                "INSERT INTO justificantes_comentarios(folio, comentario, usuario, horayfecha) "
                        + "VALUES (%s,'%s',%s,NOW())", folio, comentario, usr)
                ).getResponse();
    }
    
    public static ObservableList<JustificanteAsignatura> getJustificantesAprobadosClases(String desde, String hasta) {        
        return dbGetter.getList(String.format(
                "CALL get_justificantes_aprobados_asignatura_periodo('%s','%s')",
                desde,hasta),
                JustificanteAsignatura.class);       
    }
    
    public static ObservableList<JustificantePeriodo> getJustificantesAprobadosPeriodo(String desde, String hasta) {        
        return dbGetter.getList(
                "CALL get_justificantes_aprobados_periodo('"+desde+"','"+hasta+"')",
                JustificantePeriodo.class);       
    }
    
    public static ObservableList<JustificantePeriodo> getJustificantesAprobadosUsuario(String codigo) {        
        return dbGetter.getList(
                "CALL get_justificantes_aprobados_usuario("+codigo+")",
                JustificantePeriodo.class);       
    }
    
    public static ObservableList<JustificantePeriodo> getJustificantesPeriodoTodosUsuario(String codigo) {        
        return dbGetter.getList(
                "CALL get_justificantes_periodo_todos_usuario("+codigo+")",
                JustificantePeriodo.class);       
    }
    
    public static ObservableList<JustificanteFolio> getJustificantesUltimosUsuario(String usr, int cant) {        
        return dbGetter.getList(
                "CALL get_justificantes_ultimos_usuario("+usr+","+cant+")",
                JustificanteFolio.class);       
    }
    
    public static boolean deleteJustificanteFolio(String folio){        
        return new PHPExecuter(
                String.format("CALL delete_justificante_folio(%s)",folio))
                .getResponse();
    }
    
    public static boolean aprobarJustificanteFolio(String folio){
        return new PHPExecuter(
                String.format("UPDATE justificantes_folios SET aprobado = TRUE, aprobadopor = %s where folio = %s",
                        Autenticator.getCurrentUser().getCodigo(),
                        folio))
                .getResponse();
    }

    public static boolean noAprobarJustificanteFolio(String folio) {
        return new PHPExecuter(
                String.format("UPDATE justificantes_folios SET aprobado = FALSE, aprobadopor = %s where folio = %s",
                        Autenticator.getCurrentUser().getCodigo(),
                        folio))
                .getResponse();
    }
    
    public static boolean aceptarJustificanteFolio(String folio){
        return new PHPExecuter(
                String.format("UPDATE justificantes_folios SET aceptado = TRUE, aceptadopor = %s where folio = %s",
                        Autenticator.getCurrentUser().getCodigo(),
                        folio))
                .getResponse();
    }    
    
    public static boolean noAceptarJustificanteFolio(String folio) {
        return new PHPExecuter(
                String.format("UPDATE justificantes_folios SET aceptado = FALSE, aceptadopor = %s where folio = %s",
                        Autenticator.getCurrentUser().getCodigo(),
                        folio))
                .getResponse();
    }
    
    
    //---------------------------------------------------------------------------------------------
    public static ObservableList<Usuario> getComentarios(String codigo) {
        return dbGetter.getList(
                "SELECT comentario FROM usuarios WHERE usuario = '"+codigo+"' ",
                Usuario.class);
       
    }
            
    public static ObservableList<HorarioCrn> getMateriasParaAsistenciaPeriodo(String inicio, String fin, String depa){
        return dbGetter.getList(
                "SELECT * FROM get_horario_crn WHERE '"+inicio+"' <= fin AND '"+fin+"' >= inicio "
                        + " AND departamento = '"+depa+"' "
                        + "ORDER BY nombre ASC",
                HorarioCrn.class);
        
    }
    public static ObservableList<HorarioCrn> getMateriasParaAsistenciaPeriodo(String inicio, String fin, String depa,String usuario){
        return dbGetter.getList(
                "SELECT * FROM get_horario_crn WHERE '"+inicio+"' <= fin AND '"+fin+"' >= inicio "
                        + " AND departamento = '"+depa+"' AND usuario = '"+usuario+"' ",
                HorarioCrn.class);
        
    }
    public static ObservableList<HorarioCrn> get_checking_regfull(String inicio, String fin,String usuario){
        return dbGetter.getList(
                "SELECT * FROM registrosfull Where registrosfull.fechahora Between '"+inicio+"'AND"+fin+"' "
                +"'AND"+usuario+"''",
                HorarioCrn.class);
        
    }
    
    public static ObservableList<HorarioCrn> getMateriasParaAsistenciaPeriodoJefe(String inicio, String fin, String depa, String jefe){
        return dbGetter.getList(
                "SELECT * FROM get_horario_crn WHERE '"+inicio+"' <= fin AND '"+fin+"' >= inicio " 
                +"AND departamento = '"+depa+"' AND usuario IN (SELECT usuario FROM usuarios WHERE usuarios.`departamento` = " 
                +"(SELECT instancias.`codigo` FROM instancias WHERE jefe = "+jefe+") OR usuario = "+jefe+")",
                HorarioCrn.class);
        
    }
    
    public static boolean deleteHorarios(String codigo) {        
        String query  = String.format(
                "DELETE from horariousuarios WHERE usuario = '%s' AND vigencia = '0000-00-00'",
                codigo);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static boolean saveHorario(String codigo, String dias, String entrada, String salida, boolean diasig) {
        
        String query  = String.format(
                " INSERT INTO horariousuarios (usuario,dias,entrada,salida,diasig,vigencia)"
                        + " VALUES ('%s','%s','%s','%s',%d,'0000-00-00')",
                codigo,
                dias,
                entrada,
                salida,
                diasig? 1: 0);
        
        return new PHPExecuter(query).getResponse();
        
    }

    public static boolean endHorarios(String codigo) {
        String query  = String.format(
                "UPDATE horariousuarios SET vigencia = DATE(NOW()) WHERE usuario = '%s'",
                codigo);
        
        return new PHPExecuter(query).getResponse();
    }

    public static boolean addRegistro(String usuario, String fechahora ){
        
        log("Inserta registro justificado: "+usuario+", "+fechahora);
        
        String query  = String.format(
                "INSERT INTO registrosfull (usuario,fechahora,tipo,modificado) VALUES"
                        + " ('%s', '%s', '%s', '%s' )",
                usuario,
                fechahora,
                "justificado",
                Autenticator.getCurrentUser().getCodigo());
        
        return new PHPExecuter(query).getResponse();
    }
    
    public static ObservableList<Configuracion> getAppConfigs() {
        return dbGetter.getList(
                "SELECT * FROM configuraciones",
                Configuracion.class);        
    }
    
    public static boolean updateConfig(String config, String val) {
        String query  = String.format(
                "UPDATE configuraciones SET valor = '%s' WHERE configuracion = '%s'",
                val, config);
        
        return new PHPExecuter(query).getResponse();
    }
    
    public static boolean sendConfigUpdateRequest() {
        return new PHPExecuter(
                "INSERT INTO actualizaciones (usuario, actualizado) VALUES (0, NOW())"
        ).getResponse();
    }
    
    public static ObservableList<HorarioCrn> getAsignaturasActuales(String codigo){
        Calendar c = Calendar.getInstance();              
        return dbGetter.getList("SELECT * FROM get_horario_crn "
                + " WHERE usuario = "+codigo 
                + " AND anio = "+c.get(Calendar.YEAR)
                + " AND ciclo = '" +((c.get(Calendar.MONTH)<6)? "A":"B" ) +"' ",
                HorarioCrn.class);
      
    }
    
    public static ObservableList<HorarioUsuario> getHorarioActualUsuario(String usr){
        return dbGetter.getList("CALL get_horario_actual_usuario("+usr+")",
                HorarioUsuario.class);       
    }
    public static ObservableList<HorarioCrn> getChecado(String usuario){
        return dbGetter.getList("SELECT * FROM registrosfull where usuario= "+usuario,HorarioCrn.class);
    }
     public static ObservableList<HorarioCrn> get_Full_Checado(String inicio, String fin,String usuario){
        return dbGetter.getList(
                "SELECT * FROM registrosfull Where registrosfull.fechahora Between '"+inicio+"'AND"+fin+"' "
                +"'AND"+usuario+"''",
                HorarioCrn.class);
        
    }
    
   /* public static String RegistrarTolerancia(String codigo, String horafecha){
       
       return dbGetter.getList("SELECT * FROM registrosfull SET fechahora = CONCAT(DATE(fechahora),'%s')"
               +"where usuario = '%s'",horafecha,codigo);
   }    
    
    /*
    
     public static ResultSet getConfiguraciones(){
        try {            
            return con.get().prepareStatement("SELECT * FROM configuraciones").executeQuery();            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
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
    
    public static ResultSet getClaseActual(String usr){
        try {            
            return con.get().prepareStatement("CALL get_clase_actual_usuario('"+usr+"')").executeQuery();
            
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

    public static ResultSet getMessage(String usr) {
        try{
            return con.get().prepareStatement(" CALL get_mensaje('"+usr+"') ").executeQuery();
        }catch (SQLException e){
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ResultSet newRegistro(String usr, String tipo) {
        try{
            return con.get().prepareStatement(" CALL make_registro_usuario('"+usr+"','"+tipo+"') ").executeQuery();
            
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
    
    /*
    public static ObservableList<HorarioCrn> getAsignaturasActuales(String codigo){    
        try{
            Calendar c = Calendar.getInstance();              
            return new RsToObject<>(
                    con.get().prepareStatement("SELECT * FROM get_horario_crn "
                    + " WHERE usuario = "+codigo 
                    + " AND anio = "+c.get(Calendar.YEAR)
                    + " AND ciclo = '" +((c.get(Calendar.MONTH)<6)? "A":"B" ) +"' ").executeQuery(),
                    HorarioCrn.class).getList();
            
        } catch (SQLException e) {
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<HorarioUsuario> getHorarioActualUsuario(String usr){
        try {            
            return new RsToObject<>(
                    con.get().prepareStatement("CALL get_horario_actual_usuario("+usr+")").executeQuery(),
                    HorarioUsuario.class).getList();
            
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<HorarioUsuario> getHorariosUsuarioPerdiodo(String usr, String desde, String hasta){
        try {                        
            return new RsToObject<>(
                con.get().prepareStatement("CALL get_horarios_usuario_periodo("+usr+",'"+desde+"','"+hasta+"')").executeQuery(), 
                HorarioUsuario.class).getList();       
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<Registro> getRegistrosUsuarioPeriodo(String usr, String desde, String hasta){        
        try {            
            return new RsToObject<>(
                con.get().prepareStatement("CALL get_registros_usuario_periodo("+usr+",'"+desde+"','"+hasta+"')").executeQuery(),
                Registro.class).getList();      
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<Evento> getEventosJornadaFechas(String desde, String hasta) {        
        try {            
            return new RsToObject<>(
                con.get().prepareStatement( "SELECT * FROM get_eventos WHERE asignaturas = FALSE "
                        + "AND ((inicio BETWEEN '"+desde+"' AND '"+hasta+"') "
                        + "OR fin BETWEEN  '"+desde+"' AND '"+hasta+"') ").executeQuery(),
                Evento.class).getList();    
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<JustificantePeriodo> getJustificantesAprobadosUsuario(String codigo) {        
        try {            
            return new RsToObject<>(
                con.get().prepareStatement("CALL get_justificantes_aprobados_usuario("+codigo+")").executeQuery(),
                JustificantePeriodo.class).getList();       
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<HorarioCrn> getMateriasParaAsistenciaPeriodo(String inicio, String fin, String depa,String usuario){
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "SELECT * FROM get_horario_crn WHERE '"+inicio+"' < fin AND '"+fin+"' > inicio "
                        + " AND departamento = '"+depa+"' AND usuario = '"+usuario+"' ").executeQuery(),
                HorarioCrn.class).getList();
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
        
    }
    public static ObservableList<Evento> getEventosAsignaturasFechas(String desde, String hasta) {        
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "SELECT * FROM get_eventos WHERE (inicio BETWEEN '"+desde+"' AND '"+hasta+"') "
                        + "OR fin BETWEEN  '"+desde+"' AND '"+hasta+"'").executeQuery(),
                Evento.class).getList();        
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    public static ObservableList<JustificanteAsignatura> getJustificantesAprobadosClases(String desde, String hasta) {        
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "CALL get_justificantes_aprobados_asignatura_periodo('"+desde+"','"+hasta+"')").executeQuery(),
                JustificanteAsignatura.class).getList();     
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<JustificantePeriodo> getJustificantesAprobadosPeriodo(String desde, String hasta) {        
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "CALL get_justificantes_aprobados_periodo('"+desde+"','"+hasta+"')").executeQuery(),
                JustificantePeriodo.class).getList();       
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<TipoJustificante> getJustificantesListaTipoUsuario(String tipo){
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "CALL get_lista_justificantes_tipousuario('"+tipo+"')").executeQuery(),
                TipoJustificante.class).getList();
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<TipoJustificante> getJustificantesListaCompleta(){
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "CALL get_lista_justificantes()").executeQuery(),
                TipoJustificante.class).getList();
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    
    public static ObservableList<Fraccion> getFraccionesJustificante(String id){
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "SELECT * FROM justificantes_fracciones WHERE justificante_id = "+id).executeQuery(),
                Fraccion.class).getList();
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
    /*
    public static Folio insertJustificanteClase(String usr, String justif, String fracc, String fecha, String crn, String cmnt){
        String query  = String.format(
                "CALL insert_justificante_asignatura(%s,%s,'%s','%s','%s','%s')",
                usr,
                justif,
                fracc,
                fecha,
                crn,
                cmnt);
        
        ObservableList<Folio> list = new DBGetter<>(query,Folio.class).getList();
        return (list.size() > 0)? list.get(0): null;
    }
    public static Folio insertJustificantePeriodo(String usr, String justif, String fracc, String fecha, String hasta, String cmnt){
            
        String query  = String.format(
                "CALL insert_justificante_periodo(%s,%s,'%s','%s','%s','%s')",
                usr,
                justif,
                fracc,
                fecha,
                hasta,
                cmnt);
        
        ObservableList<Folio> list = new DBGetter<>(query,Folio.class).getList();
        return (list.size() > 0)? list.get(0): null;
    }
        
    public static ObservableList<Comentario> getComentariosFolio(String folio){
        try {            
            return new RsToObject<>( con.get().prepareStatement(
                "SELECT justificantes_comentarios.*, usuarios.nombre as nombreusuario "
                + "FROM justificantes_comentarios INNER JOIN usuarios USING (usuario) "
                + "WHERE folio = "+folio+" ORDER BY horayfecha DESC").executeQuery(),
                Comentario.class).getList();    
        } catch (SQLException e){           
            log.error("{} - {}",e.getLocalizedMessage(),e.getMessage());
            return null;            
        }
    }
}
*/

    /*public static String getTolerancia(ObservableList<FaltaClase> item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void getChecado(TableColumn<FaltasUsuario, String> codigoProf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void getChecado(TableColumn<FaltasUsuario, String> codigoProf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void getChecado(TableColumn<FaltasUsuario, String> codigoProf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
}