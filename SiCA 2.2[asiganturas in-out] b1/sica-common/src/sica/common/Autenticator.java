package sica.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import sica.common.usuarios.Privilegios;
import sica.common.usuarios.Usuario;

public class Autenticator {
    private static Usuario currentUser;

    public static Usuario getCurrentUser() {
        return currentUser;
    }
        
    public static Usuario autenticate(String user, String pass){         
        currentUser = null;
        if (siiauLogin(user, pass)){     
            if ( (currentUser=DBQueries.getAdmin(user)) != null ){
                currentUser.setPrivilegios(Privilegios.ADMINISTRADOR);
                
            }
              else if ( (currentUser=DBQueries.getAdminView(user)) != null ){
                currentUser.setPrivilegios(Privilegios.ADMINISTRADORVIEW);
                
            }
              else if ( (currentUser=DBQueries.getDirectivo(user)) != null ){
                currentUser.setPrivilegios(Privilegios.DIRECTIVO);
                
            } else if ( (currentUser=DBQueries.getJefeDepartamento(user)) != null ){
                currentUser.setPrivilegios(Privilegios.JEFE);
                
            } else if ( (currentUser=DBQueries.getUsuario(user)) != null ){
                currentUser.setPrivilegios(Privilegios.USUARIO);                
            }                        
        }
        return currentUser;
    }
    
    public static boolean siiauLogin(String usuario, String password){
        System.out.println("USUARIO COMMON: "+usuario);
        System.out.println("PASS COMMON: "+password);
        if (usuario.isEmpty() || password.isEmpty()) return false;
        
        StringBuilder response = new StringBuilder();
        
        try {               
            
            System.out.println(Configs.SIIAU_LOGIN());
            URL url = new URL(Configs.SIIAU_LOGIN());
            
            StringBuilder postData = new StringBuilder();           
            
            postData.append("user=");
            postData.append(URLEncoder.encode(usuario, "UTF-8"));
            postData.append("&pass=");
            postData.append(URLEncoder.encode(password, "UTF-8"));
            
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
            System.out.println("SIIAU login response: "+response.toString());
            return Boolean.parseBoolean(response.toString());
            
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace(System.out);
            return false;
        }
    }

    
}
