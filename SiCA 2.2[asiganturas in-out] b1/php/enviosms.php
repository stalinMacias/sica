<?php

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
	echo "enviosms.php - GET</br>";
	include('/Requests/library/Requests.php');
	Requests::register_autoloader();
	$response = Requests::post('http://148.202.119.37/sica/php/enviosms.php');
	echo ($response->body);

} else {
	echo "enviosms.php - POST</br>";
	require "/twilio-php/Services/Twilio.php";
	 
	// set your AccountSid and AuthToken from www.twilio.com/user/account
	$AccountSid = "ACf5a571893b101b8d446028c63abe9c18";
	$AuthToken = "049575b13a45313840576b42afe3891e";
	 
	$client = new Services_Twilio($AccountSid, $AuthToken);
	try{
		$message = $client->account->messages->create(array(
		    'From' => "+12097875211",
		    'To'   => "+523318481192",
		    'Body' => "Se ha rechazado el justificante Folio XXXXX, para consultar detalles acceder a sica.cuvalles.udg.mx SiCA-CUValles",
		));
		echo $message;

	} catch (Services_Twilio_RestException $e) {
	    echo $e->getMessage();
	}
}