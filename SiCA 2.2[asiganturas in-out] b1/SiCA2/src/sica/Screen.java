package sica;

import javafx.scene.Parent;

public abstract class Screen {    
    private Parent parent;   

    public Parent getParent() {        
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
    
    public abstract void start();
}
