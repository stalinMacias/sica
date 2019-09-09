<?php
	include("conectar.php");
	include("class.phpmailer.php"); 		
	date_default_timezone_set ("America/Mexico_City");
	
	if (isset($_POST["opcion"]) && isset($_POST["correo"]) && isset($_POST["nombre"]) ){

		if (filter_var($correo, FILTER_VALIDATE_EMAIL)){
			$today = date("d/m/Y");  

			switch ($opcion) {
				case "rechazado":
					$titulo = "Justificante rechazado";
					$mensaje = "<p><b>C. $nombre</b></p>
					<p align='justify'> Se ha generado una <b>falta</b> en el sistema el d&iacute;a <b>$today</b>, en su registro de Entrada a Jornada Laboral. <br />Registro fuera del tiempo de tolerancia, por lo que le recordamos que tiene <b>40 minutos para que el sistema considere su registro como asistencia</b>. Lo anterior, de acuerdo a la cl&aacute;usula 59 del Contrato Colectivo del Trabajo del SUTUdeG, que a la letra dice:</p>
					<p align='justify'>&ldquo;<i>Se considerar&aacute; como retardo la presencia del trabajador despu&eacute;s de los 20 minutos de su hora de entrada y hasta 30 minutos, en las zonas urbanas, y despu&eacute;s de 30 minutos y hasta 40 en zonas especiales. <br /> En el caso de que el trabajador llegue despu&eacute;s del tiempo considerado como retardo, el jefe inmediato o el encargado de personal, deber&aacute; notificar por escrito al trabajador que debe retirarse, dentro de la hora siguiente a la registrada en su ingreso por el trabajador.</i>&rdquo;</p>
					<p align='justify'>Deber&aacute; acudir de inmediato a la Coordinaci&oacute;n de Personal con el justificante correspondiente, de lo contrario se proceder&aacute; conforme a la normatividad</p>";
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

			$mail->SetFrom (MAIL_USERNAME,"Sistema de Control de Asistencia(SiCA) CUSUR"); 
			//$mail->AddReplyTo('carmen.hernandez@valles.udg.mx',"Carmen Elvira Hernandez");

			$mail->Subject = "CUSUR SICA: - ".$titulo; 
			$mail->AltBody = "Para visualizar el mensaje favor de utilizar un visor de correos compatible con HTML"; 

			$mensaje = " <div align='justified'>".$mensaje."<p align='justify' style='color:grey;'>Este es un correo autom&aacute;tico generado por el Sistema de Control de Asistencia (SICA) del Centro Universitario del sur de la Universidad de Guadalajara. No es necesario que responda a este correo.</p><p>Sistema de Control de Asistencia, CUSUR.</p></div>"; // Favor de atender esta notificaci&oacute;n conforme se indica. se elimino por indicaciones de Marco Tulio							

			$mail->MsgHTML($mensaje);
			$mail->IsHTML(true); 

			$mail->AddAddress($correo,$nombre);			
			$mail->AddCC("sicacusur@cusur.udg.mx","SiCA");
			
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