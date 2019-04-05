package com.nioserver.host;

import com.nioserver.pane.ServerRecevied;
import com.nioserver.pane.ServerSend;
import com.protocol.User;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioServer extends Thread {

    private static ServerRecevied serverRe = new ServerRecevied();
    private static ServerSend serverSe = new ServerSend();
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private List<User> list;
    private Boolean stop;

    public NioServer() {
        try {
            this.stop = false;
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
            this.stop = true;
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
                        handleKey(key);
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
        try {
            serverRe.getRetext().appendText("Server close\n");
            selector.close();
            serverSocketChannel.close();
            //没有关闭所有socketChannel
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return;
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                //handleAccept(client);
            }
            try {
                if (key.isReadable()) {
                    handleRead(key);
                }
            } catch (Exception e) {
                logout(key);
            }
        }
    }

    private void handleAccept(SocketChannel client) {

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //读取客户端传过来的请求参数
        int read = client.read(buffer);
        if (read > 0) {
            //获取到了login请求数据，对字节进行编解码
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String request = new String(bytes, Charset.forName("UTF-8"));
            String[] name = request.split(",");
            login(key, name[0], name[1]);
            System.out.println("============MultiplexerTimerServer receive message:" + request);
            handleWrite(client);
            //服务器回包update(all);
        } else if (read < 0) {
            //没有获取到请求数据，需要关闭SocketChannel(C/S 连接链路)
            logout(key);
        }
    }

    private void handleWrite(SocketChannel client) {
        String response = null;
        if (response != null && !response.isEmpty()) {
            byte[] bytes = response.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void login(SelectionKey key, String name, String port) {
        //添加在线客户端
        //通知所有客户端login消息
        SocketChannel client = (SocketChannel) key.channel();
        String ip = client.socket().getInetAddress().toString();
        User u = new User();
        u.setName(name);
        u.setIp(ip.substring(1, ip.length()));
        u.setVport(client.socket().getPort());
        u.setPort(Integer.valueOf(port));
        key.attach(u);
        //不能用list.add(u)，会触发logout
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverSe.getItems().add(name+":"+u.getVport());
            }
        });
        serverSe.getList().setItems(serverSe.getItems());
        ServerSend.count++;
        try {
            serverRe.getRetext().appendText("Connected from  " + client.getRemoteAddress() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverRe.getRetext().appendText(ServerSend.count + " client connect successfully\n");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverSe.getStatus().setText(ServerSend.count + " Connecting");
            }
        });
    }

    private void logout(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        User u=(User)key.attachment();
        ServerSend.count--;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverSe.getItems().remove(u.getName()+":"+u.getVport());
                serverSe.getStatus().setText(ServerSend.count + " Connecting");
            }
        });
        try {
            serverRe.getRetext().appendText("Disconnect from " + client.getRemoteAddress() + "\n");
            key.cancel();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通知所有客户端logout消息
    }

    public void setStop() {
        this.stop = true;
    }

    public static ServerRecevied getServerRe() {
        return serverRe;
    }

    public static ServerSend getServerSe() {
        return serverSe;
    }

}
