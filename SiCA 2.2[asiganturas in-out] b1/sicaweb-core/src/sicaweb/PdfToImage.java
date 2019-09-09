package sicaweb;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class PdfToImage {    
    
    private final Service<ObservableList<Image>> loadFileService;
    private final SimpleObjectProperty<File> fileProperty;
    private final SimpleObjectProperty<ObservableList<Image>> imagesProperty;
    
    public PdfToImage(){        
        fileProperty = new SimpleObjectProperty<>();        
        imagesProperty = new SimpleObjectProperty<>(
                FXCollections.observableArrayList(
                        new ArrayList<Image>()));
        
        fileProperty.addListener(new InvalidationListener() {
            @Override public void invalidated(Observable o) {
                updateFile();                
            }
        });    
        
        loadFileService = new Service<ObservableList<Image>>(){               
            @Override protected Task<ObservableList<Image>> createTask() {
                return loadFileTask(fileProperty.get());
            }          
        };
        imagesProperty.bind(loadFileService.valueProperty());
    }
    
    public void updateFile(){           
        if (loadFileService.getState() != State.READY) 
            loadFileService.cancel();        
        
        loadFileService.reset();      
        loadFileService.start();
        
    }
    
    private Task<ObservableList<Image>> loadFileTask(final File file) {        
        return new Task<ObservableList<Image>>() {
            @Override protected ObservableList<Image> call() {                 
                ArrayList<Image> a = new ArrayList<>();
                
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    if (raf.length() > 317){                    
                        FileChannel channel = raf.getChannel();
                        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                        PDFFile pdfFile = new PDFFile(buffer);

                        for (int i = 1; i<=pdfFile.getNumPages(); i++){
                            updateProgress(i, pdfFile.getNumPages());
                            
                            PDFPage page = pdfFile.getPage(i);
                            Rectangle2D bbox = page.getBBox();

                            final int width = (int) (bbox.getWidth() * 2);
                            final int height = (int) (bbox.getHeight() * 2);
                            // width, height, clip, imageObserver, paintBackground, waitUntilLoaded:
                            java.awt.Image awtImage = page.getImage(width, height, bbox, null, true, true);

                            // draw image to buffered image:
                            BufferedImage buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                            buffImage.createGraphics().drawImage(awtImage, 0, 0, null);

                            a.add(SwingFXUtils.toFXImage(buffImage, null));		                        
                        }    
                    }

                } catch (IOException e){
                    e.printStackTrace(System.out);
                }                
                return FXCollections.observableArrayList(a);
            }
        };			
    }
        
    public ReadOnlyBooleanProperty loadingProperty() {        
        return loadFileService.runningProperty();
    }     
    public ReadOnlyDoubleProperty progressProperty(){
        return loadFileService.progressProperty();
    }
    public SimpleObjectProperty<File> fileProperty(){
        return fileProperty;
    }
    public ReadOnlyObjectProperty<ObservableList<Image>> imagesProperty(){
        return imagesProperty;
    }
    
}
