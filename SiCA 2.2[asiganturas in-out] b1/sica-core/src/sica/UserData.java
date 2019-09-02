package sica;

import sica.common.horarios.HorarioMateria;
import sica.common.horarios.HorarioUsuario;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;

public class UserData {
    
    private Usuario usuario;
    private String foto;
    private String mensaje;
    
    private String horaServidor;
    private String fechaServidor;
    
    private String tipoJornada;
    private HorarioUsuario horario;
    private HorarioMateria materiaActual;    
    private Boolean materiasPendiente;
    private HorarioMateria materiaFueraTolerancia;
    private Registro registro;
    private TipoRegistro tipoReg;
    
    //Agregados el 26 de Julio de 2016, con motivo al SiCA v2.02 -> entrada y salida
    private HorarioMateria materiaAnterior;
    private HorarioMateria materiaEnCurso; //materia que está en transcurso, es decir, hora actual  > clase+20min && hora actual < finclase-20min
    private Lapso lapso; //para saber si en el lapso es checar entrada o entrada y salida
    private TipoRegistro TipoRegistroMat; //para usarlo con el EyS
    
    public UserData() {
        materiasPendiente = false;
    }       

    public TipoRegistro getTipoReg() {
        return tipoReg;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public void setTipoReg(TipoRegistro tipoReg) {
        this.tipoReg = tipoReg;
    }
    
    public String getFechaServidor() {
        return fechaServidor;
    }

    public void setFechaServidor(String fechaServidor) {
        this.fechaServidor = fechaServidor;
    }
    
    public Registro getRegistro() {
        return registro;
    }

    public void setRegistro(Registro registro) {
        this.registro = registro;
    }   

    public String getHoraServidor() {
        return horaServidor;
    }

    public void setHoraServidor(String horaServidor) {
        this.horaServidor = horaServidor;
    }

    public HorarioMateria getMateriaActual() {
        return materiaActual;
    }

    public void setMateriaActual(HorarioMateria materiaActual) {
        this.materiaActual = materiaActual;
    }

    public Boolean getMateriasPendiente() {
        return materiasPendiente;
    }

    public void setMateriasPendiente(Boolean materiaPendiente) {
        this.materiasPendiente = materiaPendiente;
    }

    public HorarioMateria getMateriaFueraTolerancia() {
        return materiaFueraTolerancia;
    }

    public void setMateriaFueraTolerancia(HorarioMateria materiaFueraTolerancia) {
        this.materiaFueraTolerancia = materiaFueraTolerancia;
    }    
    
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
    
    public HorarioUsuario getHorario() {
        return horario;
    }

    public void setHorario(HorarioUsuario horario) {
        this.horario = horario;
    }

    public String getTipoJornada() {
        return tipoJornada;
    }

    public void setTipoJornada(String tipoJornada) {
        this.tipoJornada = tipoJornada;
    }

    public HorarioMateria getMateriaAnterior() {
        return materiaAnterior;
    }

    public void setMateriaAnterior(HorarioMateria materiaAnterior) {
        this.materiaAnterior = materiaAnterior;
    }

    public HorarioMateria getMateriaEnCurso() {
        return materiaEnCurso;
    }

    public void setMateriaEnCurso(HorarioMateria materiaEnCurso) {
        this.materiaEnCurso = materiaEnCurso;
    }

    public Lapso getLapso() {
        return lapso;
    }

    public void setLapso(Lapso lapso) {
        this.lapso = lapso;
    }

    public TipoRegistro getTipoRegistroMat() {
        return TipoRegistroMat;
    }

    public void setTipoRegistroMat(TipoRegistro TipoRegistroMat) {
        this.TipoRegistroMat = TipoRegistroMat;
    }
    
    
    

    
    
}
