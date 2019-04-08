package com.nioclient.host;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author GOLD
 */
public class NioClientMain extends Application {

    private NioClient c;
    private UdpClient u;
    private int i=1;

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
                        u = new UdpClient(Integer.valueOf(lport));
                        u.start();
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
        NioClient.getClientSe().getSend().setOnAction(e -> {
            System.out.println("Send");
            try {
                DatagramChannel channel = DatagramChannel.open();
                String data = "Test for UDP!" + System.currentTimeMillis()+"\n";
                ByteBuffer buffer = ByteBuffer.allocate(48);
                buffer.clear();
                buffer.put(data.getBytes("UTF-8"));
                buffer.flip();
                /*发送UDP数据包*/
                channel.send(buffer, new InetSocketAddress("127.0.0.1", 12345));
                System.out.println("Send successful");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        primaryStage.show();
    }

}
