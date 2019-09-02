package sica.common.asistencias;

import java.util.Date;
import sica.common.justificantes.JustificanteInterface;
import sica.common.objetos.Registro;

public class AsistenciaUsuario {

    private String usuario;
    private String nombre;
    private String entrada;
    private String salida;
    private Registro registroEntrada;
    private Registro registroSalida;
    private JustificanteInterface justif;
    private Date fecha;
    
    public String getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEntrada() {
        return entrada;        
    }

    public String getSalida() {
        return salida;
    }

    public Registro getRegistroEntrada() {
        return registroEntrada;
    }

    public Registro getRegistroSalida() {
        return registroSalida;
    }

    public JustificanteInterface getJustif() {
        return justif;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
        
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.entrada = horaEntrada;
    }

    public void setHoraSalida(String horaSalida) {
        this.salida = horaSalida;
    }

    public void setRegistroEntrada(Registro registroEntrada) {
        this.registroEntrada = registroEntrada;
    }

    public void setRegistroSalida(Registro registroSalida) {
        this.registroSalida = registroSalida;
    }

    public void setJustif(JustificanteInterface justif) {
        this.justif = justif;
    }
        
    
}
