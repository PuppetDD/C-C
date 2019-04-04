package com.nioserver.host;

import com.nioserver.pane.SeverRecevied;
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

    private static SeverRecevied serverRe = new SeverRecevied();
    private static ServerSend serverSe = new ServerSend();
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Boolean stop = false;

    public NioServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(9999), 1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverRe.getRetext().appendText("Waiting for Connecting...\n");
        } catch (IOException e) {
            serverRe.getRetext().appendText("Server Startup Failure\n");
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
                    try {
                        HandleKey(key);
                    } catch (Exception e) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (Exception e1) {

                        }
                    }
                }
            } catch (IOException e) {
                serverRe.getRetext().appendText("Nobody connect\n");
            }
        }
    }

    private void HandleKey(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel client = (SocketChannel) key.channel();
            if (key.isAcceptable()) {
                if (client.finishConnect()) {
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    Adduser(client);
                } else {
                    System.out.println("Client connecting failed");
                }
            }
            if (key.isReadable()) {

            }
        }
    }

    public void Adduser(SocketChannel client) {
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
            serverRe.getRetext().appendText(ServerSend.count + " nioclient connect successfully\n");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    serverSe.getStatus().setText(ServerSend.count + " Connecting");
                }
            });
        } catch (Exception ex) {
            serverRe.getRetext().appendText("Connected failed\n");
        }
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        stop = true;
    }

    public static SeverRecevied getServerRe() {
        return serverRe;
    }

    public static ServerSend getServerSe() {
        return serverSe;
    }

}
