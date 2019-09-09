package sicaweb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.concurrent.Task;
import sica.common.Configs;

public class FileManagerTask extends Task<String> {        
    
    public enum Type {
        UPLOAD("u"), DELETE("d"), RENAME("r"), LIST("l");
        private final String val;
        private Type(String val) {
            this.val = val;
        }        
    }
    public enum Option{
        JUSTIFICANTE("j"),FOTOGRAFIA("f"),CAPTURA("c");
        private final String val;
        private Option(String val) {
            this.val = val;
        }
    }
    
    private File file;
    private final Type type;
    private final Option opc;
    private String name;
    private String oldname;
    
    public FileManagerTask(File file, Type type, Option opc){
        this.file = file;
        this.type = type;
        this.opc = opc;
    }   
    public FileManagerTask(File file, Type type, Option opc, String name){
        this(file,type,opc);
        this.name = name;
    }    
    public FileManagerTask(Type type, Option opc, String name){
        this(null,type,opc,name);        
    }    

    public FileManagerTask(Type type, Option opc, String name, String oldname) {
        this(type,opc,name);
        this.oldname = oldname;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setOldname(String oldname) {
        this.oldname = oldname;
    }

    public void setFile(File file) {
        this.file = file;
    }    
    
    public File getFile() {
        return file;
    }      
       
    
    @Override
    protected String call() throws Exception {
        String resp = "";
        try {
            if (name == null) {
                throw new Error("Nombre no establecido");
            }
            HttpURLConnection httpUrlConnection = (HttpURLConnection)
                    new URL(Configs.PHP_UPLOAD()+"?"+
                            "type="+type.val+
                            "&opc="+opc.val+
                            "&nombre="+name+
                            ((type==Type.RENAME)?"&oldname="+oldname:""))
                            .openConnection();
            
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
             
            if (type == Type.UPLOAD && file!= null){
                OutputStream os = httpUrlConnection.getOutputStream();
                Thread.sleep(1000);
                BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
                for (int i = fis.read(); i != -1; i=fis.read()){
                    os.write(i);
                }
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            for (String s = in.readLine(); s != null; s = in.readLine()){                
                resp+=s+"\n";
            }            
            if (resp.length()>2) resp = resp.substring(0, resp.length()-1);
        
        } catch (IOException | InterruptedException | Error e){
            e.printStackTrace(System.out);
        }
        
        return resp;        
    }
    
}
