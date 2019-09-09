package sica.common;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;

public abstract class DBGetter {
    
    public abstract <T> ObservableList<T> getList(String consulta, Class<T> classType); 

    protected static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    String getList(String string, String hora, String codigo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
