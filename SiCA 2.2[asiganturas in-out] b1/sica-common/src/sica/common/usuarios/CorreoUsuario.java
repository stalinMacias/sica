package sica.common.usuarios;

public class CorreoUsuario {    
    private String correo;
    private String principal;

    public String getCorreo() {
        return correo;
    }

    public Boolean getPrincipal() {
        return principal.equals("1");
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal? "1":"0";
    }
    
}
