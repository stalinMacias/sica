package sica.common.usuarios;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Usuario implements Serializable {
    
    @Id private String usuario;
    private String nombre;
    private String tipo;    
    private String departamento;
    private String status;
    private String telefono;
    private String correo;
    private String isadmin;
    @Transient private String comentario;
    @Transient private Privilegios privilegios;
        
    public void setCodigo(String c){
        usuario = c;
    }
    public void setNombre(String n){
        nombre = n;
    }
    public void setTipo(String t){
        tipo = t;
    }    
    public void setDepartamento(String d){
        departamento = d;
    }
    public void setStatus(String s){
        status = s;
    }
    public void setTelefono(String s){
        telefono = s;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    } 

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public Privilegios getPrivilegios() {
        return privilegios;
    }

    public void setPrivilegios(Privilegios privilegios) {
        this.privilegios = privilegios;
    }
        
    public String getCorreo() {
        return correo;
    }
    public String getCodigo(){
        return usuario;
    }
    public String getNombre(){
        return nombre;
    }
    public String getTipo(){
        return tipo;
    }    
    public String getDepartamento(){
        return departamento;
    }
    public String getStatus(){
        return status;
    }
    public String getTelefono(){
        return telefono;
    }
    public void setAdmin(String admin){
        this.isadmin = admin;
    }
    public Boolean isAdmin(){
        return isadmin!=null? isadmin.equals("1") : false;
    }
    
    @Override public String toString(){
        return usuario.concat(" - ").concat(nombre);
    }
}

