package sicaweb.reportes.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.Date;
import javafx.collections.ObservableList;
import sica.common.Utils;
import sica.common.faltas.Falta;
import sica.common.faltas.FaltaDia;
import sica.common.faltas.FaltasUsuario;
import sica.common.justificantes.Evento;
import sica.common.usuarios.TipoUsuario;

public class ReporteFaltasPeriodo extends ReportePDF {

    private final ObservableList<FaltasUsuario> periodo;
    private final TipoUsuario usuarios;
    private final Date inicio, fin;

    public ReporteFaltasPeriodo(ObservableList<FaltasUsuario> periodo, TipoUsuario usuarios, Date inicio, Date fin) {
        this.periodo = periodo;
        this.usuarios = usuarios;
        this.inicio = inicio;
        this.fin = fin;
    }

    @Override
    public void createReport(Document document) throws DocumentException {

        Paragraph p = new Paragraph("REPORTE DE FALTAS DE "+usuarios.getDescripcion().toUpperCase()+" PERDIODO DEL "
            +Utils.formatDate(inicio)+" AL "+Utils.formatDate(fin));
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);
        
        PdfPTable table = new PdfPTable(4);         
        
        PdfPCell cell = new PdfPCell(new Phrase("Código",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Profesor",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Faltas",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Detalles",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        table.completeRow();
                 
        for (FaltasUsuario f : periodo){
            
            cell = new PdfPCell(new Phrase(f.getUsuario(),fReg)); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            table.addCell(cell);            
            cell = new PdfPCell(new Phrase(f.getNombre(),fReg)); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            table.addCell(cell);            
            

            String temp="";
            int cant = 0;
            for(Falta fa : f.getFaltas()){                
                FaltaDia falta = (FaltaDia) fa;   
                if (falta.getJustificante()== null || (falta.getJustificante()!=null && !(falta.getJustificante() instanceof Evento)))
                    temp+=Utils.formatDate(falta.getFecha())
                        + ((falta.getJustificante()!=null)? "( Justificada - "+falta.getJustificante().getNombrejustificante()+") ": " ")
                        + "\n";
                
                if (falta.getJustificante()==null) cant++;
                
            }
            cell = new PdfPCell(new Phrase(cant+"",fReg)); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(temp,fReg)); 
            table.addCell(cell);

            table.completeRow();
            
        }
        
        table.setWidths(new int[]{1,4,1,6});
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        document.add(table);
        
    }
    
    
    
}
