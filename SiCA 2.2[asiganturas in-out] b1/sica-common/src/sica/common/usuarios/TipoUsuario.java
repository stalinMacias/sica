package sica.common.usuarios;

public class TipoUsuario {

    private String descripcion;
    private String tipo;
    private String orden;
    private String jornada;
        
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public String getJornada() {
        return jornada;
    }    

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getTipo() {
        return tipo!=null ? Integer.parseInt(tipo): null;
    }

    public Integer getOrden() {
        return orden!=null ? Integer.parseInt(orden): null;
    }
    
}
