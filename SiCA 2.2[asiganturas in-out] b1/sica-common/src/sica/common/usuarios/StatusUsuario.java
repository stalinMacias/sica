package sica.common.usuarios;

public class StatusUsuario {

    private String status;
    private String descripcion;

    public Integer getStatus() {
        return status!=null? Integer.parseInt(status) : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
}
