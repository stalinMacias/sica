package sica.common.objetos;

public class Crn {

    private String crn;
    private String codmat;
    private String materia;
    private String codProf;
    private String profesor;
    private String anio;
    private String ciclo;
    
    
    public void setCrn(String s){
        crn = s;
    }
    public void setCodMat(String s){
        codmat = s;
    }
    public void setMateria(String s){
        materia = s;
    }
    public void setCodProf(String s){
        codProf = s;
    }
    public void setProfesor(String s){
        profesor = s;
    }    
    public void setAnio(int s){
        anio = s+"";
    }
    public void setCiclo(String s){
        ciclo = s;
    }
    
    public String getCrn(){
        return crn;
    }
    public String getCodMat(){
        return codmat;
    }
    public String getMateria(){
        return materia;
    }
    public String getCodProf(){
        return codProf;
    }
    public String getProfesor(){
        return profesor;
    }
    public Integer getAnio(){
        return anio!=null? Integer.parseInt(anio) : null;
    }
    public String getCiclo(){
        return ciclo;
    }
    
}
