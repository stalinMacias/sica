<?php
	date_default_timezone_set ("America/Mexico_City");
	echo date("g:i:s");
	
/*
	include("conectar.php");
	include("class.phpmailer.php"); 		

	$mail = new PHPMailer(); 
	$mail->IsSMTP(); 		
	$mail->SMTPDebug  = 1;

	$mail->SMTPAuth = false;
	//$mail->SMTPSecure = "ssl"; 

	$mail->Host = "148.202.89.10"; 
	$mail->Port = 25;//465;//25; 
	$mail->Username = "sica@valles.udg.mx"; 
	$mail->Password = "hola123";

	$mail->SetFrom ("sica@valles.udg.mx","Sistema de Control de Asistencia(SiCA) CUVALLES"); 
	$mail->AddReplyTo('sica@valles.udg.mx',"Sistema de Control de Asistencia(SiCA) CUVALLES");

	$mail->Subject = "CUVALLES SICA: - TestMail"; 
	$mail->AltBody = "Para visualizar el mensaje favor de utilizar un visor de correos compatible con HTML"; 

	$mensaje = " <div align='justified'><p align='justify'><font color='grey'>Este es un correo autom&aacute;tico generado por el Sistema de Control de Asistencia (SICA) del Centro Universitario de los Valles de la Universidad de Guadalajara. No es necesario que responda a este correo.</font></p></div>";							

	$mail->MsgHTML($mensaje);
	$mail->IsHTML(true); 

	$mail->AddAddress("uh.hugo@gmail.com","Hugo Uh");
	
	$flag = false;
	for ($i=1 ; $i<2 && !$flag ; $i++){
		set_time_limit(60);
		$flag = $mail->Send();
		if(!$flag) { 
			echo "</br>".$mail->ErrorInfo." intento ".$i."</br>"; 
		} else { 
			echo "Mensaje enviado correctamente, intento ".$i; 
		}
	}

*/
/*-------------------------------------------------------------------------------------------


	//error_reporting(E_ALL);
//error_reporting(E_STRICT);

date_default_timezone_set('America/Toronto');

require_once('class.phpmailer.php');
//include("class.smtp.php"); // optional, gets called from within class.phpmailer.php if not already loaded

$mail             = new PHPMailer();

//$body             = file_get_contents('contents.html');
//$body             = eregi_replace("[\]",'',$body);
$body   = "'<h1>Se ha registrado exitosamente al Evento ''</h1>'";

$mail->IsSMTP(); // telling the class to use SMTP

$mail->SMTPAuth   = false;                  // enable SMTP authentication
  $mail->Host       = "148.202.89.10"; // sets the SMTP server
  //$mail->Port       = 443;                    // set the SMTP port for the GMAIL server
  $mail->Port       = 25;
  $mail->Username   = "escritoriode_ayuda@valles.udg.mx"; // SMTP account username
  //$mail->Password   = "ayuda";       
$mail->SMTPDebug  = 1;                     // enables SMTP debug information (for testing)
                                           // 1 = errors and messages
                                           // 2 = messages only

$mail->SetFrom('escritoriode_ayuda@valles.udg.mx', '4to. Encuentro estudiantes');

$mail->AddReplyTo("escritoriode_ayuda@valles.udg.mx","4to. Encuentro estudiantes");

$mail->Subject    = "¡¡Registro Exitoso!!";

$mail->AltBody    = "To view the message, please use an HTML compatible email viewer!"; // optional, comment out and test

$mail->MsgHTML($body);

$address = "sica@valles.udg.mx";
$mail->AddAddress($address, "SICA");

//$mail->AddAttachment("images/phpmailer.gif");      // attachment
//$mail->AddAttachment("images/phpmailer_mini.gif"); // attachment

if(!$mail->Send()) {
  echo "Mailer Error: " . $mail->ErrorInfo;
} else {
  echo "Message sent!";
}


/* -----------------------------------------------------------------------------------------


date_default_timezone_set('America/Toronto');

require_once('../class.phpmailer.php');
//include("class.smtp.php"); // optional, gets called from within class.phpmailer.php if not already loaded

$mail             = new PHPMailer();

//$body             = file_get_contents('contents.html');
//$body             = eregi_replace("[\]",'',$body);

$body    = "Nuevo participante registrado de: ".$centroU."'<br />'"."Número de registro: ".$numRegistro."'<br/>'"."'<a href='http://www.web.valles.udg.mx/encuentro/?q=inscripciones'>Ir a consultar</a>'";

$mail->IsSMTP(); // telling the class to use SMTP
$mail->Host       = "148.202.89.5"; // SMTP server
//$mail->SMTPDebug  = 2;                     // enables SMTP debug information (for testing)
                                           // 1 = errors and messages
                                           // 2 = messages only

$mail->SetFrom('portal@valles.udg.mx', '4to. Encuentro estudiantes');

$mail->AddReplyTo("portal@valles.udg.mx","4to. Encuentro estudiantes");

$mail->Subject    = "Nuevo participante registrado a su evento: ".$evento;

$mail->AltBody    = "To view the message, please use an HTML compatible email viewer!"; // optional, comment out and test

$mail->MsgHTML($body);

$address="teresa.alarcon@profesores.valles.udg.mx";
    //$address="amaury.suarezr@profesores.valles.udg.mx";

$mail->AddAddress($address, "Teresa Alacrón");
//$mail->AddCC("teresa.alarcon@profesores.valles.udg.mx","Administrador General");
$mail->AddAttachment("images/phpmailer.gif");      // attachment
$mail->AddAttachment("images/phpmailer_mini.gif"); // attachment

if(!$mail->Send()) {
  echo "Mailer Error: " . $mail->ErrorInfo;
} else {
  echo "Message sent!";
}
		
$mailConf             = new PHPMailer();

/*$body             = file_get_contents('contents.html');
$body             = eregi_replace("[\]",'',$body);
$bodyConf   = "'<h1>Se ha registrado exitosamente a ".$evento."</h1>'";

$mailConf ->IsSMTP(); // telling the class to use SMTP
$mailConf ->Host       = "148.202.89.5"; // SMTP server
//$mail->SMTPDebug  = 2;                     // enables SMTP debug information (for testing)
                                           // 1 = errors and messages
                                           // 2 = messages only

$mailConf ->SetFrom('portal@valles.udg.mx', '4to. Encuentro estudiantes');

$mailConf ->AddReplyTo("portal@valles.udg.mx","4to. Encuentro estudiantes");

$mailConf ->Subject    = "¡¡Registro Exitoso!!";

$mailConf ->AltBody    = "To view the message, please use an HTML compatible email viewer!"; // optional, comment out and test

$mailConf ->MsgHTML($bodyConf);

$addressConf = $email;
$mailConf ->AddAddress($address, $nombre);

$mailConf ->AddAttachment("images/phpmailer.gif");      // attachment
$mailConf ->AddAttachment("images/phpmailer_mini.gif"); // attachment

if(!$mailConf->Send()) {
  echo "Mailer Error: " . $mail->ErrorInfo;
} else {
  echo "Message sent!";
}



*/








?>