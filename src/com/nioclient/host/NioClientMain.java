package com.nioclient.host;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class NioClientMain extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox hbox=new HBox();
        hbox.getChildren().addAll(NioClient.getClientSe(),NioClient.getClientRe());
        hbox.setSpacing(20);
        hbox.setPadding(new Insets(20,30,20,30));
        primaryStage.setTitle("Client");
        Scene scene = new Scene(hbox, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        NioClient.getClientSe().getConnect().setOnAction(e -> {
            String s=NioClient.getClientSe().getConnect().getText();
            String ip=NioClient.getClientSe().getTextip().toString();
            int port=Integer.valueOf(NioClient.getClientSe().getTextport().toString());
            if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0) {
                NioClient.getClientSe().getConnect().setText("Disconnect");
                NioClient c = new NioClient(ip,port);
                c.start();
            }
        });
        primaryStage.show();
    }

}
