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
        String port = NioServer.getServerSe().getTextport().getText();
        if (port != null) {
            if (str.compareTo("Begin") == 0||str.compareTo("Restart") == 0) {
                Boolean error = false;
                try {
                    int p = Integer.valueOf(port);
                    if (p < 0 || p > 65535) {
                        error = true;
                    }
                } catch (Exception e1) {
                    error = true;
                }
                if (!error) {
                    s = new NioServer(Integer.valueOf(port));
                    s.start();
                } else {
                    NioServer.getServerRe().getRetext().appendText("Input is wrong\n");
                    NioServer.getServerSe().getTextport().setText(null);
                }

            } else {
                s.setStop();
                NioServer.getServerSe().getBegin().setText("Begin");
                NioServer.getServerSe().getTextip().setText(null);
                NioServer.getServerSe().getTextport().setText(null);
                NioServer.getServerSe().getTextport().setEditable(true);
            }
        } else {
            NioServer.getServerRe().getRetext().appendText("Please enter the necessary port information\n");
        }
    }

}
