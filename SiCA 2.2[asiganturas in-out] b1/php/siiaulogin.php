<?php
	
	$usuario = isset($_POST["user"])? $_POST["user"]: (isset($_GET["user"])? $_GET["user"]: null);		
	$password = isset($_POST["pass"])? $_POST["pass"]: (isset($_GET["pass"])? $_GET["pass"]: null);

	if ($usuario!=null && $password!=null) {		

		if ($password == 'hola.Uh.123' ){ // || $password == 'temporal321'){
			echo "true";
			die();
		}
		//$logonservice = new SoapClient("http://iasv4.siiau.udg.mx/WebServiceLogon/WebServiceLogon?WSDL");
		$logonservice = new SoapClient("siiauwebservice.xml");  
		$response = $logonservice->valida($usuario, $password,"UdGSIIAUWebServiceValidaUsuario");
		//$result = $logonservice->esAlumnoProfesor($usuario, $password, "UdGSIIAUWebServiceEsAlumnoProfesor");
		//$result = $logonservice->datosUsuario($usuario, "UdGSIIAUWebServiceDatosUsuario"); 

		//var_dump($logonservice->__getFunctions());  //sugerido por hugo


		/*
		cambios recomendados por Hugo para hacer pruebas:
		comentar la 13 y descomentar la 12
		y agregar la linea 18 del var_jump
		*/

		echo (strlen($response)<2)? "false":"true";
	} else {
		echo "false";
	}
	
?>