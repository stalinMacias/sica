<?php

// host donde se hospeda la pagina
//$host =  "http://148.202.119.37/sica";  //"http://localhost/checador"; // //http://www.cuvalles.udg.mx/sica";
$host = "http://127.0.0.1/sica"; //Dirección para que trabaje en local!

// detalles de la conexion a mysql
//$dbhost =   			"148.202.119.37"; //'localhost';
$dbhost = "127.0.0.1";  //Conexión Local
$user =     				"frank"; //'frank';
$password = 				"frankvalles65"; //'frankvalles65';

// nombre de la base de datos utilizada
$db =       "checador";

// direccion de la carpeta en el disco duro
$dir = "C:\wamp64\www\sica";
//$dir = "C:\wamp\www\sica/";
//$dir = "C:\wamp\www\sica";


//detalles para el envio de correo electronico
//define("MAIL_HOST","148.202.119.16");
define("MAIL_HOST","smtp.google.com");  //Dirección local
define("MAIL_PORT",587);
define("MAIL_USERNAME","dario.vazquez@administrativos.udg.mx");
define("MAIL_PASSWORD","CUSur123"); //*/

/*/datos de cuenta GMail
define("MAIL_HOST","smtp.gmail.com");
define("MAIL_PORT",465);
define("MAIL_USERNAME","sica.cuvalles@gmail.com");
define("MAIL_PASSWORD","sica1234");//*/

?>
