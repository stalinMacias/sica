package sicaweb.reportes.xml;

import java.util.Date;
import javafx.collections.ObservableList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import sica.common.Utils;
import sica.common.faltas.Falta;
import sica.common.faltas.FaltaClase;
import sica.common.faltas.FaltasUsuario;
import sica.common.justificantes.Evento;
import sica.common.objetos.Departamento;

public class ExportFaltasAsignatura extends Export {

    private final ObservableList<FaltasUsuario> periodo;
    private final Departamento departamento;
    private final Date inicio, fin;
    
    public ExportFaltasAsignatura(ObservableList<FaltasUsuario> p, Departamento d, Date i, Date f){
        periodo = p;
        departamento = d;
        inicio = i;
        fin = f;
    }
    
    @Override
    public int[] createDocument(HSSFWorkbook libro) {
                        
        Sheet hoja = libro.createSheet("Hoja1");        
        
        Row fila = hoja.createRow(0);        
        Cell celda = fila.createCell(0);        
        celda.setCellValue(departamento.getNombre()
                +" PERIODO "+Utils.formatDate(inicio)+" AL "+Utils.formatDate(fin));
        celda.setCellStyle(sHeader);        
        celda = fila.createCell(3);        
        celda.setCellStyle(sHeader);        
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3)); 
        fila.setHeightInPoints(50);
        
        fila = hoja.createRow(1);
        fila.createCell(0).setCellValue("Código");
        fila.createCell(1).setCellValue("Nombre");
        fila.createCell(2).setCellValue("Faltas");
        fila.createCell(3).setCellValue("Días y Materias");
        
        fila.getCell(0).setCellStyle(sHeader);
        fila.getCell(1).setCellStyle(sHeader);
        fila.getCell(2).setCellStyle(sHeader);
        fila.getCell(3).setCellStyle(sHeader);
        fila.setHeightInPoints(30);
        
        int i = 2;
        for (FaltasUsuario f : periodo){
            fila = hoja.createRow(i);
            int faltas = 0, faltasreales = 0;
            
            for (Falta fa : f.getFaltas()){
                FaltaClase fc = (FaltaClase) fa;
                if (fc.getJustifcante() == null){
                    faltasreales++;
                }
                
                if (fc.getJustifcante() == null || 
                        (fc.getJustifcante()!=null && !(fc.getJustifcante() instanceof Evento))){
                    faltas++;
                }
            }                

            fila.createCell(0).setCellValue(f.getUsuario());
            fila.createCell(1).setCellValue(f.getNombre());
            fila.createCell(2).setCellValue(faltasreales);
            
            fila.getCell(0).setCellStyle(sTopLeft);
            fila.getCell(1).setCellStyle(sTopMid);
            fila.getCell(2).setCellStyle(sTopMid);            

            boolean first = true;
            
            for (Falta falta : f.getFaltas()){
                FaltaClase fa = (FaltaClase)falta;
                
                if (fa.getJustifcante() == null || 
                        (fa.getJustifcante()!=null && !(fa.getJustifcante() instanceof Evento))){
                
                    fila.createCell(3).setCellValue(Utils.formatDate(fa.getFecha())
                        +" - "+fa.getCrn().getMateria()+"("+fa.getCrn().getCrn()+"), "
                        + (fa.getJustifcante()!=null? " Justificada - "+fa.getJustifcante().getNombrejustificante()
                            :fa.getDia()+", "+fa.getHorario().substring(0,5)) );                
                    
                    if (first){
                        fila.getCell(3).setCellStyle(sTopRight);            
                        first = false;
                    } else {
                        fila.getCell(3).setCellStyle(sRight);            
                    }

                    i++;
                    fila = hoja.createRow(i);                
                }
            }
            
            if (faltas!=0){
                hoja.addMergedRegion( new CellRangeAddress( i-faltas, i-1, 0, 0 ) ); 
                hoja.addMergedRegion( new CellRangeAddress( i-faltas, i-1, 1, 1 ) ); 
                hoja.addMergedRegion( new CellRangeAddress( i-faltas, i-1, 2, 2 ) );                 
                
            } else {
                fila.createCell(3).setCellValue("");
                fila.getCell(3).setCellStyle(sTopRight); 
                i++;
            }
        }
        
        hoja.setColumnWidth(1, 8000);
        hoja.setColumnWidth(3, 20000);
        
        return new int[]{i,4};
    }
    
}
