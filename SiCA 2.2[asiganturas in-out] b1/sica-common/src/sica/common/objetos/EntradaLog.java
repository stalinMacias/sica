package sica.common.objetos;

public class EntradaLog {

    private String usuario;
    private String nombre;
    private String fecha;
    private String descripcion;
    
    public EntradaLog(){
        usuario = "";
        nombre = "";
        fecha = "";
        descripcion = "";
    }
    
    public void setUsuario(String s){
        usuario = s;
    }
    public void setNombre(String s){
        nombre = s;
    }
    public void setFecha(String s){
        fecha = s;
    }
    public void setDescripcion(String s){
        descripcion = s;
    }
    
    public String getUsuario(){
        return usuario;
    }
    public String getNombre(){
        return nombre;
    }
    public String getFecha(){
        return fecha;
    }
    public String getDescripcion(){
        return descripcion;
    }
    
}
