<?php 

//-------------------------Especificacion de la ruta-------------------------------------- 				
    include ("../globals.php");
    $ruta = $dir."capturas/";
	
	if ($_SERVER['REQUEST_METHOD'] == 'POST'){
		
		$statusAS = "iniciando POST";		
		$ok = true;
		
		if (isset($_FILES["archivo"])){
			$file = $_FILES['archivo'];			
		} else {
			$ok = false;
		}
		
		//validaciones del archivo
		if ($ok){
			if ( $file['error'] > 0 ) {						
				$ok = false;			
			} 
		}
		
		//si no hay ningun error guardamos archivo
		if ($ok){
			$archivo = $_FILES["archivo"]['name'];			
			$archivo = preg_replace('/\s+/', '', $archivo); //eliminamos espacios del nombre
			
			if ($archivo != "") {
				$destino =  $ruta.$archivo.".jpg";
				if (move_uploaded_file($_FILES['archivo']['tmp_name'],$destino)){
					$statusAS = "Archivo subido: <b>".$archivo."</b>";					
					$statusAS = $statusAS."<br/><b>Exitosamente!</b>";
				} else {
					$ok = false;
				}				
			} 
			
		} 
		
		if (!$ok){
			$statusAS = "Error subiendo archivo";
		}			
	
	}
?>
