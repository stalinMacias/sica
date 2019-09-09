package sica.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class PHPExecuter {
    
    private Boolean result;
        
    public PHPExecuter(String consulta){
        this( consulta, "exec.php" );
        System.out.println("PHPexecuter (saludos desde el constructor)");
    }
    
    @SuppressWarnings("deprecation")
    public PHPExecuter(String consulta, String phpPage){
        System.out.println("--"+consulta);
        result = false;
        try {            
            
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Configs.SERVER_PHP()+phpPage);
            
            post.setHeader("User-Agent", "Mozilla/5.0");
            
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("query", consulta));
                
            post.setEntity(new UrlEncodedFormEntity(urlParameters) );
            
            HttpResponse response = client.execute(post);
            
            BufferedReader br = new BufferedReader(new InputStreamReader 
                        (response.getEntity().getContent()) );
                
            result = Boolean.parseBoolean(br.readLine());
            String s;
            while((s=br.readLine()) != null)
                System.out.println("$"+s);
            
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace(System.out);            
        }
    }
        
    public Boolean getResponse(){        
        return result;        
    }
    
}
