package sica;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sica.common.Utils;

public class CapturaUploader extends Thread implements Runnable {        
    private final static Logger log = LoggerFactory.getLogger(CapturaUploader.class);
    
    private final BufferedImage image;
    private final String name;   

    private CapturaUploader(BufferedImage image, String name) {
        this.image = image;
        this.name = name;
    }    
    
    @Override public void run() {        
        try {
            if (name == null) {
                log.error("Nombre no establecido");
                return;
            }
            
            HttpURLConnection httpUrlConnection = (HttpURLConnection)
                    new URL( Configs.PHP_UPLOAD() +
                            "?type=u"+
                            "&opc=c"+
                            "&nombre="+name)
                            .openConnection();
            
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
             
            
            try (OutputStream os = httpUrlConnection.getOutputStream()) {
                Thread.sleep(1000);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                ByteArrayInputStream fis = new ByteArrayInputStream(baos.toByteArray());
                int i;
                while ( (i = fis.read()) != -1) {
                    os.write(i);
                }                
            }
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    httpUrlConnection.getInputStream()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    log.info(s);
                }
            }
        } catch (IOException | InterruptedException e){
            log.error(e.getMessage());
        }             
    }
    
    public static void upload(UserData userData, BufferedImage image) {
        
        if ( !Utils.urlExist(Configs.PHP_UPLOAD()) || image==null){
            log.info("Error subiendo fotografia");
        
        } else {
            String nom = userData.getUsuario().getCodigo() + "_"
                    + userData.getFechaServidor() + "_"
                    + userData.getHoraServidor().replace(':', '-')
                    + ".jpg";
            
            log.info("Comenzando subida de foto {} a servidor", nom);
            
            new CapturaUploader(image, nom).start();            
        }
    }
    
}
