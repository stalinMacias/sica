<?php		
	date_default_timezone_set("America/Mexico_City");
	$hora = date("H") - 2;	

	if ($hora == 8 || $hora==10 || $hora==12 || $hora==14 || $hora==16 ){	
		include("conectar.php");
		include("class.phpmailer.php"); 
		include("class.smtp.php");	

		$query = "CALL get_faltas_clases_hora('$hora:00:00')";
		//echo $query;
		
		if ($result = mysqli_query($con, $query)) {
			$today = date("d/m/Y");  

			while($row = mysqli_fetch_assoc($result)) {
				echo var_dump($row);

				if (filter_var($row["correo"], FILTER_VALIDATE_EMAIL)) {
				    //enviar email

				    $mail = new PHPMailer(); 
					$mail->IsSMTP(); 
					$mail->SMTPAuth = false; 
					//$mail->SMTPSecure = "ssl"; 
					
					$mail->Host = MAIL_HOST; 
					$mail->Port = MAIL_PORT; 
					$mail->Username = MAIL_USERNAME; 
					$mail->Password = MAIL_PASSWORD;

					$mail->SetFrom (MAIL_USERNAME,"Sistema de Control de Asistencia(SiCA) CUVALLES"); 
					//$mail->AddReplyTo('carmen.hernandez@valles.udg.mx',"Carmen Elvira Hernandez");

					$mail->Subject = "CUSUR SICA - Falta a asignatura"; 
					$mail->AltBody = "Correo de incidencia"; 
					
					$mensaje = "<div align='justified'>	<p><b>Acad&eacute;mico $row[nombre] </b></p>
						<p align='justify'>Se ha generado una falta en el sistema el d&iacute;a <b>$row[dia] $today</b>, en la materia <b>$row[materia] ($row[crn])</b> registrada en el horario <b>$row[horario] hrs.</b> por ausencia de registro. Deber&aacute; acudir con su Jefe de Departamento a efectuar la justificaci&oacute;n correspondiente; se le recuerda que tiene 5 d&iacute;as h&aacute;biles para presentarla, de lo contrario se proceder&aacute; conforme a la normatividad.</p>
						<p align='justify' style='color:grey;'>Este es un correo autom&aacute;tico generado por el Sistema de Control de Asistencia (SICA) del Centro Universitario del sur de la Universidad de Guadalajara. No es necesario que responda a este correo.</p> 
						<p>Sistema de Control de Asistencia, CUVALLES.</p></div>"; 		//Favor de atender esta notificaci&oacute;n conforme se indica. se elimino del correo auntomatico indicaciones de Marco Tulio			

					$mail->MsgHTML($mensaje);
					$mail->IsHTML(true); 
					$mail->AddAddress($row["correo"],$row["nombre"]);					
					$mail->AddCC("sicacusur@cusur.udg.mx","SiCA");

					$flag = false;
					for ($i=1 ; $i<4 && !$flag ; $i++){
						set_time_limit(60);
						$flag = $mail->Send();
						if(!$flag) { 
							echo "Error: " . $mail->ErrorInfo."</br>"; 
						} else { 
							echo "Mensaje enviado correctamente</br>"; 
						}
					}
					
				} else {
					echo "Error: usuario no cuenta con correo electronico o este es invalido</br>";
				}
			}
			mysqli_free_result($result);

		} else {
			echo mysqli_error($con);
		}
	} else {
		echo $hora.":00:00";
	}
?>