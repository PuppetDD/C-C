package com.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.Socket;

public class SendPane extends VBox {

    private Label ipaddress =new Label("IPAddress:");
    private TextField textip =new TextField();
    private Label port =new Label("Port:");
    private TextField textport =new TextField();
    private Label chooseclient =new Label("Online Client:");
    private Label sendmessage =new Label("Send Message:");
    private TextArea message =new TextArea(null);
    private Button connect =new Button("Connect");
    private Label status=new Label("0 Connecting");
    private Button send =new Button("Send");
    private ListView<Socket> list = new ListView<>();
    private ObservableList<Socket> items = FXCollections.observableArrayList ();
    public static int count=0;

    public SendPane(String s){
        GridPane grid=new GridPane();
        textip.setEditable(false);
        textport.setEditable(false);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(ipaddress,0,0);
        grid.add(textip,1,0);
        grid.add(port,0,1);
        grid.add(textport,1,1);
        list.setMaxWidth(350);
        list.setMinWidth(350);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        connect.setMaxSize(100,30);
        connect.setMinSize(100,30);
        send.setMaxSize(70,30);
        send.setMinSize(70,30);
        status.setMaxSize(100,30);
        status.setMinSize(100,30);
        HBox h=new HBox();
        h.setPadding(new Insets(20, 40, 20, 40));
        h.setSpacing(80);
        if(s.compareTo("Client")==0){
            list.setMaxHeight(100);
            list.setMinHeight(100);
            h.getChildren().addAll(connect, send);
            this.getChildren().addAll(grid, chooseclient, list, sendmessage, message,h);
        }else{
            this.getChildren().addAll(grid, chooseclient, list, status);
        }
        this.setSpacing(20);
    }

    public TextField getTextip(){
        return textip;
    }

    public TextField getTextport(){
        return textport;
    }

    public ListView<Socket> getList(){  return list; }

    public TextArea getMessage(){
        return message;
    }

    public Button getConnect(){
        return connect;
    }

    public Label getStatus(){ return  status; }

    public Button getSend(){
        return send;
    }

    public  ObservableList<Socket> getItems(){ return items; }

}
