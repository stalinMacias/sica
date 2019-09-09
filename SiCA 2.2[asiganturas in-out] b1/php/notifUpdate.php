<?php
header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');

include ("conectar.php");
	
if ($result = $con->query( "SELECT * FROM actualizaciones ORDER BY actualizado DESC LIMIT 1")) {
      
   while ($fila = $result->fetch_row()) {

      echo "data: lastUpdate {$fila[1]}\n\n";      
			
   }

}
   
flush();

?>