package sica.common.usuarios;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sica.common.DBQueries;

public class ReportesUsuarios {

    public static  ObservableList <Usuario> getUsuariosPorTipo(
            TipoUsuario tipoUsuarios, Usuario usuario){
        
        ObservableList <Usuario> usuarios;       

        if (usuario.getPrivilegios() == Privilegios.ADMINISTRADOR || usuario.getPrivilegios() == Privilegios.DIRECTIVO){
            usuarios = DBQueries.getAlgunosUsuarios(tipoUsuarios.getTipo());

        }else if (usuario.getPrivilegios() == Privilegios.JEFE){            
            usuarios = DBQueries.getAlgunosUsuarios(tipoUsuarios.getTipo(), usuario.getCodigo());
            if (tipoUsuarios.getDescripcion().equals(usuario.getTipo()))
                usuarios.add(0,DBQueries.getUsuario(usuario.getCodigo()));
        } else {
            usuarios = FXCollections.observableArrayList();
            usuarios.add(DBQueries.getUsuario(usuario.getCodigo()));
        }
        
        return usuarios;
    }
}
