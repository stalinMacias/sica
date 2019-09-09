<?php
	
	$globalsPath =  dirname(dirname(__FILE__))."\globals.php";  
	
	include_once($globalsPath);

    $con = new mysqli($dbhost, $user, $password, $db);
	
    if ( isset($_GET["show"])){
	
	    $conected = mysqli_ping ($con) ? 'true' : 'false';            
        echo $conected;
    }
    

?>