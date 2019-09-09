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
import sica.common.faltas.FaltaClase;
import sica.common.faltas.FaltasUsuario;
import sica.common.justificantes.Evento;
import sica.common.objetos.Departamento;

public class ReporteFaltasAsignatura extends ReportePDF{

    private final ObservableList<FaltasUsuario> periodo;
    private final Departamento departamento;
    private final Date inicio, fin;
    
    public ReporteFaltasAsignatura(ObservableList<FaltasUsuario> p, Departamento d, Date i, Date f){
        periodo = p;
        departamento = d;
        inicio = i;
        fin = f;
    }
    
    @Override
    public void createReport(Document document) throws DocumentException {
        
        Paragraph p = new Paragraph(departamento.getNombre()
                +" PERIODO "+Utils.formatDate(inicio)+" AL "+Utils.formatDate(fin));
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
            int faltasreales = 0;
            for (Falta fa : f.getFaltas()){
                FaltaClase fc = (FaltaClase) fa;
                if (fc.getJustifcante() == null)
                    faltasreales++;
            }
            cell = new PdfPCell(new Phrase(faltasreales+"",fReg)); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(cell);

            String temp="";
            for(Falta fa : f.getFaltas()){                         
                FaltaClase falta = (FaltaClase) fa;
                if (falta.getJustifcante()== null || (falta.getJustifcante()!=null && !(falta.getJustifcante() instanceof Evento)))
                    temp+=Utils.formatDate(falta.getFecha())+": "
                        +falta.getCrn().getMateria()+"("+falta.getCrn().getCrn()+")"
                        + (falta.getJustifcante()!=null? " Justificada - "+falta.getJustifcante().getNombrejustificante()+"\n":
                        ", "+falta.getDia()+", "+falta.getHorario().substring(0, 5) + "\n");
            }
            
            cell = new PdfPCell(new Phrase(temp,fReg)); 
            table.addCell(cell);

            table.completeRow();
            
        }
        
        table.setWidths(new int[]{1,3,1,6});
        table.setSpacingBefore(30);
        table.setWidthPercentage(100);
        document.add(table);
        
    }

    
    
}
