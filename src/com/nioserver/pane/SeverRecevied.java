package com.nioserver.pane;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class SeverRecevied extends VBox {

    private Label re=new Label("Received Message:");;
    private TextArea retext=new TextArea();

    public SeverRecevied(){
        retext.setMinSize(300,430);
        retext.setEditable(false);
        this.getChildren().addAll(re,retext);
        this.setSpacing(15);
    }

    public TextArea getRetext(){ return retext; }

}
