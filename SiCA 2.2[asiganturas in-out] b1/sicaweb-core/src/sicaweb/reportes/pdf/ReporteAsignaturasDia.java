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
import sica.common.asistencias.AsistenciaClase;

public class ReporteAsignaturasDia extends ReportePDF {

    private final ObservableList<AsistenciaClase> tabla;
    private final Date fecha;

    public ReporteAsignaturasDia(ObservableList<AsistenciaClase> tabla, Date fecha) {
        this.tabla = tabla;
        this.fecha = fecha;
    }

    @Override
    public void createReport(Document document) throws DocumentException {
        Paragraph p = new Paragraph("REPORTE DE ASISTENCIA A ASIGNATURAS"
                +" FECHA "+Utils.formatDate(fecha));
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);
                
        PdfPTable table = new PdfPTable(5);         
        
        PdfPCell cell = new PdfPCell(new Phrase("CRN",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Profesor",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Materia",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Horario",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Status",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        table.completeRow();
                 
        for (AsistenciaClase ac : tabla){
            
            cell = new PdfPCell(new Phrase(ac.getCrn(),fReg));
            table.addCell(cell);  
            
            cell = new PdfPCell(new Phrase(ac.getNombre(),fReg));
            table.addCell(cell);  
            
            cell = new PdfPCell(new Phrase(ac.getMateria(),fReg));
            table.addCell(cell);  
            
            cell = new PdfPCell(new Phrase(ac.getHorario(),fReg));
            table.addCell(cell);  
            
            
            if (ac.getJustificante()!=null){                
                cell = new PdfPCell(new Phrase("J - "+ac.getJustificante().getNombrejustificante(), fReg));
                table.addCell(cell);  
                
            } else if (ac.getRegistro() != null){
                cell = new PdfPCell(new Phrase(Utils.formatTime(ac.getRegistro().getFechahora()), fReg));
                table.addCell(cell);  
                
            } else {
                cell = new PdfPCell(new Phrase("No checó", fReg));
                table.addCell(cell);  
            }
            
            table.completeRow();
            
        }
        
        table.setWidths(new int[]{1,5,5,2,2});
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        document.add(table);
        
    }
    
    
    
}
