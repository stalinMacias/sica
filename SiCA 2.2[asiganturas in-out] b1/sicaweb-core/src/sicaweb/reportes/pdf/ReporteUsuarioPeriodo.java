package sicaweb.reportes.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.Calendar;
import javafx.collections.ObservableList;
import sica.common.Utils;
import sica.common.asistencias.AsistenciaUsuario;
import sica.common.asistencias.EstadisticasAsistencias;
import sica.common.asistencias.SemanaAsistencia;
import sica.common.horarios.ReportesHorarios;
import sica.common.usuarios.Usuario;

public class ReporteUsuarioPeriodo extends ReportePDF {
    
    private final Usuario user;
    private final ObservableList<SemanaAsistencia> periodo;
    private final EstadisticasAsistencias totales;
        
    public ReporteUsuarioPeriodo(Usuario usr, ObservableList<SemanaAsistencia> p){
        this(usr,p,new EstadisticasAsistencias());
        
        for (SemanaAsistencia s : p)
            totales.add(s.getEstadisticas());       
        
    }
    
    public ReporteUsuarioPeriodo(Usuario usr, ObservableList<SemanaAsistencia> p, EstadisticasAsistencias est){
        user = usr;
        periodo = p;
        totales = est;
    }
    
    @Override
    public void createReport(Document document) throws DocumentException {

        document.add(new Paragraph("Reporte de asistencias del usuario "
                +user.getNombre()+" ( "+user.getCodigo()+" ) "));

        PdfPTable table = new PdfPTable(8);

        String dias[] = new String[]{"DOMINGO","LUNES","MARTES","MIERCOLES","JUEVES"
                ,"VIERNES","SABADO","HORAS"};

        
        for (SemanaAsistencia s : periodo){            
            
            if (!s.isSpecial()){                
                for (int i =1; i<=7 ; i++){
                    
                    PdfPCell cell;
                    String temp = "";
                    
                    if (s.contiene(i)){
                        AsistenciaUsuario a = s.getDia(i);
                        temp+=s.getFecha(i);
                        
                        if (a.getRegistroEntrada() != null || a.getRegistroSalida()!=null){
                            temp += (a.getRegistroEntrada() != null)?
                                    "\n E - "+Utils.formatTime(a.getRegistroEntrada().getFechahora()):
                                    (a.getJustif()!=null)? "\n J - "+ a.getEntrada() :
                                    "\n E -   --:--:--";

                            temp += (a.getRegistroSalida() != null)?
                                    "\n S - "+Utils.formatTime(a.getRegistroSalida().getFechahora()) : 
                                    (a.getJustif()!=null)? "\n J - "+ a.getSalida() :
                                    "\n S -   --:--:--";
                            
                            if (a.getJustif()!=null)
                                temp += "\n "+a.getJustif().getNombrejustificante();

                        } else if (a.getJustif()!=null && 
                                (a.getRegistroEntrada() == null || a.getRegistroSalida() == null) ){
                             
                            temp += "\n J - "+a.getEntrada()+
                                    "\n J - "+a.getSalida()+
                                    "\n"+a.getJustif().getNombrejustificante();
                            
                        } else if (s.debioAsistir(i) && 
                                a.getRegistroEntrada() == null && a.getRegistroSalida() == null) {

                            temp += "\n No asistió";                            

                        } else if (!s.debioAsistir(i)){
                            temp+= "\n Día libre";
                        }
                        
                    } 
                    
                    cell = new PdfPCell(new Phrase(temp,new Font(FontFamily.TIMES_ROMAN,9))); 
                    table.addCell(cell);

                }
                String hrs = "\n"+(s.getEstadisticas().getDiasAsistidos()-s.getEstadisticas().getDiasConErrores())+" Dia(s)"
                        + "\n"+Utils.millisToTime(s.getEstadisticas().getTiempoTrabajado())+" hrs.";
                PdfPCell cell = new PdfPCell(new Phrase(hrs,new Font(FontFamily.TIMES_ROMAN,9))); 
                table.addCell(cell);
                
                table.completeRow();

            } else {                    
                PdfPCell cell = new PdfPCell(new Phrase(s.getDia(Calendar.TUESDAY).getNombre()
                        +" "+s.getDia(Calendar.WEDNESDAY).getNombre()
                        ,new Font(FontFamily.TIMES_ROMAN,18,Font.BOLD)));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                cell.setColspan(8);
                table.addCell(cell);
                table.completeRow();
                
                for (String dia : dias){
                    PdfPCell celldia = new PdfPCell(new Phrase(dia,
                            new Font(FontFamily.TIMES_ROMAN,10,Font.BOLDITALIC)));

                    celldia.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    table.addCell(celldia);
                }                

                table.completeRow();
            }
        }

        table.setSpacingBefore(30);
        table.setWidthPercentage(98);
        document.add(table);
        
        table = new PdfPTable(4);        
        Font f = new Font(FontFamily.TIMES_ROMAN,9);
        
        PdfPCell cell = new PdfPCell(new Phrase("TOTALES ",new Font(FontFamily.TIMES_ROMAN,10,Font.BOLDITALIC)));
        cell.setColspan(4);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
        table.completeRow();
        
        cell = new PdfPCell(new Phrase("DIAS ",f));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setColspan(2);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("HORAS ",f));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setColspan(2);
        table.addCell(cell);
        table.completeRow();
        
        table.addCell(new PdfPCell(new Phrase("  Del periodo",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasTotales()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Carga semanal",f)));
        long cargaSem = ReportesHorarios.getCargaHorariaSemanal(user.getCodigo());
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(cargaSem),fBold)));
        
        table.addCell(new PdfPCell(new Phrase("  Laborables",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasConJornada()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Carga del periodo (contando dias festivos)",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoCargaPeriodo()),fBold)));
        
        table.addCell(new PdfPCell(new Phrase("  Libres",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasLibres()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Reales registradas (sin contar dias con errores)",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoTrabajado()),fBold)));
        
        table.addCell(new PdfPCell(new Phrase("  Asistidos",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasAsistidos()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Justificadas (cantando dias con errores justificados)",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoJustificado()),fBold)));
        
        table.addCell(new PdfPCell(new Phrase("  Faltados",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasConFaltas()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Dias festivos o especiales",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoInhabil()),fBold))); 
        
        table.addCell(new PdfPCell(new Phrase("  Justificados",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasConJustificantes()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Reales + justificadas + festivos",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoJustificado()
                + totales.getTiempoTrabajado() + totales.getTiempoInhabil()),fBold)));    
        
        table.addCell(new PdfPCell(new Phrase("  Festivos o especiales",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasInhabiles()+"",fBold)));
        table.addCell(new PdfPCell(new Phrase("  Pendientes de justificar (dias con errores)",f)));
        table.addCell(new PdfPCell(new Phrase(Utils.millisToTime(totales.getTiempoPendJustificar()),fBold))); 
        
        table.addCell(new PdfPCell(new Phrase("  Con errores (ausencia de registro)",f)));
        table.addCell(new PdfPCell(new Phrase(totales.getDiasConErrores()+"",fBold)));       
        table.completeRow();
        
        table.setWidths(new float[]{3,.5f,5,1.5f});
        
        table.setSpacingBefore(20);
        table.setSpacingAfter(30);
        table.setWidthPercentage(85);
        document.add(table);
    }   
    
}
