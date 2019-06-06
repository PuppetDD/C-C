package com.nioserver.host;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author GOLD
 */
public class NioServerMain extends Application {

    private NioServer s;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox hbox = new HBox();
        hbox.getChildren().addAll(NioServer.getServerSe(), NioServer.getServerRe());
        hbox.setSpacing(20);
        hbox.setPadding(new Insets(20, 30, 20, 30));
        primaryStage.setTitle("NioServer");
        Scene scene = new Scene(hbox, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        NioServer.getServerSe().getBegin().setOnAction(e -> {
            listen();
        });
        primaryStage.show();
    }

    public void listen() {
        String str = NioServer.getServerSe().getBegin().getText();
        if (str.compareTo("Begin") == 0 || str.compareTo("Restart") == 0) {
            s = new NioServer();
            s.start();
        } else {
            s.setStop();
            NioServer.getServerSe().getBegin().setText("Begin");
            NioServer.getServerSe().getTextip().setText(null);
            NioServer.getServerSe().getTextport().setText(null);
        }
    }

}
