package sica.common.objetos;

public class MateriaSimple {

    private String codigo;
    private String nombre;
    private String departamento;    

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
        
    public void setCodigo(String c){
        codigo = c;
    }
    public void setNombre(String n){
        nombre = n;
    }
    
    public String getCodigo(){
        return codigo;
    }
    public String getNombre(){
        return nombre;
    }
    
}
