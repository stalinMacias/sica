package sica.common.justificantes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import sica.common.DBQueries;

public class ReportesJustificantes {
        
    public static Task<ObservableList<TipoJustificante>> getListaJustificantesTipo(final String tipo){
        return new Task<ObservableList<TipoJustificante>>() {
            @Override protected ObservableList<TipoJustificante> call() throws Exception {
                
                ObservableList<TipoJustificante> lista = DBQueries.getJustificantesListaTipoUsuario(tipo);
                for (TipoJustificante j : lista){
                    j.setFracciones( DBQueries.getFraccionesJustificante(j.getId()) );
                }
                return lista;
            }
        };        
    }
    
    public static ObservableMap<String,ObservableList<TipoJustificante>> getListaJustificantes(){
        ObservableList<TipoJustificante> lista = DBQueries.getJustificantesListaCompleta();
            for (TipoJustificante j : lista){
                j.setFracciones( DBQueries.getFraccionesJustificante(j.getId()) );
            }
        ObservableMap<String,ObservableList<TipoJustificante>> map = FXCollections.observableHashMap();
        
        for (TipoJustificante t : lista){
            if (!map.containsKey(t.getTipousuario())){
                map.put(t.getTipousuario(), FXCollections.observableArrayList(t));
            } else {
                map.get(t.getTipousuario()).add(t);
            }                
        }
        
        return map;
    }
    
    public static Task<ObservableList<JustificanteFolio>> getJustificantesPendientesAprobar(){
        return new Task<ObservableList<JustificanteFolio>>() {
            @Override protected ObservableList<JustificanteFolio> call() throws Exception {                
                return DBQueries.getJustificantesParaAprobacion();                                
            }
        };           
    }
    
    
    public static Task<ObservableList<JustificanteFolio>> getJustificantesPendientesAprobarJefe(final String user){
        return new Task<ObservableList<JustificanteFolio>>() {
            @Override protected ObservableList<JustificanteFolio> call() throws Exception {                
                return DBQueries.getJustificantesParaAprobacionJefe(user);                
            }
        };           
    }
}
