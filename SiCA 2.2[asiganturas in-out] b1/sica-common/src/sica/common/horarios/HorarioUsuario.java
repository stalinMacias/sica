package sica.common.horarios;

import java.util.Calendar;
import java.util.Date;
import sica.common.Utils;
import sica.common.objetos.Registro;

public class HorarioUsuario {
    private String dias;
    private String entrada;
    private String salida;
    private String diasig;
    private String vigencia;
    private Registro registroEntrada;
        
    public HorarioUsuario() {
        dias = "";
        vigencia = "0000-00-00";
    }
    
    public HorarioUsuario(String dias, String entrada, String salida) {
        this.dias = dias;
        this.entrada = entrada;
        this.salida = salida;
    }   
    
    public Registro getRegistroEntrada() {
        return registroEntrada;
    }

    public void setRegistroEntrada(Registro registroEntrada) {
        this.registroEntrada = registroEntrada;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public void addDia(String dias) {
        this.dias += dias;
    }
    
    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public void setDiasig(String diasig) {
        this.diasig = diasig;
    }
    
    public String getDias() {
        return dias;
    }    
    
    public Boolean debioAsistir(Integer dia){
        //Modificaciones por hugo el 10 de marzo de 2016
        // pero vuelto a su original por Diego 20 de Julio 2016, por problemas a la hora de generar reportes en SiCA Web
        // en especifico Reportes -> Faltas -> Por Periodo
       
        //intento de código para implementar la opción del "Día Siguiente"
        
        /*
        return dias.matches("[0-9]*"+dia+"{1}[0-9]*")? true :  
                getDiasig()? false : 
                dias.matches("[0-9]*"+(dia-1)+"{1}[0-9]*"); 
        */
        
        /*
        entonces lo del dia siguiente funciona asi?:
        
        si se tiene un horario de dia siguiente donde checa entrada el lunes y salida el martes, al
        chercar el martes la salida el software deberia analizar que el dia hoy - 1 corresponde al dia anterior cuando se checo
        Sinceramente ya no le entendí jaja,
        
        */
        
        //este es el código original
        return dias.matches("[0-9]*"+dia.toString()+"{1}[0-9]*");
        
        
    }
    
    public String getEntrada() {
        return entrada;
    }

    public String getSalida() {
        return salida;
    }

    public Boolean getDiasig() {
        if( diasig != null && !diasig.isEmpty() ){
            return diasig.equals("1");
        } else {
            return false; //por default en un error retornaria que no es dia siguiente
        }
        
    }
        
    public void setVigencia(String vigencia){
        this.vigencia = vigencia;
    }
    
    public Date getVigencia() {        
        return (vigencia == null || vigencia.equals("0000-00-00"))?
             Calendar.getInstance().getTime() : Utils.parseDate(vigencia);
    }
    
    public String getVigenciaString(){
        return this.vigencia;
    }
    
}
