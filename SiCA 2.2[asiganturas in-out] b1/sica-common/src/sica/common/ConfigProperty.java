package sica.common;

import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

public final class ConfigProperty <T> {
    private static Preferences preferences;
    
    private final T defaultValue;
    private final ObjectProperty<T> value;        
    private final String name;
    private final Class c;
    
    public ConfigProperty(String n, T t){
        c = t.getClass();
        defaultValue = t;
        value = new SimpleObjectProperty<>(t);
        name = n;                
    }
    
    public static void setPreferences(Preferences p){
        preferences = p;
    }
    
    public void addListener(ChangeListener<T>ch){
        value.addListener(ch);
    }
    
    @SuppressWarnings("unchecked")
    public void load(){     
        if (preferences != null){
            if (defaultValue instanceof Integer){
                value.set((T)c.cast(preferences.getInt(name, (Integer)defaultValue)));            
            } else if (defaultValue instanceof String){
                value.set((T)c.cast(preferences.get(name, (String)defaultValue)));        
            } else if (defaultValue instanceof Boolean){
                value.set((T)c.cast(preferences.getBoolean(name, (Boolean)defaultValue)));        
            } else if (defaultValue instanceof Double){
                value.set((T)c.cast(preferences.getDouble(name, (Double)defaultValue)));        
            } 
        }        
    }
    
    @Override public String toString(){
        return name+": "+get();
    }
    
    private void save(T t){
        if (preferences != null){
            if (defaultValue instanceof Integer){
                preferences.putInt(name, (Integer)t);
            } else if (defaultValue instanceof String){
                preferences.put(name, (String)t);
            } else if (defaultValue instanceof Boolean){
                preferences.putBoolean(name, (Boolean)t);
            } else if (defaultValue instanceof Double){
                preferences.putDouble(name, (Double)t);
            }
        }
    }
    
    public void saveDefault(){
        save(defaultValue);
    }
    
    public void set(T t){
        if (preferences!=null){
            save(t);
            load();
        } else {
            value.set(t);
        }
    }
    
    public T get(){
        return value.get()!=null? value.get(): defaultValue;
    }
   
}
