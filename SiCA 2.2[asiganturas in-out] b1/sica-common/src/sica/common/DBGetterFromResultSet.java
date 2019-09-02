package sica.common;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DBGetterFromResultSet extends DBGetter{    
    private final ObjectProperty<Connection> conProperty;

    public DBGetterFromResultSet(ReadOnlyObjectProperty<Connection> con) {
        conProperty = new SimpleObjectProperty<>();
        conProperty.bind(con);
    }    
    
    @Override public <T> ObservableList<T> getList(String consulta, Class<T> type) {        
        ArrayList<T> list = new ArrayList<>();    
        try {
            ResultSet rs = conProperty.get().prepareStatement(consulta).executeQuery(); 
            
            while (rs.next()){                
                T t = type.newInstance();
                
                for (Field f : getAllFields(new LinkedList<>(), type)){
                    f.setAccessible(true); 
                    f.set(t, isInResultSet(rs, f.getName())?
                            rs.getString(f.getName()):
                            null);
                }   
                list.add(t);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException | SecurityException e){
            System.err.println(e.getMessage() + " parsing " + type);
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(list);        
    }
       
    public static boolean isInResultSet(ResultSet rs, String column) throws SQLException {
        for (int i=1; i<=rs.getMetaData().getColumnCount(); i++){
            if (rs.getMetaData().getColumnLabel(i).equals(column)){
                return true;
            }
        }
        return false;
    }

}
