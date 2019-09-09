package sicaweb;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.collections.ObservableList;
import sica.common.Utils;

public class PDFJustificantes {
    
    public static boolean validate(File file){
        try {
            PdfReader reader = new PdfReader(file.toString());
            int pages = reader.getNumberOfPages();
            if ( pages > 10){                
                return false;
            }
        } catch (IOException e){
             e.printStackTrace(System.out);
        }                
        return true;
    }
    
    public static File concatenateFiles(ObservableList<File> archivos){        
              
        File file = Utils.getTempFile(archivos.hashCode()+".pdf");
        
        try(FileOutputStream fos = new FileOutputStream(file)) {
            
            Document document = new Document();  
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            writer.open();
            document.open();
            
            for ( File f : archivos ){

                if (f.getName().toLowerCase().endsWith("jpg")){
                    Image instance = Image.getInstance(f.toString());
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                    - document.rightMargin()) / instance.getWidth()) * 100;
                    instance.scalePercent(scaler);
                    instance.setAlignment(Image.ALIGN_CENTER|Image.ALIGN_MIDDLE);      
                    
                    document.newPage();
                    document.add(instance);                                        

                } else if (f.getName().toLowerCase().endsWith("pdf")){
                    PdfReader reader = new PdfReader(f.toString());
                    PdfContentByte cb = writer.getDirectContent();
                    
                    for (int i=1; i<=reader.getNumberOfPages();i++){
                        PdfImportedPage page = writer.getImportedPage(reader, i); 
                        document.newPage();
                        cb.addTemplate(page, 0, 0);
                    }
                    
                    reader.close();
                }
            }
            
            document.close();
            writer.close();
            fos.close();
            
        } catch (DocumentException | IOException e) {
            e.printStackTrace(System.out);
        }
        
        return file;
    }
    
}
