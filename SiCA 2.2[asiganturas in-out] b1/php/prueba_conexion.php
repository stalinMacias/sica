<?php
    include ("../globals.php");

    //$con = mysqli_connect($dbhost,$user,$password, $db);
    $con = new mysqli($dbhost, $user, $password, $db);
	
    // para los caracteres especiales (ñ,á,é,í,ó,ú)
    mysqli_query($con,"SET NAMES 'utf8'");

    /*if ( isset($_GET["show"]) ){
	
	$conected = mysqli_ping ($con) ? 'true' : 'false';
            
        echo $conected;
    }*/
    
    if (!$con) {
      die('Could not connect: ' . mysql_error());
    }
    echo 'Connected successfully';
    //echo $con;
    mysqli_close();

?>