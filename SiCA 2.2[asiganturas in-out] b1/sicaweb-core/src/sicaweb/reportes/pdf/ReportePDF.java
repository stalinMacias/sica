package sicaweb.reportes.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.swing.ImageIcon;
import sicaweb.reportes.Reporte;

public abstract class ReportePDF extends Reporte implements Runnable {
    
    protected Font fBold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLDITALIC);
    protected Font fReg = new Font(Font.FontFamily.TIMES_ROMAN,8);
    private final BooleanProperty loading;
        
    public ReportePDF(){
        super(".pdf");
        loading = new SimpleBooleanProperty(false);
    }
    
    public abstract void createReport(Document document) throws DocumentException;
    
    @Override public void run(){
        
        loading.set(true);
        Document document = new Document(PageSize.LETTER, 30, 30, 100, 70);
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(FILE_TEMP));
            writer.setPageEvent(getPageEvent());
            
            document.open();            
            createReport(document);            
            addEndLegend(document);
            
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace(System.out);                       
            
        } finally {            
            document.addAuthor("SiCA");
            document.addCreationDate();
            document.close();                   
            end();
        }                
        loading.set(false);        
    }
    
    public PdfPageEventHelper getPageEvent(){
        return new PdfPageEventHelper(){
            @Override
            public void onEndPage(PdfWriter writer, Document document) {                    
                try {  

                    Rectangle rect = writer.getPageSize();

                    ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER, new Phrase("Página "+writer.getPageNumber()),
                        rect.getWidth()/2, 50f, 0);

                    java.awt.Image imagenAwt = new ImageIcon(getClass()
                            .getResource("membrete.jpg")).getImage();
                    imagenAwt = imagenAwt.getScaledInstance(
                            Float.valueOf(rect.getWidth()).intValue()-60, 
                            Float.valueOf(rect.getHeight()).intValue()-30, 
                            java.awt.Image.SCALE_SMOOTH);

                    Image imagen = Image.getInstance(writer,imagenAwt,1);
                    imagen.setAbsolutePosition(30, 15);  
                    document.add(imagen);

                } catch (IOException | DocumentException ex) {
                   ex.printStackTrace(System.out);
                }
            }

        };
    }
        
    private void addEndLegend(Document document) throws DocumentException {
        Paragraph p = new Paragraph("Fuente: CUSUR. Secretaria Administrativa, Coordinación de Personal" 
                + "\nCorte: "+df.format(Calendar.getInstance().getTime()).toUpperCase());
        p.setSpacingBefore(10);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        p.setFont(new Font(Font.FontFamily.TIMES_ROMAN,10));
        
        document.add(p);
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }
    
        
}
