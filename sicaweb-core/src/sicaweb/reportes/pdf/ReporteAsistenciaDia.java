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
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.usuarios.TipoUsuario;

public class ReporteAsistenciaDia extends ReportePDF {

    private final ObservableList<AsistenciaUsuario> tabla;
    private final Date fecha;
    private final TipoUsuario tipo;

    public ReporteAsistenciaDia(ObservableList<AsistenciaUsuario> tabla, Date fecha, TipoUsuario tipo) {
        this.tabla = tabla;
        this.fecha = fecha;
        this.tipo = tipo;
    }
    
    @Override
    public void createReport(Document document) throws DocumentException {
        Paragraph p = new Paragraph("REPORTE DE ASISTENCIA "+tipo.getDescripcion().toUpperCase()
                +" FECHA "+Utils.formatDate(fecha));
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);
                
        PdfPTable table = new PdfPTable(4);         
        
        PdfPCell cell = new PdfPCell(new Phrase("Código",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Nombre",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Entrada",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Salida",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        table.completeRow();
                 
        for (AsistenciaUsuario au : tabla){
            
            cell = new PdfPCell(new Phrase(au.getUsuario(),fReg));
            table.addCell(cell);  
            
            cell = new PdfPCell(new Phrase(au.getNombre(),fReg));
            table.addCell(cell);  
            
            if (au.getJustif()!=null){
                
                cell = new PdfPCell(new Phrase(au.getJustif().getNombrejustificante(), fReg));

                table.addCell(cell);  
                table.addCell(cell);  

                
            } else {                
                cell = new PdfPCell(new Phrase(au.getRegistroEntrada()!=null? 
                        Utils.formatTime(au.getRegistroEntrada().getFechahora()):
                        (au.getEntrada().length()>8? au.getEntrada() :
                        " No checó ("+au.getEntrada()+")") , fReg));

                table.addCell(cell);  

                cell = new PdfPCell(new Phrase(au.getRegistroSalida()!=null? 
                        Utils.formatTime(au.getRegistroSalida().getFechahora()) :
                        (au.getSalida().length()>8? au.getSalida() :
                        " No checó ("+au.getSalida()+")") , fReg));

                table.addCell(cell);  
            }
            
            table.completeRow();
            
        }
        
        table.setWidths(new int[]{1,4,2,2});
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        document.add(table);
        
    }
    
}
