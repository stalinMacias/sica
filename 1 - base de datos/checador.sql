-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 27-08-2019 a las 12:46:50
-- Versión del servidor: 5.5.24-log
-- Versión de PHP: 5.3.13

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `checador`
--

DELIMITER $$
--
-- Procedimientos
--
CREATE DEFINER=`frank`@`%` PROCEDURE `add_correo_usuario`(IN _usr VARCHAR(10), IN _correo VARCHAR(200))
BEGIN
	DECLARE _prin BOOLEAN;
	SET _prin = (SELECT COUNT(1)=0 FROM correosusuarios WHERE usuario = _usr);
	INSERT INTO correosusuarios (usuario,correo,principal) VALUES (_usr,_correo,_prin);
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `delete_justificante_folio`(in _folio int(8) unsigned)
BEGIN
	delete from justificantes_folios where folio = _folio;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `delete_usuario`(in _usr varchar(15))
BEGIN	
	delete from horariousuarios where horariousuarios.`usuario` = _usr;
	delete from crn where crn.`usuario` = _usr;
	delete from administradores where administradores.`codigo` = _usr;
	delete from directivos where directivos.`codigo` = _usr;
	delete from huellas where huellas.`usuario` = _usr;
	delete from registrosfull where registrosfull.`usuario` = _usr;
	delete from justificantes_folios where justificantes_folios.`usuario` = _usr;
	delete from mensajes where mensajes.`usuario` = _usr;
	DELETE FROM usuarios WHERE usuarios.`usuario` = _usr;
	delete from correosusuarios where correosusuarios.`usuario` = _usr;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `getall_usuarios_para_asistencia`(IN `_tipo` INT, IN `_fecha` DATE)
BEGIN
		
	if (SELECT COUNT(1) FROM horariousuarios WHERE _fecha < vigencia ) = 0 then 
	
		SELECT horariousuarios.usuario AS usuario,
		usuarios.nombre AS nombre,
		horariousuarios.entrada AS entrada,
		horariousuarios.salida AS salida,
		horariousuarios.diasig AS diasig,
		horariousuarios.vigencia AS vigencia
		FROM horariousuarios inner join usuarios on horariousuarios.usuario = usuarios.usuario
		WHERE vigencia IS NULL AND dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%') 
			and usuarios.tipo = _tipo 
			and usuarios.`Status` = 1;
	
	else
	
		SELECT t.usuario AS usuario,
		usuarios.nombre AS nombre,
		t.entrada AS entrada,
		t.salida AS salida,
		t.diasig AS diasig,
		t.vigencia AS vigencia FROM (
		(SELECT * FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha))
			UNION (SELECT * FROM horariousuarios WHERE usuario NOT IN 
				(SELECT usuario FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha)))
		 ) AS t INNER JOIN usuarios ON t.usuario = usuarios.usuario 
		 WHERE t.dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%') 
		 AND usuarios.tipo = _tipo and usuarios.`Status` = 1 order by entrada;
		
	end if;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `getsome_registros_usuario`(IN `usr` VARCHAR(9), IN `_fecha` DATE)
BEGIN
 
 SELECT
  `registrosfull`.`fechahora`      AS `fechahora`,
  `registrosfull`.`tipo`        AS `tiporegistro`,
  `registrosfull`.`modificado`  AS `modificado`
FROM `registrosfull`
     
 WHERE registrosfull.usuario = usr and date(registrosfull.fechahora) = _fecha;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `getsome_usuarios_para_asistencia`(IN `_tipo` INT, IN `_jefe` int(8) unsigned, IN `_fecha` DATE)
BEGIN
		
	if (SELECT COUNT(1) FROM horariousuarios WHERE _fecha < vigencia ) = 0 then 
	
		SELECT horariousuarios.usuario AS usuario,
		usuarios.nombre AS nombre,
		horariousuarios.entrada AS entrada,
		horariousuarios.salida AS salida,
		horariousuarios.diasig AS diasig,
		horariousuarios.vigencia AS vigencia
		FROM horariousuarios inner join usuarios on horariousuarios.usuario = usuarios.usuario
		WHERE vigencia IS NULL AND dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%') and usuarios.`Status` = 1 and usuarios.tipo = _tipo 
		and usuarios.departamento = (SELECT instancias.`codigo` FROM instancias WHERE instancias.`jefe` = _jefe );
	
	else
	
		SELECT t.usuario AS usuario,
		usuarios.nombre AS nombre,
		t.entrada AS entrada,
		t.salida AS salida,
		t.diasig AS diasig,
		t.vigencia AS vigencia FROM (
		(SELECT * FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha))
			UNION (SELECT * FROM horariousuarios WHERE usuario NOT IN 
				(SELECT usuario FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha)))
		 ) AS t INNER JOIN usuarios ON t.usuario = usuarios.usuario WHERE t.dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%') 
			AND usuarios.`Status` = 1 
			AND usuarios.tipo = _tipo 			
			AND usuarios.departamento = (select instancias.`codigo` from instancias where instancias.`jefe` = _jefe )
		order by entrada;
		
	end if;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clases_eys_fueratolerancia_usuario`(IN `_usr` VARCHAR(10))
BEGIN
SELECT 
    `usuarios`.`usuario` AS `usuario`,
    `crn`.`crn` AS `crn`,
    `crn`.`anio` AS `anio`,
    `crn`.`ciclo` AS `ciclo`,        
    bloques.bloque AS bloque,
    `horarioscrn`.`hora` AS `horario`,
    `horarioscrn`.`dia` AS `dia`,
    `horarioscrn`.`Aula` AS `aula`,
    `horarioscrn`.`duracion` AS `duracion`,
    `materias`.`nombre` AS `materia` 
    
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
    JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
    JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
        AND (`horarioscrn`.`anio` = `crn`.`anio`) 
        AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
    JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
    JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
    
WHERE usuarios.usuario = _usr
    and (  
    -- verificar si la clase esta entre [20 a 60] minutos antes de la clase o entre [20 a 60] minutos despues de la salida de clase 
            
            (   -- [20 a 60] minutos despues de la salida de clase
                `horarioscrn`.`hora` >  SUBTIME(SUBTIME(CURTIME(),`horarioscrn`.`duracion`),'01:00:00.0')  
                AND  `horarioscrn`.`hora` < SUBTIME(SUBTIME(CURTIME(),`horarioscrn`.`duracion`),'00:20:00.0') 
            )
            OR 
            (  -- [20 a 60] minutos antes de la clase
               `horarioscrn`.`hora` > TIME(NOW() + INTERVAL 20 MINUTE)
        	   AND `horarioscrn`.`hora` < TIME(NOW() + INTERVAL 60 MINUTE)
            )
        )

    AND (`crn`.`anio` = YEAR(NOW())) 
    AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
    AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
    AND ((`bloques`.`bloque` = `CURRENT_BLOQUE`()) 
       OR (`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN `bloques`.`inicio` AND `bloques`.`fin`));
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clases_fuera_tolerancia_usuario`(IN `_usr` VARCHAR(10))
BEGIN
SELECT 
    `usuarios`.`usuario` AS `usuario`,
    `crn`.`crn` AS `crn`,
    `crn`.`anio` AS `anio`,
    `crn`.`ciclo` AS `ciclo`,        
    bloques.bloque AS bloque,
    `horarioscrn`.`hora` AS `horario`,
    `horarioscrn`.`dia` AS `dia`,
    `horarioscrn`.`Aula` AS `aula`,
    `horarioscrn`.`duracion` AS `duracion`,
    `materias`.`nombre` AS `materia` 
    
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
    JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
    JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
        AND (`horarioscrn`.`anio` = `crn`.`anio`) 
        AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
    JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
    JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
    
WHERE usuarios.usuario = _usr
    and ((horarioscrn.`hora` > TIME(NOW() - INTERVAL 60 MINUTE) 
	and horarioscrn.`hora` < TIME(NOW() - INTERVAL 19 MINUTE) )
    or ( horarioscrn.`hora` > TIME(NOW() + INTERVAL 19 MINUTE)
	and horarioscrn.`hora` < TIME(NOW() + INTERVAL 98 MINUTE)))
    AND (`crn`.`anio` = YEAR(NOW())) AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
    AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
    AND ((`bloques`.`bloque` = `CURRENT_BLOQUE`()) 
       OR (`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`));
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clases_pendientes_usuario`(IN `_usr` VARCHAR(10))
BEGIN
	SELECT 
    `usuarios`.`usuario` AS `usuario`,
    `crn`.`crn` AS `crn`,
    `crn`.`anio` AS `anio`,
    `crn`.`ciclo` AS `ciclo`,        
    bloques.bloque AS bloque,
    `horarioscrn`.`hora` AS `horario`,
    `horarioscrn`.`dia` AS `dia`,
    `horarioscrn`.`Aula` AS `aula`,
    `horarioscrn`.`duracion` AS `duracion`,
    `materias`.`nombre` AS `materia` 
    
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
    JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
    JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
        AND (`horarioscrn`.`anio` = `crn`.`anio`) 
        AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
    JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
    JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
WHERE (`horarioscrn`.`hora` > ( TIME(NOW() + INTERVAL 99 MINUTE ) ) ) 
    AND (`crn`.`anio` = YEAR(NOW())) AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
    AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
    AND ((`bloques`.`bloque` = `CURRENT_BLOQUE`()) OR (`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`))
    and usuarios.usuario = _usr;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clase_actual_usuario`(IN `_usr` VARCHAR(10))
BEGIN
SELECT 
	`usuarios`.`usuario` AS `usuario`,
	`crn`.`crn` AS `crn`,
	`crn`.`anio` AS `anio`,
	`crn`.`ciclo` AS `ciclo`,		
	bloques.bloque AS bloque,
	`horarioscrn`.`hora` AS `horario`,
	`horarioscrn`.`dia` AS `dia`,
	`horarioscrn`.`Aula` AS `aula`,
    `horarioscrn`.`duracion` AS `duracion`,
	`materias`.`nombre` AS `materia` 
	
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
	JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
	JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
		AND (`horarioscrn`.`anio` = `crn`.`anio`) 
		AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
	JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
	JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
WHERE (((`horarioscrn`.`hora` > (NOW() - INTERVAL 20 MINUTE)) 
	AND (`horarioscrn`.`hora` < (NOW() + INTERVAL 20 MINUTE)) )
	AND (`crn`.`anio` = YEAR(NOW())) AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
	AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
	AND ((`bloques`.`bloque` = `CURRENT_BLOQUE`()) OR (`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`))
	and usuarios.usuario = _usr);
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clase_anterior_usuario`(IN `_usr` VARCHAR(10))
BEGIN
SELECT
	`usuarios`.`usuario` AS `usuario`,
	`crn`.`crn` AS `crn`,
	`crn`.`anio` AS `anio`,
	`crn`.`ciclo` AS `ciclo`,		
	`bloques`.`bloque` AS `bloque`,
	`horarioscrn`.`hora` AS `horario`,
	`horarioscrn`.`duracion` AS `duracion`,
	`horarioscrn`.`dia` AS `dia`,
	`horarioscrn`.`Aula` AS `aula`,
	`materias`.`nombre` AS `materia`
	
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`)))
	JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`)))
	JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`)
		AND (`horarioscrn`.`anio` = `crn`.`anio`)
		AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`))))
	JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
	JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))

WHERE (
		(

			# (    `horarioscrn`.`hora` < (NOW() - `horarioscrn`.`duracion` +  INTERVAL 20 MINUTE)  )
			(    `horarioscrn`.`hora` < (ADDTIME( SUBTIME(CURTIME(),`horarioscrn`.`duracion`) , '00:20:00.0' ))  )

			# SELECT ADDTIME(CURTIME(), '00:20:00.0')
			AND
		    #(    `horarioscrn`.`hora` > (NOW() - `horarioscrn`.`duracion` -  INTERVAL 20 MINUTE)  )
		    (    `horarioscrn`.`hora` > (SUBTIME( SUBTIME(CURTIME(),`horarioscrn`.`duracion`) , '00:20:00.0' ))  )  
		)
		AND (`crn`.`anio` = YEAR(NOW())  )
		AND (`crn`.`ciclo` = `CURRENT_CICLO`())
		AND (`horarioscrn`.`dia` = `CURRENT_DIA`())
		AND (
				(`bloques`.`bloque` = `CURRENT_BLOQUE`())
				OR
				(`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`)
			)
		AND usuarios.usuario = _usr
	);
    
END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_clase_encurso_usuario`(IN `_usr` VARCHAR(10))
BEGIN
SELECT 
	`usuarios`.`usuario` AS `usuario`,
	`crn`.`crn` AS `crn`,
	`crn`.`anio` AS `anio`,
	`crn`.`ciclo` AS `ciclo`,		
	`bloques`.`bloque` AS `bloque`,
	`horarioscrn`.`hora` AS `horario`,
	`horarioscrn`.`duracion` AS `duracion`,
	`horarioscrn`.`dia` AS `dia`,
	`horarioscrn`.`Aula` AS `aula`,
	`materias`.`nombre` AS `materia` 
	
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
	JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
	JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
		AND (`horarioscrn`.`anio` = `crn`.`anio`) 
		AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
	JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
	JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))

WHERE (
		( 
			# clase < hora actual - 20 min && [clase + duracion] > hora actual + 20min
			-- se acomoda de la sig. manera para que "clase" quede sola y funcione adeucadamente en mysql.
			# clase < hora actual - 20 min && clase > hora actual - duracion + 20min
			(    `horarioscrn`.`hora` < SUBTIME(CURTIME(),'00:20:00.0')  )        
			AND 
		    (    `horarioscrn`.`hora` > ADDTIME( SUBTIME( CURTIME(),`horarioscrn`.`duracion`) , '00:20:00.0')  )
		)
		AND (`crn`.`anio` = YEAR(NOW())  ) 
		AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
		AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
		AND (
				(`bloques`.`bloque` = `CURRENT_BLOQUE`()) 
				OR 
				(`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`)
			)
		AND usuarios.usuario = _usr
	);
    
END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_faltas_clases_hora`(in _time time)
BEGIN	
	DECLARE dtime DateTIME;
	SET dtime = CONCAT(date(now()),' ',TIME(_time));
	
	SELECT get_horario_crn.*, correosusuarios.`correo` 
	FROM get_horario_crn INNER JOIN correosusuarios ON get_horario_crn.`usuario` = correosusuarios.`usuario` and correosusuarios.`principal` = true
		
	WHERE horario = time(_time)
		AND anio = YEAR(dtime)
		AND ciclo = current_ciclo() 
		AND dia = current_dia()
		AND (bloque = current_bloque() OR (bloque = 0 and date(dtime) between inicio and fin))
		AND ((SELECT COUNT(1) FROM eventos WHERE DATE(dtime) BETWEEN eventos.inicio AND eventos.fin ) = 0 ) 
		AND get_horario_crn.usuario NOT IN ( 
			SELECT registrosfull.usuario FROM registrosfull 
			WHERE fechahora >= dtime - INTERVAL 20 MINUTE
			AND fechahora <= dtime + INTERVAL 20 MINUTE
			
			UNION SELECT justificantes_folios.`usuario`
				from justificantes_folios inner JOIN justificantes_periodo USING(folio)
				where justificantes_folios.`aprobado` = true
					and DATE(dtime) BETWEEN justificantes_periodo.`fecha_inicial` 
						AND justificantes_periodo.`fecha_final`)
		and get_horario_crn.`crn` not in (
			SELECT `justificantes_asignaturas`.`crn`
			FROM justificantes_folios inner JOIN justificantes_asignaturas USING(folio)
			WHERE justificantes_folios.`aprobado` = TRUE
				AND (justificantes_asignaturas.`crn` = get_horario_crn.`crn`
					AND justificantes_asignaturas.`fecha` = DATE(dtime)));	
			
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_faltas_clases_hora_e`(IN `_time` TIME, IN `_duracion` TIME)
BEGIN	
	DECLARE dtime DateTIME;
	SET dtime = CONCAT(date(now()),' ',TIME(_time));
	
	SELECT get_horario_crn.*, correosusuarios.`correo` 
	FROM get_horario_crn INNER JOIN correosusuarios ON get_horario_crn.`usuario` = correosusuarios.`usuario` and correosusuarios.`principal` = true
		
	WHERE horario = time(_time)
		AND duracion = time(_duracion)
		AND anio = YEAR(dtime)
		AND ciclo = current_ciclo() 
		AND dia = current_dia()
		AND (bloque = current_bloque() OR (bloque = 0 and date(dtime) between inicio and fin))
		AND ((SELECT COUNT(1) FROM eventos WHERE DATE(dtime) BETWEEN eventos.inicio AND eventos.fin ) = 0 ) 
		AND get_horario_crn.usuario NOT IN ( 

			SELECT * FROM (
				SELECT registrosfull.usuario FROM registrosfull 
				INNER JOIN get_horario_crn ON registrosfull.usuario = get_horario_crn.usuario 
				WHERE fechahora >= SUBTIME(dtime,'00:20:00') -- dtime - INTERVAL 20 MINUTE -- 
				AND fechahora <= ADDTIME(dtime,'00:20:00')  -- 
				
				UNION SELECT justificantes_folios.`usuario`
					from justificantes_folios inner JOIN justificantes_periodo USING(folio)
					where justificantes_folios.`aprobado` = true
						and DATE(dtime) BETWEEN justificantes_periodo.`fecha_inicial` 
						AND justificantes_periodo.`fecha_final`
			) AS subquery # esto obtiene un super rendimiento porque genera como un especie de caché de la subConsulta evitando que se realice cada vuelta del ciclo Where
		) 

		AND get_horario_crn.`crn` not in ( 
			SELECT * FROM (
				SELECT `justificantes_asignaturas`.`crn`
				FROM justificantes_folios 
				inner JOIN justificantes_asignaturas USING(folio)
				INNER JOIN get_horario_crn ON justificantes_folios.usuario = get_horario_crn.usuario
				WHERE justificantes_folios.`aprobado` = TRUE
					AND (justificantes_asignaturas.`crn` = get_horario_crn.`crn`
						AND justificantes_asignaturas.`fecha` = DATE(dtime))
			) AS subquery2
		);	
			
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_faltas_clases_hora_eys`(IN `_time` TIME, IN `_duracion` TIME)
BEGIN	
	DECLARE dtime DateTIME;
	SET dtime = CONCAT(date(now()),' ',TIME(_time));
	
	SELECT get_horario_crn.*, correosusuarios.`correo` 
	FROM get_horario_crn INNER JOIN correosusuarios ON get_horario_crn.`usuario` = correosusuarios.`usuario` and correosusuarios.`principal` = true
		
	WHERE horario = time(_time)
		AND duracion = time(_duracion)
		AND anio = YEAR(dtime)
		AND ciclo = current_ciclo() 
		AND dia = current_dia()
		AND (bloque = current_bloque() OR (bloque = 0 and date(dtime) between inicio and fin))
		AND ((SELECT COUNT(1) FROM eventos WHERE DATE(dtime) BETWEEN eventos.inicio AND eventos.fin ) = 0 ) 
		AND get_horario_crn.usuario NOT IN ( 

			SELECT * FROM (
				SELECT registrosfull.usuario FROM registrosfull 
				INNER JOIN get_horario_crn ON registrosfull.usuario = get_horario_crn.usuario 
				WHERE fechahora >= SUBTIME(dtime,'00:20:00') -- dtime - INTERVAL 20 MINUTE -- 
				AND fechahora <=  ADDTIME( ADDTIME(dtime,'00:20:00') , get_horario_crn.duracion) -- 
				
				UNION SELECT justificantes_folios.`usuario`
					from justificantes_folios inner JOIN justificantes_periodo USING(folio)
					where justificantes_folios.`aprobado` = true
						and DATE(dtime) BETWEEN justificantes_periodo.`fecha_inicial` 
						AND justificantes_periodo.`fecha_final`
			) AS subquery # esto obtiene un super rendimiento porque genera como un especie de caché de la subConsulta evitando que se realice cada vuelta del ciclo Where
		) 

		AND get_horario_crn.`crn` not in ( 
			SELECT * FROM (
				SELECT `justificantes_asignaturas`.`crn`
				FROM justificantes_folios 
				inner JOIN justificantes_asignaturas USING(folio)
				INNER JOIN get_horario_crn ON justificantes_folios.usuario = get_horario_crn.usuario
				WHERE justificantes_folios.`aprobado` = TRUE
					AND (justificantes_asignaturas.`crn` = get_horario_crn.`crn`
						AND justificantes_asignaturas.`fecha` = DATE(dtime))
			) AS subquery2
		);	
			
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_faltas_clases_hora_s`(IN `_time` TIME, IN `_duracion` TIME)
BEGIN	
	DECLARE dtime DateTIME;
	SET dtime = CONCAT(date(now()),' ',TIME(_time));
	
	SELECT get_horario_crn.*, correosusuarios.`correo` 
	FROM get_horario_crn INNER JOIN correosusuarios ON get_horario_crn.`usuario` = correosusuarios.`usuario` and correosusuarios.`principal` = true
		
	WHERE horario = time(_time)
		AND duracion = time(_duracion)
		AND anio = YEAR(dtime)
		AND ciclo = current_ciclo() 
		AND dia = current_dia()
		AND (bloque = current_bloque() OR (bloque = 0 and date(dtime) between inicio and fin))
		AND ((SELECT COUNT(1) FROM eventos WHERE DATE(dtime) BETWEEN eventos.inicio AND eventos.fin ) = 0 ) 
		AND get_horario_crn.usuario NOT IN ( 

			SELECT * FROM (
				SELECT registrosfull.usuario FROM registrosfull 
				INNER JOIN get_horario_crn ON registrosfull.usuario = get_horario_crn.usuario 
				WHERE fechahora >= ADDTIME ( SUBTIME(dtime,'00:20:00') , get_horario_crn.duracion)-- dtime - INTERVAL 20 MINUTE -- 
				AND fechahora <=  ADDTIME( ADDTIME(dtime,'00:20:00') , get_horario_crn.duracion) -- 
				
				UNION SELECT justificantes_folios.`usuario`
					from justificantes_folios inner JOIN justificantes_periodo USING(folio)
					where justificantes_folios.`aprobado` = true
						and DATE(dtime) BETWEEN justificantes_periodo.`fecha_inicial` 
						AND justificantes_periodo.`fecha_final`
			) AS subquery # esto obtiene un super rendimiento porque genera como un especie de caché de la subConsulta evitando que se realice cada vuelta del ciclo Where
		) 

		AND get_horario_crn.`crn` not in ( 
			SELECT * FROM (
				SELECT `justificantes_asignaturas`.`crn`
				FROM justificantes_folios 
				inner JOIN justificantes_asignaturas USING(folio)
				INNER JOIN get_horario_crn ON justificantes_folios.usuario = get_horario_crn.usuario
				WHERE justificantes_folios.`aprobado` = TRUE
					AND (justificantes_asignaturas.`crn` = get_horario_crn.`crn`
						AND justificantes_asignaturas.`fecha` = DATE(dtime))
			) AS subquery2
		);	
			
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_faltas_jornada_obligatoria_dia_anterior`()
BEGIN
SELECT horariousuarios.usuario AS usuario,
	usuarios.nombre AS nombre,	
	horariousuarios.`entrada` AS entrada,
	horariousuarios.salida AS salida,
	time(regentrada.`fechahora`) AS registroentrada,
	time(regsalida.`fechahora`) AS registrosalida,
	correosusuarios.`correo` as correo
	
FROM horariousuarios 
	INNER JOIN usuarios ON horariousuarios.usuario = usuarios.usuario
	inner join tipousuarios on usuarios.`tipo` = tipousuarios.`tipo` 
	inner join correosusuarios on correosusuarios.`usuario` = horariousuarios.usuario and correosusuarios.`principal` = true
	
	LEFT JOIN registrosfull AS regentrada
		ON regentrada.`usuario` = usuarios.`usuario` 
		AND regentrada.fechahora = (SELECT MIN(fechahora) FROM registrosfull WHERE registrosfull.`usuario` = regentrada.`usuario`
						AND DATE(fechahora)= DATE(SUBDATE(NOW(), 1)))
	LEFT JOIN registrosfull AS regsalida
		ON regsalida.`usuario` = usuarios.`usuario` 
		AND regsalida.fechahora = (SELECT MAX(fechahora) FROM registrosfull WHERE registrosfull.`usuario` = regsalida.`usuario`
						AND DATE(fechahora)= DATE(SUBDATE(NOW(), 1)) )
		
WHERE usuarios.`Status` = 1 
    AND tipousuarios.`jornada` = 'obligatoria'
    and horariousuarios.`diasig` is FALSE
    AND ((SELECT COUNT(1) FROM eventos WHERE DATE(SUBDATE(NOW(),1)) BETWEEN inicio AND fin  AND asignaturas = FALSE) = 0)
    AND usuarios.`usuario` NOT IN ( 
	SELECT justificantes_folios.usuario 
	FROM justificantes_folios inner join justificantes_periodo using (folio)
	WHERE justificantes_folios.`aprobado` = TRUE 
		AND DATE(SUBDATE(NOW(),1)) BETWEEN fecha_inicial AND fecha_final )  
    AND vigencia IS NULL AND dias LIKE CONCAT('%',DAYOFWEEK(SUBDATE(NOW(), 1)),'%')     
    AND (regentrada.`fechahora` IS NULL 
	OR regsalida.`fechahora` IS NULL 
	OR regentrada.`fechahora` = regsalida.`fechahora`)
	
ORDER BY usuario;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horarios_usuario_periodo`(IN `_usr` VARCHAR(10), IN `_desde` DATE, IN `_hasta` DATE)
BEGIN
	IF ( SELECT COUNT(1) FROM horariousuarios WHERE usuario = _usr  ) = 1 THEN
	
		SELECT dias,entrada,salida,diasig,CAST(vigencia AS CHAR) AS vigencia FROM horariousuarios WHERE usuario = _usr;
	
	ELSEIF (SELECT COUNT(1) FROM horariousuarios WHERE usuario = _usr AND _hasta <= vigencia) > 0 THEN
	
		SELECT dias,entrada,salida,diasig,CAST(vigencia AS CHAR) AS vigencia FROM (	
		( SELECT * FROM horariousuarios WHERE usuario = _usr AND vigencia > _hasta ORDER BY vigencia ASC LIMIT 1)
		UNION (SELECT * FROM horariousuarios WHERE usuario = _usr AND (vigencia <= _hasta AND vigencia >= _desde) ))
		AS t ORDER BY t.vigencia ASC;
	ELSE
		SELECT * FROM (	
		(SELECT dias,entrada,salida,diasig,CAST(vigencia AS CHAR) AS vigencia
			FROM horariousuarios WHERE usuario = _usr AND vigencia >= _desde)
		UNION (SELECT dias,entrada,salida,diasig,cast(DATE(NOW() + INTERVAL 1 DAY) as char)AS vigencia 
			FROM horariousuarios WHERE usuario = _usr AND vigencia IS NULL)
		) AS t ORDER BY t.vigencia ASC;
	END IF;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horario_actual_usuario`(IN `usr` VARCHAR(9))
BEGIN
	select cast(`horariousuarios`.`vigencia` as char) as vigencia,
        horariousuarios.`dias`,
        horariousuarios.`diasig`,
        horariousuarios.`entrada`,
        horariousuarios.`salida`,
        horariousuarios.`usuario`        
        
     from horariousuarios where horariousuarios.usuario = usr and vigencia is null;
     
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horario_asignaturas_dia_departamento`(in _fecha date, in _dia varchar(20), in _jefe int(8) unsigned)
BEGIN
	select  `usuarios`.`usuario` AS `usuario`,  
		`usuarios`.`nombre` AS `nombre`,  
		`usuarios`.`departamento` AS `departamento`,  
		`crn`.`crn` AS `crn`,  `crn`.`anio` AS `anio`,  
		`horarioscrn`.`hora` AS `horario`,  
		`horarioscrn`.`dia` AS `dia`,  
		`materias`.`nombre` AS `materia`,  
		`bloques`.`inicio` AS `inicio`,  
		`bloques`.`fin` AS `fin`
	from (((((`crn`  join `usuarios`  on ((`crn`.`usuario` = `usuarios`.`usuario`)))  
		join `horarioscrn`  on (((`horarioscrn`.`crn` = `crn`.`crn`)  
			and (`horarioscrn`.`anio` = `crn`.`anio`)  
			and (`horarioscrn`.`ciclo` = `crn`.`ciclo`))))  
		join `materias`  on ((`materias`.`codigo` = `crn`.`materia`)))  		
		join `bloques`  on (((`horarioscrn`.`bloque` = `bloques`.`bloque`)  
			and (`horarioscrn`.`anio` = `bloques`.`anio`)  
			and (`horarioscrn`.`ciclo` = `bloques`.`ciclo`))))
		inner join instancias on instancias.`codigo` = usuarios.`departamento`)
	where materias.`departamento` = (select instancias.`codigo` from instancias where instancias.`jefe` = _jefe)
		AND _fecha BETWEEN inicio AND fin
                AND horarioscrn.dia = _dia 
                                     
        ORDER BY horario ASC;
			
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horario_clase_usuario`(IN `_usr` VARCHAR(9), IN `_ciclo` VARCHAR(1), IN `_anio` YEAR)
BEGIN
	SELECT
  `usuarios`.`usuario`      AS `usuario`,
  `usuarios`.`nombre`       AS `nombre`,
  `usuarios`.`departamento` AS `departamento`,
  `crn`.`crn`               AS `crn`,
  `crn`.`anio`              AS `anio`,
  `horarioscrn`.`hora`      AS `horario`,
  `horarioscrn`.`dia`       AS `dia`,
  `materias`.`nombre`       AS `materia`,
  `bloques`.`inicio`        AS `inicio`,
  `bloques`.`fin`           AS `fin`
FROM (((((`usuarios`
       JOIN `tipousuarios`
         ON ((`usuarios`.`tipo` = `tipousuarios`.`tipo`)))
      JOIN `crn`
        ON ((`crn`.`usuario` = `usuarios`.`usuario`)))
     JOIN `horarioscrn`
       ON (((`horarioscrn`.`crn` = `crn`.`crn`)
            AND (`horarioscrn`.`anio` = `crn`.`anio`)
            AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`))))
    JOIN `materias`
      ON ((`materias`.`codigo` = `crn`.`materia`)))
   JOIN `bloques`
     ON ((`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
 where usuarios.usuario = _usr and crn.ciclo = _ciclo and crn.anio = _anio
	;
 
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horario_fecha_usuario`(IN `usr` VARCHAR(9), IN `fecha` DATE)
BEGIN
	IF ( SELECT COUNT(1) FROM horariousuarios WHERE usuario = usr AND fecha < vigencia ) > 0 THEN 
	
		SELECT horariousuarios.usuario AS usuario,
		usuarios.nombre AS nombre,
		horariousuarios.entrada AS entrada,
		horariousuarios.salida AS salida,
		horariousuarios.diasig AS diasig,
		CAST(horariousuarios.vigencia AS CHAR) AS vigencia 
		FROM (horariousuarios INNER JOIN usuarios ON horariousuarios.usuario = usuarios.usuario )
		WHERE usuarios.usuario = usr AND vigencia = ( SELECT min(vigencia) FROM horariousuarios WHERE  vigencia > fecha )
			and dias LIKE CONCAT('%',DAYOFWEEK(fecha),'%');
	
	ELSE
	
		SELECT horariousuarios.usuario AS usuario,
		usuarios.nombre AS nombre,
		horariousuarios.entrada AS entrada,
		horariousuarios.salida AS salida,
		horariousuarios.diasig AS diasig,
		CAST(horariousuarios.vigencia AS CHAR) AS vigencia 
		FROM (horariousuarios inner join usuarios ON horariousuarios.usuario = usuarios.usuario)
		WHERE usuarios.usuario = usr AND vigencia IS NULL ANd dias LIKE CONCAT('%',DAYOFWEEK(fecha),'%');
	
	END IF;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_horario_hoy_usuario`(IN `usr` VARCHAR(9))
BEGIN
	select * from horariousuarios where usuario = usr and vigencia is null
	and horariousuarios.dias like  concat( '%', DAYOFWEEK(CURRENT_DATE()) ,'%' );
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_instancias`()
BEGIN
	select instancias.*, usuarios.`nombre` from instancias left join usuarios on instancias.`jefe` = usuarios.`usuario`;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_aprobados_asignatura_periodo`(in _desde date, in _hasta Date)
BEGIN
	SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		justificantes_asignaturas.`fecha` ,
		justificantes_asignaturas.`crn`,
		justificantes_lista.`nombre` as nombrejustificante,
		justificantes_fracciones.`categoria` as nombrefraccion,
		justificantes_folios.`aceptado`,
		aceptado.`nombre` AS aceptadonombre,
		justificantes_folios.`aprobado`,
		aprobado.`nombre` AS aprobadonombre
		
	FROM justificantes_folios 
		INNER JOIN justificantes_asignaturas USING (folio)
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
			    AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`    		
		LEFT JOIN usuarios aceptado ON justificantes_folios.`aceptadopor` = aceptado.`usuario`		
		LEFT JOIN usuarios aprobado ON justificantes_folios.`aprobadopor` = aprobado.`usuario`
		
	where justificantes_folios.`aprobado` = true
		and justificantes_asignaturas.`fecha` between _desde and _hasta;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_aprobados_periodo`(IN _desde DATE, IN _hasta DATE)
BEGIN
	SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		justificantes_periodo.`fecha_inicial`,
		justificantes_periodo.`fecha_final`,
		justificantes_lista.`nombre` AS nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` AS nombrefraccion,
		justificantes_folios.`aceptado`,
		aceptado.`nombre` AS aceptadonombre,
		justificantes_folios.`aprobado`,
		aprobado.`nombre` AS aprobadonombre
		
		
	FROM justificantes_folios 
		INNER JOIN justificantes_periodo USING(folio) 
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
			AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`
		LEFT JOIN usuarios aceptado ON justificantes_folios.`aceptadopor` = aceptado.`usuario`
		LEFT JOIN usuarios aprobado ON justificantes_folios.`aprobadopor` = aprobado.`usuario`
	
	where justificantes_folios.`aprobado` = true;	
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_aprobados_usuario`(IN _usr int(8) unsigned)
BEGIN
	SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		justificantes_periodo.`fecha_inicial`,
		justificantes_periodo.`fecha_final`,
		justificantes_lista.`nombre` AS nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` AS nombrefraccion,
		justificantes_folios.`aceptado`,
		aceptado.`nombre` AS aceptadonombre,
		justificantes_folios.`aprobado`,
		aprobado.`nombre` AS aprobadonombre
		
	FROM justificantes_folios 
		INNER JOIN justificantes_periodo USING(folio) 
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
			AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`
		LEFT JOIN usuarios aceptado ON justificantes_folios.`aceptadopor` = aceptado.`usuario`
		LEFT JOIN usuarios aprobado ON justificantes_folios.`aprobadopor` = aprobado.`usuario`
		
	where justificantes_folios.`aprobado` = TRUE
		and justificantes_folios.`usuario` = _usr
	ORDER by justificantes_periodo.`fecha_inicial` DESC;	
end$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_pendientes`()
BEGIN
	(SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		usuarios.`nombre` as nombreusuario,
		"a" as tipo,
		justificantes_asignaturas.`fecha` ,
		null as `fecha_inicial`,
		null as `fecha_final`,		
		justificantes_asignaturas.`crn`,
		materias.`nombre` as nombremateria,
		justificantes_lista.`nombre` as nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` as nombrefraccion,
		justificantes_folios.`aceptado`,
		aceptado.nombre as `aceptadonombre`,
		justificantes_folios.`aprobado`	,	
		aprobado.nombre as aprobadonombre
		
	    FROM justificantes_folios 
		INNER JOIN justificantes_asignaturas USING (folio)
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
			    AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`    
		INNER JOIN usuarios ON justificantes_folios.`usuario` = usuarios.`usuario`        
		left join crn on crn.`crn` = justificantes_asignaturas.`crn` 
		    and crn.`anio` = YEAR(justificantes_asignaturas.`fecha`) 
		    and crn.`usuario` = justificantes_folios.`usuario`
		    and crn.`ciclo` = if(MONTH(justificantes_asignaturas.`fecha`)<=6,'A','B')
		left join materias on crn.`materia` = materias.`codigo`
		left join usuarios aceptado on aceptado.usuario = justificantes_folios.`aceptadopor`
		LEFT JOIN usuarios aprobado ON aprobado.usuario = justificantes_folios.`aprobadopor`		
		    
	    WHERE justificantes_folios.`aprobado` IS NULL 
		and (justificantes_folios.`aceptado` is null or justificantes_folios.`aceptado` = TRUE))
		
	union
	(SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		usuarios.`nombre` as nombreusuario,
		"p" as tipo,
		null as fecha,
		justificantes_periodo.`fecha_inicial`,
		justificantes_periodo.`fecha_final`,
		null as crn,
		null as nombremateria,
		justificantes_lista.`nombre` AS nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` AS nombrefraccion ,        
		justificantes_folios.`aceptado`,
		aceptado.nombre AS `aceptadonombre`,
		justificantes_folios.`aprobado`	,	
		aprobado.nombre AS aprobadonombre	              
		
	    FROM justificantes_periodo
		INNER JOIN justificantes_folios USING(folio) 
		inner join usuarios using (usuario)
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
		    AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`
		LEFT JOIN usuarios aceptado ON aceptado.usuario = justificantes_folios.`aceptadopor`
		LEFT JOIN usuarios aprobado ON aprobado.usuario = justificantes_folios.`aprobadopor`	
	    
	    where justificantes_folios.`aprobado` is null
		AND (justificantes_folios.`aceptado` IS NULL OR justificantes_folios.`aceptado` = TRUE))
		    
    ORDER BY folio asc;
		
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_pendientes_jefe`(IN _usr INT(8) UNSIGNED )
BEGIN
	(select justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		usuarios.`nombre` as nombreusuario,
		"a" as tipo,
		justificantes_asignaturas.`fecha` ,
		null as `fecha_inicial`,
		null as `fecha_final`,        
		justificantes_asignaturas.`crn`,
		materias.`nombre` as nombremateria,
		justificantes_lista.`nombre` as nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` as nombrefraccion,
		justificantes_folios.`aceptado`,
		aceptado.nombre as `aceptadonombre`,
		justificantes_folios.`aprobado`,
		aprobado.nombre as aprobadonombre    
        
	FROM justificantes_folios 
		INNER JOIN justificantes_asignaturas USING (folio)
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
					AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`	
		INNER JOIN usuarios ON justificantes_folios.`usuario` = usuarios.`usuario`		
		LEFT JOIN crn ON crn.`crn` = justificantes_asignaturas.`crn` 
			AND crn.`anio` = YEAR(justificantes_asignaturas.`fecha`) 
			AND crn.`usuario` = justificantes_folios.`usuario`
			AND crn.`ciclo` = IF(MONTH(justificantes_asignaturas.`fecha`)<=6,'A','B')
		LEFT JOIN materias ON crn.`materia` = materias.`codigo`
		LEFT JOIN usuarios aceptado ON aceptado.usuario = justificantes_folios.`aceptadopor`
		LEFT JOIN usuarios aprobado ON aprobado.usuario = justificantes_folios.`aprobadopor`
			
	WHERE justificantes_folios.`aceptado` IS NULL 
		AND (justificantes_folios.`aprobado` IS NULL OR justificantes_folios.`aprobado` != FALSE)	
		AND folio IN (SELECT justificantes_asignaturas.`folio` FROM instancias
				INNER JOIN materias ON instancias.`codigo` = materias.`departamento`
				INNER JOIN crn ON crn.`materia` = materias.`codigo`
				INNER JOIN justificantes_asignaturas ON justificantes_asignaturas.`crn` = crn.`crn`
				WHERE crn.`anio` = YEAR(justificantes_asignaturas.`fecha`) 
				AND instancias.`jefe` = _usr))
	union
	(SELECT justificantes_folios.`folio`, 
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		usuarios.`nombre` as nombreusuario,
		"p" as tipo,
		null as fecha,
		justificantes_periodo.`fecha_inicial`,
		justificantes_periodo.`fecha_final`,
		null as crn,
		null as nombremateria,
		justificantes_lista.`nombre` AS nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_fracciones.`categoria` AS nombrefraccion ,        
		justificantes_folios.`aceptado`,
		aceptado.nombre AS `aceptadonombre`,
		justificantes_folios.`aprobado`,
		aprobado.nombre AS aprobadonombre     
		
	    FROM justificantes_folios
		INNER JOIN justificantes_periodo USING(folio) 
		INNER JOIN usuarios USING (usuario)
		INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
		LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
		    AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`
		LEFT JOIN usuarios aceptado ON aceptado.usuario = justificantes_folios.`aceptadopor`
		LEFT JOIN usuarios aprobado ON aprobado.usuario = justificantes_folios.`aprobadopor`
		
	    WHERE justificantes_folios.`aceptado` IS NULL  
		AND (justificantes_folios.`aprobado` IS NULL OR justificantes_folios.`aprobado` != FALSE)
		AND usuarios.`departamento` = (SELECT codigo FROM instancias WHERE jefe = _usr)
	)
				
				
	ORDER BY folio ASC;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_periodo_todos_usuario`(in _usr int(8) unsigned)
BEGIN
	SELECT justificantes_folios.`folio`, 
        justificantes_folios.`usuario`,
        justificantes_folios.`fechayhora`,
        justificantes_periodo.`fecha_inicial`,
        justificantes_periodo.`fecha_final`,
        justificantes_lista.`nombre` AS nombrejustificante,
        justificantes_lista.`descripcion_gral`,
        justificantes_fracciones.`categoria` AS nombrefraccion,
        justificantes_folios.`aceptado`,
        aceptado.`nombre` as aceptadonombre,
	justificantes_folios.`aprobado`,
	aprobado.`nombre` AS aprobadonombre
        
    FROM justificantes_folios 
        INNER JOIN justificantes_periodo USING(folio) 
        INNER JOIN justificantes_lista ON justificantes_folios.`justificante` = justificantes_lista.`id`
        LEFT JOIN justificantes_fracciones ON justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
            AND justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`        
        LEFT JOIN usuarios aceptado ON justificantes_folios.`aceptadopor` = aceptado.`usuario`
        left join usuarios aprobado on justificantes_folios.`aprobadopor` = aprobado.`usuario`
        
    where justificantes_folios.`usuario` = _usr
    ORDER by justificantes_periodo.`fecha_inicial` DESC;    
    
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_justificantes_ultimos_usuario`(in _usr int(8) unsigned, in _cant tinyint)
BEGIN
	select justificantes_folios.`folio`,
		justificantes_folios.`usuario`,
		justificantes_folios.`fechayhora`,
		if (justificantes_asignaturas.`fecha`is not null,'a','p') as tipo,
		justificantes_lista.`nombre` as nombrejustificante,
		justificantes_lista.`descripcion_gral`,
		justificantes_folios.`fraccion`,
		justificantes_fracciones.`categoria` as nombrefraccion,		
		justificantes_asignaturas.`fecha`,
		justificantes_periodo.`fecha_inicial`,
		justificantes_periodo.`fecha_final`,
		justificantes_folios.`aprobado`,
		aprobado.`nombre` AS aprobadonombre,
		justificantes_folios.`aceptado`,
		aceptado.`nombre` as aceptadonombre,
		justificantes_asignaturas.`crn`,
		materias.`nombre` AS nombremateria		
		
	from justificantes_folios
		inner join justificantes_lista on justificantes_folios.`justificante` = justificantes_lista.`id`
		left join justificantes_fracciones on justificantes_folios.`justificante` = justificantes_fracciones.`justificante_id`
			and justificantes_folios.`fraccion` = justificantes_fracciones.`fraccion`
		left join justificantes_asignaturas using (folio)		
		left join justificantes_periodo using(folio)
		left join usuarios aprobado on justificantes_folios.`aprobadopor` = aprobado.`usuario`
		LEFT JOIN usuarios aceptado ON justificantes_folios.`aceptadopor` = aceptado.`usuario`
		Left join crn 
			on crn.`crn` = justificantes_asignaturas.`crn` 
			ANd crn.`anio` = YEAR(justificantes_asignaturas.`fecha`)
			AND crn.`ciclo` = IF(MONTH(justificantes_asignaturas.`fecha`)<=6,'A','B')
		LEFT JOIN materias ON crn.`materia` = materias.`codigo`
			
		
	where justificantes_folios.`usuario` = _usr
	order by justificantes_folios.`folio` desc
	limit _cant;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_lapso_actual`()
SELECT * FROM `horarioscrn_lapsos` ORDER BY `horarioscrn_lapsos`.`fecha_inicial` DESC LIMIT 1$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_lista_justificantes`( )
BEGIN
	select tipousuarios.`descripcion` as tipousuario,
		justificantes_lista.`id` as id,
		justificantes_lista.`nombre` as nombre,
		justificantes_lista.`descripcion` as descripcion,
		justificantes_lista.`descripcion_gral` as descripcion_gral,
		justificantes_lista.`documentos` as documentos
		
	from justificantes_lista 
		inner join justificantes_tipousuarios ON justificantes_lista.`id` = justificantes_tipousuarios.`justificante_id`
		Inner join tipousuarios on justificantes_tipousuarios.`tipousuario_id` = tipousuarios.`tipo`;
		
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_lista_justificantes_tipousuario`( in _tipo varchar(30) )
BEGIN
	select justificantes_lista.`id` as id,
		justificantes_lista.`nombre` as nombre,
		justificantes_lista.`descripcion` as descripcion,
		justificantes_lista.`descripcion_gral` as descripcion_gral,
		justificantes_lista.`documentos` as documentos
	from justificantes_lista 
		inner join justificantes_tipousuarios ON justificantes_lista.`id` = justificantes_tipousuarios.`justificante_id`
		Inner join tipousuarios on justificantes_tipousuarios.`tipousuario_id` = tipousuarios.`tipo`
	where tipousuarios.`descripcion` = _tipo;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_materia_para_asistencia`(IN `_usr` VARCHAR(10))
BEGIN
SELECT 
	`usuarios`.`usuario` AS `usuario`,
	`crn`.`crn` AS `crn`,
	`crn`.`anio` AS `anio`,
	`crn`.`ciclo` AS `ciclo`,		
	bloques.bloque AS bloque,
	`horarioscrn`.`hora` AS `horario`,
	`horarioscrn`.`dia` AS `dia`,
	`horarioscrn`.`Aula` AS `aula`,
	`materias`.`nombre` AS `materia` 
	
FROM (((((`usuarios` JOIN `tipousuarios` ON((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) 
	JOIN `crn` ON((`crn`.`usuario` = `usuarios`.`usuario`))) 
	JOIN `horarioscrn` ON(((`horarioscrn`.`crn` = `crn`.`crn`) 
		AND (`horarioscrn`.`anio` = `crn`.`anio`) 
		AND (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) 
	JOIN `materias` ON((`materias`.`codigo` = `crn`.`materia`))
	JOIN bloques ON (`horarioscrn`.`bloque` = `bloques`.`bloque` AND horarioscrn.anio = bloques.anio AND horarioscrn.ciclo = bloques.ciclo)))
WHERE (((`horarioscrn`.`hora` > (NOW() - INTERVAL 20 MINUTE)) 
	AND (`horarioscrn`.`hora` < (NOW() + INTERVAL 20 MINUTE)) )
	AND (`crn`.`anio` = YEAR(NOW())) AND (`crn`.`ciclo` = `CURRENT_CICLO`()) 
	AND (`horarioscrn`.`dia` = `CURRENT_DIA`()) 
	AND ((`bloques`.`bloque` = `CURRENT_BLOQUE`()) OR (`bloques`.`bloque` = 0 AND DATE(NOW()) BETWEEN bloques.`inicio` AND bloques.`fin`))
	and usuarios.usuario = _usr);
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_mensaje`(IN `usr` VARCHAR(10))
BEGIN
	SELECT * FROM mensajes WHERE mensajes.usuario = usr;
	DELETE FROM mensajes WHERE mensajes.usuario = usr;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_pruebafecha`(
	IN `fech_ini` DATE,
	IN `fech_sal` DATE

)
BEGIN
SELECT horariousuarios.usuario,
	usuarios.nombre,	
	horariousuarios.`entrada` AS entrada,
	horariousuarios.salida AS salida,
	TIME(regentrada.`fechahora`) AS registroentrada,
	TIME(regsalida.`fechahora`) AS registrosalida,
	correosusuarios.`correo`	
	
FROM horariousuarios 
	INNER JOIN usuarios ON horariousuarios.usuario = usuarios.usuario
	INNER JOIN tipousuarios ON usuarios.`tipo` = tipousuarios.`tipo` 
	INNER JOIN correosusuarios ON correosusuarios.`usuario` = horariousuarios.usuario AND correosusuarios.`principal` = TRUE
	
	LEFT JOIN registrosfull AS regentrada
		ON regentrada.`usuario` = usuarios.`usuario` 
		AND regentrada.fechahora = (SELECT MAX(fechahora) FROM registrosfull WHERE registrosfull.`usuario` = regentrada.`usuario`
						AND DATE(fechahora)= DATE(SUBDATE(fech_ini, 0)))
	LEFT JOIN registrosfull AS regsalida
		ON regsalida.`usuario` = usuarios.`usuario` 
		AND regsalida.fechahora = (SELECT MAX(fechahora) FROM registrosfull WHERE registrosfull.`usuario` = regsalida.`usuario`
						AND DATE(fechahora)= DATE(SUBDATE(fech_ini, 0)) )
		
WHERE usuarios.`Status` = 1 
    AND tipousuarios.`jornada` = 'obligatoria'
    AND horariousuarios.`diasig` IS FALSE
    AND ((SELECT COUNT(1) FROM eventos WHERE DATE(SUBDATE(fech_ini,0)) BETWEEN inicio AND fin  AND asignaturas = FALSE) = 0)
    AND usuarios.`usuario` NOT IN ( 
	SELECT justificantes_folios.usuario 
	FROM justificantes_folios INNER JOIN justificantes_periodo USING (folio)
	WHERE justificantes_folios.`aprobado` = TRUE 
		AND DATE(SUBDATE(fech_sal,0)) BETWEEN fecha_inicial AND fecha_final )
    AND vigencia IS NULL AND dias LIKE CONCAT('%',DAYOFWEEK(SUBDATE(fech_ini,0)),'%')     
    AND (regentrada.`fechahora` IS NULL 
	OR regsalida.`fechahora` IS NULL 
	OR regentrada.`fechahora` = regsalida.`fechahora`)

ORDER BY nombre ASC;
END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_clase_salida`(IN `_hora` VARCHAR(10) CHARSET utf8, IN `_duracion` VARCHAR(10) CHARSET utf8, IN `_usr` VARCHAR(10) CHARSET utf8)
SELECT * FROM registrosfull WHERE DATE(fechahora) = DATE(NOW()) AND TIME(fechahora) > SUBTIME( ADDTIME (TIME(_hora),TIME(_duracion)) , '00:20:00.0' ) AND TIME(fechahora) < ADDTIME( ADDTIME (TIME(_hora),TIME(_duracion)) , '00:20:00.0' ) AND usuario = _usr$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_fecha`(IN `_fecha` DATE)
BEGIN
	select * from registrosfull where date(registrosfull.fechahora) = _fecha;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_jefe_periodo`(in _jefe int(8) unsigned, IN `_ini` DATE, IN `_fin` DATE)
BEGIN
	select * from registrosfull 
	where usuario in (SELECT usuario FROM usuarios 
		WHERE usuarios.`departamento` = (SELECT instancias.`codigo` FROM instancias WHERE jefe = _jefe) 
		OR usuario = _jefe)
	    AND date(registrosfull.fechahora) >= _ini and DATE(registrosfull.fechahora) <= _fin;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_periodo`(IN `_ini` DATE, IN `_fin` DATE)
BEGIN
	select * from registrosfull where date(registrosfull.fechahora) >= _ini and DATE(registrosfull.fechahora) <= _fin;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_periodo_tipo_usuarios`(IN `_ini` DATE, IN `_fin` DATE, IN _tipo tinyint(1) unsigned)
BEGIN
	select * from registrosfull inner join usuarios on registrosfull.`usuario` = usuarios.`usuario` 
	where usuarios.`tipo` = _tipo 
		and date(registrosfull.fechahora) >= _ini and DATE(registrosfull.fechahora) <= _fin;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_usuario`(IN `usr` VARCHAR(9))
BEGIN
 
 SELECT
  `registrosfull`.`fechahora`      AS `fechahora`,
  `registrosfull`.`tipo`        AS `tipo`,
  `registrosfull`.`modificado`  AS `modificado`
FROM `registrosfull`
     
 WHERE registrosfull.usuario = usr;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_registros_usuario_periodo`(IN `_usr` VARCHAR(10), IN `_desde` DATE, IN `_hasta` DATE)
BEGIN
	SELECT fechahora , tipo , modificado
	FROM `registrosfull`
	     
	WHERE usuario = _usr and date(fechahora) >= _desde and date(fechahora) <= _hasta;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `get_usuarios_para_asistencia`(IN `_fecha` DATE)
BEGIN
		
	if (SELECT COUNT(1) FROM horariousuarios WHERE _fecha < vigencia ) = 0 then 
	
		SELECT horariousuarios.usuario AS usuario,
		usuarios.nombre AS nombre,
		horariousuarios.entrada AS entrada,
		horariousuarios.salida AS salida,
		horariousuarios.diasig AS diasig,
		horariousuarios.vigencia AS vigencia
		FROM horariousuarios inner join usuarios on horariousuarios.usuario = usuarios.usuario
		WHERE vigencia IS NULL AND dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%')
		and usuarios.`Status` = 1;
	
	else
	
		SELECT t.usuario AS usuario,
		usuarios.nombre AS nombre,
		t.entrada AS entrada,
		t.salida AS salida,
		t.diasig AS diasig,
		t.vigencia AS vigencia FROM (
		(SELECT * FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha))
			UNION (SELECT * FROM horariousuarios WHERE usuario NOT IN 
				(SELECT usuario FROM horariousuarios WHERE vigencia IN ( SELECT MIN(vigencia) FROM horariousuarios WHERE  vigencia > _fecha)))
		 ) AS t INNER JOIN usuarios ON t.usuario = usuarios.usuario WHERE t.dias LIKE CONCAT('%',DAYOFWEEK(_fecha),'%')
		 and usuarios.`Status` = 1 order by entrada;
		
	end if;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `insert_justificante_asignatura`(
		in _usr int(8) unsigned, in _justif int(5) unsigned, in _fracc varchar(5), 
		in _fecha date, in _crn int(5) unsigned,in _cmt varchar(512))
BEGIN
	declare _folio integer;
	
	insert into justificantes_folios (usuario,justificante,fraccion) values (_usr,_justif,if(char_length(_fracc)>0,_fracc,null));	
	set _folio = (select folio from justificantes_folios where usuario = _usr and justificante = _justif order by folio desc limit 1);
	
	insert into justificantes_asignaturas (folio,fecha,crn) values (_folio,_fecha,_crn);
	
	if ( char_length(_cmt) > 0 ) then	
		insert into justificantes_comentarios (folio,usuario,comentario,horayfecha) values (_folio,_usr, _cmt,now());
	end if;
	
	select _folio as folio;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `insert_justificante_periodo`(IN _usr INT(8) UNSIGNED, IN _justif INT(5) UNSIGNED, IN _fracc VARCHAR(5), 
        IN _fecha DATE, IN _hasta DATE, IN _cmt VARCHAR(512))
BEGIN    
    declare _folio integer;
    
    insert into justificantes_folios (usuario,justificante,fraccion) values (_usr,_justif,if(char_length(_fracc)>0,_fracc,null));    
    set _folio = (select folio from justificantes_folios where usuario = _usr and justificante = _justif order by folio desc limit 1);
    
    insert into justificantes_periodo (folio,fecha_inicial,fecha_final) values (_folio,_fecha,_hasta);
    
    if ( char_length(_cmt) > 0 ) then   
	INSERT INTO justificantes_comentarios (folio,usuario,comentario,horayfecha) VALUES (_folio,_usr, _cmt,NOW());         
    end if;
    
    select _folio as folio;
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `make_registro_usuario`(IN `usr` VARCHAR(9), IN `tipo` VARCHAR(8))
BEGIN
	declare dt datetime;
	set dt = now();		
	
	insert into registrosfull (usuario,fechahora,tipo) values (usr,dt,tipo);
	
	select fechahora, tipo, modificado from registrosfull where usuario = usr and fechahora = dt;
	
    END$$

CREATE DEFINER=`frank`@`%` PROCEDURE `make_registro_usuario_equipo`(IN `usr` VARCHAR(9), IN `tipo` VARCHAR(8), IN `equipo` VARCHAR(7))
BEGIN
	declare dt datetime;
	set dt = now();		
	insert into registrosfull (usuario,fechahora,tipo, equipo) values (usr,dt,tipo,equipo);
	select fechahora, tipo, modificado from registrosfull where usuario = usr and fechahora = dt;
	
    END$$

--
-- Funciones
--
CREATE DEFINER=`frank`@`%` FUNCTION `CURRENT_BLOQUE`() RETURNS varchar(1) CHARSET utf8
    DETERMINISTIC
BEGIN
		RETURN (
	 SELECT bloque FROM bloques WHERE bloque != 0 AND DATE(NOW()) BETWEEN inicio AND fin
 
 );
    END$$

CREATE DEFINER=`frank`@`%` FUNCTION `CURRENT_CICLO`() RETURNS varchar(1) CHARSET utf8
    DETERMINISTIC
BEGIN
	
	RETURN (SELECT IF ( MONTH(NOW()) <= 5 , 'A','B' ) );	
	
    END$$

CREATE DEFINER=`frank`@`%` FUNCTION `CURRENT_DIA`() RETURNS varchar(9) CHARSET utf8
    DETERMINISTIC
BEGIN
RETURN (
	SELECT 
		CASE 
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 1) THEN 'DOMINGO'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 2) THEN 'LUNES'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 3) THEN 'MARTES'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 4) THEN 'MIERCOLES'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 5) THEN 'JUEVES'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 6) THEN 'VIERNES'
			WHEN (DAYOFWEEK(CURRENT_DATE()) = 7) THEN 'SABADO'
	END
	);
		END$$

CREATE DEFINER=`frank`@`%` FUNCTION `CURRENT_LAPSO`() RETURNS varchar(7) CHARSET utf8
    DETERMINISTIC
BEGIN RETURN ( SELECT checar FROM `horarioscrn_lapsos` ORDER BY `horarioscrn_lapsos`.`fecha_inicial` DESC LIMIT 1 ); END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actualizaciones`
--

CREATE TABLE IF NOT EXISTS `actualizaciones` (
  `usuario` int(8) unsigned NOT NULL,
  `actualizado` datetime DEFAULT NULL,
  `huella` int(10) unsigned DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `actualizaciones`
--

INSERT INTO `actualizaciones` (`usuario`, `actualizado`, `huella`) VALUES
(211770975, '2017-09-15 13:36:00', NULL),
(21092008, '2017-09-15 13:36:00', NULL),
(1111111, '2017-09-15 13:36:31', NULL),
(213021406, '2017-09-28 08:10:13', NULL),
(0, '2017-11-06 08:20:22', NULL),
(0, '2017-11-10 17:33:54', NULL),
(211770975, '2017-12-13 09:05:40', NULL),
(2902095, '2018-01-11 08:35:44', NULL),
(2902095, '2018-01-11 14:03:37', NULL),
(304050452, '2018-01-11 08:12:15', NULL),
(304050452, '2018-01-11 08:10:24', NULL),
(304050452, '2018-01-11 08:10:26', NULL),
(304050452, '2018-01-15 08:12:30', NULL),
(304050452, '2018-01-15 08:12:32', NULL),
(304050452, '2018-01-15 08:12:32', NULL),
(304050452, '2018-01-15 08:12:32', NULL),
(304050452, '2018-01-15 08:12:33', NULL),
(304050452, '2018-01-15 08:12:33', NULL),
(304050452, '2018-01-15 08:10:36', NULL),
(304050452, '2018-01-15 08:28:49', NULL),
(304050452, '2018-01-15 08:29:40', NULL),
(2902095, '2018-02-13 16:44:25', 1),
(2902095, '2018-02-13 16:44:37', 2),
(304050452, '2018-02-13 16:46:44', 3),
(304050452, '2018-02-13 16:46:52', 4),
(9412662, '2018-03-13 15:51:07', NULL),
(9412662, '2018-03-13 15:51:17', NULL),
(2902095, '2018-03-14 16:57:47', NULL),
(2103206, '2018-03-14 17:07:09', NULL),
(2103206, '2018-03-14 17:07:32', NULL),
(2959391, '2018-03-14 17:11:08', NULL),
(2959391, '2018-03-14 17:11:22', NULL),
(2959391, '2018-03-14 17:11:41', NULL),
(2505274, '2018-03-15 09:52:25', NULL),
(2953995, '2018-03-15 09:54:13', NULL),
(2953995, '2018-03-15 09:54:22', NULL),
(2953995, '2018-03-15 09:54:43', NULL),
(2505274, '2018-03-15 09:55:14', NULL),
(2902095, '2018-05-10 16:21:29', 1),
(2902095, '2018-05-10 16:21:29', 2),
(2902095, '2018-05-10 16:21:50', 5),
(2902095, '2018-05-10 16:22:00', 6),
(2948332, '2018-05-25 16:39:20', NULL),
(2948332, '2018-05-25 16:39:54', NULL),
(2948332, '2018-05-25 16:39:58', NULL),
(2948332, '2018-05-25 16:40:00', NULL),
(2958599, '2018-05-25 17:13:09', NULL),
(2958599, '2018-05-25 17:13:19', NULL),
(2958599, '2018-05-25 17:13:36', NULL),
(2948332, '2018-05-25 17:26:21', NULL),
(2704897, '2018-05-28 09:39:51', NULL),
(2704897, '2018-05-28 09:40:07', NULL),
(2704897, '2018-05-28 09:40:08', NULL),
(2704897, '2018-05-28 09:40:08', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:40:09', NULL),
(2704897, '2018-05-28 09:42:00', 7),
(2704897, '2018-05-28 09:42:11', 8),
(2505274, '2018-05-28 09:42:52', NULL),
(2505274, '2018-05-28 09:44:01', NULL),
(2505274, '2018-05-28 09:44:02', NULL),
(2505274, '2018-05-28 09:44:25', 9),
(2505274, '2018-05-28 09:44:35', 10),
(9412662, '2018-05-28 09:45:15', 11),
(9412662, '2018-05-28 09:45:29', 12),
(2103206, '2018-05-28 10:56:03', NULL),
(2103206, '2018-05-28 10:56:33', NULL),
(2103206, '2018-05-28 10:56:33', NULL),
(2103206, '2018-05-28 10:58:21', 13),
(2103206, '2018-05-28 10:58:39', 14),
(211770975, '2018-06-19 12:53:13', NULL),
(213021406, '2018-06-19 17:36:00', NULL),
(2959391, '2018-10-09 18:40:58', NULL),
(9412662, '2018-10-09 18:47:27', NULL),
(2103206, '2018-10-09 18:47:53', NULL),
(2103206, '2018-10-09 18:49:52', NULL),
(2505274, '2018-10-09 18:50:58', NULL),
(2505274, '2018-10-09 18:50:58', NULL),
(2953995, '2018-10-09 18:53:29', NULL),
(2953995, '2018-10-09 18:53:34', NULL),
(2505274, '2018-10-09 18:53:46', NULL),
(9412662, '2018-10-09 18:54:27', NULL),
(2953995, '2018-10-09 18:56:02', NULL),
(304050452, '2018-10-09 18:58:11', NULL),
(304050452, '2018-10-10 17:50:31', 15),
(304050452, '2018-10-10 17:50:43', 16),
(304050452, '2018-10-10 17:52:53', NULL),
(304050452, '2019-08-19 00:57:55', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `administradores`
--

CREATE TABLE IF NOT EXISTS `administradores` (
  `codigo` varchar(10) NOT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `administradores`
--

INSERT INTO `administradores` (`codigo`) VALUES
(''),
('213021406'),
('2902095'),
('2948332');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `bloques`
--

CREATE TABLE IF NOT EXISTS `bloques` (
  `bloque` tinyint(4) NOT NULL,
  `inicio` date NOT NULL,
  `fin` date NOT NULL,
  `anio` year(4) NOT NULL,
  `ciclo` enum('A','B','V') NOT NULL,
  PRIMARY KEY (`bloque`,`anio`,`ciclo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `bloques`
--

INSERT INTO `bloques` (`bloque`, `inicio`, `fin`, `anio`, `ciclo`) VALUES
(0, '2018-01-15', '2018-06-30', 2018, 'A'),
(1, '2018-01-15', '2018-04-12', 2018, 'A'),
(2, '2018-04-12', '2018-06-30', 2018, 'A');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuraciones`
--

CREATE TABLE IF NOT EXISTS `configuraciones` (
  `configuracion` varchar(20) NOT NULL,
  `valor` varbinary(50) NOT NULL,
  PRIMARY KEY (`configuracion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `configuraciones`
--

INSERT INTO `configuraciones` (`configuracion`, `valor`) VALUES
('datatime', '6'),
('decorated', 'true'),
('fullscreen', 'true'),
('hostserver', 'http://148.202.119.37/sica'),
('huellatime', '6'),
('preventaltf4', 'true'),
('preventesc', 'true'),
('servertimeout', '10'),
('sincronizar', 'true'),
('tecladotime', '5');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `correosusuarios`
--

CREATE TABLE IF NOT EXISTS `correosusuarios` (
  `usuario` varchar(10) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `principal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`usuario`,`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `correosusuarios`
--

INSERT INTO `correosusuarios` (`usuario`, `correo`, `principal`) VALUES
('213021406', 'alan.2500gpr@gmail.com', 1),
('2704897', 'christian.ramirez@cusur.udg.mx', 1),
('2902095', 'fernando.cosio@cusur.udg.mx', 1),
('2948332', 'jorge.prieto@cusur.udg.mx', 1),
('2958599', 'karla.esparza@cusur.udg.mx', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crn`
--

CREATE TABLE IF NOT EXISTS `crn` (
  `usuario` int(8) unsigned NOT NULL,
  `materia` varchar(10) NOT NULL DEFAULT '',
  `crn` int(8) unsigned NOT NULL,
  `anio` year(4) NOT NULL,
  `ciclo` enum('A','B') NOT NULL,
  PRIMARY KEY (`usuario`,`materia`,`crn`,`anio`,`ciclo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `crn`
--

INSERT INTO `crn` (`usuario`, `materia`, `crn`, `anio`, `ciclo`) VALUES
(2902095, '45TRE', 102, 2018, 'A'),
(2902095, 'I-123', 45900, 2018, 'B'),
(211770975, '45TR9', 100, 2018, 'A'),
(213021406, 'I-123', 101, 2018, 'A');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `directivos`
--

CREATE TABLE IF NOT EXISTS `directivos` (
  `codigo` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos`
--

CREATE TABLE IF NOT EXISTS `eventos` (
  `tipo` tinyint(3) unsigned NOT NULL,
  `inicio` date NOT NULL,
  `fin` date NOT NULL,
  `asignaturas` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`tipo`,`inicio`,`fin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`tipo`, `inicio`, `fin`, `asignaturas`) VALUES
(1, '2018-06-23', '2018-06-23', 0),
(2, '2018-06-25', '2018-06-29', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos_tipos`
--

CREATE TABLE IF NOT EXISTS `eventos_tipos` (
  `tipo` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) DEFAULT NULL,
  `color` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`tipo`),
  KEY `id` (`tipo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=3 ;

--
-- Volcado de datos para la tabla `eventos_tipos`
--

INSERT INTO `eventos_tipos` (`tipo`, `nombre`, `color`) VALUES
(1, 'PARTIDO DE MEXICO', '77,128,77'),
(2, 'Periodo Vacacional Verano', '204,51,51');

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_crns`
--
CREATE TABLE IF NOT EXISTS `get_crns` (
`crn` int(8) unsigned
,`anio` year(4)
,`ciclo` enum('A','B')
,`codmat` varchar(10)
,`materia` varchar(150)
,`codProf` int(8) unsigned
,`profesor` varchar(200)
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_eventos`
--
CREATE TABLE IF NOT EXISTS `get_eventos` (
`tipo` tinyint(3) unsigned
,`inicio` date
,`fin` date
,`nombre` varchar(50)
,`asignaturas` tinyint(1)
,`color` varchar(15)
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_horario`
--
CREATE TABLE IF NOT EXISTS `get_horario` (
`usuario` int(8) unsigned
,`crn` int(8) unsigned
,`anio` year(4)
,`ciclo` enum('A','B')
,`bloque` tinyint(4)
,`horario` time
,`dia` enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO')
,`aula` varchar(5)
,`materia` varchar(150)
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_horario_crn`
--
CREATE TABLE IF NOT EXISTS `get_horario_crn` (
`usuario` int(8) unsigned
,`nombre` varchar(200)
,`crn` int(8) unsigned
,`anio` year(4)
,`ciclo` enum('A','B')
,`bloque` tinyint(4)
,`inicio` date
,`fin` date
,`horario` time
,`dia` enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO')
,`materia` varchar(150)
,`departamento` varchar(5)
,`aula` varchar(5)
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_horario_para_asistencia`
--
CREATE TABLE IF NOT EXISTS `get_horario_para_asistencia` (
`usuario` int(8) unsigned
,`nombre` varchar(200)
,`departamento` char(3)
,`tipo` varchar(30)
,`crn` int(8) unsigned
,`anio` year(4)
,`horario` time
,`dia` enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO')
,`materia` varchar(150)
,`inicio` date
,`fin` date
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_log`
--
CREATE TABLE IF NOT EXISTS `get_log` (
`usuario` varchar(10)
,`nombre` varchar(200)
,`fecha` datetime
,`descripcion` varbinary(200)
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_some_usuarios`
--
CREATE TABLE IF NOT EXISTS `get_some_usuarios` (
`usuario` int(8) unsigned
,`nombre` varchar(200)
,`codtipo` tinyint(1) unsigned
,`tipo` varchar(30)
,`coddepto` char(3)
,`departamento` varchar(100)
,`status` tinyint(1) unsigned
);
-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `get_usuario`
--
CREATE TABLE IF NOT EXISTS `get_usuario` (
`usuario` int(8) unsigned
,`nombre` varchar(200)
,`tipo` varchar(30)
,`status` varchar(15)
,`departamento` varchar(100)
,`telefono` varchar(20)
,`correo` varchar(100)
);
-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarioscrn`
--

CREATE TABLE IF NOT EXISTS `horarioscrn` (
  `crn` int(8) unsigned NOT NULL,
  `bloque` tinyint(1) unsigned NOT NULL,
  `dia` enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') NOT NULL,
  `hora` time NOT NULL,
  `duracion` time NOT NULL DEFAULT '02:00:00',
  `aula` varchar(5) DEFAULT NULL,
  `anio` year(4) NOT NULL,
  `ciclo` enum('A','B') NOT NULL,
  PRIMARY KEY (`crn`,`bloque`,`dia`,`hora`,`anio`,`ciclo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `horarioscrn`
--

INSERT INTO `horarioscrn` (`crn`, `bloque`, `dia`, `hora`, `duracion`, `aula`, `anio`, `ciclo`) VALUES
(100, 0, 'LUNES', '10:00:00', '02:00:00', 'A12', 2018, 'A'),
(101, 0, 'MARTES', '10:00:00', '02:00:00', 'AAAA', 2018, 'A'),
(102, 0, 'MARTES', '19:00:00', '02:00:00', 'R1', 2018, 'A'),
(45900, 1, 'VIERNES', '18:00:00', '02:00:00', 'R1', 2018, 'B');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarioscrn_hrs`
--

CREATE TABLE IF NOT EXISTS `horarioscrn_hrs` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `duracion` time NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarioscrn_lapsos`
--

CREATE TABLE IF NOT EXISTS `horarioscrn_lapsos` (
  `fecha_inicial` date NOT NULL,
  `checar` enum('entrada','entysal','otro') NOT NULL DEFAULT 'entrada',
  PRIMARY KEY (`fecha_inicial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horariousuarios`
--

CREATE TABLE IF NOT EXISTS `horariousuarios` (
  `usuario` int(8) unsigned NOT NULL,
  `dias` varchar(7) NOT NULL,
  `entrada` time DEFAULT NULL,
  `salida` time DEFAULT NULL,
  `diasig` tinyint(1) DEFAULT '0',
  `vigencia` date NOT NULL,
  PRIMARY KEY (`usuario`,`dias`,`vigencia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `horariousuarios`
--

INSERT INTO `horariousuarios` (`usuario`, `dias`, `entrada`, `salida`, `diasig`, `vigencia`) VALUES
(2103206, '234567', '07:00:00', '15:00:00', 0, '0000-00-00'),
(2505274, '23456', '14:00:00', '22:00:00', 0, '0000-00-00'),
(2505274, '7', '06:00:00', '14:00:00', 0, '0000-00-00'),
(2704897, '23456', '12:30:00', '20:00:00', 0, '0000-00-00'),
(2704897, '23456', '12:30:00', '20:00:00', 0, '2018-05-28'),
(2902095, '24', '08:00:00', '10:00:00', 0, '0000-00-00'),
(2902095, '3', '21:00:00', '22:00:00', 0, '0000-00-00'),
(2948332, '23456', '08:00:00', '16:00:00', 0, '0000-00-00'),
(2958599, '6', '08:00:00', '16:00:00', 0, '0000-00-00'),
(9412662, '23456', '08:00:00', '16:00:00', 0, '0000-00-00'),
(211770975, '35', '08:00:00', '16:00:00', 0, '0000-00-00'),
(211770975, '4', '12:25:00', '13:00:00', 0, '0000-00-00'),
(213021406, '2', '08:00:00', '22:00:00', 0, '0000-00-00'),
(213021406, '34', '08:00:00', '10:00:00', 0, '0000-00-00'),
(213021406, '56', '08:00:00', '16:00:00', 0, '0000-00-00'),
(304050452, '256', '08:00:00', '16:00:00', 0, '0000-00-00'),
(304050452, '3', '08:00:00', '10:00:00', 0, '0000-00-00'),
(304050452, '4', '17:30:00', '22:00:00', 0, '0000-00-00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `huellas`
--

CREATE TABLE IF NOT EXISTS `huellas` (
  `usuario` int(8) unsigned NOT NULL,
  `huella` blob,
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `FK_huellas` (`usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

--
-- Disparadores `huellas`
--
DROP TRIGGER IF EXISTS `huella_deleted`;
DELIMITER //
CREATE TRIGGER `huella_deleted` AFTER DELETE ON `huellas`
 FOR EACH ROW BEGIN 	INSERT INTO actualizaciones (usuario, actualizado, huella) VALUES (Old.usuario , NOW(), Old.id);     END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `huella_insert`;
DELIMITER //
CREATE TRIGGER `huella_insert` AFTER INSERT ON `huellas`
 FOR EACH ROW BEGIN 	INSERT INTO actualizaciones (usuario, actualizado, huella) VALUES (New.usuario , NOW(), New.id);     END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `instancias`
--

CREATE TABLE IF NOT EXISTS `instancias` (
  `codigo` varchar(5) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `jefe` int(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`codigo`),
  KEY `jefe` (`jefe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `instancias`
--

INSERT INTO `instancias` (`codigo`, `nombre`, `jefe`) VALUES
('1', 'RECTORIA', 2948332),
('2', 'CASA DEL ARTE', 2958599),
('4', 'Coordinacion de Tecnologias Para el Aprendizaje', NULL),
('5', 'ESTANCIA INFANTIL', NULL),
('6', 'Secretaría Administrativa', NULL),
('CCI', 'DEPTO. CIENCIAS COMPUTACIONALES E INGENIERIAS', 211770975);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_asignaturas`
--

CREATE TABLE IF NOT EXISTS `justificantes_asignaturas` (
  `folio` int(8) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `crn` int(8) unsigned NOT NULL,
  PRIMARY KEY (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_comentarios`
--

CREATE TABLE IF NOT EXISTS `justificantes_comentarios` (
  `folio` int(8) unsigned zerofill NOT NULL,
  `usuario` int(8) unsigned NOT NULL,
  `comentario` varchar(512) NOT NULL,
  `horayfecha` datetime NOT NULL,
  PRIMARY KEY (`folio`,`usuario`,`horayfecha`),
  KEY `justificantes_comentarios_ibfk2` (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_folios`
--

CREATE TABLE IF NOT EXISTS `justificantes_folios` (
  `folio` int(8) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `fechayhora` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario` int(8) unsigned NOT NULL,
  `justificante` smallint(5) unsigned NOT NULL,
  `fraccion` varchar(5) DEFAULT NULL,
  `aceptado` tinyint(1) DEFAULT NULL,
  `aceptadopor` int(8) unsigned DEFAULT NULL,
  `aprobado` tinyint(1) DEFAULT NULL,
  `aprobadopor` int(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`folio`),
  KEY `usuario` (`usuario`),
  KEY `justificante` (`justificante`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_fracciones`
--

CREATE TABLE IF NOT EXISTS `justificantes_fracciones` (
  `justificante_id` smallint(5) unsigned NOT NULL,
  `fraccion` varchar(5) NOT NULL,
  `categoria` varchar(150) DEFAULT NULL,
  `documentos` tinyint(1) DEFAULT NULL,
  `descripcion` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`justificante_id`,`fraccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `justificantes_fracciones`
--

INSERT INTO `justificantes_fracciones` (`justificante_id`, `fraccion`, `categoria`, `documentos`, `descripcion`) VALUES
(2, 'I', 'I.  Descarga horaria del 50% de la jornada laboral.', 1, 'Hasta 30 autorizaciones de descarga horaria del 50% de la jornada laboral, para quienes realizan estudios de licenciatura.'),
(2, 'II', 'II. licencias con goce de sueldo para estudios de posgrado.', 1, 'Hasta 30 licencias con goce de sueldo para estudios de posgrado en modalidad escolarizada, siempre y cuando no se cuente con beca  de la UdeG o de alguna otra institución.'),
(5, 'I', 'I. Incapacidad o cita médica IMSS', 1, 'Incapacidad o cita médica entregando a la dependencia de adscripción certificado expedido por el IMSS.'),
(5, 'II', 'II. Permiso de cuatro días hábiles, fallecimiento de familiar directo.\r\n', 1, 'Permiso de cuatro días hábiles con goce de salario, cuando fallezca algún familiar directo.'),
(5, 'III', 'III. Permiso de tres días hábiles, enfermedad de los hijos. \r\n', 1, 'Permiso de tres días hábiles con goce de salario, en caso de enfermedad de los hijos menores, presentando constancia del IMSS'),
(5, 'IV', 'IV.  Permiso de tres días hábiles, periodo de adaptación del menor en guarderías.\r\n', 1, 'Permiso de tres días hábiles con goce de salario,  para que las madres o padres acudan al periodo de adaptación del menor en guarderías'),
(5, 'V', 'V.  Permiso de ocho días hábiles, cuando contraigan matrimonio civil.   \r\n', 1, 'Permiso de ocho días hábiles con goce de salario cuando contraigan matrimonio civil'),
(5, 'VI', 'VI.  Permiso económico sin goce, cuando el titular de la dependencia lo  autorice.  \r\n', 0, 'Permiso económico sin goce de sueldo hasta por 8 días, cuando el titular de la dependencia lo  autorice'),
(5, 'VIII', 'VIII.  Licencia de un mes hasta 1 año sin goce de sueldo.\r\n', 0, 'Licencia para dejar de concurrir a sus labores por un término de un mes hasta 1 año sin goce de sueldo'),
(9, 'A', 'A) Personal / Familiar (deberá reponer el tiempo al final de su jornada o en fecha establecida).\r\n', 0, 'Personal / Familiar (deberá reponer el tiempo al final de su jornada o en fecha establecida).\r\n'),
(9, 'B', 'B) Atención médica IMSS (debe de anexar documento probatorio expedido por la misma institución)\r\n', 1, 'Atención médica IMSS (debe de anexar documento probatorio expedido por la misma institución)\r\n'),
(9, 'C', 'C) Trámites UdeG, SutUdeG (anexar documento probatorio expedido por el sindicato).\r\n', 1, 'Trámites UdeG, SutUdeG (anexar documento probatorio expedido por el sindicato).\r\n'),
(9, 'Z', 'D) Otro', 0, 'Motivo no enlistado.'),
(10, 'A', 'A) Personal / Familiar (deberá reponer el tiempo al final de su jornada o en fecha establecida).\r\n', 0, 'Personal / Familiar (deberá reponer el tiempo al final de su jornada o en fecha establecida).\r\n'),
(10, 'B', 'B) Atención médica IMSS (debe de anexar documento probatorio expedido por la misma institución)\r\n', 1, 'Atención médica IMSS (debe de anexar documento probatorio expedido por la misma institución)\r\n'),
(10, 'C', 'C) Trámites UdeG, SutUdeG (anexar documento probatorio expedido por el sindicato).\r\n', 1, 'Trámites UdeG, SutUdeG (anexar documento probatorio expedido por el sindicato).\r\n'),
(10, 'Z', 'D) Otro', 0, 'Motivo no enlistado.'),
(11, 'I', 'I. Por incapacidad o cita médica IMSS.\r\n', 1, 'Por incapacidad o cita médica expedida por el IMSS, la cual deberá ser entregada oportunamente en la dependencia de adscripción.\r\nEl trabajador recibirá su salario íntegro por parte de la UdeG.\r\nCuando la incapacidad coincida en periodo de vacacio'),
(11, 'II', 'II. Permiso de cuatro días hábiles con goce de salario, fallecimiento de familiar directo.\r\n', 0, 'Permiso de cuatro días hábiles con goce de salario cuando fallezca algún familiar directo, entendiéndose como tales a los padres, hermanos, hijos y cónyuge o concubinario, exhibiendo la constancia correspondiente;\r\n'),
(11, 'III', 'III. Permiso de ocho días hábiles con goce de salario, cuando contraigan matrimonio civil.\r\n', 0, 'Permiso de ocho días hábiles con goce de salario cuando contraigan matrimonio civil, exhibiendo la constancia correspondiente;\r\n'),
(11, 'IV', 'IV. Permiso por motivos personales en una o varias ocasiones,15 días al semestre o 30 al año.\r\n', 0, 'Permiso por motivos personales en una o varias ocasiones, pero sin que la suma de los días exceda de 15 al semestre o de 30 al año. Siempre que los intereses académicos no resulten afectados;\r\n'),
(11, 'V', 'V. Permiso económico durante el ciclo escolar, cuando el titular de la dependencia lo autorice.\r\n', 0, 'Permiso económico durante el ciclo escolar, cuando el titular de la dependencia lo autorice en los términos del artículo 147 y 176 del Estatuto General y el artículo 52 del EPA;\r\n'),
(11, 'VI', 'VI. Permiso o licencia para asistir a seminarios, foros, congresos y demás reuniones de carácter académico.\r\n', 0, 'Permiso o licencia para asistir a seminarios, foros, congresos y demás reuniones de carácter académico; hacer investigación, dictar cátedras, cursillos o conferencias en ésta y otras universidades e instituciones de educación superior, conforme a'),
(11, 'VII', 'VII. Licencia sin goce de salario, hasta por un año, a las madres trabajadoras.\r\n', 0, 'Licencia sin goce de salario, hasta por un año, a las madres trabajadoras, con el objeto de que se dediquen a la crianza de sus hijos menores de dos años, y \r\n'),
(11, 'VIII', 'VIII. Los trabajadores académicos tendrán derecho a licencia sin goce de salario hasta por un año.', 0, 'Los trabajadores académicos tendrán derecho a licencia sin goce de salario hasta por un año, posterior a un año efectivo de labores sin ser renovable, exceptuándose lo señalado en el artículo 57 del EPA. \r\nLas licencias a que se refiere esta fr'),
(13, 'A', 'A)  Licencias con goce de sueldo al personal académico que estudien un posgrado del padrón de excelencia del CONACyT;.\r\n', 0, 'Se otorgarán 50 licencias con goce de sueldo al personal académico que estudien un posgrado del padrón de excelencia del CONACyT;'),
(13, 'B', 'B)  Licencia con goce de salario, si no son del padrón de excelencia del CONACYT y se requiere residir fuera del estado de Jalisco.\r\n', 0, 'Hasta 30 licencias con goce de salario, si no son del padrón de excelencia del CONACYT y se requiere residir fuera del estado de Jalisco'),
(13, 'C', 'C)  Hasta 30 licencias con goce de salario si el posgrado es fuera de México.', 0, 'Hasta 30 licencias con goce de salario si el posgrado es fuera de México.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_lista`
--

CREATE TABLE IF NOT EXISTS `justificantes_lista` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) DEFAULT NULL,
  `descripcion_gral` varchar(150) DEFAULT NULL,
  `descripcion` varbinary(250) DEFAULT NULL,
  `documentos` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17 ;

--
-- Volcado de datos para la tabla `justificantes_lista`
--

INSERT INTO `justificantes_lista` (`id`, `nombre`, `descripcion_gral`, `descripcion`, `documentos`) VALUES
(1, 'Cláusula 34 -Estímulo por Puntualidad', 'Estímulos por puntualidad para el personal administrativo sindicalizado. SutUdeG', 'Para los trabajadores que no hagan uso de las tolerancias convenidas en este Contrato dentro del mes calendario, contarán con un día de descanso como estímulo por puntualidad, el cual se otorgará el día que elija el trabajador, dentro del mes in', 1),
(2, 'Cláusula 44 -Licencias para Estudios', 'Licencia para estudios de licenciatura y posgrado para el personal administrativo. SutUdeG', 'La UdeG otorgará licencias con goce de sueldo al personal administrativo sindicalizado que tenga más de 3 años de antigüedad, para realizar estudios de licenciatura o de posgrado.', 1),
(3, 'Cláusula 61 -Cumpleaños del trabajador', 'Se otorga el día por el cumpleaños del  personal administrativo. SutUdeG', 'En los casos que el día del cumpleaños del trabajador coincida con las vacaciones o día de descanso obligatorio, la Universidad otorgará al trabajador otro día con goce de sueldo íntegro\r\n', 0),
(4, 'Cláusula 63 -Incapacidad en Vacaciones', 'Diferimiento de incapacidad cuando coincida en periodo vacacional. SutUdeG', 'Cuando coincida el período de vacaciones con la incapacidad del trabajador, ésta no contará para dicho período, en consecuencia, los trabajadores incapacitados disfrutarán de ellas al terminar su incapacidad.\r\n', 1),
(5, 'Cláusula 65 -Incapacidades, Permisos y Licencias', 'Incapacidades, Permisos y Licencias. SutUdeG', 'Incapacidades, Permisos y Licencias. SutUdeG', 0),
(6, 'Cláusula 66 -Licencia por Maternidad', 'Diferimiento de incapacidad por maternidad. SutUdeG', '"Las mujeres trabajadoras tendrán derecho a disfrutar de un período de noventa días repartidos a su elección antes o después del parto.                                                         \nEn caso de que la trabajadora opte por el período d', 1),
(7, 'Tiempo por tiempo', 'Permiso con goce de sueldo por reponer el tiempo.', 'En los casos en que el trabajador se presente el día de la Prueba de Aptitud Académica, o tiempo extraordinario solicitado por el jefe inmediato, tendrá derecho a un día con goce de sueldo íntegro\r\n', 0),
(8, 'Cursos - Comisión, Congresos, Talleres', 'Permiso por asistir a actividades relacionadas con el área de trabajo.', 'En los casos que el trabajador se le asignen actividades fuera del Centro por su jefe inmediato podrán ser justificados anexando documento probatorio.\r\n', 1),
(9, 'Pase de Entrada', 'Permiso para integrarse a su jornada laboral', '"En caso de que el trabajador haga uso de este los motivos son los siguientes:\na) personal/familiar\nb) Atención médica IMSS (anexar documento)\nc) Trámites UdeG, SutUdeG (anexar documento)"\r\n', 1),
(10, 'Pase de Salida', 'Permiso para retirarse de su jornada laboral', '"En caso de que el trabajador haga uso de este los motivos son los siguientes:\na) personal/familiar\nb) Atención médica IMSS (anexar documento)\nc) Trámites UdeG, SutUdeG (anexar documento)"\r\n', 1),
(11, 'Clausula 37 -Incapacidades, Permisos y Licencias', 'Incapacidades, Permisos y Licencias. StaUdeG', 'Los trabajadores académicos tendrán derecho a disfrutar de licencias y permisos para faltar a sus labores', 1),
(12, 'Clausula 33 -Cumpleaños', 'Se otorga el día por el cumpleaños del  personal académico. StaUdeG', 'Son días de descanso obligatorio con goce de salario: XII. Día del cumpleaños del trabajador académico; cuando éste coincida con domingo, período de vacaciones o en cualquiera de los anteriores, se le diferirá el disfrute de tal día.', 0),
(13, 'Clausula 61 -Licencia con goce de salario', 'Licencia para estudios de posgrado para el personal Académico. StaUdeG', 'La UdeG otorgará licencias con goce de sueldo, a los académicos de carrera con contrato definitivo que tengan más de cinco años de antigüedad, para realizar estudios de Maestrías o Doctorado, siempre y cuando los programas sean en la modalidad ', 0),
(14, 'Tolerancia', 'Registros fuera del tiempo de tolerancia', 'Cuando el trabajador Académico haga sus registros fuera del tiempo de tolerancia (20 minutos antes o después de la hora de entrada a asignatura).\r\n', 0),
(15, 'Otro', 'Motivo no enlistado', 'Describir motivo de la falta', 0),
(16, 'Cambio de horario', 'Movimiento de Programación Académica', 'Cuando al profesor de asignatura se le asigne movimientos en su horario en base a la Programación Académica del ciclo a cursar\r\n', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_periodo`
--

CREATE TABLE IF NOT EXISTS `justificantes_periodo` (
  `folio` int(8) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `fecha_inicial` date NOT NULL,
  `fecha_final` date NOT NULL,
  PRIMARY KEY (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `justificantes_tipousuarios`
--

CREATE TABLE IF NOT EXISTS `justificantes_tipousuarios` (
  `justificante_id` smallint(5) unsigned NOT NULL,
  `tipousuario_id` tinyint(1) unsigned NOT NULL,
  `tipoUs` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`justificante_id`,`tipousuario_id`),
  KEY `tipousuario_id` (`tipousuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `justificantes_tipousuarios`
--

INSERT INTO `justificantes_tipousuarios` (`justificante_id`, `tipousuario_id`, `tipoUs`) VALUES
(1, 3, 'administartivo'),
(1, 7, 'tecnico academico'),
(2, 3, 'administartivo'),
(2, 7, 'tecnico academico'),
(3, 3, 'administartivo'),
(3, 7, 'tecnico academico'),
(4, 3, 'administartivo'),
(4, 7, 'tecnico academico'),
(5, 3, 'Administrativo'),
(6, 3, 'administartivo'),
(7, 3, 'Administrativo'),
(8, 3, 'administartivo'),
(8, 7, 'tecnico academico'),
(9, 3, 'administartivo'),
(9, 7, 'tecnico academico'),
(10, 3, 'Administrativo'),
(10, 7, 'tecnico academico'),
(11, 1, 'asignatura'),
(11, 2, 'profe tiempo completo'),
(11, 4, 'prof medio tiempo'),
(12, 1, 'asignatura'),
(12, 2, 'profe tiempo completo'),
(12, 4, 'prof medio tiempo'),
(13, 1, 'asignatura'),
(13, 2, 'profe tiempo completo'),
(13, 4, 'prof medio tiempo'),
(14, 1, 'asignatura'),
(14, 2, 'prof tiempo completo'),
(14, 4, 'prof medio tiempo'),
(15, 1, 'asignatura'),
(15, 2, 'profe tiempo completo'),
(15, 3, 'administartivo'),
(15, 4, 'prof medio tiempo'),
(15, 5, 'becario'),
(15, 6, 'prestador de servicio'),
(15, 7, 'tecnico academico');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `log`
--

CREATE TABLE IF NOT EXISTS `log` (
  `usuario` varchar(10) DEFAULT NULL,
  `fecha` datetime DEFAULT NULL,
  `descripcion` varbinary(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `log`
--

INSERT INTO `log` (`usuario`, `fecha`, `descripcion`) VALUES
('213021406', '2017-12-13 09:05:40', 'Actualiza los datos del usuario: 211770975'),
('2902095', '2018-01-11 08:12:15', 'Crea nuevo usuario: 304050452, David Cosio'),
('2902095', '2018-01-11 08:10:24', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-11 08:10:26', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:30', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:32', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:32', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:32', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:33', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:12:33', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:10:36', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:28:47', 'A�ade correo: fernando.cosio@cusur.udg.mx, al usuario 304050452'),
('2902095', '2018-01-15 08:28:49', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-01-15 08:29:40', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-03-13 15:51:06', 'Crea nuevo usuario: 9412662, G�MEZ GALINDO ROSA CECILIA'),
('2902095', '2018-03-13 15:51:15', 'A�ade correo: cecy@cusur.udg.mx, al usuario 9412662'),
('2902095', '2018-03-13 15:51:17', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:43', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:46', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:47', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:47', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:47', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:47', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:48', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-13 15:58:48', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-03-14 16:51:24', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:55:48', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:55:54', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:56:07', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:56:19', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:56:46', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:56:54', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:57:45', 'A�ade correo: fernando.cosio@cusur.udg.mx, al usuario 2902095'),
('2902095', '2018-03-14 16:57:47', 'Actualiza los datos del usuario: 2902095'),
('2902095', '2018-03-14 16:58:47', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 16:59:30', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:02:47', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:03:14', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:03:34', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:04:44', 'Crea nuevo usuario: 2103206, GOMEZ GOMEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:07:04', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:07:09', 'Crea nuevo usuario: 2103206, G�MEZ G�MEZ LUZ ERENDIRA'),
('2902095', '2018-03-14 17:07:30', 'A�ade correo: luz.gomez@cusur.udg.mx, al usuario 2103206'),
('2902095', '2018-03-14 17:07:32', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-03-14 17:11:08', 'Crea nuevo usuario: 2959391, ANGULO REYES FELIPE OCTAVIO'),
('2902095', '2018-03-14 17:11:20', 'A�ade correo: Felipe.angulo@cusur.udg.mx, al usuario 2959391'),
('2902095', '2018-03-14 17:11:22', 'Actualiza los datos del usuario: 2959391'),
('2902095', '2018-03-14 17:11:41', 'Actualiza los datos del usuario: 2959391'),
('2902095', '2018-03-15 09:52:25', 'Crea nuevo usuario: 2505274, GUZMAN AVALOS OSCAR'),
('2902095', '2018-03-15 09:54:13', 'Crea nuevo usuario: 2953995, RODRIGUEZ ROMERO OSCAR'),
('2902095', '2018-03-15 09:54:16', 'A�ade correo: Oscar.rodriguez@cusur.udg.mx, al usuario 2953995'),
('2902095', '2018-03-15 09:54:22', 'Actualiza los datos del usuario: 2953995'),
('2902095', '2018-03-15 09:54:43', 'Actualiza los datos del usuario: 2953995'),
('2902095', '2018-03-15 09:55:12', 'A�ade correo: oscar.guzmana@cusur.udg.mx, al usuario 2505274'),
('2902095', '2018-03-15 09:55:13', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-05-25 16:39:20', 'Crea nuevo usuario: 2948332, Jorge Antonio Prieto Becerra'),
('2902095', '2018-05-25 16:39:49', 'A�ade correo: jorge.prieto@cusur.udg.mx, al usuario 2948332'),
('2902095', '2018-05-25 16:39:54', 'Actualiza los datos del usuario: 2948332'),
('2902095', '2018-05-25 16:39:58', 'Actualiza los datos del usuario: 2948332'),
('2902095', '2018-05-25 16:40:00', 'Actualiza los datos del usuario: 2948332'),
('2902095', '2018-05-25 16:42:27', 'A�ade materia: I-123, POR DEFINIR'),
('2902095', '2018-05-25 16:43:32', 'Crea de nuevo crn: 45900, 2902095, I-123, 2018B'),
('2902095', '2018-05-25 16:43:55', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:15', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:16', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:17', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:17', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:18', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:21', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:44:22', 'Inserta horario a crn: 45900, B, VIERNES, 18:00:00'),
('2902095', '2018-05-25 16:47:18', 'A�ade materia: 45TR9, otra'),
('2948332', '2018-05-25 17:13:09', 'Crea nuevo usuario: 2958599, Karla Paola Esparza Tejeda'),
('2948332', '2018-05-25 17:13:17', 'A�ade correo: karla.esparza@cusur.udg.mx, al usuario 2958599'),
('2948332', '2018-05-25 17:13:19', 'Actualiza los datos del usuario: 2958599'),
('2948332', '2018-05-25 17:13:36', 'Actualiza los datos del usuario: 2958599'),
('2948332', '2018-05-25 17:21:31', 'Establece nuevo jefe de instancia: 1,304050452'),
('2948332', '2018-05-25 17:21:33', 'Establece nuevo jefe de instancia: 1,2948332'),
('2948332', '2018-05-25 17:21:44', 'Establece nuevo jefe de instancia: CCI,304050452'),
('2948332', '2018-05-25 17:21:47', 'Establece nuevo jefe de instancia: CARTE,2958599'),
('2948332', '2018-05-25 17:26:21', 'Actualiza los datos del usuario: 2948332'),
('2902095', '2018-05-28 09:39:51', 'Crea nuevo usuario: 2704897, Christian Livier Ramirez Rodriguez'),
('2902095', '2018-05-28 09:40:01', 'A�ade correo: christian.ramirez@cusur.udg.mx, al usuario 2704897'),
('2902095', '2018-05-28 09:40:07', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:08', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:08', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:40:09', 'Actualiza los datos del usuario: 2704897'),
('2902095', '2018-05-28 09:42:52', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-05-28 09:44:01', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-05-28 09:44:02', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-05-28 10:56:03', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-05-28 10:56:10', 'Elimina correo luz.gomez@cusur.udg.mx del usuario 2103206'),
('2902095', '2018-05-28 10:56:31', 'A�ade correo: erendira.gomez@cusur.udg.mx, al usuario 2103206'),
('2902095', '2018-05-28 10:56:33', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-05-28 10:56:33', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-06-19 09:39:58', 'A�ade materia: 45TRE, test1'),
('2948332', '2018-06-19 11:12:10', 'A�ade bloque: 0, 2018A'),
('2948332', '2018-06-19 11:12:10', 'A�ade bloque: 1, 2018A'),
('2948332', '2018-06-19 11:12:10', 'A�ade bloque: 2, 2018A'),
('2948332', '2018-06-19 11:24:06', 'Crea de nuevo crn: 100, 21092008, 45TR9, 2018A'),
('2948332', '2018-06-19 11:24:39', 'Inserta horario a crn: 100, A, LUNES, 10:00:00'),
('2948332', '2018-06-19 11:28:26', 'Crea nuevo evento: 1, 2018-06-23, 2018-06-23'),
('2948332', '2018-06-19 11:28:40', 'Crea nuevo evento: 2, 2018-06-25, 2018-06-29'),
('2948332', '2018-06-19 11:36:59', 'Crea mensaje para usuario: 211770975'),
('2948332', '2018-06-19 11:58:38', 'Actualiza bloque: 0, 2018A'),
('2948332', '2018-06-19 11:58:38', 'Actualiza bloque: 1, 2018A'),
('2948332', '2018-06-19 11:58:38', 'Actualiza bloque: 2, 2018A'),
('2948332', '2018-06-19 11:58:45', 'Actualiza crn: 100, 211770975, 45TR9, 2018A'),
('2948332', '2018-06-19 12:04:44', 'Actualiza materia: 45TR9, OTRA'),
('2948332', '2018-06-19 12:04:44', 'Actualiza materia: 45TR9, OTRA'),
('2948332', '2018-06-19 12:04:46', 'Actualiza materia: 45TRE, TEST1'),
('2948332', '2018-06-19 12:04:46', 'Actualiza materia: 45TRE, TEST1'),
('2948332', '2018-06-19 12:53:12', 'Actualiza los datos del usuario: 211770975'),
('2948332', '2018-06-19 13:00:35', 'Establece nuevo jefe de instancia: CCI,211770975'),
('213021406', '2018-06-19 02:28:30', 'Crea de nuevo crn: 101, 213021406, I-123, 2018A'),
('213021406', '2018-06-19 02:29:10', 'Inserta horario a crn: 101, A, MARTES, 15:00:00'),
('213021406', '2018-06-19 17:36:00', 'Actualiza los datos del usuario: 213021406'),
('213021406', '2018-06-19 17:36:20', 'A�ade correo: alan.2500gpr@gmail.com, al usuario 213021406'),
('213021406', '2018-06-19 17:36:37', 'Elimina horario de crn: 101, A, MARTES, 15:00:00'),
('213021406', '2018-06-19 17:36:47', 'Inserta horario a crn: 101, A, MARTES, 10:00:00'),
('2902095', '2018-06-19 18:40:35', 'Crea de nuevo crn: 102, 2902095, 45TRE, 2018A'),
('2902095', '2018-06-19 18:41:06', 'Inserta horario a crn: 102, A, MARTES, 18:00:00'),
('2902095', '2018-06-19 18:42:45', 'Elimina horario de crn: 102, A, MARTES, 18:00:00'),
('2902095', '2018-06-19 18:43:03', 'Inserta horario a crn: 102, A, MARTES, 19:00:00'),
('2902095', '2018-10-09 18:40:39', 'Elimina correo fernando.cosio@cusur.udg.mx del usuario 304050452'),
('2902095', '2018-10-09 18:40:52', 'Elimina correo Felipe.angulo@cusur.udg.mx del usuario 2959391'),
('2902095', '2018-10-09 18:40:58', 'Actualiza los datos del usuario: 2959391'),
('2902095', '2018-10-09 18:47:24', 'Elimina correo cecy@cusur.udg.mx del usuario 9412662'),
('2902095', '2018-10-09 18:47:27', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-10-09 18:47:50', 'Elimina correo erendira.gomez@cusur.udg.mx del usuario 2103206'),
('2902095', '2018-10-09 18:47:53', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-10-09 18:49:52', 'Actualiza los datos del usuario: 2103206'),
('2902095', '2018-10-09 18:50:56', 'Elimina correo oscar.guzmana@cusur.udg.mx del usuario 2505274'),
('2902095', '2018-10-09 18:50:58', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-10-09 18:50:58', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-10-09 18:53:28', 'Elimina correo Oscar.rodriguez@cusur.udg.mx del usuario 2953995'),
('2902095', '2018-10-09 18:53:29', 'Actualiza los datos del usuario: 2953995'),
('2902095', '2018-10-09 18:53:34', 'Actualiza los datos del usuario: 2953995'),
('2902095', '2018-10-09 18:53:46', 'Actualiza los datos del usuario: 2505274'),
('2902095', '2018-10-09 18:54:27', 'Actualiza los datos del usuario: 9412662'),
('2902095', '2018-10-09 18:56:02', 'Actualiza los datos del usuario: 2953995'),
('2902095', '2018-10-09 18:58:10', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2018-10-10 17:52:53', 'Actualiza los datos del usuario: 304050452'),
('2902095', '2019-08-19 00:57:55', 'Actualiza los datos del usuario: 304050452');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `materias`
--

CREATE TABLE IF NOT EXISTS `materias` (
  `codigo` varchar(10) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `departamento` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`codigo`,`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `materias`
--

INSERT INTO `materias` (`codigo`, `nombre`, `departamento`) VALUES
('45TR9', 'OTRA', 'CCI'),
('45TRE', 'TEST1', 'CCI'),
('I-123', 'POR DEFINIR', 'CCI');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `mensajes`
--

CREATE TABLE IF NOT EXISTS `mensajes` (
  `usuario` int(8) unsigned NOT NULL,
  `mensaje` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `registrosfull`
--

CREATE TABLE IF NOT EXISTS `registrosfull` (
  `usuario` int(8) unsigned NOT NULL,
  `fechahora` datetime NOT NULL,
  `tipo` enum('huella','teclado','justificado') DEFAULT NULL,
  `equipo` varchar(7) DEFAULT NULL,
  `modificado` int(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`usuario`,`fechahora`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `registrosfull`
--

INSERT INTO `registrosfull` (`usuario`, `fechahora`, `tipo`, `equipo`, `modificado`) VALUES
(2103206, '2018-05-28 10:58:54', 'huella', 'dmin-PC', NULL),
(2103206, '2018-05-28 14:36:49', 'huella', 'dmin-PC', NULL),
(2505274, '2018-05-28 08:13:57', 'huella', 'dmin-PC', NULL),
(2902095, '2017-09-15 08:08:19', 'teclado', 'UR-SICA', NULL),
(2902095, '2017-09-15 08:11:51', 'teclado', NULL, NULL),
(2902095, '2017-10-18 08:09:03', 'teclado', NULL, NULL),
(2902095, '2017-10-18 08:13:08', 'teclado', NULL, NULL),
(2902095, '2017-10-18 08:14:33', 'teclado', NULL, NULL),
(2902095, '2017-10-18 12:58:17', 'teclado', NULL, NULL),
(2902095, '2017-10-18 14:46:47', 'teclado', NULL, NULL),
(2902095, '2017-12-13 08:34:08', 'teclado', 'ALANPC', NULL),
(2902095, '2018-01-11 08:08:14', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-11 08:08:15', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-11 08:11:33', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-11 08:12:08', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-11 08:13:47', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:08:14', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:08:28', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:08:30', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:08:31', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:10:05', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:10:15', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:11:43', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-01-15 08:13:41', 'teclado', 'vdaniel', NULL),
(2902095, '2018-02-13 16:42:53', 'teclado', 'vdaniel', NULL),
(2902095, '2018-02-13 16:45:03', 'huella', 'vdaniel', NULL),
(2902095, '2018-02-13 16:47:28', 'huella', 'vdaniel', NULL),
(2902095, '2018-02-13 16:57:38', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-02-13 17:00:56', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-02-13 17:01:28', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-02-13 17:07:16', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-03-13 08:08:43', 'teclado', NULL, NULL),
(2902095, '2018-03-13 09:23:55', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-05-10 16:09:51', 'teclado', 'dmin-PC', NULL),
(2902095, '2018-05-10 16:22:10', 'huella', 'dmin-PC', NULL),
(2902095, '2018-05-10 17:03:50', 'teclado', NULL, NULL),
(2902095, '2018-05-25 10:26:55', 'huella', 'novo-PC', NULL),
(2902095, '2018-05-25 10:32:40', 'huella', 'novo-PC', NULL),
(2902095, '2018-05-25 11:26:25', 'teclado', NULL, NULL),
(2902095, '2018-05-25 16:33:30', 'teclado', 'r-coorp', NULL),
(2902095, '2018-05-28 05:08:16', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-05-28 08:08:15', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-05-28 09:30:38', 'huella', 'dmin-PC', NULL),
(2902095, '2018-05-28 09:33:37', 'huella', 'dmin-PC', NULL),
(2902095, '2018-05-28 11:00:36', 'huella', 'dmin-PC', NULL),
(2902095, '2018-06-19 04:08:36', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 04:11:50', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 04:15:01', 'teclado', 'E1D7AG2', NULL),
(2902095, '2018-06-19 09:20:03', 'teclado', 'E1D7AG2', NULL),
(2902095, '2018-06-19 18:15:37', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 18:15:48', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 18:19:01', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 18:36:17', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-19 18:42:00', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-20 17:29:19', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-06-20 17:30:04', 'teclado', 'UR-SICA', NULL),
(2902095, '2018-10-10 17:35:09', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-10-10 17:35:19', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-10-10 17:48:18', 'huella', 'E1D7AG2', NULL),
(2902095, '2018-10-10 17:48:26', 'huella', 'E1D7AG2', NULL),
(2902095, '2019-01-17 06:41:36', 'teclado', 'UR-SICA', NULL),
(2902095, '2019-01-17 06:51:45', 'teclado', 'E1D7AG2', NULL),
(2902095, '2019-08-19 00:53:32', 'teclado', 'UR-SICA', NULL),
(2902095, '2019-08-19 00:53:56', 'teclado', 'UR-SICA', NULL),
(2902095, '2019-08-19 00:54:11', 'teclado', 'UR-SICA', NULL),
(2902095, '2019-08-19 00:56:12', 'teclado', 'UR-SICA', NULL),
(2902095, '2019-08-19 08:08:24', 'teclado', 'UR-SICA', NULL),
(2948332, '2018-05-25 17:17:20', 'teclado', 'r-coorp', NULL),
(9412662, '2018-05-10 16:20:47', 'teclado', 'dmin-PC', NULL),
(9412662, '2018-05-28 06:29:44', 'teclado', 'dmin-PC', NULL),
(210692008, '2018-01-16 08:41:16', 'teclado', 'TUQT1EM', NULL),
(210692008, '2018-01-16 10:32:08', 'teclado', 'TUQT1EM', NULL),
(210692008, '2018-01-16 10:32:53', 'teclado', 'TUQT1EM', NULL),
(210692008, '2018-02-06 09:01:57', 'teclado', 'TUQT1EM', NULL),
(210692008, '2018-02-06 12:26:07', 'teclado', 'TUQT1EM', NULL),
(211770975, '2017-09-15 08:08:56', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-10-18 09:10:45', 'teclado', NULL, NULL),
(211770975, '2017-11-06 08:12:47', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:13', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:15', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:16', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:17', 'teclado', NULL, NULL),
(211770975, '2017-12-13 08:08:21', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:23', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:26', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:31', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:33', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:08:56', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:09:00', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:09:25', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:09:32', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:09:46', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 08:10:20', 'teclado', 'vdaniel', NULL),
(211770975, '2017-12-13 08:10:57', 'teclado', 'vdaniel', NULL),
(211770975, '2017-12-13 08:11:13', 'teclado', 'ninguno', NULL),
(211770975, '2017-12-13 08:12:08', 'teclado', 'vdaniel', NULL),
(211770975, '2017-12-13 08:15:07', 'teclado', 'E1D7AG2', NULL),
(211770975, '2017-12-13 08:19:53', 'teclado', 'E1D7AG2', NULL),
(211770975, '2017-12-13 08:20:36', 'teclado', 'E1D7AG2', NULL),
(211770975, '2017-12-13 08:23:09', 'teclado', 'vdaniel', NULL),
(211770975, '2017-12-13 08:36:32', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 08:39:46', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 08:39:56', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 08:41:48', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 08:41:58', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 09:00:27', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 09:02:39', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 09:03:07', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 09:09:46', 'teclado', 'ALANPC', NULL),
(211770975, '2017-12-13 16:22:20', 'teclado', 'UR-SICA', NULL),
(211770975, '2017-12-13 16:31:34', 'teclado', NULL, NULL),
(211770975, '2018-01-11 08:08:28', 'teclado', 'UR-SICA', NULL),
(211770975, '2018-01-11 08:08:32', 'teclado', 'UR-SICA', NULL),
(211770975, '2018-01-11 08:09:22', 'teclado', 'UR-SICA', NULL),
(211770975, '2018-01-11 08:09:48', 'teclado', 'ALANPC', NULL),
(211770975, '2018-01-11 08:10:13', 'teclado', 'UR-SICA', NULL),
(211770975, '2018-01-11 08:11:01', 'teclado', 'UR-SICA', NULL),
(211770975, '2018-06-19 11:34:58', 'teclado', 'r-coorp', NULL),
(211770975, '2018-06-19 11:37:40', 'teclado', 'r-coorp', NULL),
(211770975, '2018-06-19 16:08:52', 'teclado', 'SUR-FER', NULL),
(211770975, '2018-06-19 16:12:18', 'teclado', 'SUR-FER', NULL),
(211770975, '2018-06-19 17:34:21', 'huella', NULL, NULL),
(211770975, '2018-06-19 17:34:54', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-09-28 08:08:35', 'teclado', NULL, NULL),
(213021406, '2017-11-03 08:08:09', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-11-03 08:09:15', 'teclado', NULL, NULL),
(213021406, '2017-11-03 08:11:10', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-11-06 08:10:10', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-11-06 08:13:01', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-11-09 17:01:11', 'teclado', 'arioDes', NULL),
(213021406, '2017-11-09 17:01:21', 'teclado', 'arioDes', NULL),
(213021406, '2017-11-09 17:01:40', 'teclado', 'arioDes', NULL),
(213021406, '2017-11-26 16:47:04', 'teclado', NULL, NULL),
(213021406, '2017-12-13 08:08:12', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:15', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:16', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:18', 'teclado', 'ninguno', NULL),
(213021406, '2017-12-13 08:08:20', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:21', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:25', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:34', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:39', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:42', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:52', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:08:53', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:09:07', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:09:31', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:09:35', 'teclado', 'UR-SICA', NULL),
(213021406, '2017-12-13 08:10:30', 'teclado', 'vdaniel', NULL),
(213021406, '2017-12-13 08:11:04', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:11:28', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:20:28', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:20:59', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:21:11', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:21:47', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:22:22', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:22:49', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:23:08', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:28:34', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:28:46', 'teclado', 'ALANPC', NULL),
(213021406, '2017-12-13 08:28:54', 'teclado', 'E1D7AG2', NULL),
(213021406, '2017-12-13 08:31:36', 'teclado', 'ALANPC', NULL),
(213021406, '2017-12-13 08:51:21', 'teclado', 'ALANPC', NULL),
(213021406, '2017-12-13 08:52:32', 'teclado', 'ALANPC', NULL),
(213021406, '2017-12-13 11:30:18', 'teclado', NULL, NULL),
(213021406, '2017-12-13 16:21:26', 'teclado', NULL, NULL),
(213021406, '2018-01-11 08:08:12', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:16', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:17', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:19', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:27', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:33', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:37', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:39', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:08:43', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:09:40', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:10:58', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:11:15', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:12:57', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:13:28', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:14:31', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:15:30', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:15:39', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-01-11 08:16:03', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:16:16', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 08:17:40', 'teclado', 'TUQT1EM', NULL),
(213021406, '2018-01-11 08:22:04', 'teclado', 'TUQT1EM', NULL),
(213021406, '2018-01-11 08:42:15', 'teclado', 'vdaniel', NULL),
(213021406, '2018-01-11 11:26:17', 'teclado', 'ALANPC', NULL),
(213021406, '2018-01-11 11:27:06', 'teclado', 'ALANPC', NULL),
(213021406, '2018-02-07 13:05:59', 'huella', 'TUQT1EM', NULL),
(213021406, '2018-03-14 12:55:34', 'teclado', 'TUQT1EM', NULL),
(213021406, '2018-03-16 14:32:20', 'teclado', 'TUQT1EM', NULL),
(213021406, '2018-05-11 10:28:31', 'teclado', 'TUQT1EM', NULL),
(213021406, '2018-06-19 03:15:15', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 03:17:28', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 03:18:51', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 04:08:16', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-06-19 05:08:28', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 07:08:14', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-06-19 07:08:31', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 15:56:05', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 15:56:30', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 15:59:07', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 15:59:54', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 16:00:16', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 16:05:22', 'teclado', 'SUR-FER', NULL),
(213021406, '2018-06-19 16:06:24', 'teclado', 'SUR-FER', NULL),
(213021406, '2018-06-19 16:08:41', 'teclado', 'SUR-FER', NULL),
(213021406, '2018-06-19 16:22:06', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-06-19 16:23:07', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-06-19 16:23:21', 'teclado', 'UR-SICA', NULL),
(213021406, '2018-06-19 16:24:46', 'huella', NULL, NULL),
(213021406, '2018-06-19 16:57:34', 'teclado', NULL, NULL),
(213021406, '2018-06-19 17:37:18', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 17:37:57', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 17:39:43', 'teclado', 'E1D7AG2', NULL),
(213021406, '2018-06-19 17:43:12', 'teclado', 'E1D7AG2', NULL),
(304050452, '2018-01-11 08:08:20', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-11 08:11:21', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-11 08:13:58', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 08:08:14', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 08:08:53', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 08:10:05', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 08:11:01', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 08:13:26', 'teclado', 'vdaniel', NULL),
(304050452, '2018-01-15 08:28:04', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 09:16:58', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-01-15 14:26:49', 'teclado', 'E1D7AG2', NULL),
(304050452, '2018-02-13 16:45:33', 'teclado', 'vdaniel', NULL),
(304050452, '2018-02-13 16:47:05', 'huella', 'vdaniel', NULL),
(304050452, '2018-02-13 16:47:15', 'huella', 'vdaniel', NULL),
(304050452, '2018-02-13 17:04:39', 'huella', 'E1D7AG2', NULL),
(304050452, '2018-05-28 05:08:16', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-05-28 05:08:35', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-06-19 18:16:01', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-06-19 18:18:19', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-06-19 18:18:50', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-06-19 18:37:38', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-06-20 17:29:31', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-10-09 18:27:10', 'teclado', 'UR-SICA', NULL),
(304050452, '2018-10-10 17:35:38', 'huella', 'E1D7AG2', NULL),
(304050452, '2018-10-10 17:35:47', 'huella', 'E1D7AG2', NULL),
(304050452, '2018-10-10 17:36:47', 'teclado', NULL, NULL),
(304050452, '2018-10-10 17:40:58', 'teclado', NULL, NULL),
(304050452, '2018-10-10 17:51:20', 'huella', 'E1D7AG2', NULL),
(304050452, '2018-10-10 17:51:27', 'huella', 'E1D7AG2', NULL),
(304050452, '2018-10-10 17:53:26', 'huella', 'E1D7AG2', NULL),
(304050452, '2019-01-14 10:34:44', 'teclado', 'UR-SICA', NULL),
(304050452, '2019-01-17 06:41:17', 'teclado', 'UR-SICA', NULL),
(304050452, '2019-01-17 06:48:04', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 06:59:09', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:01:42', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:15:16', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:16:42', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:16:58', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:17:18', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:17:31', 'teclado', 'E1D7AG2', NULL),
(304050452, '2019-01-17 07:18:06', 'teclado', 'E1D7AG2', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `statususuarios`
--

CREATE TABLE IF NOT EXISTS `statususuarios` (
  `status` tinyint(3) unsigned NOT NULL,
  `descripcion` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `statususuarios`
--

INSERT INTO `statususuarios` (`status`, `descripcion`) VALUES
(1, 'Activo'),
(2, 'Inactivo'),
(3, 'Licencia'),
(4, 'Comisionado'),
(5, 'Incapacitado'),
(6, 'Otra');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipousuarios`
--

CREATE TABLE IF NOT EXISTS `tipousuarios` (
  `tipo` tinyint(1) unsigned NOT NULL,
  `descripcion` varchar(30) NOT NULL,
  `orden` tinyint(1) unsigned NOT NULL,
  `jornada` enum('sinjornada','obligatoria','libre') NOT NULL,
  PRIMARY KEY (`tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `tipousuarios`
--

INSERT INTO `tipousuarios` (`tipo`, `descripcion`, `orden`, `jornada`) VALUES
(1, 'Asignatura', 3, 'sinjornada'),
(2, 'Profesor de tiempo completo', 1, 'libre'),
(3, 'Administrativo', 5, 'obligatoria'),
(4, 'Profesor de medio tiempo', 2, 'libre'),
(5, 'Becario', 6, 'obligatoria'),
(6, 'Prestador de servicio social', 7, 'libre'),
(7, 'Técnico académico', 4, 'libre');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE IF NOT EXISTS `usuarios` (
  `usuario` int(8) unsigned NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `departamento` char(3) NOT NULL,
  `tipo` tinyint(1) unsigned NOT NULL,
  `status` tinyint(1) unsigned NOT NULL,
  `pass` varchar(25) NOT NULL DEFAULT 'hola123',
  `telefono` varchar(20) DEFAULT NULL,
  `comentario` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`usuario`),
  KEY `tipo` (`tipo`),
  KEY `departamento` (`departamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`usuario`, `nombre`, `departamento`, `tipo`, `status`, `pass`, `telefono`, `comentario`) VALUES
(1111111, 'STAFF', '1', 1, 1, 'hola123', NULL, NULL),
(2103206, 'GÓMEZ GÓMEZ LUZ ERENDIRA', '2', 3, 1, 'hola123', '3418862136', ''),
(2505274, 'GUZMAN AVALOS OSCAR', '2', 3, 1, 'hola123', '3411270218', ''),
(2704897, 'Christian Livier Ramirez Rodriguez', '1', 3, 1, 'hola123', '3411122776', '			'),
(2902095, 'COSIO FERNANDO', '1', 2, 1, 'hola123', '', ''),
(2948332, 'Jorge Antonio Prieto Becerra', '1', 3, 1, 'hola123', '3333934777', ''),
(2953995, 'RODRIGUEZ ROMERO OSCAR', '2', 3, 1, 'hola123', '33327115244', ''),
(2958599, 'Karla Paola Esparza Tejeda', '1', 3, 1, 'hola123', '3331538735', ''),
(2959391, 'ANGULO REYES FELIPE OCTAVIO', '2', 3, 1, 'hola123', '5554313162', ''),
(9412662, 'GÓMEZ GALINDO ROSA CECILIA', '2', 3, 1, 'hola123', '3411030419', ''),
(21092008, 'CHIPOL CESAR', 'CCI', 1, 1, 'hola123', NULL, NULL),
(211770975, 'RAMIREZ DIEGO', 'CCI', 3, 1, 'hola123', '', ''),
(213021406, 'PARRA ROBLEDO ALAN GILBERTO', 'CCI', 2, 1, 'hola123', '', ''),
(304050452, 'David Cosio', '5', 3, 1, 'hola123', '', '');

--
-- Disparadores `usuarios`
--
DROP TRIGGER IF EXISTS `user_insert`;
DELIMITER //
CREATE TRIGGER `user_insert` AFTER INSERT ON `usuarios`
 FOR EACH ROW BEGIN 	INSERT INTO actualizaciones (usuario, actualizado) VALUES (New.usuario , NOW());     END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `user_update`;
DELIMITER //
CREATE TRIGGER `user_update` AFTER UPDATE ON `usuarios`
 FOR EACH ROW BEGIN 	insert into actualizaciones (usuario, actualizado) values (Old.usuario , now());     END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios_huellas`
--

CREATE TABLE IF NOT EXISTS `usuarios_huellas` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `usuario` int(8) unsigned NOT NULL,
  `huella` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17 ;

--
-- Volcado de datos para la tabla `usuarios_huellas`
--

INSERT INTO `usuarios_huellas` (`id`, `usuario`, `huella`) VALUES
(3, 304050452, 0x00f80601c82ae3735cc0413709ab71f0fb145592ee57b2078968866875ea87cad816072ea91160b6a2dc98caa72e43d03b90b61c7e0ab56f02f589cbf2738c6d6167a7b769e19a2d0ab0a90a3b00bf201b6d1648c2c63ed33a0ac4858c1400ca5926c0a499dbec282a2b085a20184d2323136f5e81ee45c0e645c1f8cf6d891133baad7f0977ee783526a6ca8ca8a1473d0c3bd6cf128e3f1e93616ea39d986203838b2b9b4e3cb68d71fe5e7868923a05c81aef3cb3d753795bc45b2834fa8c2cb9c0b993805aa4948ccfd61195ab467bea9c9cac26f8f735e7127b57d725c639298aac530086bd4cd292d2e9e50988c35c37bebb22c0d93e68fab58f8ece2d06f8e9433d0b90b641366f00f81c01c82ae3735cc0413709ab71b0f514559276eedb8ad5144b103a42081b39e352a33245ebb932fe73121ae31b6b8f89d1e95d2306a97c5f1e96c7108d835b6f501a8b712ebb86c950998b286c192d8a8b1ec831eb15440a1e22ac874fdde36563c8293892f9ec82e4e7444ebfba3f9e5c4a7bdb0bb6aa48410f2108093c879d7f169fe84428937b98d50135b80cd4eec91291257afbcb015fbb1a5c7cab24dd593dc72ba9e146af51577eb21462e5a38228ce19f9b5c7437b5bef8d3af5dc59f264fe287824201b5d5ee32a36ef2f6edc96b6de0746d7d5b047417c4b12027379fd996e76d39e22e50d88db579952ee98b61a9be4028f650d7916bfa807850b9a784292f72df88985eaa830322042c8ddc79fdc126fd67e9567d59d7d8f6f00f80d01c82ae3735cc0413709ab7170e1145592a1ff9f1a33b885547216f7419d971c56d8f73920aa3601d2e5b44ec529217e2f69636917fdda21ea366b02cc1df0582b8ecbafe9e6b0f84917429ec413e14837e184e06f739260eff78f680acd329b70e76fd99c8b90c4c5038c71731fc1c82ab4c5ad9babe2a527c58d9e735bb6de6e52ec6d69a0eb5e56246701b1fc298a126d18d6070f40a39795047954a12d00edaaf9362148526513da51736251bd8c41d3c67fd9b871b11372248eac6a9d33c88ad9c4973f6ce67e147e55ed8cd603b77ed2e8693fea9f7a275749ac94c16905280fda66ebce956954ec43940747ea4880c2964aa752e433c2156235ac0879ad23d46d6cac9f6fdd267c7eda486f00e81b01c82ae3735cc0413709ab7130fc1455929e6caa073d32fe1b2de279868584a15f2122d2cbd57dfa0dde8fe168184de931c6bbe2c4ec8865d4677828b85d70b82ab658a93963b26a9bc20b9b5234706332ce62a64d89ff1bf77e052fcb8dc01b89a277eee4aeb2cf2b7b76f559c1e553073099d1ea3440b6ecbb49746a6fd218f47474ceae70faacc2b3648e857ae44afeb333e967e50d0f67b6e2ad99deee1dabe66a401a59552a316d7a5291a15d1144a697ad7e46f74729927f6c8b9418cb9fa82e42fca442e965abdfa89c9cef11435f139666d2c506ab0f6ea4242e3b650bfe396e970f74a400eb154eeefb139f6c7af02e43045172434e77913e17acb643eba8774d822866d600dc7a6581d539db1f4006800d3167984d95776f00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(4, 304050452, 0x00f84401c82ae3735cc0413709ab71b0fe1455928c11974719049d0dbe3390d0ec5d1d88732178bc948b4c152a1f2f2bb81bde9eecd8fba177642ea580c94256610cf667814e63604dacd62a5e011fc7e9ef26c8ba8f1c5031a7a71ef855ebde04216c8ee6d7010c1dc129aac3779f71b5becb9e0819448a1460873b0002c7c02a97b3d61cf21512b14a4ba0ccd109259fc635bb80be9763f5c16c790d21ad48d0c54e38a0085cde8ee3bfb090bb01ab374f4073031f41adc84ae7687297374c1e289e50d6ceaff2bad2bd3c7fa493f28c9a05623bc874c4c936890c46b8455f11eb320b69750222f975b5001c6ed49b501055825db9eeb2852de55eb2f1fd1b9d2208cb77d4bd8c984cd750dcaf16206d85958b3d9f3ede1b31132d80c239d009e95a3f949ed60d64672ad1af70bf4e7294de6ef64832e27eeada877746adc5381413fd48d9f6d46f00f83801c82ae3735cc0413709ab71f0e21455927d79ad4924bce247161bcc7bd572ddb4c708c7ab403a04b2cf87f81ef502bf8477e3c197d043ae8c5bfc012360c9bdab4ea6f2a1e716428cc81ea27371f7b757faff61ff6bd4fdb707659c1c0a94474f21fe9609eed74c7d530662fd0cf7b8cd30e6b1f5fdc0841cb34dec3b57c4dc98cc1a5a6b2f6531cc1ed400d90031f7cdd2b31108d2e3e4d9b5e9f2ab3c50d8a0d6b971b66662fd9696f4ae1f5d44fce6e07952a43d8e7a4c08713a486c32bf3a75531272bd5979c9b67891db3381752c31dca10c2111a84154c2f4c6bb3cdb698cf992b83f6a4ef1d022244b47aabf29847b23b81eb0d19c6ae098d6637f7a16d24b74c71860e2897290991bba794d5e87c6d6924098d7eff32386be480f167860fed2fb99b682e4082538d9579b1c24e7e47e9395632ccd6f00f81801c82ae3735cc0413709ab7130fd145592fd72c026327e1519231ac0bc03fed01f6090176a78c3ccc4b54bd36607f35f36614b06facdb425aff1c668c2250838d3a658542ff605eaaf2ba3f6f660af99548e14c873d6896b43a6c62ad93021ab49459d06c43aa6291e9b89ed9d628718619909486184a67da655738d8b6cad9745a4117aa830d53e59aee46df109f75081de178bec8beb79103e9152ca4eafb4a134238ff460267e3cd028baf8eda3b90775bf93fb6e137d123fd5105ea3fc32f6873f2df75d0b155e3b59694fdadd9ce5600d630ca9df386e5d02d8f6b8f2e9174debcde1110b52832594fa2cd7e1d0d014a48f45570c060983444099cc71635c03358d36b437c73ca96264520511ab7204ff954f3b1552c36f00e83b01c82ae3735cc0413709ab7130ee145592daab4ec9ce4a8c423b1f5157d7e0f7f39a0a98e21945caf1b3631ca1a692db0372dec86f79730019aafb81fdb065dc3966198859e7ff1b09885420b6db710d8c0b1377c5a1afef1cd2a019d39ebaddf6b5b301235ab65a5506bd4cbc7989de1eb23541058e519f28bf6617e885750201d795bd6c0b8d26a38b735777e276570825fe3ae7751f650281163a3d9e0c1b9e115976f20c1fce0afc3a913a5abf6ec2138f6dd9cb28c394c0e3f828d57079d228ed4948105b50ffd2ea1d0accce89b0759b090abd2bae1b369e16d92d37fe07ababf5a698264734c2c3d28a5b3a07c48382cd3932bbbf7c592a69698ebf6264afc8f164bb1a3f97e055796e1a463d73493259f9a7a504e2524fbee1c14ee3fb02f95aaad86c6c70a5b7325459c2ccd8a0ab836b8ed050cb658c3b6f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(5, 2902095, 0x00f83701c82ae3735cc0413709ab71b0891455920fa7a3c1bc9167288f2222d5ca954020e39ccefb72dbd1dbf46ff6b919084a8198b7f0da0902172b25c70822f85493394070c585fac011bc8eb40640415ecdf9bb8c2d34fe2587fc85d7f64bad8ad832ee70aa6a452e6f2d8e54f071616f7747eab7665094632e2a809317852a7e28445a8199d7fd773b0d40316973a32ef2dab29249d73653b109d04b868545fe63e92b341a920031067591934595895ec67e29419ba08a257a5ef5b1ad9a371767d9d33c15e5ba63e92ac16ccb8ff548ccc2fdc3c5524ee3cef90379de215bb0210a63bb6a3a05c1a2708c63671d70e9463ea3c5e502cbf7235124e2777d518f5b61bd46e3dbaab99d7a55616a5de5517ceff99af55436de9a49fb14dd7553ad8c282dfff964b899ffcedc9c6fa9d348b4f6750ebfefe5dec06f00f85f01c82ae3735cc0413709ab717084145592e39a0e987420ed129d9fd188b71dcb767c9dbe9b5064488af2662df45cbae5bca3ee2814de13a0cbc967fe18dc12ad1d21451f9ff50899f5ccf993db1346cd95934671943da90aff19d23d5bcd59922dc1a00ff0cca5340f0c3d141c287323674571519497498830233b83c8a9c50bf814335fba002f50cef6aee1c3db9c022060bcd110e458e8412d5679e3ac41e195bd7af1d63bb33573af554446ea50ef821931a188eb994233635c597fb31d4da097f67a078360c6e48a592cedfff371e42339d9d7e752b12b39d5c0793947bfaf48caf276935dc601cde01e67fe88fff510e5d0e8ba15c775718b331a7c47b56b270ec11d475eea9042938bcb3753c76f6eb3cf87e0a557ffc6bd03fa9b72d2b3cf05427a89507e66990fd308bf4fa54e8f6f2c96188417b0cb3654eaaab57b7683afaf66f5b238badcb6bfbc3e46967f11e54d784ad000d62cc4d2d2ef43126f00f87e01c82ae3735cc0413709ab71309e14559243f16c58b5a3ee539eb937709fcbd0a3ef9cdf488c940a2a1b77d46f6953b83fd535d7ee1cb4a81b9f719b8619bcf394cd479e53b0604d859508626f39ac55a4f9a2879bbd274d4f08011b02dc1e2bf48c972c250b3b3bdad24791607d58958372c9022db1e2b375afd075cbb627b7ceeb12f315378ac5abd04fa1638d8a90abdca5f2846c173f3adb3df07b4b32d975e39fa642b82dfc4c46cfe2b425fd8331a98d1d793f1e8c7654f400e7f4ff3ad06ffb6f06eec875d67b9b98f21b956ffd8f0cc56683486844264e69e0ce9d2695a91933e746121669f57f217bb8c30b759b307ae026c38274a5973260dd3cdc2c1ed89834f721ffa6ad9bea14d2a62ef2916b37469bdeb574ab165d57fad8a06c5be3f81def59dc4f8553c06f34d0940e4232ffcd167ceb59b2ec0c46afbd33dfe27dbaa37bb60f529fed014a4b9849d2c5a1b789d9bf468c3ddf7ed49f4d92aafbb00a2d7fc7167371a289c8cf533e9360514e02bd048561fbb3b462b9896f00e83101c82ae3735cc0413709ab71b08e145592533f7a25a9df76857186b90a5908391dea04d68047416c045a47f6562e5accd9467547b38a8d8f910b47b61e78813de7ccd82a1c7e63147d02eb08f4f5e389cff188e493980aa7434b080ed0778510ae6e6c190827e36ae91cc7d8cf4deec103f353f7eaa6ba101549b653fa420a398586fc3c3680c35a042c000d847bfb315b7abc8c01b93ffc07800c890d09cb5ddc63ab79f3a590981344d36917d64273a50311bffbd5fb611d39e5f3ad80ec04a4a236ee641661f888e0a08845203183aff7ae6f35680226e0088bbfb5b9b3d4e02359beb2a0a8d295257f4045f6a2de02e8fb60f667d15ada5fbf77e5c1b63250091b81c4ab8ee4141d67f9909bb2c05b8ef7153d69bb93a4daff69802727f66064885ab2d7f8ad9fac67e729a19d61addb6f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(6, 2902095, 0x00f83301c82ae3735cc0413709ab7170f8145592a7aaf0983cb14014db99d3380aacacad666698394cbf403c3e0db55d804ec5f712ae0121d9a51c5fbfe95891a5eb44996edef58ab0ff62b5fb0c3482d99412d001398a0ab30fd7eb3878bc94d4afc436e3e4ffe047c4b868ebad21d5b4b3c6ddedcc576cd9af3c8c7f2ba6d9e0fe676d8796c22a8193942c20d320adc7b048115998cf7361909976dad92af4d4f03d1847b843d87cc3a65b702407e9293c6d35687f3ec9b0a83fde02a7f0d7149fb583dfe04436edcd4de39c768a3a95218adc009b2f9dd4df77f15f384a12f50866bd9987182a2011a9dfb2f1b4c53396db378b3f250274c382c5e805a57de64a2062794319a1b08efaf6e8397f8be5b2008868dbaaefa3e1431848333b7237d435417af4bc839287152cd72fd99837d72715bc18a96f00f85901c82ae3735cc0413709ab71f0f8145592b278a0eb33020c67780eeb6f054fc3c6084d71b88bba4805fed8c0b87e5df5e6e46794e9afee3c35e58adbc176c1e498d5a2a0195ce515c3530f2be07951e57106ecdce5259c78cf64da7cbe01b60f467fd1c37823f9de74a2cd624fbdf1109ad88684df801d15086acd2e589782e9a3c3745510101c973f83f849c27645b0bb95e2ce156b821b051d1369d5a48731baba4f79fea72c5d42d9a4b335ba5ec9a4794e024ae73440441c304712ef16104f3dcf99fea41faeb0009a5d92fd37cab1c1fd3ac9d0910448703db2933e19c5babf56e6f69aed6410023aadf5fd03eede586ef3fc45e401399fe9f88cd2b0665e914294d28f342b3fa213b4bd71ab307f8b50ab45c4b5855e2a84468028e1ad4cacc5e863b6e06c55c9a071de3b306fba51ddefbc7fa750aa53bbdb455370c4300f7dfe787e063c4dd44cf61c38bcc9528264d1c712d62c23846f00f84601c82ae3735cc0413709ab7170fb145592e771942004eeae898f52818458a99b631859c72b25171ab04b79598394c763f3708e9f9377ccd309594d8a0bcc5ef022ecbfe7b2c76e0e5231bb205bec2b3eb1888ddc57f04b7a1c3038ff2cbbbf9a10f342ebe954273d281218f512f3e806a28a5fa995e06b7953ecc33487721086288ec8801cb1fe6a958ac3d0d6777978d1212eedc01b870818bcec2eea2fe9502981545b780fe49eba6093965d394342c8762cc334b1e69813a4184cf65a840e2eb4ccc0956c81c8e1e2141a4f4038a3aaf23bb50f5ed7aa870ba6a0772cadd73ef7cbef55af07a08c84a522b12c7b807a56bbe97b2db5cf0843bde63ffa507bb244b15aca7386b75c8e2f3021383f8dd2240c343b416e7a8836d24cb5a1c582f3857fa1fd1e695dfc9acba0af64fb09d40149e00b4bb963e7b19af4c01bc30ca68871abb6c4a06f00e83901c82ae3735cc0413709ab7170fe1455925d0cefe2a611c7fa60409369e27c71597f5c1a7895a074a881ad09f1aa62209c3ae8424f0e7352077a273d351caee8f59f130bb4f8ac800ef95155d043b1067ced60d5c24a5c0d8ef9d35fe6aeb74ddf9f671f21ae343331e2739311aa75f694ab4e0cd5e46ee68ac0b0ea9842c2a352652b1d96eb620f9331d2636f810423ce0328e0057fcee5f92d31b2e788e7247f0ebf4628c4c6e6fd51027cddf6ab67851385d76fca21820c2cc3cacaef608adbb062a46753e767608bcfaa4b40118ad755dc441a63e09dac8ff4b09fed0b1f19f1afa58e16c776e5c160205166221b91f6ecdef36b7433dd909d4700eb4744ce26a51cfc291fa7ce57d0fcb691bf82ebdba1bdd1fb492ac6b3e076425ba74532682c837c22c9894b43b2c5fc8e67dfd17c726535ab341dcfe86f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(7, 2704897, 0x00f8fc00c82ae3735cc0413709ab71f0e6145592b73632cd235fd3ee43edf48745ed295b6890bc5d4dd3577e8fe7707c9891f18ab208889282eb2ce7523dbb261f5dbc52b8360fe99841dffce19ce2dd7179e462fd3b56158dea5cb1e9614108fcf3ca56dd654735c91049777eca405fe40fdddf8e66a71317a0410e08d8c99e531e6be487bea6de914ce208c172c855ffdbe31296cabab7a6fc650771a6dd9e903c168a022a1dc8f20c25df47aef0ebd92b42347a29ca962ff74ccfc336679aded0ae0b22d35778d8dd234a2c0fafca420788b1e4044a7bd0c86995cbaf21b540940e0ac5a06455eaf4adadd4aafa4d02d06c7a21057d5459e6a9899cfee5ba6f00f80e01c82ae3735cc0413709ab71f0e41455920d627fb79aba876c3f30230f2714dd1eb3400bcd5401f3c71c6c8558fe8c86c91d23836356ccbd5fbad8b6f67ae28e66aa85ba5c905e6437014d8f85b8f55a564ec001476807473a12aac369747a4ec540b6da6700caf0e4d3324ca44938ac94b7f85060f314348b46e50f437a1962894781d176cd504f8fda3f90bf00eb89567f8c88db41cffc97ff12047fa9e6c48a16bf5edfae7c705d445e481630d5a0bf23cefee055f525dac5f9d65eb3847b45bb5495a3ab12cf265db1e2bf62367fc617e409d34d5fb0c96ea111baf1e58a709e9a866a34cd6c41c85da71342491713016b7e0f04290cc713fb1a8f2e77afc2d654afe13cc1e6b1bd7535dc850f6f00f80601c82ae3735cc0413709ab7170ec145592899480feaf46e525a2b9f62a8a862ad7342c0a41a33a1ffb94849272e7ef6e5e02502ef2e178fdee711e83cbfb4023850ce64f5f26bf3133fbf845f0eecc3a9b155013048a3178792fe22f2386cb977e1a29513332ef2392a7359a3ee3944da426f38195bbfc12b43c030e1fcfa5f89e25495d32ed42bfc82e8d054275a283f674e47ec2786baa7075d4634191e2733696ef13899a246ef8a35cf2560f3e8def748a211f979322a6110cc640fff45e28a0d0ab69c937105898f52d7277b573910d8e14d8cde8a0f4ecf79e0e4fa452dd76f0c5700fafe1e96bfd89161fad1f384e2e5e2bb7b56750225684884579601c1ba0c5aa873c6f00e80601c82ae3735cc0413709ab71b0e514559289f688aac82cf50b3c4f896ff6b8154af803f5850bc5d6c969974947626077d281c8dbb7c82f2812f35367a5356f4ac0668889fe64624fd0935460ea9bd383dd46c2dfe2760ad0d27a33908bbb95fe43a6daf0cfb694d763c7ec1159faecadf8ea3873b4a25840abfcf330a6feef16a26a0b4f4404b7f778eb838734534e42aa03ae187d449aa489752407439709aac11a7c3c20c1f736928f8aacef0080a1544d6da2089a74e9519e03916be18e6f1bd3dddafa4e4d26ca74616e8c6cb3c856e7370d1512e24ca549184e47ac19ce2385eedc8e7467d0060371dc8969fb2f2901921d4979a85113dfbfe3d474fa8ec38f4c08fd94b96f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(8, 2704897, 0x00f85b01c82ae3735cc0413709ab71b080145592274c01709b0f8810da5cbfc15e80056f315a2feec8aef8194bdec2f467df21dbd78368a5192b3f6b9668745aa86e1106061a17f3cd125e4bc5c2a4de84e8a4ae7a8cc6d6760b9276b3fb340467009a12c601bca3c3ab3ec03c9c5eaabf435d26fc7284dd71bf1c414d6b8a319af4feb5eff58a769b454082b67695dde7910e195e14b8f5e5dc92b935c3e297e54485e3d8aeefea31e86df80511d7207665e69df84256185262c327daa19588bbe277a6f5f71a68a0ecd237c099a59529e1f80d0499a599133db6815b487421392e6deec5abb6adb1dc2b4561ccec32bfa3cbfd3278891bc1752f1cedff800e336f3835e1cafd520bf997ce9394647c7333d88129637ac182c1dda94621da753695abcf48d2544bbbd46b1d294e6a0fb6210c81646db8ab1ba440e6d788fc667d71040c927945abdcd5bb069b9c872a4a27efb1622cd5822bbf324b65171d6f00f85601c82ae3735cc0413709ab71b0831455925799a3b71d15acf155b2ebc3f5d7e4143d83f811e9e942726b38febe23abdabb4451d609557d16b2bb96c5723b3370dc881bcf1b30c33c122f09c394522cd7de918af5ffa60443e91f8943f73d9ce53edbca15a9bb4e4fdd0af96fa570ee1db32188fd8a6a7ee3868662b0e6983f37d2ee3161006b5b2226be2ba8c83f7c6c05fb611ca1b63b1e017ebd290b24df0acb35c51d03a19abbcc6c436148b9111d561ba314ec646bd48bba3c70fe32bd85a97a8c051a80b86957478b9c04a726e44def507e761436bfad1fe43b1578fefd2e821a6736529ee743505885a4d6ef42458999ce9991beffaf4aeb4104cc7102362371aef0dc0f16594f5552d7185720b8610aa12e836704917c83249a1fa2ad03ccd51991b96246b66a062f05a40d573b16c457a89b2c3b1aec32a3def5b9efad5c75322c201c9b8180542b20f2a47a1f88943ae4da0d6f00f84701c82ae3735cc0413709ab717081145592bc46d23674c08d1d6156b795722a259e46106d1b9cd6dd3537df71ab181069e3cbacd4aeaa9951e5529e3adaaaf9f98cfb973a80b816b2e7ee6acea771c42601961952d0cdd8a0e193be4d3f6f5056230009fa8448dcfad4a6fb17708216ee1e38b9479e90ecc73dd5721ab94afc914fa63e5fac6dbd77ff72093621097bd0c6c1e0b7d1acc5e17529e90e3f4f43bc403fb74140c7d3e15c63f9c5479dff741cbee3517edbbeb56d6ffa3ed3945549aa4b26fad234c8194c9927bf90e9fe41a408fdbf4f80a10d545bb7dc4e12bcdee60b20a5ed3103ead0aa793974700cc8d601f56e1d3d3ab6c372bae286986999eb93328bdc6abd07acb9a99f52e4f2dcdaae6b3d32d01a564773f297bbac62dd31e9c182c28784ec13991de11795519c64b74410909cce81ff21712aff8b328f5a651a48a1dbbdda6f00e85901c82ae3735cc0413709ab71f08514559257d8e31d4834855368464b8aa6b27b5d3d12d4d5844aabcb6fd2299d79cc444bc2cb27a84fb9ef5d1e05b89a87f6d5e7b96e1418b72fe0defbbb9d86cc0e60f68b2b3230dbc48483ee5a7dd16a92aaa8d440044f34ff9edcad893365e032e249931cbcff7f9fad952194c099893653f4603298c35a72cb72ca04c74fbd649846bfe8eb8ac35a774c3e146089bf5a216f92c8a19f367c25eab164bde8081cd906a8f362cb78080ef191b7c74ca2be6efe9c8df4c01263c12c0658a04baf6a343d61f4fee8bb18c4c383cbde16fca9d8d8cf3a3e6ed4e9a993ec5c8bb805cce7bdfa3332db305f8abf1c8110c5d2a7b858cb3b2164d5ace74466e650be6e2cd15d1cf6e73843b7301e20d957de01f1660e9ae308ccd8016ef0f5e2f92d1ae6dcac921659891946744c07025e32d3dea91cd0e88d354df1e8494d12dcb62d5674c9a289a15d63b07cba576f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(9, 2505274, 0x00f84d01c82ae3735cc0413709ab71f0981455923ee5ee301bb4819ae53bcf8be951d0bd8fb87b4387530346cc5c8b67e6403da6768f4506240d284929381e40b6d9d5965d227d22f08f096b439d6acf0e4eb10645b4613936ff7216367bf8538b8a22404230d968c927001ac2561cc717f7e06e077f0cdde9b3ed22f7a44ef55382deddf7342297da332b05090f0c045ec2a3b0fe64845d74c4865f91f46e62e3cf9084feaa7157882a8f58411433d1fd2839884f85a8b7207e6ad693593ca55dfc14da88e26a52c00d3d63927158d678f0dd755f961aaaba81d0a0881f3165517662d4263098cd4f08dc991627cbdc58cfd97ac6a4acb1b31d5b6885d7443bf4d148753b378014339857a35a4188c42c06772077f4e792944c3c5aeefadbcafebbeb384fa4297271bc68196c271e0bee8271ce53b8f8b6fa7a051ae509549aa92d6539772e9099e468c663de634a43226f00f84401c82ae3735cc0413709ab71b099145592dc4249c9e3efd2bb8f5078caa0a5bc91d9d48c87493408826c4ac7cf5077286f9d63febe9836f0b4c24c9da529ee08a3af0f77db26eab00e103a5e895e40d8448b7c735da6bf5a5760efdb003e8ad4d93b203f6ce9123a4d8cf87bd71e58286293d6a202bcef5be2e1c232ac0d447edbb4d168a215b029734066a2b70c3b340cbe7c1e25d5b51153795a9780a914f14dec6d6352fd7a4c9df20c8b2a1d8bbf9b1facf8932502e6906bfd9311734f388608b669f202471dbf04b1aa3a533b36f3ae1fb50eeb0e843a8bf12f23147c7add63b6ff097503ff2f2e5ccba350049eb9f4a8ae09daa4dc9574a3fd25ba5c8fbdfe5d30357e6cd210a42d77cbd630a6ca01c96e1e2f7955ab3a0abb0ff735056d514d82bf6d4e2eb9a0ad32f0e0af0ce2f165b0817f05bf25388e4604d8ac401d4753f3086f00f84601c82ae3735cc0413709ab717084145592e4850b868e24327fc0bcd9ba17cdcf2e687598e156120bd88aa6284c2f5c6d1739999d21b97adbf5b9f28e8283848d32b9914bede34523884b677ccbc6092212ecd9838148e609f2c23b84b5ff718d9de91129bbd2f784e479c013efa3d0806277f8d37de6345c22cf0730a630bc2e756d58a0b7c1269c85d3751da83f8d5c1d02218d7f82fb242335e4d712d13285cef44eb2426439591f83abbe5d6908999472459353ac026f160aa370c6b2009fba06674fef1be4518aa9e04b5fc2e01d7a693dbbd15e16ecae41cad72cd792b5ba557762d4c9b6329f42cd26bb64e6aa1ad3101a936a2b86ca55a2009bc3f98a94681b13bbc2f6f8a091c031204489ca622c798baf7d5b485a1376ad479048df7aa8f3f25d21031b6a2c2b826dcf991984be397461478857f91dade14ba692b26b6a2f7f6d9deb6f00e82c01c82ae3735cc0413709ab71309d1455928972b4d477cbf2b90be32163477823fa4d3a2abfcec25642e4c01e514e57c9f9c0c77b2ac86fd1c6bffa0fa04c26ae404898d15845a5227afd5b64220ef726cdf7311dd890180e9af1a98dcd2150a57105ed7ae4fae0c535479eb8f6379ad5305b61ede82c97bd8ed3ef75c36252e9c6eaea66323156099e2c134a0b4d7cfb4c7a201e0bb68d3665f55a30f15afd4eeab91a4f40956f5dad34d24e075deb503733d78a378205a94c3eba623f3de432baa479e791d78bbbb4aea76b484e00c532a0047f8836474a4a745ddc42f05bb5cf102289f18549c04b3e41533ee17c349b42ae6bccd45017a2e97aec28dd24ed448f80e775aac560966a4ec5c0e4e05e38ddd3abf191395306f37a7107ae22f26f83c2e0153f4412f6db24d6a26f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(10, 2505274, 0x00f84901c82ae3735cc0413709ab7170fb145592e67d58d222930b5900673529b9bf8c000cb6e6287f14f92366e362f47ca26a48f0f11a03fccf2469fdfbe50011625e0bfea52207d2107bd92c527dbf91febc61c3982bab4f5e4bcb3dd32728aac8b044649a5e344f2cd8597d3c2eeb2de6c66e20caf2a7afae361ed67fe01114fc3f84f1dc90da24cbfdd4f89632d8edcfd55c67861795ae78b99dc9e72d1aba95bf7402d02a91d7abf272b4f7fbe865b07bd7f3c14f086ca60badce465a1c2d4b8238121e4fbd5594fca073dbf9029067a276dff096d5c8a876f7749a4b546d565c1ea4a1747ef66ef5ba88a484fc68f203b9a8e8a0204e54a3d9a0f04bcb52a0de840778611623d740869db83094936da36ae91805cc245b7e96d5f5ea07bebd89209aeefb1cab4aadf5be6e529146fc1296a5298325c845ce2e6cbffed004235a99db1bab43f8f789361d6f00f84301c82ae3735cc0413709ab71f0f9145592db2c488b98209b52e6ddb098e6a8ede74132fbfc5e5c84042e1a4bd0273078430de07b5c2d2ac8eb2b28fca405d6003e50b5e2bb85ae826225b62f8d71c1c692a0feec5af0269e186f552ff646595885025e5d3c79980016077285e2061c3bea4f056d50dd3317fda0b15ff5c6864695e64ef92c43c8fefed0a43575a980046847f23736c154f72dce10bede5f015d2e84b8bf0323d6c29c460e0f158e2f89419a67a61c3979dcc5d71c764bc02aca184d68d48dbca37100dc3adc5736bd3c3ae4105cb298e4abe3b5d0abc1cc5dd15194b03f803b931658a6a8afbc3338c8b5a9d8bf7ee15ed5fd2488f5d7b73b6f85eb1b2ee6e7f0d0ea945a6617f61d6f57dffdee78282cd2abb54664bf67f62d55c255d6300647c05457efa1af9999ef9389a13d6e0e17eabebcc746f7878e26ad171d546f00f82601c82ae3735cc0413709ab71b0fc145592af768810545b178cd1479965443b18398cc8ca51ea1fa33c48f06bf7d53e52efd0d6f98d7045f5007f5b56c8e77db5df08743ba25df399c92e6a814761b4a082c95e497de06147122103b538a1af98a6428c941c87c3ccf6fc90188a6a25f948c19eadad0291f254123399c4004b7499a1777f35eb8629f2f6ae8a52594b47cc941862d0820a8e6a52f51d05c28c2d081369575badce48a87b701b426ec2328a6c00c1bd63e65436efe3bc343d8d00d223bebd439f364896c1a9889c7e0e16b9bade8cc6fbb6d71b43294592dcfc96f5f326eb65ce48c1eb0149502c40d75adb595e13f37ce21ff3baf1b6d747147267a8b2728f669cd54228d8650be92064d4ef7bd31068fe2e2a4e432d4d50e2f68489b3d04976a46f00e83b01c82ae3735cc0413709ab71b0ea1455922aa9a7e2d813517c0f03a62da1b78138b294b85ac0a56b8f35c7d0c81644894132f8daf1c379738c2494bc0fd1d59ff79acdbd948621afe2f41a59e4c4697da7c59f0a8409869710a3225fd8c341908637644409cf9c6dcbbe2e7b499ed96342b4120e1796a91fb1afe3731954f4c3eb3deface26402e05cb1d64251151c90980d102fafbc0df691e10fcf6ce16cedf07b210144e2eac18285c84c5586fc72993d76c783c79ebc07950dbb27c647281bdbe648972a5ea988e04ad00fb69f4b5ed758b687db51059d20bf03366d313ceac99659868c97772a25fb6c694a8dac5826e5bad91868d9d1468976b7e2d78930583fb2ce655f174de5e04ec93eeee74742cadf576eb01b031064533b04beec70912970f20ec390c12c253c2c0da2377a3486ad8db67766b880c7c76f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(11, 9412662, 0x00f87e01c82ae3735cc0413709ab7130991455924b6d317c02412c0b0cb48fcceefda35749f6cf26b62414d36abb79acf6cc1aa204f2e248ef7a348ca18cd6009b2401ed2a56c53ba28401c3e55a26e2b258140bc67f5e791acecf289a3e70f85f18f7f11b49bdb6f027f87dd370af8b3cf05e2c67ca6ed44c37e3fdfefe914d0efe4362fa68941897ebd72c1543c952b4cb453bc02e67b34b47379d021a347ab0135fe2da743262f9d51fc21bfd36e9675d8649b3d84d2326749e6e7d0aca74a92c0e1597f0cb518a08e8dd1ce445289567a48578fbedc8695bac25075faf22eafa455365d26be54ea4fcfb33a5222cf67cdd2728f7c5f5f1aa854c24267e37ab83ffe6b55e027f0b367ea0d701a2a89eeae3eb581ecff148c14829b164d3c1ef8d088150bcadeb5e8b964e8befe5a943618d8e5d43aa83d8d940ab34f270f73d2151d716c186b0127b9dda46d52c5a81104a7a992b18314e85267c86da874f76c77986001f9783d869e41f7e378b18088ff690bda960fbbdee80603ee166d4763d6f00f88101c82ae3735cc0413709ab71f08814559232f1d93374616a9e6b5f6c6db87dd4f120814f74e31ff4acc2afdd902b0d3f8e29223e9e2beb6e19dcb76d2a50f31689ba4f3dcddff50b863fed554d03e19b931e7c82a7afcc58eb32c4ef7d17c9b0d7de6307b9f4263776d045cb07204ceafddf21d5b558d95095f086b9a287eadec802e6dcbaddabdb7da42b1e0ed2e1fed1a553229a91aaec45b156424bb731d307a44991fae3f8af4fe2ed0e6cf30702fa65cdac3efdbf4c82862f9e5cac0f97815219e31ae93f08614e5b73b63c3a31dc64e81e9f9355e709f41a73db4fe92f3cc08da0e31cdbfbd16d5ea97751ee8a4cc79741cab84ee48b8992b57705041f6e8802077d7990016009d2f7d329fbb5730947a67301eabb4dc7b2ed012f71b9845e98518bfa2cb8c3ca4fa2d7ac67b5fe66bbb5434b877b09a1b944d7390f9e134b9a883df5dad05c1a210c809026b7c4aff574b60940d4a5f4f9ec348d8b9d37bff2ef9314580f15b7475a58fc6c9bf3ef5fdff88aeba6be0a47c0789e89ee5a386f00f87e01c82ae3735cc0413709ab71f09d1455928f0203f3dcb7bd6a76c624dbee385cd348fd16b693440e6ddaaf5228100bbafdb7da06252565c752341c3f1b2d9f7b2e4c7d117c744a1cacffcdbf1ecdf1c2e7ef6b7e7fc9790f4cd1bf0c562226598d00d674f5473ab422fcbba1787a236b1d3f5703e5e71333a649ed62d22faac2f04f092e0abdd746534d1d5f9244a04825ceda0533178ce9120e30cebde9582570b57bc4eef30fa10a4260a804bd783b7f52f4e41319724f5ece20e9debd7e1cf2078668119dfd159d389ddfb29db4d0920f3293b9e519c8c0c7d487ad2635e0fa590100d241eb3a60310cfe03273e4da7882322bd96a899ca1db1905ac224766c5b222964d0889bdcde1910f60ae7cff40e14e646508f8298e1dc99bb0587e801e0e4248c560037dc914193e3cb6fd9ab6c4a85bfab1d3242cd1b652d1ee146685c1c2dc1bbf780d40dae2aef4e88bdf21b4a5fc36115296d1d5dac72a97e6ad2f2e4840bd076719264567fb11aed177d94c2edbe61f51e296b015b9692bd6f00e88101c82ae3735cc0413709ab713095145592bc2bad52e79f3defc8900d84739b67776c86d3543c9fce376c2b5adc40fd7c167294901983ebf83e55ec1862b5d2a4a079fbd55b74883592b636bd744a8333bd86dc2015215af26f8a890b34a2a5be46c94efe98630ea577f19e97d8f9fab201381ba25d8f4cba89074bd14e830f1ea78f37ba49bbedb368ba62360ca2c90cbeb31c4fe2d830c3b8167401145d64bb74697cb3fd70baaff6bb8cbe9ebbec7539cf66e468622530e5a998bb6f01035a2589a4844b3c0c832d464025bb10ad5acc00fdd6f64c362c2b9d552f11c39bf240811df9281c8cae5183590b5304b945ab1c22b8dfc179f0b64d2fc0e221ba0cedc6d26c0dc66d51f1ab18c0fa5903caa80a4c342e4ab10ab79df2ac40cdaca978d4cc25c9cd57e4b8b23234b7c565beb2c2dd0284871fe269c19a1ddc781ce2d38498885932da2ed634be8b4685c0aa3ab7f2de80f3ae9a5118c208f7f9af9f99396cd812e3642ce254fea2d4703f974c2299a1ff8c1a02a269346b4f5b89970cfc6f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(12, 9412662, 0x00f88101c82ae3735cc0413709ab71309d1455929c7742535a4249f44243f112f81a6c7289bd0956a7613069f852a1535ec0bb6470eb941271bea7fe0c3ff2404759bbbba6542fd1ac25eb58bc3da8d21acb41cf50c98152a1399bcd0b5e26f1d3fe16a02be9ee5c336841f72aeaf4fffb240aeaf88b00f0ce316c1c9a1924064932ea0c6d6ffcbca2f48fb4724653b64844c392da4a2bd19af8d8712e239e50240d72c833b91e38c131ab89e68e5f13980ea3bca63f25ef24646aa4160907630b169e18484137c2ddf6c2d725343aa8bb88f6e7aa06942c64ddcdf917ed6f675ab2487f5b0945c205a1b35b3daa5263a7fd76b5c06cd6b6078de848521dc35ac90c74bdb83fcb4f84faa652193dd379ba51535594f362bcb701645d3758773dcd9c0626343f353e8db20d3ed5b43ebb8eef4b3fcbdafb2bea39d7ca1e9437da8d2a12cd35a754a75fe126a003f7cbe586cbb32bf8f5d1aa825573e1e00895b8b60c952533a05e75e96aa03f68488a8a318a1f4c74b012f9597fb4699d068bc01ddd0059256f00f88101c82ae3735cc0413709ab7130e1145592f699153eaf8a74f74ecca9467feb19bad72e61b06ba43fb62ed41dc91d4bd19613486ccc5eb1e68c5982cec0db33266f901f83ecbb3327a58a4312b82064df604aab588ff45e375ddf63366262e034a13c870a386273acc0da99e6b984976437fb0ea8613e2680929c4a427775128dfc0cdaea738a211c2d509d15676cb1413617a17fc2d74171732fc092dcc76702d252af5e344e6f9879083dbf23c4e8026be697738cc24502251f7e245fa35926539c78dd6f9ab648c91cc26140c1e96448212baa2a5b2f15ddb14515dbace673f893f9959f33e9c142fe9251297badbeecb13d51410ce36539415df73aff395c6969db001ebd039407b2574b9a3dc85d474c6b994d89f944eb777348fcc6d7bb785d6cc3b1097e67174886e0c67dcb6552a6d8e580aa4a115c0030420fc05a7451844d9d35a890fbd4e9230b8dc60c2214247ceb9d74841415ffa7c9ec982539fee0b05261d9fda7f9f3b301a9859f2a64ac47d1db4c46a4326c3f38a049a8ca62c66f00f87e01c82ae3735cc0413709ab7130831455926b13096d16e9b915dd30735f81e2aacb3cee90affe386980265240e09924b75993cda9cc4de09dabf1f5bff5ffd3c1e2b5d255b3ec47c42e3123a5fd80eeb8fc669aa56fe2524e19720c8c3508f1ff95a05ce489f9d7570d7de72c0af22d6d78593a135d3a20b99534b3251d9b34be4b129b784ef9c1942d33544b83f72c25ba485313c2217e25797d98016938f93108329e19494657e73f9ed9c7642a5526187157f8d85ed726884944f6529793e88f3dc34e51ed8fa913cb24486afcf06ba65535d3e246e56f7ae8c357411ccbaf69976807d3f88ca07524fd32ee7c86bf494906acae348c4fb6d76c35bc7bd27080bf892f5ea1fa2b081a8b239044ea48f90af7443a840f5cccb4f5542d76c871fd3c6a60b81ac7a73caefe4fc59b76a5c3dcf902cf6d7feee9ed28a857129cb830c1c1a25dd7f9f4ebd6b4ef9ddb4278af51690b5c4a72624ade080c59de567a49e2f2b74829f10319b700bd2f32dd422d2a2ec76f09545d5ad377aceb5fe26f00e88101c82ae3735cc0413709ab7170e6145592277dab0da827e5aa1d3d1ffb9da087b7dfc23da0d56cd03223b0194b5b9912db5da5eb205cee0c290f44b2d9fdf38a51a1540b82f5d5037e122fb098a79cb639ff71c70f5c64023b11962a695e126e501ef83b1bfcae6f02fb0639c9adfb045e7b27c0595d4d48ca5380701436d811788ac4a6a229bb317c1e8f953c90e6d2df4a5ebf04c0eccf21b49d5c0ddce731262930fa9eb9600a07aa50ff89c74415e8c544fe35ab19ad8d61f85c04bb35fa6f10022fd727b9815e4c892d9684c9cb59af0b214497627e3532031b0832e2045f60b4767169d35a500fad7f59d007016781c3387b932e5dbeb94aef8156fc38b4f84210a9f5c22f2bf0cc2c4ebf79501d61fdd70ef004b5bfa3c29e5266427f98b3bfb75abbef394d042c05eb69ab36cabfe0065393e13823314996643fec4b2f1a5961de0f04d2646c0460c5fcb7bfa926154374be99d16e8548f8135bd5f7d7f3b5f11ff69e7b700f7245cad7752efb74cd3176cba49dc595e55d3096c9b39b136f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(13, 2103206, 0x00f83301c82ae3735cc0413709ab71b0ea1455922ca9dc28c60d5102de9dac4106752e938de25c58004f010a5fe76dfa2ced90350193e3c71adf30aceeee29b16571de605c42ca438409e4a80b02c2306f15ca642e9bc9feb9380b20103a0496c28165f6929129cbbe81ddc081f85e7a4dfe908d27255aa8547871adb25ffe0a080d13ca336c7784e9e3854ed539d912862a799dfaf6b4dcfe3f8095b5afc3958b0fbb10b0101c300327d14a200d7abc9dfb67e91dfd0f07710079734aedadc20ce833e6338792e9e23cddc381390b931a49af1f28366e1272110c311dd8aff7e9e6653a3bc704219114fe5ed242c85da1aae911ed59c97de27726328cfe9476fa4e6bd0f324cd5ce14f84b3667b046bf737a512018181766264a7706f538a698dad3284d90b25bbf9a0c5eea1bfe39a03656f1cc97a1b6f00f83101c82ae3735cc0413709ab7130d11455922a3bd196c4c70c58fb5615100a449d85898b4bf16e618f5cdd06da9a39b4cf21accf3f60302a5aa8e2b46fa01571fed1b4acb1339d6eee9757a7afad0e2ba3696e743e6984b786b931cdaa5528066e815db5523bc86f5b8bc1ef41bf4c4a8ab99694113eccabb5d2cc5f31398c4f60c16a99e3f2cc32b799f77d854aa5554bec70658229885f151d9f320bba74339e5d75b92b5bbce1e3509e454fe3ee60af7f3b2160e6659b6e35a666b6be5daecf978db53082a72c1ccbc146317fc5b6dd0e0c2c7b44ec0f1f5492c4dbc48499ec255692e7db0663f397571f3c3f05343a441c45b6e8c170559f72b294c222b0b81ca686f7be77880028c84c9363df716f107d04897f977a388a5d1a66494ab8440badfe87ee9122c322e3fc12350a6e727e716f00f83501c82ae3735cc0413709ab71b0d114559207f71392a0ca85299ce95ef1587f9d98030bf2ba9a351bff177220fa9726d58a42b1ecaabd21f39b4b822632ec88bddb6083e577eab82e5141fcb8437fb2fb142832ffd4f3189bfee3b79867f81d242f752dd0e29a21c7a25b0f5de26797658f2ec8cbd9f82d515398a82f5a6353808b9fa69feea27b110df40fc42c649e3c568273d47b891d0fdd04be287835977a9fef994462b8c973888cc33921b9ce41ce5245819e5889fb05032cf2ead467062e78c6a16f001ecaed4c80b24229cf35acd1ccfffabfb88ad82ed0d942b7aef17a29719247cac70cb506d4eeda333f67d17df7b004fe9b6b08cf677ec8c331f8288778d9bb26568f3b699e23362d591cbcd0ad0dfbcced4e4cc5ada502089c2693965cc56e39dc6bd788d97a35208e0896c08016650a6f00e82401c82ae3735cc0413709ab7170c2145592a5bdbd20b7df9a739d0ea41d9c797ee3997e5dff8fa4baccb99e8a50c9801f672c45892fd5ba70131779b989641ac36772713cfaa779ea9ef0e1efab794aed64a89e46f202449812e890e9470ef49f641ba50c97ef5c4fd259bcd914d8c75471e1dfaf69243c680bc8a335868e6482407012f22183222a706271c3d0d1e2812acd5631b3e7f5861156542a7a1ab98a76b9405c17caa6ed7569ff15cb28af0a66ab7a09d5e95207267507f0285c7bc6fc9c8f312991f454bbc01c3d3bb134553a7cf5df51b133225095b54469091aed4928ee3630e4bb8cc8d8553e4138d6e6bdeb587da041819dfa1755d5a75524ef244f6e13cf6b3db0b4cc37b8517ec06a728e1042ece8f0e81583f11df169248f31a50951c16f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(14, 2103206, 0x00f82301c82ae3735cc0413709ab7170cd145592514e979359f4aa7618a772d2a4f3403542a6fa291f4ba65d1463c5571ffab06e5f3ee10f6f116c256f0dcb44ba199a58f5af77179d85021dc8ed30b8b23c827dac8f0b74bea6949d7c5d33df77eb41ceb4e0ea15f210d1bd8129cf047e1595accac5ce7b84b7c4df962786095a2cb2fb9d09405161ceb7e81534b4d9cc4b0bdd0d851c2556dab961e373a51f3c379d27629c6585a35050c81530e9f8ae5dc66c1cbf746df11db4cb718eb64c6c5a64544a22a3d1b2896c56a17b27f354ea998e713594760ecd087414d01653b107184db27c7121d5869054f95f8aa8ec3be74cc8482fb9215f31f0d4ed05fff2a1757290b4a6d0fd5886612d4183e1d2e6ed152d38984acb349ba0bff42f5f853d40aab8c1496f00f84701c82ae3735cc0413709ab717031145592785f706f0a84751baecd4c3ad721a0f210bed22f6c0b6d9642dc8e5e208c25d09fc265cef4a8793ac65d73459a1734fdcdf1049b1665510ddbb916a5ebb23732eb5e7161585c8ddb00d250e194c29c7d25c078160f098355ac531ba2d244bc9be04d0098be88d2b1c2d4242f5a239708d9646f55354d3a494707ca2b01e9a436617a4ab40140e0fc411903759ccf18d505ebe338705ba60d89a1fc49dbf6941338143b4add744822222c4e91596b602df19bdbb309e832c8cd8dfc74c7dd324f2582f9795d4abe8bcd3904ee550b9d2a7979664baaecb521a22139d5cf3738f3d1b7dc978d9f1ad9784410b18f7e858b4b3639a7637f42a1bb57829978830668a234d0dafbfeb16dcd5d04f7b15d54008b891ba719b270379aad9993deff61169887643233dea6141b5ad0ac11a49d08dfa6d06b8597d06f00f83401c82ae3735cc0413709ab71b029145592204a1e6292ae3e9573c2485c2f763915ce9726570fa99091b95f8f72c5fd1d4e6af0ce440a57ef64ab600f3ada2be80cce78ae76f5e82dcf81c66152419aee52e7b6a5821275336246e9c1e31c8c240c9eca3613becfe7af5f5d8bc66f713cb802a4ec0ce9cc226d2613dccf633396a8391853d2d58f8edb0231f3a988a376ad873f9ae8f11ca024c86d4cf430a4b8c819c03b5cf82d7f1545af8a5ee2f9dbfa8a858d71349f1d11cd460a8e81e28956e97bfe8aefae0bbd9632687c9278fd753f31d26dde62844db9595cce7ada58569fefce6676a6306ba17ea8f56ee24ec27c40b53a3f77331286a5deb014788c0ef6499e94e25339353ce1cadc50fa177deea6d2b994b05449767b8e588ceda6ecab6c99e20331fe59c7aa42b49547cbb557dce97e6f00e83501c82ae3735cc0413709ab71f0ca145592d92cca32e27a0c1aeabb36c66e6cfd3f0bb7725d35bc2ff49cd46117d73e4215de9136b4643ac5f832ca8001ecb060ed8243a85064d9d4eb6d4f01c7f93b64c51ac3e9ea7800918be342091c8a7c1af26afa829c8856a21b5ad276f14928290cba52f856813b94cd21d910647cdcc9d1b23e0776c3d49be9cfe0e22e45e3b17058705aaf4f86350f1c7ac640f7c36e90cdc5ca9def80b176b0b77d41a335a8dfc6e2b35e767d89fb24fb09af463ef205f40c72a77b6560741c81104438db8a2b4313b907ef2b5a0a9d5e0cc652a822a682ec7a0ed835917f8196162e83f3fcf3f63194171a1f545c0b0a47b77b56ac20a1ad4bbfc64e5fcc9b98d856ef1265627f59fef82b1f473d11992f1e1bd901995b10d65854411d72db63a7eb0f4e1bb093ea192d6b6f0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(15, 304050452, 0x00f85f01c82ae3735cc0413709ab71b08f145592f08024d4a3789f1ce6551ae546626da66aec5ed5f24fd14f24bdaaf74fcd0496bcd2fac8aadbd2108eb31460760ebbe0512a1eb5213a7b6ba165ba95ce66a584deb96e463aa393f63f19d58617c602bad684ed371886413f1c256984a8b860206bd42e11670fd11e408f90ab294415d2695b6fce3ecd637cdd930ba115d9d167a62377b4152fdb5ec8ae1ef55652060516ead4c744673ac1ea74b1b90b91f8fbf7805415c606e737af2747d0bd2f38e8df2532bdccd4d464007a01ed45439f9d7c525b6b5df687c3f87f505a49069fa238459d5791d723e0ff3e03a549ccc5fc7d1176c0f720ac3d1255073854250e71ad2284f400070d88c42c5f72da53f3d9108f79667f2dc5ae2a6b1129edd419cbd4cc16a7daea673037b64bf139c46b52f602a92de42807a3044df1bac4b8ef3f3b81ea6527fe3ae86146c6bd1e2472b9b059230c0d9b421e01cc2967c781a56f00f84401c82ae3735cc0413709ab71f08f145592cd4647707615ffb919c88541825364fc0a1e31a68c84b57b1be6097c290cb694a4873d79e5c56162cfe8f00aee1c65b20dd520d9d182a9a7deb0f11a084c15bf8d8517cf6ae3870e25aea539064d2464f271edc79a1c6037f2a43a7d79d7f1b6f413ac551451a7f8f1336702993c9f4f7acd8bb73161f82769f5966e3037978df3f8e5e56e83f3a70849fc19008b073aaf77e576f694f4a848b4defe459cdd4450c98384905e965d7689612fdfeef85218a59638922cf83662e27e8dd21a60e7650a74c66c067c9b58f7a85054b2f7356f1f8f7a98c4fc01f79347c1ad6332e52ee246ff42d7a31987a8dd710fde0fb165274aa27dbe3ef6bda568a655835486b23a55fa5d8086d0560c7e3c12062785c0b9386294fb65f0815b17ea2ec4d65a71e575f46e6a485dc74e3777de7a3e6002bc53786f00f83a01c82ae3735cc0413709ab71b086145592c7fe906e3197d8984f5768543ed3111f595b4d27ea80196ae269d603ef13bc5e561c469e3110304036c00194f4e368517b25823e22567c645f70ba9d056ccc113471a5b6f2c8cf477b324e6be01c17ddb0e4f4d76667a5f5ce1002269a6047977f02d627535289ad0d631aa3a18cff187e1ea0692c6f857075b3ffef8026cfaf07b8d79bbf9e3c092f7824dbfab5836583e36d646268862def1ee0d12d7284c198d4bf985e1fa0865f58e1607a82f951241acc0f265c5da3610e634f774f2e574ccd1e0550ece511bd29858caf794b8e38a653607508d3f3deb281992de54b23dff7786dbe13d63ed7431c5026d2c6f44c823fb844c50c38e6d41f14208e371a066652093b805a01efa6e97e9a7ee4093e6f7c48be3615fe1fcf6e15da2bb10408a88ab288dae573a1f46f00e81601c82ae3735cc0413709ab7170f71455928efd17c4a076eef5a0aed158ea0eb6b99fa94897d05113c6bd9e5449b18b0abc56dad5793837ba280aac973437da8ae46cc9ee0c682fd2d3a8dab24e7a86228978275671f54543216fa8e2d3878cab202a82f07319e1fa7882150c5fcded07b24717eb367a4df09751406f0c458f1528173122bd9157748ec4ff52c935eb4953da1d12c3a0002ace7187227c49e1f0b99e3ef5a0a67b1721a95d179bc5098911301943ea3e492de79a4dc967bf57fa5062b401132cb4c27a52f4eb7cd496627fb2658ef40ed6b8436f6e8672802807b1f8b75d6f9e2fb1d65668120df2f71e7dc12045dd6b389ddf8a769fcc1b4d8e99cbee5e182979f3abdb92302b9d1c6a604f962884cb5f6f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
(16, 304050452, 0x00f82f01c82ae3735cc0413709ab71b0f2145592c2fc19cc812993aa500a8a056a0ff4c38b2d14e9098bb2e11d5927ba8ede1b3ea6ad1da7d4e426452cb99d040598f17c915be828a459aec19a5952f30f91ea810fd481697d753ba36b347c2af85472c14bd3e7dfa62cf560150c8fd2e3ad173efcc2f20fd3116ff1d44ea4c2b55ab5838bf3c5a73015235474a348c96c45b9b2e8e62b039760e6da1a86df969f5b3b90d33b2f4e92b54eb9da0582c7bd23cbcd4409fb344980160ad4c227046eeace1f6ef22078e1727f01dd8a50385fb900c846823101ed5d3585708e309b0c87885541e7d9609105e563cf9708254af33c5399af3c0ce66666e9af84b7f7be93d7765bec3beea1a4d4884b76a25e09661fc74f3c92446aa4f55cf4ebe656b3687a440a1ed074464e1e2e420eeae235671f6f00f81b01c82ae3735cc0413709ab71308d14559249d62408627e097c8feedb00ec5777c56218254fabcc3cacbcdb5b683bae80b018cf2cdf5a76e2c4a33df8c360488f97a718575f6eefdfc0fb7181f39a23f6f6df4d868a952f1f7adf601e9e50f2b6a88b23f7959b3bf76e140fcf482ef041110ad2cacdde7369ddc8fa07b5322eacf27f1c7f03dc2786a91edb873e811f811a1b7fbdc7100c2611174ebdfb318a33e528338b0549a3cd5529ef4447b7b9a94b03537ad2e62d980419ae2cb72069cb09569d3c261dc5c4a342e67446de6960940de778d457fa5b1333b4bc2d760c12f12afa3e58d31e1b082209d59344eea4f37bd558e5de8192b181ec4e625eb1c6b394e0747f52ab2dc73057b9499155c487254ad87a2779ae6198244e6f00f84601c82ae3735cc0413709ab717087145592acb6ada8fd0b79af640b0d8db6617c932ecf16eb43ba25b595e8a4ded2606775a91ae1e26d0f81fcfd92affffb006ca168fb23c4d66d346a79bf18c8a07e0fd663bf7628cd037c2c6cd5e6bd4d3ae6042691fbc34104ac4b1aaa5336bb3310a6158587f5e5f4bc17bb6f4fa40c221d1b01e0836eea2e5c83b113c81d9451db101d7629ede28328167e580dfc212f094f1a90cb4aa209b96b6d3a1b6581a96584e13e8de928aaba118c21b227514478c2de4e947bfb0c7264cb98ee632f87ff0fecda5de922b3ccf733ae2d63aec6bed5747b79376a6ff34606d56060eb231e4dd4a2210be04ec6b45504c597c659d5e73b5839a29a44f86195f45863ab9c06e2f8d65172ae38b36585dac14600e4ce4be31fb05578c88248c8ac057a9d5ac7a7418ea013f4d852e330221ce1f2cbf1de4a7125b41bbd6f00e81b01c82ae3735cc0413709ab7130e81455927cbbee55621475bf26d368e4f400b9a85495d7697eaa69e19a4917de5e6ec4e8413f1486b2bd9bbc8a8dad78936e627f7aa04d24d817081227d9ee59be3e31c8ff8f72cb1b2e2e3d6a00c4f3e8cf2d928dbc3cf2ddfd69be5ac1e11e0017458e35ce765b3432acf4302fbd4cf1f57b4835978bb1e5767441cb5fcb5b3ca4535af66a5b462e494d3dea6b09862456dcb0121181c02912aaf7064409be44361eb1832e16816a70e034d7f0d005744050052ac08bf677300ea1097677aa0448e7986849b5eefb2aabe20eac46e067ac4ceec87f718ce651b853814e4e978a4d05ab47dd0aa4e710184b4e2303095bbd2711b233867b90764eb5ffcfc4ed3493e17f6e1fad34a8cbcfb32f10506f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000);

--
-- Disparadores `usuarios_huellas`
--
DROP TRIGGER IF EXISTS `usuarios_huellas_deleted`;
DELIMITER //
CREATE TRIGGER `usuarios_huellas_deleted` AFTER DELETE ON `usuarios_huellas`
 FOR EACH ROW BEGIN 	INSERT INTO actualizaciones (usuario, actualizado, huella) VALUES (Old.usuario , NOW(), Old.id);     
    END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `usuarios_huellas_insert`;
DELIMITER //
CREATE TRIGGER `usuarios_huellas_insert` AFTER INSERT ON `usuarios_huellas`
 FOR EACH ROW BEGIN 	INSERT INTO actualizaciones (usuario, actualizado, huella) VALUES (New.usuario , NOW(), New.id);     
    END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura para la vista `get_crns`
--
DROP TABLE IF EXISTS `get_crns`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_crns` AS (select `crn`.`crn` AS `crn`,`crn`.`anio` AS `anio`,`crn`.`ciclo` AS `ciclo`,`crn`.`materia` AS `codmat`,`materias`.`nombre` AS `materia`,`crn`.`usuario` AS `codProf`,`usuarios`.`nombre` AS `profesor` from ((`crn` join `materias` on((`crn`.`materia` = `materias`.`codigo`))) join `usuarios` on((`crn`.`usuario` = `usuarios`.`usuario`))) order by `materias`.`nombre`);

-- --------------------------------------------------------

--
-- Estructura para la vista `get_eventos`
--
DROP TABLE IF EXISTS `get_eventos`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_eventos` AS (select `eventos`.`tipo` AS `tipo`,`eventos`.`inicio` AS `inicio`,`eventos`.`fin` AS `fin`,`eventos_tipos`.`nombre` AS `nombre`,`eventos`.`asignaturas` AS `asignaturas`,`eventos_tipos`.`color` AS `color` from (`eventos` join `eventos_tipos` on((`eventos`.`tipo` = `eventos_tipos`.`tipo`))));

-- --------------------------------------------------------

--
-- Estructura para la vista `get_horario`
--
DROP TABLE IF EXISTS `get_horario`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_horario` AS (select `usuarios`.`usuario` AS `usuario`,`crn`.`crn` AS `crn`,`crn`.`anio` AS `anio`,`crn`.`ciclo` AS `ciclo`,`bloques`.`bloque` AS `bloque`,`horarioscrn`.`hora` AS `horario`,`horarioscrn`.`dia` AS `dia`,`horarioscrn`.`aula` AS `aula`,`materias`.`nombre` AS `materia` from (((((`usuarios` join `tipousuarios` on((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) join `crn` on((`crn`.`usuario` = `usuarios`.`usuario`))) join `horarioscrn` on(((`horarioscrn`.`crn` = `crn`.`crn`) and (`horarioscrn`.`anio` = `crn`.`anio`) and (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) join `materias` on((`materias`.`codigo` = `crn`.`materia`))) join `bloques` on(((`horarioscrn`.`bloque` = `bloques`.`bloque`) and (`horarioscrn`.`anio` = `bloques`.`anio`) and (`horarioscrn`.`ciclo` = `bloques`.`ciclo`)))) where ((`horarioscrn`.`hora` > (now() - interval 20 minute)) and (`horarioscrn`.`hora` < (now() + interval 20 minute)) and (`crn`.`anio` = year(now())) and (`crn`.`ciclo` = `CURRENT_CICLO`()) and (`horarioscrn`.`dia` = `CURRENT_DIA`()) and ((`horarioscrn`.`bloque` = `CURRENT_BLOQUE`()) or (`bloques`.`bloque` = 0)) and (cast(now() as date) between `bloques`.`inicio` and `bloques`.`fin`)));

-- --------------------------------------------------------

--
-- Estructura para la vista `get_horario_crn`
--
DROP TABLE IF EXISTS `get_horario_crn`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_horario_crn` AS (select `usuarios`.`usuario` AS `usuario`,`usuarios`.`nombre` AS `nombre`,`crn`.`crn` AS `crn`,`crn`.`anio` AS `anio`,`crn`.`ciclo` AS `ciclo`,`bloques`.`bloque` AS `bloque`,`bloques`.`inicio` AS `inicio`,`bloques`.`fin` AS `fin`,`horarioscrn`.`hora` AS `horario`,`horarioscrn`.`dia` AS `dia`,`materias`.`nombre` AS `materia`,`materias`.`departamento` AS `departamento`,`horarioscrn`.`aula` AS `aula` from (((((`usuarios` join `tipousuarios` on((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) join `crn` on((`crn`.`usuario` = `usuarios`.`usuario`))) join `horarioscrn` on(((`horarioscrn`.`crn` = `crn`.`crn`) and (`horarioscrn`.`anio` = `crn`.`anio`) and (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) join `materias` on((`materias`.`codigo` = `crn`.`materia`))) join `bloques` on(((`horarioscrn`.`bloque` = `bloques`.`bloque`) and (`horarioscrn`.`anio` = `bloques`.`anio`) and (`horarioscrn`.`ciclo` = `bloques`.`ciclo`)))));

-- --------------------------------------------------------

--
-- Estructura para la vista `get_horario_para_asistencia`
--
DROP TABLE IF EXISTS `get_horario_para_asistencia`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_horario_para_asistencia` AS (select `usuarios`.`usuario` AS `usuario`,`usuarios`.`nombre` AS `nombre`,`usuarios`.`departamento` AS `departamento`,`tipousuarios`.`descripcion` AS `tipo`,`crn`.`crn` AS `crn`,`crn`.`anio` AS `anio`,`horarioscrn`.`hora` AS `horario`,`horarioscrn`.`dia` AS `dia`,`materias`.`nombre` AS `materia`,`bloques`.`inicio` AS `inicio`,`bloques`.`fin` AS `fin` from (((((`crn` join `usuarios` on((`crn`.`usuario` = `usuarios`.`usuario`))) join `horarioscrn` on(((`horarioscrn`.`crn` = `crn`.`crn`) and (`horarioscrn`.`anio` = `crn`.`anio`) and (`horarioscrn`.`ciclo` = `crn`.`ciclo`)))) join `materias` on((`materias`.`codigo` = `crn`.`materia`))) join `bloques` on(((`horarioscrn`.`bloque` = `bloques`.`bloque`) and (`horarioscrn`.`anio` = `bloques`.`anio`) and (`horarioscrn`.`ciclo` = `bloques`.`ciclo`)))) left join `tipousuarios` on((`usuarios`.`tipo` = `tipousuarios`.`tipo`))));

-- --------------------------------------------------------

--
-- Estructura para la vista `get_log`
--
DROP TABLE IF EXISTS `get_log`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_log` AS (select `log`.`usuario` AS `usuario`,`usuarios`.`nombre` AS `nombre`,`log`.`fecha` AS `fecha`,`log`.`descripcion` AS `descripcion` from (`log` join `usuarios` on((`log`.`usuario` = `usuarios`.`usuario`))) order by `log`.`fecha` desc limit 500);

-- --------------------------------------------------------

--
-- Estructura para la vista `get_some_usuarios`
--
DROP TABLE IF EXISTS `get_some_usuarios`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_some_usuarios` AS (select `usuarios`.`usuario` AS `usuario`,`usuarios`.`nombre` AS `nombre`,`usuarios`.`tipo` AS `codtipo`,`tipousuarios`.`descripcion` AS `tipo`,`usuarios`.`departamento` AS `coddepto`,`instancias`.`nombre` AS `departamento`,`usuarios`.`status` AS `status` from ((`usuarios` join `tipousuarios` on((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) join `instancias` on((`usuarios`.`departamento` = `instancias`.`codigo`))) where (`usuarios`.`status` = 1));

-- --------------------------------------------------------

--
-- Estructura para la vista `get_usuario`
--
DROP TABLE IF EXISTS `get_usuario`;

CREATE ALGORITHM=UNDEFINED DEFINER=`frank`@`%` SQL SECURITY DEFINER VIEW `get_usuario` AS (select `usuarios`.`usuario` AS `usuario`,`usuarios`.`nombre` AS `nombre`,`tipousuarios`.`descripcion` AS `tipo`,`statususuarios`.`descripcion` AS `status`,`instancias`.`nombre` AS `departamento`,`usuarios`.`telefono` AS `telefono`,`correosusuarios`.`correo` AS `correo` from ((((`usuarios` join `tipousuarios` on((`usuarios`.`tipo` = `tipousuarios`.`tipo`))) join `instancias` on((`usuarios`.`departamento` = `instancias`.`codigo`))) join `statususuarios` on((`statususuarios`.`status` = `usuarios`.`status`))) left join `correosusuarios` on(((`correosusuarios`.`usuario` = `usuarios`.`usuario`) and (`correosusuarios`.`principal` = 1)))));

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `instancias`
--
ALTER TABLE `instancias`
  ADD CONSTRAINT `instancias_ibfk_1` FOREIGN KEY (`jefe`) REFERENCES `usuarios` (`usuario`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Filtros para la tabla `justificantes_asignaturas`
--
ALTER TABLE `justificantes_asignaturas`
  ADD CONSTRAINT `justificantes_asignaturas_ibfk_2` FOREIGN KEY (`folio`) REFERENCES `justificantes_folios` (`folio`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `justificantes_comentarios`
--
ALTER TABLE `justificantes_comentarios`
  ADD CONSTRAINT `justificantes_comentarios_ibfk2` FOREIGN KEY (`folio`) REFERENCES `justificantes_folios` (`folio`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `justificantes_folios`
--
ALTER TABLE `justificantes_folios`
  ADD CONSTRAINT `justificantes_folios_ibfk_1` FOREIGN KEY (`usuario`) REFERENCES `usuarios` (`usuario`),
  ADD CONSTRAINT `justificantes_folios_ibfk_2` FOREIGN KEY (`justificante`) REFERENCES `justificantes_lista` (`id`);

--
-- Filtros para la tabla `justificantes_fracciones`
--
ALTER TABLE `justificantes_fracciones`
  ADD CONSTRAINT `justificantes_fracciones_ibfk_1` FOREIGN KEY (`justificante_id`) REFERENCES `justificantes_lista` (`id`) ON UPDATE CASCADE;

--
-- Filtros para la tabla `justificantes_periodo`
--
ALTER TABLE `justificantes_periodo`
  ADD CONSTRAINT `justificantes_periodo_ibfk_1` FOREIGN KEY (`folio`) REFERENCES `justificantes_folios` (`folio`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `justificantes_tipousuarios`
--
ALTER TABLE `justificantes_tipousuarios`
  ADD CONSTRAINT `justificantes_tipousuarios_ibfk_1` FOREIGN KEY (`justificante_id`) REFERENCES `justificantes_lista` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `justificantes_tipousuarios_ibfk_2` FOREIGN KEY (`tipousuario_id`) REFERENCES `tipousuarios` (`tipo`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`tipo`) REFERENCES `tipousuarios` (`tipo`),
  ADD CONSTRAINT `usuarios_ibfk_2` FOREIGN KEY (`departamento`) REFERENCES `instancias` (`codigo`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
