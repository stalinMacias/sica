package sicaw.gui.menus;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sicaw.Screens;
import sicaw.ScreenManager;

public class MenuController implements Initializable {

    private ScreenManager mainManager;
    private VBox container;
    private String current;
    public static String titulo;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {                
        current = "";
    }    
    
    public void setScreenParent(ScreenManager screenP) {
        mainManager = screenP;
    }
    
    public void setContainer (VBox cont){
        container = cont;
    }
        
    @FXML protected void exitSession(){
        mainManager.setScreen(Screens.MAIN_LOGIN);        
        current = Screens.MAIN_LOGIN;
    }             
    
    @FXML protected void setScreenJustificar(){
        setScreen("Justificar");
    }
    
    @FXML protected void setScreenMiperfil(){
        setScreen("Mi perfil");
    }
    
    @FXML protected void setScreenMensajes(){
        setScreen("Personalizado");
    }
    
    @FXML protected void setScreen(ActionEvent e){  
        if (e.getSource() instanceof MenuItem){
            MenuItem m = (MenuItem)e.getSource();
            setScreen(m.getText());        
        }
                    
    }
    
    public void setScreen(String s){
        boolean change = false;
        
        switch (s){           
            //menu ayuda 
            case "Enviar justificantes":                
                current = Screens.PAGE_AYUDA;     
                titulo = "  Ayuda enviar justificantes";
                change = true;
                break;      
            case "Aprobar  justificantes":               
                current = Screens.PAGE_AYUDA;     
                titulo = "  Ayuda aprobar justificantes";
                change = true;
                break;
            case "Acerca de":
                if (!current.equals(Screens.PAGE_CALENDARIO_AGREGAREVENTO)) {
                    current = Screens.PAGE_ACERCADE;
                    titulo = "  Acerca de SiCA Web";
                    change = true;
                }
                break;
                
            // menu asistencias
            case "Por día":
                if (! current.equals(Screens.PAGE_ASISTENCIA_DIA)){
                    current = Screens.PAGE_ASISTENCIA_DIA;     
                    titulo = "  Asistencia de usuarios por día";
                    change = true;
                } 
                break;
            case "Por periodo":
                if (! current.equals(Screens.PAGE_ASISTENCIA_PERIODO)){
                    current = Screens.PAGE_ASISTENCIA_PERIODO;     
                    titulo = "  Asistencia de usuarios por periodo";
                    change = true;
                } 
                break;
            case "Por asignatura": 
                if (! current.equals(Screens.PAGE_ASISTENCIA_ASIGNATURA)){
                    current = Screens.PAGE_ASISTENCIA_ASIGNATURA;    
                    titulo = "  Asistencia de usuarios por asignatura";
                    change = true;
                } 
                break;
            //menu faltas
            case "A asignaturas":
                if (! current.equals(Screens.PAGE_FALTAS_ASIGNATURAS)){
                    current = Screens.PAGE_FALTAS_ASIGNATURAS;    
                    titulo = "  Faltas de usuarios a asignaturas";
                    change = true;
                } 
                break;    
            case "En periodo":
                if (! current.equals(Screens.PAGE_FALTAS_PERIODO)){
                    current = Screens.PAGE_FALTAS_PERIODO;    
                    titulo = "  Faltas de usuarios en periodo";
                    change = true;
                } 
                break;
            // menu justificacion     
            case "Justificar":
                titulo = "  Justificacion de faltas";
                current = Screens.PAGE_JUSTIFICANTES_USUARIO;                      
                change = true;                    
                break;                
            case "Asignatura": 
                titulo = "  Justificacion de faltas a asignaturas";
                if (!current.equals(Screens.PAGE_JUSTIFICANTES_ASIGNATURA)){
                    current = Screens.PAGE_JUSTIFICANTES_ASIGNATURA;                      
                    change = true;
                    
                } 
                break;
            case "Tiempo completo": 
                titulo = "  Justificacion de faltas por fecha";
                if (!current.equals(Screens.PAGE_JUSTIFICANTES_TC)){
                    current = Screens.PAGE_JUSTIFICANTES_TC;                     
                    change = true;
                    
                }  
                break;
            case "Aprobar justificantes": 
                if (! current.equals(Screens.PAGE_JUSTIFICANTES_JEFE_APROBACION)){
                    current = Screens.PAGE_JUSTIFICANTES_JEFE_APROBACION;
                    titulo = "  Aprobación de justificantes recibidos";
                    change = true;
                } 
                break;
                
            // menu administrar
            case "Mi perfil":
                if (!current.equals(Screens.PAGE_BIENVENIDA)){
                    current = Screens.PAGE_BIENVENIDA;
                    titulo = "  Mi perfil";
                    change = true;
                }
            break;
            case "Usuarios": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_USUARIOS)){
                    current = Screens.PAGE_ADMINISTRAR_USUARIOS;   
                    titulo = "  Administración de usuarios";
                    change = true;
                } 
                break;
            case "Materias": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_MATERIAS)){
                    current = Screens.PAGE_ADMINISTRAR_MATERIAS;  
                    titulo = "  Administración de materias";
                    change = true;
                } 
                break;
            case "Horarios": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_HORARIOS)){
                    current = Screens.PAGE_ADMINISTRAR_HORARIOS;                        
                    titulo = "  Administración de horarios";
                    change = true;
                } 
                break;
             case "Bloques": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_BLOQUES)){
                    current = Screens.PAGE_ADMINISTRAR_BLOQUES;                        
                    titulo = "  Administración de bloques de 4x4";
                    change = true;
                } 
                break;
            case "Instancias": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_DEPARTAMENTOS)){
                    current = Screens.PAGE_ADMINISTRAR_DEPARTAMENTOS; 
                    titulo = "  Administración de instancias";
                    change = true;
                } 
                break;
            case "Log": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_LOG)){
                    current = Screens.PAGE_ADMINISTRAR_LOG; 
                    titulo = "  Registro de eventos (Log)";
                    change = true;
                } 
                break;
            case "Aplicación": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_APLICACION)){
                    current = Screens.PAGE_ADMINISTRAR_APLICACION; 
                    titulo = "  Configuración de la aplicación";
                    change = true;
                } 
                break;
            case "Envío de correos": 
                if (! current.equals(Screens.PAGE_ADMINISTRAR_ENVIOCORREOS)){
                    current = Screens.PAGE_ADMINISTRAR_ENVIOCORREOS; 
                    titulo = "  Configuración del envío de correos automáticos";
                    change = true;
                } 
                break;
                
            // menu registros
            case "Registros": 
                if (! current.equals(Screens.PAGE_REGISTROS_REGISTROS)){
                    current = Screens.PAGE_REGISTROS_REGISTROS;
                    titulo = "  Registros de usuarios";
                    change = true;
                } 
                break;
            // menu mensajes
            case "Personalizado": 
                if (! current.equals(Screens.PAGE_MENSAJES_PERSONALIZADO)){
                    current = Screens.PAGE_MENSAJES_PERSONALIZADO;
                    titulo = "  Envío de mensajes personalizado";
                    change = true;
                } 
                break;
            case "General": 
                if (! current.equals(Screens.PAGE_MENSAJES_GENERAL)){
                    current = Screens.PAGE_MENSAJES_GENERAL;   
                    titulo = "  Envío de mensajes general";
                    change = true;
                } 
                break;
                
            // menu calendario    
            case "Calendario": 
                if (! current.equals(Screens.PAGE_CALENDARIO)){
                    current = Screens.PAGE_CALENDARIO; 
                    titulo = "  Calendario";
                    change = true;
                } 
                break;
            case "Agregar evento": 
                if (! current.equals(Screens.PAGE_CALENDARIO_AGREGAREVENTO)){
                    current = Screens.PAGE_CALENDARIO_AGREGAREVENTO;
                    titulo = "  Agreagar eventos";
                    change = true;
                } 
                break;
                
            default: System.out.println("Menu pendiente: '"+s+"' ");
                
        }
        
        if (change) {
            changeScreen();
        }
    }
    
    
    private void changeScreen(){
        
        try {               
            FXMLLoader myLoader = new FXMLLoader(Screens.class.getResource(current));
            final Parent contenido = (Parent) myLoader.load();
            final Label tit = new Label(titulo);
            tit.getStyleClass().add("h1");
            VBox.setVgrow(contenido, Priority.ALWAYS);
            
            if (container.getChildren().size() > 1) { 
                
                container.getChildren().remove(1);  
                container.getChildren().remove(1);  
                container.getChildren().addAll(tit,contenido);
                
            } else {    
                container.getChildren().addAll(tit,contenido);                                       
            }                        
            
        } catch (IOException | IllegalStateException ex){
            System.out.println("Error loading: "+current);
            ex.printStackTrace(System.out);
        }
    }
}
