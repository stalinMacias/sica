package sicaweb.reportes.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.text.DateFormat;
import java.util.Date;
import javafx.collections.ObservableList;
import sica.common.Utils;
import sica.common.objetos.Registro;
import sica.common.usuarios.Usuario;

public class ReporteRegistros extends ReportePDF{

    private final Usuario user;
    private final ObservableList<Registro> periodo;
    private final Date inicio, fin;
    private final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);

    public ReporteRegistros(Usuario user, ObservableList<Registro> periodo, Date desde, Date hasta) {
        this.user = user;
        this.periodo = periodo;
        this.inicio = desde;
        this.fin = hasta;
    }
    
    @Override
    public void createReport(Document document) throws DocumentException {
    
        Paragraph p = new Paragraph("REPORTE DE REGISTROS DE "+user.getNombre()+"("+user.getCodigo()+"), "
                + "\nPERDIODO DEL "+Utils.formatDate(inicio)+" AL "+Utils.formatDate(fin));        
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);        
        
        Font fBold = new Font(Font.FontFamily.TIMES_ROMAN,10,Font.BOLDITALIC);
        Font fReg = new Font(Font.FontFamily.TIMES_ROMAN,8);
        
        PdfPTable table = new PdfPTable(4);         
        
        PdfPCell cell = new PdfPCell(new Phrase("Fecha",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Hora",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Tipo",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Modificado",fBold));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        
        table.completeRow();
                 
        for (Registro r : periodo){
            cell = new PdfPCell(new Phrase(df.format(r.getFechahora()).toUpperCase(),fReg)); 
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(Utils.formatTime(r.getFechahora()),fReg)); 
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(r.getTipo().toUpperCase().charAt(0)+r.getTipo().substring(1),fReg)); 
            table.addCell(cell);
            
            if (r.getModificado()!=null){
                cell = new PdfPCell(new Phrase(r.getModificado(),fReg)); 
                table.addCell(cell);
            }
            
            table.completeRow();
        }
        
        table.setWidths(new int[]{3,3,2,2});
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        document.add(table);
        
    }
    
    
}
