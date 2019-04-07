package com.nioclient.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientSend extends VBox {

    private Label ipaddress = new Label("IP:");
    private Label port = new Label("Port:");
    private Label name = new Label("User:");
    private Label lport = new Label("Port:");
    private Label online = new Label("Client:");
    private Label sendmessage = new Label("Send Panel:");
    private TextField textip = new TextField(null);
    private TextField textport = new TextField(null);
    private TextField textname = new TextField(null);
    private TextField textlport = new TextField(null);
    private ListView<String> list = new ListView<>();
    private TextArea message = new TextArea(null);
    private Button connect = new Button("Connect");
    private Button send = new Button("Send");
    private ObservableList<String> items = FXCollections.observableArrayList();

    public ClientSend() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        textip.setMaxWidth(130);
        textport.setMaxWidth(90);
        textname.setMaxWidth(130);
        textlport.setMaxWidth(90);
        grid.add(ipaddress, 0, 0);
        grid.add(textip, 1, 0);
        grid.add(port, 2, 0);
        grid.add(textport, 3, 0);
        grid.add(name, 0, 1);
        grid.add(textname, 1, 1);
        grid.add(lport, 2, 1);
        grid.add(textlport, 3, 1);
        list.setMaxWidth(350);
        list.setMinWidth(350);
        message.setMaxSize(350, 100);
        message.setMinSize(350, 100);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        connect.setMaxSize(100, 30);
        connect.setMinSize(100, 30);
        send.setMaxSize(100, 30);
        send.setMinSize(100, 30);
        HBox h = new HBox();
        h.setPadding(new Insets(20, 20, 20, 20));
        h.setSpacing(100);
        list.setMaxHeight(100);
        list.setMinHeight(100);
        h.getChildren().addAll(connect, send);
        this.getChildren().addAll(grid, online, list, sendmessage, message, h);
        this.setSpacing(20);
    }

    public TextField getTextip() {
        return textip;
    }

    public TextField getTextport() {
        return textport;
    }

    public TextField getTextname() {
        return textname;
    }

    public TextField getTextlport() {
        return textlport;
    }

    public ListView<String> getList() {
        return list;
    }

    public TextArea getMessage() {
        return message;
    }

    public Button getConnect() {
        return connect;
    }

    public Button getSend() {
        return send;
    }

    public ObservableList<String> getItems() {
        return items;
    }

}
