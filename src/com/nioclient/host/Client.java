package com.nioclient.host;

import com.nioclient.pane.ClientRecevied;
import com.nioclient.pane.ClientSend;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.net.Socket;

public class Client extends Application {

    private static ClientRecevied clientRe =new ClientRecevied();
    private static ClientSend clientSe =new ClientSend("Client");
    private Socket socket=null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HBox hbox=new HBox();
        hbox.getChildren().addAll(clientSe,clientRe);
        hbox.setSpacing(20);
        hbox.setPadding(new Insets(20,30,20,30));
        primaryStage.setTitle("Client");
        Scene scene = new Scene(hbox, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.show();
        clientSe.getConnect().setOnAction(e->{
            connect();
        });
    }

    public void connect(){
        if(clientSe.getConnect().getText().compareTo("Connect")==0||clientSe.getConnect().getText().compareTo("Reconnect")==0){
            clientSe.getConnect().setText("Disconnect");
            try {
                socket = new Socket("localhost", 8888);
                socket.setSoTimeout(15000);
                clientRe.getRetext().appendText("Connect "+socket.getRemoteSocketAddress()+" successfully\n");
                clientSe.getTextip().setText(socket.getLocalAddress().toString());
                clientSe.getTextport().setText(String.valueOf(socket.getLocalPort()));
                /*Send send=new Send(socket,clientSe);
                send.start();
                Read read=new Read(socket,clientRe,clientSe);
                read.start();*/
            }catch (Exception ex){
                clientRe.getRetext().appendText("Connect failed....\n");
                clientSe.getConnect().setText("Reconnect");
            }
        }else {
            if(socket.isConnected()){
                try{
                    socket.close();
                    clientSe.getConnect().setText("Connect");
                }catch (Exception ex){

                }
            }
        }
    }

}
