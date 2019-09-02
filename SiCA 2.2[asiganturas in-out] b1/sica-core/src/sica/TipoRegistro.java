package sica;

public enum TipoRegistro {
    ENTRADA, //entrada normal
    ENTRADAYMATERIA, //entrada jornada y materia simultaneas
    ENTRADATARDE, //*Administrativos, despues de 40 min
    ENTRADATARDEYMATERIA, //Entrada tarde y materia simultaneas
    ENTRADATARDEYFUERATOLERANCIA, //Entrada tarde con materias fuera del periodo de tolerancia
    
    SALIDA,  //salida normal
    SALIDAANTES, //*Administrativos, salida antes
    
    MATERIA,  //entrada materia
    MATERIAFUERATOLERANCIA, //1hr antes o 1:39 en horario de materia
    MATERIAREGISTRADA, //registro ya realizado 
    
    SINACTIVIDAD, 
    OFFLINE,
    
    EYS_otros_nuevos,
    //EYS_MATERIA_E,
   // EYS_MATERIA_E_FUERATOLERANCIA,
    //EYS_MATERIA_E_REGISTRADA,
    
    /**  mostrar anuncio: ya habia registro salida de la clase... y  entrada de la clase... */
    EYS_MATERIA_REGISTRADA, 
    /**  registro existo de entrada a ... y salida a .... */
    EYS_MATERIA,
    
    /** Entrada o Salida Fuera de Tolerancia */ 
    EYS_MATERIA_FUERATOLERANCIA,
    
    /** Sin actividad programada */
    EYS_MATERIA_SINACTIVIDAD,
    
    /**  registro exitoso: entrada a materia ... */
    EYS_MATERIA_E,
    /**  registro repetido de entrada a materia... */
    EYS_MATERIA_E_REGISTRADA,
    /** entrada fuera de tolerancia a clases */
    EYS_MATERIA_E_FUERATOLERANCIA,
    
    /** Salida de clase existosa */
    EYS_MATERIA_S,
    /** salida de clase repetido */
    EYS_MATERIA_S_REGISTRADA,
    /** salida de clase antes de tiempo */
    EYS_MATERIA_S_ANTES,
    
    
    
    
    
    //salida a clase -> MATERIA_E_
    //entrada a clase -> MATERIA_S_
    
}
