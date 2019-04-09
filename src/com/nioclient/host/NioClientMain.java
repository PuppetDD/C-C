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
        String port = NioClient.getClientSe().getTextport().getText();
        String lport = NioClient.getClientSe().getTextlport().getText();
        String ip = NioClient.getClientSe().getTextip().getText();
        if (ip == null) {
            ip = "127.0.0.1";
        }
        if (port != null && lport != null) {
            String s = NioClient.getClientSe().getConnect().getText();
            String name = NioClient.getClientSe().getTextname().getText();
            if (s.compareTo("Connect") == 0 || s.compareTo("Reconnect") == 0) {
                if (isFormat(ip, port, lport)) {
                    int port1 = Integer.valueOf(port);
                    int lport1 = Integer.valueOf(lport);
                    UdpClient u = new UdpClient(lport1);
                    if (!UdpClient.error) {
                        u.start();
                        c = new NioClient(name, ip, port1, lport1);
                        c.start();
                    }
                } else {
                    NioClient.getClientRe().getRetext().appendText("Input is wrong\n");
                    NioClient.getClientSe().getTextip().setText(null);
                    NioClient.getClientSe().getTextport().setText(null);
                    NioClient.getClientSe().getTextlport().setText(null);
                }
            } else {
                c.setStop();
            }
        } else {
            NioClient.getClientRe().getRetext().appendText("Please enter the necessary two port information\n");
            NioClient.getClientSe().getTextip().setText(null);
            NioClient.getClientSe().getTextport().setText(null);
            NioClient.getClientSe().getTextlport().setText(null);
        }
    }

    private Boolean isFormat(String ipaddress, String port, String lport) {
        try {
            int p = Integer.valueOf(port);
            int lp = Integer.valueOf(lport);
            if (p < 0 || p > 65535 || lp < 0 || lp > 65535) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 判断ip地址是否与正则表达式匹配
        if (ipaddress.matches(regex)) {
            String[] ip = ipaddress.split("\\.");
            for (int i = 0; i < 4; i++) {
                int temp = Integer.valueOf(ip[i]);
                //如果某个数字不是0到255之间的数 就返回false
                if (temp < 0 || temp > 255) {
                    return false;
                }
            }
            return true;
        } else {
            System.out.println("No match");
            return false;
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
