<html><head>
  <?php 
    include("globals.php");
  ?>
  <SCRIPT src="./web-files/dtjava.js"></SCRIPT>
<script>
    function javafxEmbed() {
        var sHost = "<?php echo $host; ?>";
        var screenH = 700;
        var screenW = 1280;
        if (parseInt(navigator.appVersion)>3) {
            screenW = screen.width -50;
            screenH = screen.height -150;
            
        } else if (navigator.appName === "Netscape" 
           && parseInt(navigator.appVersion)===3
           && navigator.javaEnabled()) {
        
            var jToolkit = java.awt.Toolkit.getDefaultToolkit();
            var jScreenSize = jToolkit.getScreenSize();
            screenW = jScreenSize.width -50;
            screenH = jScreenSize.height -150;         
        }
        
        if (screenH > 1000){
            screenH = 1000;
        } else if (screenH < 650){
            screenH = 650;
        }
        if (screenW > 1280){
            screenW = 1280;
        }
              
        dtjava.embed(
            {
                url : 'SicaWeb2.jnlp',
                placeholder : 'javafx-app-placeholder',
                width : screenW,
                height : screenH,
        		params: {host:sHost, screenW:screenW, screenH:screenH},
                jnlp_content : 'PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjxqbmxwIHNwZWM9IjEuMCIgeG1sbnM6amZ4PSJodHRwOi8vamF2YWZ4LmNvbSIgaHJlZj0iU2ljYVdlYjIuam5scCI+DQogIDxpbmZvcm1hdGlvbj4NCiAgICA8dGl0bGU+U2ljYVdlYjI8L3RpdGxlPg0KICAgIDx2ZW5kb3I+Q3VWYWxsZVM8L3ZlbmRvcj4NCiAgICA8ZGVzY3JpcHRpb24+bnVsbDwvZGVzY3JpcHRpb24+DQogICAgPG9mZmxpbmUtYWxsb3dlZC8+DQogIDwvaW5mb3JtYXRpb24+DQogIDxyZXNvdXJjZXM+DQogICAgPGpmeDpqYXZhZngtcnVudGltZSB2ZXJzaW9uPSIyLjIrIiBocmVmPSJodHRwOi8vamF2YWRsLnN1bi5jb20vd2ViYXBwcy9kb3dubG9hZC9HZXRGaWxlL2phdmFmeC1sYXRlc3Qvd2luZG93cy1pNTg2L2phdmFmeDIuam5scCIvPg0KICA8L3Jlc291cmNlcz4NCiAgPHJlc291cmNlcz4NCiAgICA8ajJzZSB2ZXJzaW9uPSIxLjYrIiBocmVmPSJodHRwOi8vamF2YS5zdW4uY29tL3Byb2R1Y3RzL2F1dG9kbC9qMnNlIi8+DQogICAgPGphciBocmVmPSJTaWNhV2ViMi5qYXIiIHNpemU9IjUxOTMxMSIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL1BERlJlbmRlcmVyLTAuOS4wLmphciIgc2l6ZT0iMjEyMjc4NCIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL2NvbW1vbnMtbG9nZ2luZy0xLjEuamFyIiBzaXplPSI1Njk5MyIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL2h0dHBjbGllbnQtNC4zLmphciIgc2l6ZT0iNjMxODMyIiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWIvaHR0cGNvcmUtNC4zLmphciIgc2l6ZT0iMzA4MDc5IiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWIvaXRleHRwZGYtNS4yLjEuamFyIiBzaXplPSIxODA2NjUxIiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWIvanNvbi1zaW1wbGUtMS4xLjEuamFyIiBzaXplPSIyNjI5OCIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL2xvZzRqLTEuMi4xNy5qYXIiIHNpemU9IjUxOTg1NyIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL3BvaS0zLjEwLmphciIgc2l6ZT0iMjA5MjY4NCIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL3NjaHVkdC1qYXZhZngtZGF0ZXBpY2tlci0wLjAuMi5qYXIiIHNpemU9IjcyNTg3IiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWIvc2ljYS1jb21tb24uamFyIiBzaXplPSIyMDg1NjkiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYi9zaWNhd2ViLWNvcmUuamFyIiBzaXplPSI0MzU1NjIiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYi9zbGY0ai1hcGktMS43LjUuamFyIiBzaXplPSIyOTk1NCIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliL3NsZjRqLWxvZzRqMTItMS43LjYuamFyIiBzaXplPSIxMTAzMSIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICA8L3Jlc291cmNlcz4NCjxzZWN1cml0eT4NCiAgPGFsbC1wZXJtaXNzaW9ucy8+DQo8L3NlY3VyaXR5Pg0KICA8YXBwbGV0LWRlc2MgIHdpZHRoPSI4MDAiIGhlaWdodD0iNjAwIiBtYWluLWNsYXNzPSJjb20uamF2YWZ4Lm1haW4uTm9KYXZhRlhGYWxsYmFjayIgIG5hbWU9IlNpY2FXZWIyIiA+DQogICAgPHBhcmFtIG5hbWU9InJlcXVpcmVkRlhWZXJzaW9uIiB2YWx1ZT0iMi4yKyIvPg0KICA8L2FwcGxldC1kZXNjPg0KICA8amZ4OmphdmFmeC1kZXNjICB3aWR0aD0iODAwIiBoZWlnaHQ9IjYwMCIgbWFpbi1jbGFzcz0ic2ljYXcuTWFpbiIgIG5hbWU9IlNpY2FXZWIyIiAvPg0KICA8dXBkYXRlIGNoZWNrPSJhbHdheXMiLz4NCjwvam5scD4NCg=='
            },
            {
                javafx : '2.2+'
            },
            {}
        );		
		document.querySelector(".container").style.width = screenW;
		document.querySelector(".imgheader").style.width = screenW;
		document.querySelector(".imgfooter").style.width = screenW;
    }
    <!-- Embed FX application into web page once page is loaded -->
    dtjava.addOnloadCallback(javafxEmbed);

</script>

</head>

<body bgcolor="#999999" >
  <div class="container" style="width:1280; margin: auto">
    <img class="imgheader" src="imgs/header.jpg" />
    <!-- Applet will be inserted here -->
    <div id='javafx-app-placeholder'></div>
    <img src="imgs/comentario.jpg" /> 
    <a title="Descarga Sica" href="http://148.202.89.3/sica/web-files/Sica_Web_2.zip"><img src="imgs/descargasica2.1.png" alt="Descargar sica web" /></a>
    <img src="web-files/espacio_blanco.jpg" /> 
    <img class="imgfooter" src="imgs/foot.jpg" /> 
    <!--b>Para ejecutar como aplicación </b> <a href='SiCAWeb.jnlp' onclick="return launchApplication('SiCAWeb.jnlp');"> haz click aquí</a><br><hr><br-->
  </div>
</body>

</html>
