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
import sica.common.faltas.FaltaDia;
import sica.common.faltas.FaltasUsuario;
import sica.common.justificantes.Evento;
import sica.common.usuarios.TipoUsuario;

public class ExportFaltasPeriodo extends Export {

    private final ObservableList<FaltasUsuario> periodo;
    private final TipoUsuario usuarios;
    private final Date inicio, fin;

    public ExportFaltasPeriodo(ObservableList<FaltasUsuario> periodo, TipoUsuario usuarios, Date inicio, Date fin) {
        this.periodo = periodo;
        this.usuarios = usuarios;
        this.inicio = inicio;
        this.fin = fin;
    }
    
    
    @Override
    public int[] createDocument(HSSFWorkbook libro) {
        Sheet hoja = libro.createSheet("Hoja1");        
        
        Row fila = hoja.createRow(0);        
        Cell celda = fila.createCell(0);        
        celda.setCellValue("REPORTE DE FALTAS DE "+usuarios.getDescripcion().toUpperCase()+" PERDIODO DEL "
            +Utils.formatDate(inicio)+" AL "+Utils.formatDate(fin));
        
        celda.setCellStyle(sHeader);        
        celda = fila.createCell(3);        
        celda.setCellStyle(sHeader);        
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3)); 
        fila.setHeightInPoints(50);
        
        fila = hoja.createRow(1);
        fila.createCell(0).setCellValue("Código");
        fila.createCell(1).setCellValue("Nombre");
        fila.createCell(2).setCellValue("Faltas");
        fila.createCell(3).setCellValue("Detalles");
        
        fila.getCell(0).setCellStyle(sHeader);
        fila.getCell(1).setCellStyle(sHeader);
        fila.getCell(2).setCellStyle(sHeader);
        fila.getCell(3).setCellStyle(sHeader);
        fila.setHeightInPoints(30);
                        
        int i = 2;
        
        for (FaltasUsuario f : periodo){
            fila = hoja.createRow(i);
            
            int faltas = 0, tam = 0;
            for (Falta falta : f.getFaltas()){
                FaltaDia fa = (FaltaDia)falta;
                
                if (fa.getJustificante()==null) 
                    faltas++;
                
                if (fa.getJustificante()==null || (fa.getJustificante()!=null && !(fa.getJustificante() instanceof Evento))) 
                    tam++;
                
                
            }
            
            fila.createCell(0).setCellValue(f.getUsuario());
            fila.createCell(1).setCellValue(f.getNombre());
            fila.createCell(2).setCellValue(faltas);
            
            fila.getCell(0).setCellStyle(sTopLeft);
            fila.getCell(1).setCellStyle(sTopMid);
            fila.getCell(2).setCellStyle(sTopMid);            

            if (faltas == 0){
                fila.createCell(3);
                fila.getCell(3).setCellStyle(sTopRight);
                
            } else {            
                boolean first = true;

                for (Falta falta : f.getFaltas()){
                    FaltaDia fa = (FaltaDia)falta;
                    if (fa.getJustificante()==null || (fa.getJustificante()!=null && !(fa.getJustificante() instanceof Evento))){
                        fila.createCell(3).setCellValue(Utils.formatDate(falta.getFecha())
                            + (fa.getJustificante()!=null? " (Justificada - "+fa.getJustificante().getNombrejustificante()+")" :""));                

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
            }
            if (tam!=0){
                hoja.addMergedRegion( new CellRangeAddress( i-tam, i-1, 0, 0 ) ); 
                hoja.addMergedRegion( new CellRangeAddress( i-tam, i-1, 1, 1 ) ); 
                hoja.addMergedRegion( new CellRangeAddress( i-tam, i-1, 2, 2 ) );                 
                
            } else {
                i++;
            }
        }
        
        hoja.setColumnWidth(1, 8000);
        hoja.setColumnWidth(3, 20000);
                
        return new int[]{i,4};
        
    }
    
    

}
