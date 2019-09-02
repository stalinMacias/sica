package sica;

public enum Screens {
    HUELLATECLADO("screen/HuellaTeclado1.fxml"), 
    INFOREGISTRO("screen/InfoRegistro.fxml"), 
    MENU("screen/Menu.fxml"), 
    PRINCIPAL("screen/Principal.fxml"), 
    
    MENU_ADMIN_USR("screen/menuviews/AdminUsuario.fxml"),
    MENU_BLOQUEO("screen/menuviews/Bloqueo.fxml"),
    MENU_BLOQUEO2("screen/menuviews/Bloqueo2.fxml"),
    MENU_CONFIG("screen/menuviews/Config.fxml"),
    MENU_HORARIO("screen/menuviews/Horario.fxml"),
    MENU_OPC_ADMIN("screen/menuviews/Opciones_Admin.fxml"),
    MENU_OPC_USER("screen/menuviews/Opciones_Usuario.fxml"),
    MENU_ASISTENCIA_JORNADA("screen/menuviews/AsistenciaJornada.fxml"),
    MENU_FALTAS_ASIGNATURAS("screen/menuviews/FaltasAsignaturas.fxml"),
    MENU_JUSTIFICAR("screen/menuviews/Justificar.fxml"),
    MENU_JUSTIFICANTES("screen/menuviews/Justificantes.fxml"),
    MENU_FOLIO("screen/menuviews/Folio.fxml"),
    ;
    
    private final String resource;
    
    private Screens(String r){
        resource = r;
    }    

    public String getResourceName() {
        return resource;
    }
    
    
}
