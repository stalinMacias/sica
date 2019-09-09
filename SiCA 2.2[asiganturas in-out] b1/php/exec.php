<?php
    
    if (isset($_POST["query"])){
        $query = $_POST["query"];            
        
    } else if (isset($_GET["query"])){
        $query = $_GET["query"];                    
    }

    if (isset($query)){
    	include ("conectar.php");
        $result = mysqli_query($con, $query);
        
        if ( $result && (mysqli_affected_rows($con) > 0) ){
            echo 'true';
        } else {
            echo 'false';
        }
        
    } else {
    	echo 'false';
    }
?>

