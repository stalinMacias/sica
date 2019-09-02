package sica.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DBGetterFromJSON extends DBGetter{    
    
    private final String host;
    
    public DBGetterFromJSON(String host) {
        this.host = host;
    }           
    
    @Override public <T> ObservableList<T> getList(String consulta, Class<T> type) {        
        StringBuilder response =  new StringBuilder();
        
         try {               
            
            URL url = new URL(host+"/php/getter3.php?");
            System.out.println("--"+consulta);
            StringBuilder postData = new StringBuilder();           
            
            postData.append("query=");
            postData.append(URLEncoder.encode(consulta, "UTF-8"));

            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            try (Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                int c; 
                while ((c = in.read()) >= 0)
                    response.append((char)c);                
            }
            
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace(System.out);            
        }        
        
        ArrayList<T> list = new ArrayList<>();                
        JSONArray array = (JSONArray) JSONValue.parse(response.toString());
        
        if (array != null){
            for (Object a : array) {
                JSONObject row = (JSONObject) a;

                try {
                    T t = type.newInstance();

                    for ( Field f : getAllFields(new LinkedList<>(), type)){
                        f.setAccessible(true);                                                
                        Object val = row.get(f.getName());                        
                        if (val != null) f.set(t, val.toString());                         
                    }

                    list.add(t);

                }catch (IllegalAccessException | InstantiationException  e){
                    e.printStackTrace(System.out);
                }            
            }
        } else {
            System.out.println("Error parsing response: "+response.toString());
        }        
        return FXCollections.observableArrayList(list);        
    }      
   
}
