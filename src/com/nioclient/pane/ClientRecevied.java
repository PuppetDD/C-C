package com.nioclient.pane;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ClientRecevied extends VBox {

    private Label re = new Label("Message:");
    private TextArea retext = new TextArea();

    public ClientRecevied() {
        retext.setMaxSize(380, 425);
        retext.setMinSize(380, 425);
        retext.setEditable(false);
        this.getChildren().addAll(re, retext);
        this.setSpacing(20);
    }

    public TextArea getRetext() {
        return retext;
    }

}
