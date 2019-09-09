package sicaweb;

import javafx.concurrent.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import sica.common.Utils;


public class FileDownloadTask extends Task<File> {
    
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private HttpClient httpClient;
    private String remoteUrl;
    private File localFile;
    private int bufferSize;
    
    @SuppressWarnings("deprecation")
    public FileDownloadTask(String remoteUrl, File localFile){        
        this(new DefaultHttpClient(), remoteUrl, localFile, DEFAULT_BUFFER_SIZE);
    }


    public FileDownloadTask(HttpClient httpClient, String remoteUrl, File localFile, int bufferSize){
        System.out.println("Descargando: "+remoteUrl);
        this.httpClient = httpClient;
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = bufferSize;
    }


    public String getRemoteUrl() {
        return remoteUrl;
    }

    public File getLocalFile() {
        return localFile;
    }

    @Override
    protected File call() throws Exception  {
        if (localFile.exists()){
            System.out.println("Archivo local existente, no hay necesidad de descargar");
            return localFile;
        }        
        if (!Utils.urlExist(remoteUrl)){
            System.out.println("No existe archivo con documentos justificantes");
            return null;            
        }
        
        HttpGet httpGet = new HttpGet(this.remoteUrl);        
        HttpResponse response = httpClient.execute(httpGet);
        InputStream remoteContentStream = response.getEntity().getContent();
        OutputStream localFileStream = null;
        
        try {
            long fileSize = response.getEntity().getContentLength();
            File dir = localFile.getParentFile();
            dir.mkdirs();

            localFileStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[bufferSize];
            int sizeOfChunk;
            int amountComplete = 0;
            while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1) {
                localFileStream.write(buffer, 0, sizeOfChunk);
                amountComplete += sizeOfChunk;
                updateProgress(amountComplete, fileSize);
            }            
        } catch (IOException e){
            e.printStackTrace(System.out);
            return null;
        } finally {
            remoteContentStream.close();
            if (localFileStream != null) {
                localFileStream.close();
            }
        }     
        localFile.deleteOnExit();
        return localFile;
    }

}