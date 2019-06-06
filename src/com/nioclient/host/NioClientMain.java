package com.nioclient.host;

import com.protocol.Message;
import com.protocol.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author GOLD
 */
public class NioClientMain extends Application {

    private NioClient c;
    private String ip = "127.0.0.1";
    private int port = 9999;

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
            connect();
        });
        NioClient.getClientSe().getSend().setOnAction(e -> {
            send();
        });
        primaryStage.show();
    }

    private void connect() {
        String s = NioClient.getClientSe().getConnect().getText();
        String name = NioClient.getClientSe().getTextname().getText();
        if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0) {
            UdpClient u = new UdpClient();
            if (!UdpClient.error) {
                u.start();
                c = new NioClient(name, ip, port, u.getPort());
                c.start();
            }
        } else {
            c.setStop();
        }
    }

    private void send() {
        System.out.println("Send");
        String data = NioClient.getClientSe().getMessage().getText();
        if (data != null) {
            for (User u : NioClient.getClientSe().getList().getSelectionModel().getSelectedItems()) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    String message = new Message(u, NioClient.local, data).toString();
                    byte[] bytes = new byte[0];
                    try {
                        bytes = message.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Coding failure");
                    }
                    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                    buffer.clear();
                    buffer.put(bytes);
                    buffer.flip();
                    /*发送UDP数据包*/
                    channel.send(buffer, new InetSocketAddress(u.getIp(), u.getPort()));
                    System.out.println("send to " + u);
                    System.out.println("Send successful");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            NioClient.getClientSe().getMessage().setText(null);
        }
    }

}
