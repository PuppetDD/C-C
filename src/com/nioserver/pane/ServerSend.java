package com.nioserver.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerSend extends VBox {

    private Label ip = new Label("Server IP:");
    private Label port = new Label("Server Port:");
    private Label online = new Label("Online Client:");
    private Label status = new Label("0 Connecting");
    private TextField textip = new TextField();
    private TextField textport = new TextField();
    private ListView<String> list = new ListView<>();
    private Button begin = new Button("Begin");
    private ObservableList<String> items = FXCollections.observableArrayList();
    public static int count = 0;

    public ServerSend() {
        GridPane grid = new GridPane();
        textip.setEditable(false);
        textport.setEditable(false);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(ip, 0, 0);
        grid.add(textip, 1, 0);
        grid.add(port, 0, 1);
        grid.add(textport, 1, 1);
        list.setMaxSize(350, 265);
        list.setMinSize(350, 265);
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

    public Button getBegin() {
        return begin;
    }

    public Label getStatus() {
        return status;
    }

    public ObservableList<String> getItems() {
        return items;
    }

}
