<?php
  echo "
  
      //faltas dia
      https://api.atrigger.com/v1/tasks/create?
      key=5692867374684094416
      &secret=9XPL45Q7f54neE9v8jV8dxWsEV4m72
      &timeSlice=1day
      &count=-1
      &tag_tipo=faltaDia
      &tag_usoHorario=Invierno
      &first=2014-08-20T06%3A05%3A00Z
      &url=http%3A%2F%2F148.202.89.3%2Fsica%2Fphp%2Fcorreofaltajornada.php


      //Faltas clases     

      *** horario Verano + 5 horas ***

      https://api.atrigger.com/v1/tasks/create?
      key=5692867374684094416
      &secret=9XPL45Q7f54neE9v8jV8dxWsEV4m72
      &timeSlice=1hour
      &count=-1
      &tag_tipo=faltaClase
      &first=2014-08-19T19%3A03%3A00Z
      &url=http%3A%2F%2F148.202.89.3%2Fsica%2Fphp%2Fcorreofaltaclase.php


      *** horario invierno + 6 horas ***

      https://api.atrigger.com/v1/tasks/create?
      key=5692867374684094416
      &secret=9XPL45Q7f54neE9v8jV8dxWsEV4m72
      &timeSlice=1day
      &count=-1
      &tag_tipo=faltaClase
      &tag_horarioClase=8am
      &tag_horarioEjecucion=10hrs
      &tag_usoHorario=Invierno
      &first=2014-08-17T16%3A00%3A00Z
      &url=http%3A%2F%2F148.202.89.3%2Fsica%2Fphp%2Fcorreofaltaclasehora.php%3Fhorario%3D08%3A00%3A00
    ";

?>