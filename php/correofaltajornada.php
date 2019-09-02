<?php
	include("conectar.php");
	include("class.phpmailer.php"); 
	include("class.smtp.php");				
	date_default_timezone_set ("America/Mexico_City");
	
	$query = 'CALL get_faltas_jornada_obligatoria_dia_anterior';
	if ($result = mysqli_query($con, $query)) {
		$today = date("d/m/Y",strtotime("yesterday"));
		set_time_limit(60);	

		while($row = mysqli_fetch_assoc($result)) {		
			//echo var_dump($row);

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

				$mail->SetFrom (MAIL_USERNAME,"Sistema de Control de Asistencia(SiCA) CUSUR"); 
				//$mail->AddReplyTo('carmen.hernandez@valles.udg.mx',"Carmen Elvira Hernandez");
				
				$mensaje = "<p><b>C. $row[nombre] </b></p>";

				if ( is_null($row["registroentrada"]) && is_null($row["registrosalida"])){
					$asunto = "CUSUR SICA - Falta jornada laboral";
					
					$mensaje.="<p align='justify'>Se ha generado una falta en el sistema el d&iacute;a <b>$today</b>, con <b>ausencia de registros de entrada y salida</b> en su jornada laboral. Por lo que se le recuerda que tiene 3 d&iacute;as h&aacute;biles para efectuar la justificaci&oacute;n correspondiente, de lo contrario proceder&aacute; conforme a la normatividad.</p>"; 

				} else if ( $row["registroentrada"] === $row["registrosalida"]){
					$asunto = "CUSUR SICA - Ausencia de Registro";

					$mensaje.="<p align='justify'>Se ha generado un incidente en el sistema el d&iacute;a <b>$today</b>, por <b>ausencia de registro en la entrada &oacute; salida</b> de su jornada laboral.<br /> Lo invitamos a cumplir con las jornadas y horarios de trabajo, debiendo registrar sus asistencias tanto al inicio como al t&eacute;rmino de su jornada laboral.</p>
						<p align='justify'>Deber&aacute; acudir con su jefe inmediato a notificarlo y justificarlo en la Coordinaci&oacute;n de Personal.</p>"; 

				} else {
					echo "Error evaluando situacion</br>";
					continue;
				}

				$mensaje = "<div align='justified'>".$mensaje."<p align='justify' style='color:grey;'>Este es un correo autom&aacute;tico generado por el Sistema de Control de Asistencia (SICA) del Centro Universitario del sur de la Universidad de Guadalajara. No es necesario que responda a este correo.</p><p>Sistema de Control de Asistencia, CUSUR.</p></div>";  //Favor de atender esta notificaci&oacute;n conforme se indica. se elimino por indicacione de Marco Tulio
                                                                                                                                                                                                                                                                                                                                                          
				$mail->Subject = $asunto; 				
				$mail->MsgHTML($mensaje);
				$mail->IsHTML(true); 

				$mail->AddAddress($row["correo"],$row["nombre"]);
				$mail->AddCC("buzonsica@cusur.udg.mx","SiCA");				
				
				//echo $mensaje;

				$flag = false;
				for ($i=1 ; $i<4 && !$flag ; $i++){
					set_time_limit(60);
					$flag = $mail->Send();
					if(!$flag) { 
						echo "Error: " . $mail->ErrorInfo. ", Intento $i </br>"; 
					} else { 
						echo "Mensaje enviado correctamente, Intento $i </br>"; 
					}
				}
				
			} else {
				echo "Error: usuario no cuenta con correo electronico o este es invalido </br>";
			}
			
		}
		mysqli_free_result($result);

	} else {
		echo mysqli_error($con);
	}


?>