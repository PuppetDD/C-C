package com.nioclient.host;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author GOLD
 */
public class NioClientMain extends Application {

    private NioClient c;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox hbox = new HBox();
        hbox.getChildren().addAll(NioClient.getClientSe(), NioClient.getClientRe());
        hbox.setSpacing(20);
        hbox.setPadding(new Insets(20, 30, 20, 30));
        primaryStage.setTitle("NioClient");
        Scene scene = new Scene(hbox, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        NioClient.getClientSe().getConnect().setOnAction(e -> {
            String port = NioClient.getClientSe().getTextport().getText();
            String lport = NioClient.getClientSe().getTextlport().getText();
            if (port != null && lport != null) {
                String s = NioClient.getClientSe().getConnect().getText();
                String name = NioClient.getClientSe().getTextname().getText();
                String ip = NioClient.getClientSe().getTextip().getText();
                if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0) {
                    try {
                        c = new NioClient(name, ip, Integer.valueOf(port), Integer.valueOf(lport));
                        c.start();
                    } catch (Exception e1) {
                        NioClient.getClientRe().getRetext().appendText("Input error\n");
                    }
                } else {
                    c.setStop();
                }
            } else {
                NioClient.getClientRe().getRetext().appendText("Please enter the necessary information:port\n");
            }
        });
        primaryStage.show();
    }

}
