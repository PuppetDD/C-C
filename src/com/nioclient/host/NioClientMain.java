package com.nioclient.host;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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
            if(port!=null){
                String s = NioClient.getClientSe().getConnect().getText();
                String ip = NioClient.getClientSe().getTextip().getText();
                if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0  ) {
                    try {
                        c = new NioClient(ip, Integer.valueOf(port));
                        c.start();
                    }catch (Exception e1){
                        NioClient.getClientRe().getRetext().appendText("Input message error\n");
                    }
                }else{
                    c.setStop();
                    NioClient.getClientSe().getConnect().setText("Connect");
                    NioClient.getClientSe().getTextip().setEditable(true);
                    NioClient.getClientSe().getTextport().setEditable(true);
                    NioClient.getClientSe().getTextip().setText(null);
                    NioClient.getClientSe().getTextport().setText(null);
                }
            }
        });
        primaryStage.show();
    }

}
