package com.nioserver.pane;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ServerRecevied extends VBox {

    private Label re = new Label("Message:");
    private TextArea retext = new TextArea();

    public ServerRecevied() {
        retext.setMaxSize(380, 430);
        retext.setMinSize(380, 430);
        retext.setEditable(false);
        this.getChildren().addAll(re, retext);
        this.setSpacing(15);
    }

    public TextArea getRetext() {
        return retext;
    }

}
