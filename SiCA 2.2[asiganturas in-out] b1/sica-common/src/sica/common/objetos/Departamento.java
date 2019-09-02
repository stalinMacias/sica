package sica.common.objetos;

public class Departamento {

    private String codigo;
    private String nombre;
    private String jefe;
    
    public Departamento(){
        codigo = "";
        nombre = "";
        jefe = "";        
    }
    
    public Departamento(String c, String n, String j){
        codigo = c;
        nombre = n;
        jefe = j;
    }
    
    public void setCodigo(String s){
        codigo = s;
    }
    public void setNombre(String s){
        nombre = s;
    }
    public void setJefe(String s){
        jefe = s;
    }
    
    public String getCodigo(){
        return codigo;
    }
    public String getNombre(){
        return nombre;
    }
    public String getJefe(){
        return jefe;
    }
}
