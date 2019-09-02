package sica.common.justificantes;

public class Fraccion {
    private String fraccion;
    private String categoria;
    private String documentos;
    private String descripcion;

    public String getFraccion() {
        return fraccion;
    }

    public String getCategoria() {
        return categoria;
    }

    public Boolean getDocumentos() {
        return documentos.equals("1");
    }

    public String getDescripcion() {
        return descripcion;
    }
    
    
}
