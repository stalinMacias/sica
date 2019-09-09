<?php
	include("conectar.php");
	include("class.phpmailer.php"); 		
	date_default_timezone_set ("America/Mexico_City");
	
	if (isset($_POST["opcion"]) && isset($_POST["correo"]) && isset($_POST["nombre"]) ){
		$opcion = $_POST["opcion"];
		$correo = $_POST["correo"];
		$nombre = $_POST["nombre"];		

		if (filter_var($correo, FILTER_VALIDATE_EMAIL)){
			$today = date("d/m/Y");  

			switch ($opcion) {
				case "jornada_retardo":
					$titulo = "Retardo jornada laboral";
					$mensaje = "<p><b>C. $nombre</b></p>
					<p align='justify'> Se ha generado una <b>falta</b> en el sistema el d&iacute;a <b>$today</b>, en su registro de Entrada a Jornada Laboral. <br />Registro fuera del tiempo de tolerancia, por lo que le recordamos que tiene <b>40 minutos para que el sistema considere su registro como asistencia</b>. Lo anterior, de acuerdo a la cl&aacute;usula 59 del Contrato Colectivo del Trabajo del SUTUdeG, que a la letra dice:</p>
					<p align='justify'>&ldquo;<i>Se considerar&aacute; como retardo la presencia del trabajador despu&eacute;s de los 20 minutos de su hora de entrada y hasta 30 minutos, en las zonas urbanas, y despu&eacute;s de 30 minutos y hasta 40 en zonas especiales. <br /> En el caso de que el trabajador llegue despu&eacute;s del tiempo considerado como retardo, el jefe inmediato o el encargado de personal, deber&aacute; notificar por escrito al trabajador que debe retirarse, dentro de la hora siguiente a la registrada en su ingreso por el trabajador.</i>&rdquo;</p>
					<p align='justify'>Deber&aacute; acudir de inmediato a la Coordinaci&oacute;n de Personal con el justificante correspondiente, de lo contrario se proceder&aacute; conforme a la normatividad</p>";
					break;

				case "jornada_anticipada":
					$horasalida = isset($_POST["hora"])? $_POST["hora"] : "--:--:--";
					$titulo = " Registro anticipado de salida jornada laboral";
					$mensaje = "<p><b>C. $nombre</b></p>
					<p align='justify'> Se ha generado en el sistema un <b>registro anticipado de salida</b> de jornada laboral el d&iacute;a <b>$today</b>. Lo anterior, de acuerdo a la cl&aacute;usula 89, Fracci&oacute;n III del Contrato Colectivo del Trabajo del SUTUdeG, que a la letra dice:</p>
					<p align='justify'>&ldquo;<i>Asistir con puntualidad al desempe&ntilde;o de sus labores, cumpliendo con las jornadas y horarios de trabajo, debiendo registrar sus asistencias tanto al inicio como al t&eacute;rmino de su jornada laboral, a trav&eacute;s de los mecanismos que para ello determine la instituci&oacute;n.</i>&rdquo;</p>
					<p align='justify'>Le recordamos que el sistema tomar&aacute; como salida el &uacute;ltimo registro del d&iacute;a; su horario de salida est&aacute; registrado a las <b>$horasalida</b> hrs. Si realiz&oacute; alg&uacute;n registro despu&eacute;s de esta hora, ignorar este correo. En caso contrario deber&aacute; acudir con su jefe inmediato a notificarlo o a Coordinaci&oacute;n de Personal para el ajuste o justificaci&oacute;n correspondiente.</p>";
					break;

				case "sin_actividad_academica":
					$horaregistro = isset($_POST["hora"])? $_POST["hora"] : date("g:i:s");
					$titulo = "Registro sin actividad programada";
					$mensaje = "<p><b>Acad&eacute;mico $nombre</b></p> 
					<p align='justify'> Se ha generado un registro sin actividad programada el d&iacute;a <b>$today</b> a las <b>$horaregistro</b> hrs. Deber&aacute; notificar a su Jefe de Departamento para verificar el horario de sus asignaturas y acudir a la Coordinaci&oacute;n de Personal para realizar el ajuste correspondiente</p>";
					break;

				case "sin_actividad_laboral":
					$horaregistro = isset($_POST["hora"])? $_POST["hora"] : date("g:i:s");
					$titulo = "Registro sin actividad programada";
					$mensaje = "<p><b>C. $nombre</b></p> 
					<p align='justify'> Se ha generado un registro sin actividad programada el d&iacute;a <b>$today</b> a las <b>$horaregistro</b> hrs. Deber&aacute; notificar a su Jefe inmediato y acudir a la Coordinaci&oacute;n de Personal para realizar el ajuste correspondiente</p>";
					break;

				case "fuera_tolerancia":
					$titulo = "Registro fuera de tiempo de tolerancia";
					$mensaje = "<p><b>Acad&eacute;mico: $nombre</b></p> 
					<p align='justify'> Se ha generado una <b>falta</b> en el sistema el d&iacute;a <b>$today</b>. Registro fuera del tiempo de tolerancia, por lo que se le recuerda que tiene 20 minutos antes y 20 minutos despu&eacute;s de su hora clase para que el sistema considere su registro como asistencia. Deber&aacute; acudir con su Jefe de Departamento a efectuar la justificaci&oacute;n correspondiente dentro de los siguientes 5 d&iacute;as h&aacute;biles; en caso contrario se proceder&aacute; conforme a la normatividad.</p>";
					break;
				
				case "jornada_retardo_fuera_tolerancia":
					$titulo = "Registro fuera de tiempo";
					$mensaje = "<p><b>C. $nombre</b></p> 
					<p align='justify'> Se han generado dos incidentes en el sistema el d&iacute;a <b>$today</b>.:</p><p align='justify'>1.- En su registro de Entrada a Jornada Laboral. Registro fuera del tiempo de tolerancia, por lo que le recordamos que tiene <b>40 minutos para que el sistema considere su registro como asistencia</b>. Lo anterior, de acuerdo a la cl&aacute;usula 59 del Contrato Colectivo del Trabajo del SUTUdeG, que a la letra dice:</p>
					<p align='justify'>&ldquo;<i>Se considerar&aacute; como retardo la presencia del trabajador despu&eacute;s de los 20 minutos de su hora de entrada y hasta 30 minutos, en las zonas urbanas, y despu&eacute;s de 30 minutos y hasta 40 en zonas especiales. <br /> En el caso de que el trabajador llegue despu&eacute;s del tiempo considerado como retardo, el jefe inmediato o el encargado de personal, deber&aacute; notificar por escrito al trabajador que debe retirarse, dentro de la hora siguiente a la registrada en su ingreso por el trabajador.</i>&rdquo;</p>
					<p align='justify'>2.- Registro fuera del tiempo de tolerancia, por lo que se le recuerda que tiene 20 minutos antes y 20 minutos despu&eacute;s de su hora clase para que el sistema considere su registro como asistencia.</p>
					<p align='justify'>Deber&aacute; acudir de inmediato a la Coordinaci&oacute;n de Personal con el justificante correspondiente, de lo contrario se proceder&aacute; conforme a la normatividad</p>";
					break;

				default:
					echo "Opcion de incidencia para correo invalida";
					die();
					break;
			}

			$mail = new PHPMailer(); 
			$mail->IsSMTP(); 			
			$mail->SMTPAuth = false;
			//$mail->SMTPSecure = "ssl"; 
			
			$mail->Host = MAIL_HOST; 
			$mail->Port = MAIL_PORT; 
			$mail->Username = MAIL_USERNAME; 
			$mail->Password = MAIL_PASSWORD;

			$mail->SetFrom (MAIL_USERNAME,"Sistema de Control de Asistencia(SiCA) CUVALLES"); 
			$mail->AddReplyTo('carmen.hernandez@valles.udg.mx',"Carmen Elvira Hernandez");

			$mail->Subject = "CUSUR SICA: - ".$titulo; 
			$mail->AltBody = "Para visualizar el mensaje favor de utilizar un visor de correos compatible con HTML"; 

			$mensaje = " <div align='justified'>".$mensaje."<p align='justify' style='color:grey;'>Este es un correo autom&aacute;tico generado por el Sistema de Control de Asistencia (SICA) del Centro Universitario de los Valles de la Universidad de Guadalajara. No es necesario que responda a este correo.</p><p>Sistema de Control de Asistencia, CUVALLES.</p></div>";	//  Favor de atender esta notificaci&oacute;n conforme se indica. se elimino por instrucciones de Marco Tulio

			$mail->MsgHTML($mensaje);
			$mail->IsHTML(true); 

			$mail->AddAddress($correo,$nombre);			
			$mail->AddCC("buzonsica@valles.udg.mx","SiCA");
			
			$flag = false;
			for ($i=1 ; $i<=3 && !$flag ; $i++){
				set_time_limit(60);
				$flag = $mail->Send();
				if(!$flag) { 
					echo $mail->ErrorInfo." intento ".$i."</br>"; 
				} else { 
					echo "Mensaje enviado correctamente, intento ".$i; 
				}
			}
			
		} else {
			echo "Usuario no cuenta con correo electronico o este es invalido";
		}
	}
	
?>