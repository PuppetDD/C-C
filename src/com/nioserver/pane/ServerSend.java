package com.nioserver.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerSend extends VBox {

    private Label ipaddress = new Label("IPAddress:");
    private TextField textip = new TextField();
    private Label port = new Label("Port:");
    private TextField textport = new TextField();
    private Label online = new Label("Online Client:");
    private ListView<String> list = new ListView<>();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private Button begin = new Button("Begin");
    private Label status = new Label("0 Connecting");
    public static int count = 0;

    public ServerSend() {
        GridPane grid = new GridPane();
        textip.setEditable(false);
        textport.setEditable(false);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(ipaddress, 0, 0);
        grid.add(textip, 1, 0);
        grid.add(port, 0, 1);
        grid.add(textport, 1, 1);
        list.setMaxWidth(350);
        list.setMinWidth(350);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        begin.setMaxSize(100, 30);
        begin.setMinSize(100, 30);
        status.setMaxSize(100, 30);
        status.setMinSize(100, 30);
        HBox h = new HBox();
        h.setPadding(new Insets(20, 20, 20, 20));
        h.setSpacing(100);
        h.getChildren().addAll(begin, status);
        this.getChildren().addAll(grid, online, list, h);
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

    public Button getBegin() {
        return begin;
    }

    public Label getStatus() {
        return status;
    }

}
