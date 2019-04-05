package com.nioserver.host;

import com.nioserver.pane.ServerRecevied;
import com.nioserver.pane.ServerSend;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioServer extends Thread {

    private static ServerRecevied serverRe = new ServerRecevied();
    private static ServerSend serverSe = new ServerSend();
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Boolean stop;

    public NioServer() {
        try {
            this.stop=false;
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(9999), 1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverRe.getRetext().appendText("Waiting for Connecting...\n");
            serverSe.getTextip().setText(serverSocketChannel.socket().getInetAddress().toString());
            serverSe.getTextport().setText(String.valueOf(serverSocketChannel.socket().getLocalPort()));
        } catch (IOException e) {
            serverRe.getRetext().appendText("Server Startup Failure\n");
            this.stop=true;
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    handleKey(key);
                }
            } catch (IOException e) {
                serverRe.getRetext().appendText("Nobody connect\n");
            }
        }
        try {
            serverRe.getRetext().appendText("Server close\n");
            selector.close();
            serverSocketChannel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return;
    }

    private void handleKey(SelectionKey key) {
        try {
            if (key.isValid()) {
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client=server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    handleAccept(client);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
            }
        } catch (Exception e) {
            key.cancel();
            try {
                key.channel().close();
            } catch (Exception e1) {

            }
        }
    }

    public void handleAccept(SocketChannel client) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    serverSe.getItems().add(client);
                }
            });
            serverSe.getList().setItems(serverSe.getItems());
            SocketAddress address = client.getRemoteAddress();
            ServerSend.count++;
            serverRe.getRetext().appendText("Connected from  " + address + "\n");
            serverRe.getRetext().appendText(ServerSend.count + " client connect successfully\n");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    serverSe.getStatus().setText(ServerSend.count + " Connecting");
                }
            });
        } catch (Exception ex) {
            serverRe.getRetext().appendText("Connected failed\n");
        }
    }

    public void handleRead(SelectionKey key){
        SocketChannel client=(SocketChannel)key.channel();
    }

    public void handleWrite(SelectionKey key){

    }

    public void setStop(){
        this.stop=true;
    }

    public static ServerRecevied getServerRe() {
        return serverRe;
    }

    public static ServerSend getServerSe() {
        return serverSe;
    }

}
