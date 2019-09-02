package sica.common.horarios;

import java.util.Date;
import sica.common.Utils;

public class HorarioCrn {
    
    private String usuario;
    private String nombre;
    private String ciclo;
    private String crn;
    private String materia;
    private String bloque;
    private String dia;
    private String horario;
    private String aula;
    private String inicio;
    private String fin;    

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }
    
    public String getCiclo() {
        return ciclo;
    }

    public void setCiclo(String ciclo) {
        this.ciclo = ciclo;
    }
    
    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
    
    public void setCrn(String s){
        crn = s;
    }
    public void setBloque(String s){
        bloque = s;
    }
    public void setDia(String s){
        dia = s;
    }
    public void setHora(String s){
        horario = s;
    }
    public void setAula(String s){
        aula = s;
    }
    
    public String getCrn(){
        return crn;
    }
    public String getBloque(){
        return bloque.equals("0")? "1y2": bloque;
    }
    public String getDia(){
        return dia;
    }
    public String getHora(){
        return horario;
    }
    public String getAula(){
        return aula;
    }

    public String getInicio() {
        return inicio;
    }
    public Date getInicioDate(){
        return Utils.parseDate(inicio);
    }
    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFin() {
        return fin;
    }
    public Date getFinDate(){
        return Utils.parseDate(fin);
    }
    public void setFin(String fin) {
        this.fin = fin;
    }
    
}
