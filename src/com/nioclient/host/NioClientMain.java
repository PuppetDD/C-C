package com.nioclient.host;

import com.protocol.Message;
import com.protocol.User;
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

    public void connect() {
        String port = NioClient.getClientSe().getTextport().getText();
        String lport = NioClient.getClientSe().getTextlport().getText();
        if (port != null && lport != null) {
            String s = NioClient.getClientSe().getConnect().getText();
            String name = NioClient.getClientSe().getTextname().getText();
            String ip = NioClient.getClientSe().getTextip().getText();
            if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0) {
                Boolean error = true;
                int port1 = 0, lport1 = 0;
                try {
                    port1 = Integer.valueOf(port);
                    lport1 = Integer.valueOf(lport);
                    error = false;
                } catch (Exception e1) {
                    NioClient.getClientRe().getRetext().appendText("Input error 1\n");
                }
                if (!error) {
                    u = new UdpClient(lport1);
                    if (!UdpClient.error) {
                        try {
                            c = new NioClient(name, ip, port1, lport1);
                            c.start();
                            u.start();
                        } catch (Exception e1) {
                            NioClient.getClientRe().getRetext().appendText("Input error 2\n");
                        }
                    }
                }
            } else {
                c.setStop();
            }
        } else {
            NioClient.getClientRe().getRetext().appendText("Please enter the necessary information:port\n");
        }
    }

    public void send() {
        System.out.println("Send");
        String s = NioClient.getClientSe().getList().getSelectionModel().getSelectedItems().get(0);
        if (NioClient.getClientSe().getMessage().getText() != null && s != null) {
            for (User u : c.getList()) {
                if (u.uniqueName().compareTo(s) == 0) {
                    try {
                        DatagramChannel channel = DatagramChannel.open();
                        String data = NioClient.getClientSe().getMessage().getText();
                        String message = new Message(u, NioClient.local, data).toString();
                        ByteBuffer buffer = ByteBuffer.allocate(message.length());
                        buffer.clear();
                        buffer.put(message.getBytes("UTF-8"));
                        buffer.flip();
                        /*发送UDP数据包*/
                        channel.send(buffer, new InetSocketAddress(u.getIp(), u.getPort()));
                        System.out.println("Send successful");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            NioClient.getClientSe().getMessage().setText(null);
        }
    }

}
