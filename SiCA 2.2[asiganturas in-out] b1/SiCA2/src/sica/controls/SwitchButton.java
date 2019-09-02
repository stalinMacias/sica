package sica.controls;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SwitchButton extends HBox {
    private SimpleBooleanProperty switchedOn;
    
    public SwitchButton() {          
        setMaxWidth(90);
        setMinWidth(90);
        setMaxHeight(25);
        
        setAlignment(Pos.CENTER);
        switchedOn = new SimpleBooleanProperty();
        final Button button = new Button();
        button.setMinWidth(45);
        button.prefHeightProperty().bind(heightProperty());
        
        button.setOnAction((ActionEvent t) -> {
            switchedOn.set(!switchedOn.get());
        });
        final Label label = new Label("--");
        label.setMinWidth(45);        
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setAlignment(Pos.CENTER);

        switchedOn.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            getChildren().clear();
            if (t1){
                label.setText("SI");
                setStyle("-fx-background-color: rgba(150,181,102,0.5);"
                        +"-fx-background-radius: 3;"
                        +"-fx-background-insets: 1 3 0 3");
                getChildren().addAll(label,button);
                
            } else {
                label.setText("NO");
                setStyle("-fx-background-color: rgba(170,17,34,0.3);"
                        +"-fx-background-radius: 3;"
                        +"-fx-background-insets: 1 3 0 3");
                getChildren().addAll(button,label);
            }
        });
        switchedOn.set(true);
    }

    public SimpleBooleanProperty switchOnProperty() { return switchedOn; }
}