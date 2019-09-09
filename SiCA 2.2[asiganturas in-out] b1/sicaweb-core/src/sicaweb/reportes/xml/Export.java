package sicaweb.reportes.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import sicaweb.reportes.Reporte;

abstract class Export extends Reporte implements Runnable {
    
    private final BooleanProperty loading;
    
    protected CellStyle sTopMid,
                sTopLeft, 
                sTopRight, 
                sLeft, 
                sRight,
                sHeader;
            
    public Export(){        
        super(".xls");
        loading = new SimpleBooleanProperty(false);
    }

    @Override
    public void run() {
        loading.set(true);
                                             
        try ( FileOutputStream archivo = new FileOutputStream(new File(FILE_TEMP)) ) {            
            HSSFWorkbook libro = new HSSFWorkbook();  
            crearStyles(libro);
            int[] i = createDocument(libro);
            createEndPage(libro,i[0],i[1]);
            libro.write(archivo);            

        } catch (Exception ex) {           
            ex.printStackTrace(System.out);                
            
        } finally{            
            end();
        }      
        
        loading.set(false);
         
    }    
    
    private void createEndPage(HSSFWorkbook libro, int filas, int cols){
        
        CellStyle end = libro.createCellStyle();        
        end.setVerticalAlignment(CellStyle.VERTICAL_CENTER);        
        end.setAlignment(CellStyle.ALIGN_CENTER);
        
        Sheet hoja = libro.getSheetAt(0);
        
        Row fila = hoja.createRow(filas++);                     
        Cell celda;
        
        for (int i=0 ; i<cols ; i++){
            celda = fila.createCell(i);  
            celda.setCellStyle(sTopMid); 
        }
        
        fila = hoja.createRow(filas++);                     
        celda = fila.createCell(0);  
        celda.setCellValue("Fuente: CUSUR. Secretaria Administrativa, Coordinación de Personal");        
        celda.setCellStyle(end);
                
        fila = hoja.createRow(filas++);        
        celda = fila.createCell(0);        
        celda.setCellValue("Corte: "+df.format(Calendar.getInstance().getTime()).toUpperCase());     
        celda.setCellStyle(end);
        
        hoja.addMergedRegion(new CellRangeAddress(filas-2, filas-2, 0, cols-1)); 
        hoja.addMergedRegion(new CellRangeAddress(filas-1, filas-1, 0, cols-1));
        
    }
    
    private void crearStyles(HSSFWorkbook libro){
        Font font = libro.createFont();
        font.setFontHeightInPoints((short)8);
        
        Font font12 = libro.createFont();
        font12.setFontHeightInPoints((short)12);
        
        sTopMid = libro.createCellStyle();        
        sTopMid.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        sTopMid.setTopBorderColor(IndexedColors.BLACK.getIndex());
        sTopMid.setVerticalAlignment(CellStyle.VERTICAL_CENTER);        
        sTopMid.setAlignment(CellStyle.ALIGN_CENTER);  
        sTopMid.setFont(font);
        
        sTopLeft = libro.createCellStyle();        
        sTopLeft.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        sTopLeft.setTopBorderColor(IndexedColors.BLACK.getIndex());        
        sTopLeft.setBorderLeft(CellStyle.BORDER_MEDIUM_DASHED);
        sTopLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        sTopLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);          
        sTopLeft.setFont(font);
        
        sTopRight = libro.createCellStyle();        
        sTopRight.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        sTopRight.setTopBorderColor(IndexedColors.BLACK.getIndex());        
        sTopRight.setBorderRight(CellStyle.BORDER_MEDIUM_DASHED);
        sTopRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        sTopRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);          
        sTopRight.setFont(font);
        
        sLeft = libro.createCellStyle();               
        sLeft.setBorderLeft(CellStyle.BORDER_MEDIUM_DASHED);
        sLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        sLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);          
        sLeft.setFont(font);
        
        sRight = libro.createCellStyle();        
        sRight.setBorderRight(CellStyle.BORDER_MEDIUM_DASHED);
        sRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        sRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);          
        sRight.setFont(font);
        
        sHeader = libro.createCellStyle();        
        sHeader.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
        sHeader.setTopBorderColor(IndexedColors.BLACK.getIndex());
        sHeader.setBorderBottom(CellStyle.BORDER_MEDIUM_DASHED);
        sHeader.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        sHeader.setBorderLeft(CellStyle.BORDER_MEDIUM_DASHED);
        sHeader.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        sHeader.setBorderRight(CellStyle.BORDER_MEDIUM_DASHED);
        sHeader.setRightBorderColor(IndexedColors.BLACK.getIndex());
        sHeader.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        sHeader.setAlignment(CellStyle.ALIGN_CENTER);
        sHeader.setFont(font12);               
        
    }
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public abstract int[] createDocument(HSSFWorkbook libro);    
    
}
