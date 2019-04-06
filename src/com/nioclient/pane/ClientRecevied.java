package com.nioclient.pane;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ClientRecevied extends VBox {

    private Label re = new Label("Received Message:");
    ;
    private TextArea retext = new TextArea();

    public ClientRecevied() {
        retext.setMinSize(300, 430);
        retext.setEditable(false);
        this.getChildren().addAll(re, retext);
        this.setSpacing(15);
    }

    public TextArea getRetext() {
        return retext;
    }

}
