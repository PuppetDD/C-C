package com.server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerMain extends Application {

    private PrintWriter printWriter;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox hbox=new HBox();
        hbox.getChildren().addAll(NioServer.getServerSe(), NioServer.getServerRe());
        hbox.setSpacing(20);
        hbox.setPadding(new Insets(20,30,20,30));
        primaryStage.setTitle("NioServer");
        Scene scene = new Scene(hbox, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        NioServer s=new NioServer();
        s.start();
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        NioServer.getServerSe().getSend().setOnAction(e->{
            if(NioServer.getServerSe().getMessage().getText()!=null){
                groupSend();
                NioServer.getServerSe().getMessage().setText(null);
            }
        });
        primaryStage.show();
    }

    public void groupSend(){
        for(Socket socket: NioServer.getServerSe().getList().getSelectionModel().getSelectedItems()){
            try {
                String s=socket.getLocalAddress()+":"+String.valueOf(socket.getLocalPort());
                printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write(s+" Message:\n"+ NioServer.getServerSe().getMessage().getText() + "\n");
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        NioServer.getServerSe().getMessage().clear();
    }

}
