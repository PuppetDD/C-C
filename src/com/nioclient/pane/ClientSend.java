package com.nioclient.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientSend extends VBox {

    private Label ipaddress = new Label("IPAddress:");
    private TextField textip = new TextField(null);
    private Label port = new Label("Port:");
    private TextField textport = new TextField(null);
    private Label online = new Label("Online Client:");
    private ListView<String> list = new ListView<>();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private Label sendmessage = new Label("Send Message:");
    private TextArea message = new TextArea(null);
    private Button connect = new Button("Connect");
    private Button send = new Button("Send");

    public ClientSend() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(ipaddress, 0, 0);
        grid.add(textip, 1, 0);
        grid.add(port, 0, 1);
        grid.add(textport, 1, 1);
        list.setMaxWidth(350);
        list.setMinWidth(350);
        message.setMaxWidth(350);
        message.setMinWidth(350);
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

    public ListView<String> getList() {
        return list;
    }

    public ObservableList<String> getItems() {
        return items;
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

}
