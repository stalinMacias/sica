<?php
    include ("conectar.php");

    if (isset($_GET["query"]) || isset($_POST["query"])){
        
        $query = isset($_GET["query"])? $_GET["query"] : $_POST["query"];   

        if(!$result = mysqli_query($con, $query)) die(); 

        $rawdata = array(); 
     
        while($row = mysqli_fetch_assoc($result)) {
            $rawdata[] = array_map('utf8_encode', $row);
        }

        //var_dump($rawdata);

        echo json_encode($rawdata);

        mysqli_free_result($result);            
        
    } 

?>