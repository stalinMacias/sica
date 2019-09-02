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
        		//params: {host:sHost, screenW:screenW, screenH:screenH},
                jnlp_content : 'PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjxqbmxwIHNwZWM9IjEuMCIgeG1sbnM6amZ4PSJodHRwOi8vamF2YWZ4LmNvbSIgaHJlZj0iU2ljYVdlYjIuam5scCI+DQogIDxpbmZvcm1hdGlvbj4NCiAgICA8dGl0bGU+U2ljYVdlYjI8L3RpdGxlPg0KICAgIDx2ZW5kb3I+Q3VWYWxsZVM8L3ZlbmRvcj4NCiAgICA8ZGVzY3JpcHRpb24+bnVsbDwvZGVzY3JpcHRpb24+DQogICAgPG9mZmxpbmUtYWxsb3dlZC8+DQogIDwvaW5mb3JtYXRpb24+DQogIDxyZXNvdXJjZXM+DQogICAgPGoyc2UgdmVyc2lvbj0iMS42KyIgaHJlZj0iaHR0cDovL2phdmEuc3VuLmNvbS9wcm9kdWN0cy9hdXRvZGwvajJzZSIvPg0KICAgIDxqYXIgaHJlZj0iU2ljYVdlYjIuamFyIiBzaXplPSI1NDY3NzUiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxQREZSZW5kZXJlci0wLjkuMC5qYXIiIHNpemU9IjIxMjI3ODAiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxjb21tb25zLWxvZ2dpbmctMS4xLmphciIgc2l6ZT0iNTcwMTAiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxodHRwY2xpZW50LTQuMy5qYXIiIHNpemU9IjYzMTg0OSIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliXGh0dHBjb3JlLTQuMy5qYXIiIHNpemU9IjMwODA5MiIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliXGl0ZXh0cGRmLTUuMi4xLmphciIgc2l6ZT0iMTgwNjYyMyIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliXGpzb24tc2ltcGxlLTEuMS4xLmphciIgc2l6ZT0iMjYzMDgiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxsb2c0ai0xLjIuMTcuamFyIiBzaXplPSI1MTk4NTAiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxwb2ktMy4xMC5qYXIiIHNpemU9IjIwOTI3MzciIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxzY2h1ZHQtamF2YWZ4LWRhdGVwaWNrZXItMC4wLjIuamFyIiBzaXplPSI3MjYwNCIgZG93bmxvYWQ9ImVhZ2VyIiAvPg0KICAgIDxqYXIgaHJlZj0ibGliXHNpY2EtY29tbW9uLmphciIgc2l6ZT0iMjEzNDUxIiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWJcc2ljYXdlYi1jb3JlLmphciIgc2l6ZT0iNDM1NTQ4IiBkb3dubG9hZD0iZWFnZXIiIC8+DQogICAgPGphciBocmVmPSJsaWJcc2xmNGotYXBpLTEuNy41LmphciIgc2l6ZT0iMjk5NzIiIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgICA8amFyIGhyZWY9ImxpYlxzbGY0ai1sb2c0ajEyLTEuNy42LmphciIgc2l6ZT0iMTEwMzciIGRvd25sb2FkPSJlYWdlciIgLz4NCiAgPC9yZXNvdXJjZXM+DQo8c2VjdXJpdHk+DQogIDxhbGwtcGVybWlzc2lvbnMvPg0KPC9zZWN1cml0eT4NCiAgPGFwcGxldC1kZXNjICB3aWR0aD0iODAwIiBoZWlnaHQ9IjYwMCIgbWFpbi1jbGFzcz0iY29tLmphdmFmeC5tYWluLk5vSmF2YUZYRmFsbGJhY2siICBuYW1lPSJTaWNhV2ViMiIgPg0KICAgIDxwYXJhbSBuYW1lPSJyZXF1aXJlZEZYVmVyc2lvbiIgdmFsdWU9IjguMCsiLz4NCiAgPC9hcHBsZXQtZGVzYz4NCiAgPGpmeDpqYXZhZngtZGVzYyAgd2lkdGg9IjgwMCIgaGVpZ2h0PSI2MDAiIG1haW4tY2xhc3M9InNpY2F3Lk1haW4iICBuYW1lPSJTaWNhV2ViMiIgLz4NCiAgPHVwZGF0ZSBjaGVjaz0iYWx3YXlzIi8+DQo8L2pubHA+DQo='

            },
            {
                javafx : '8.0+'
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
    <img class="imgfooter" src="imgs/foot.jpg" /> 
    <!--b>Para ejecutar como aplicación </b> <a href='SiCAWeb.jnlp' onclick="return launchApplication('SiCAWeb.jnlp');"> haz click aquí</a><br><hr><br-->
  </div>
</body>

</html>
