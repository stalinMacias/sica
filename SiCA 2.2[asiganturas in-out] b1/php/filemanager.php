<?php
  /* Parametros
   * nombre - nombre del archivo
   * type - Operacion a realizar (u)pload, (d)elete, (r)ename, (l)ist
   * opc - Opcion del tipo de archivo (j)ustificante, (f)oto, (c)aptura
   * oldname - nombre viejo del archivo ( rename )
   */

  include ("../globals.php");  

  if (isset($_GET['nombre']) && isset($_GET['type']) && isset($_GET['opc'])){
    $t = $_GET['type'];

    switch ($_GET['opc']) {
      case 'j': $ruta = $dir."justificantes/"; break;
      case 'f': $ruta = $dir."Fotos/"; break;
      case 'c': $ruta = $dir."capturas/"; break;
      //default: $ruta = $dir."archivos/"; break;
    }    

    $filename= $ruta.$_GET['nombre'];
    echo($t.$_GET['opc']."-".$filename."-");

    //delete
    if ($t == "d" && file_exists($filename)){ 
      echo(unlink($filename)? "Archivo eliminado." : "Error eliminando archivo.");                   
    
    //upload
    } else if ($t == "u"){ 
      $fileData = file_get_contents('php://input');
      $fhandle = fopen($filename, 'wb');
      $fwrite = fwrite($fhandle, $fileData);      
      fclose($fhandle);      
      echo(($fwrite===false)? "Error subiendo archivo" : "Archivo subido");

    //rename
    } else if ($t == "r" && isset($_GET['oldname'])) { 
      $oldfile = $ruta.$_GET['oldname'];
      $rename = rename($oldfile,$filename);
      echo($rename? "Archivo renombrado" : "Error renombrando archivo");
    
    } else if ($t == "l" ) {      
      foreach (glob("$filename*") as $files) {
          $files = substr(strrchr($files, "/"), 1);
          echo $files."\n";
      }
    }

  } else {
    echo("Parametros invalidos");
  }
?>