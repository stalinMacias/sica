package sicaweb.reportes;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;

public class Reporte {
    private static int CANT_REPORTES = 0;    
    protected final String FILE_TEMP;
    protected final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
    
    public Reporte(String ext){        
        FILE_TEMP = System.getProperty("java.io.tmpdir")+"Reporte_temp"+CANT_REPORTES+ext;
        CANT_REPORTES++;        
    }
    
    protected void end() { 
        if (Desktop.isDesktopSupported()){            
            File file = new File(FILE_TEMP); 
            file.deleteOnExit();
            
            try {                
                Desktop.getDesktop().open(file);
                Thread.sleep(500);
                
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
    
}
