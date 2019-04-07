package com.nioserver.host;

import com.nioserver.pane.ServerRecevied;
import com.nioserver.pane.ServerSend;
import com.protocol.User;
import javafx.application.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class NioServer extends Thread {

    private static ServerRecevied serverRe = new ServerRecevied();
    private static ServerSend serverSe = new ServerSend();
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ArrayList<User> list = new ArrayList<User>();
    private Boolean stop;

    public NioServer() {
        try {
            this.stop = false;
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(9999), 1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverRe.getRetext().appendText("Server on\n");
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
                        serverRe.getRetext().appendText("Connection error\n");
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (Exception e1) {
                        }
                    }
                }
            } catch (IOException e) {
                serverRe.getRetext().appendText("No connection\n");
            }
        }
        try {
            serverRe.getRetext().appendText("Server down\n");
            selector.close();
            serverSocketChannel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        System.out.println("handleKey");
        if (key.isValid()) {
            if (key.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                System.out.println("isAcceptable\n");
            }
            try {
                if (key.isReadable()) {
                    handleRead(key);
                }
            } catch (Exception e) {
                System.out.println("logout Exception");
                logout(key);
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("handleRead");
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //读取客户端传过来的请求参数
        int read = client.read(buffer);
        if (read > 0) {
            //获取到了login请求数据，对字节进行编解码
            System.out.println("buffer");
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String request = new String(bytes, Charset.forName("UTF-8"));
            String[] name = request.split(",");
            //注册login
            login(key, name[0], name[1]);
            //回包所有的在线用户信息(包括自己)
            response(client);
        } else if (read < 0) {
            //客户端正常断开
            System.out.println("logout normal");
            logout(key);
        }
    }

    private void response(SocketChannel client) {
        //向login用户发送在线用户列表
        System.out.println("response");
        Set<SelectionKey> selectionKeys = selector.keys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        SelectionKey key = null;
        ByteBuffer buffer;
        while (iterator.hasNext()) {
            key = iterator.next();
            String response = null;
            if (key.isValid() && !key.isAcceptable()) {
                User u = (User) key.attachment();
                response = "user," + u.toString() + "\n";
                byte[] user = response.getBytes();
                buffer = ByteBuffer.allocate(user.length);
                buffer.put(user);
                buffer.flip();
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.print("response:" + response);
                buffer.clear();
            }
        }
        System.out.print("\n");
    }

    private void login(SelectionKey key, String name, String port) {
        //添加在线客户端,通知所有客户端login消息
        System.out.println("login");
        SocketChannel client = (SocketChannel) key.channel();
        String ip = client.socket().getInetAddress().toString();
        User u = new User();
        u.setName(name);
        u.setIp(ip.substring(1, ip.length()));
        u.setVport(client.socket().getPort());
        u.setPort(Integer.valueOf(port));
        u.setStatus("online");
        key.attach(u);
        list.add(u);
        try {
            update(key, "login");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerSend.count++;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverSe.getItems().add(u.uniqueName());
                serverSe.getStatus().setText(ServerSend.count + " Connecting");
            }
        });
        try {
            serverRe.getRetext().appendText(client.getRemoteAddress() + " is connected\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSe.getList().setItems(serverSe.getItems());
    }

    private void logout(SelectionKey key) {
        //通知所有在线客户端logout消息
        System.out.println("logout");
        SocketChannel client = (SocketChannel) key.channel();
        User u = (User) key.attachment();
        list.remove(u);
        update(key, "logout");
        ServerSend.count--;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverSe.getItems().remove(u.uniqueName());
                serverSe.getStatus().setText(ServerSend.count + " Connecting");
            }
        });
        try {
            serverRe.getRetext().appendText(client.getRemoteAddress() + " is disconnected\n");
            key.cancel();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void update(SelectionKey touch, String type) {
        System.out.println("update " + type);
        User u = (User) touch.attachment();
        Set<SelectionKey> selectionKeys = selector.keys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        SelectionKey key = null;
        ByteBuffer buffer;
        while (iterator.hasNext()) {
            key = iterator.next();
            String s = null;
            if (key.isValid() && !key.isAcceptable() && key.toString().compareTo(touch.toString()) != 0) {
                //不是ServerSocketChannel，也不是touch对应的客户端
                //而是剩余的客户端
                SocketChannel client = (SocketChannel) key.channel();
                s = type + "," + u.toString() + "\n";
                byte[] message = s.getBytes();
                buffer = ByteBuffer.allocate(message.length);
                buffer.put(message);
                buffer.flip();
                try {
                    client.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.print("touch:" + s);
                buffer.clear();
            }
        }
        System.out.print("\n");
    }

    public void setStop() {
        this.stop = true;
        Set<SelectionKey> selectionKeys = selector.keys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        SelectionKey key = null;
        while (iterator.hasNext()) {
            key = iterator.next();
            try {
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                server.close();
            } catch (Exception e) {
                if (key.isValid()) {
                    logout(key);
                }
            }
        }
    }

    public static ServerRecevied getServerRe() {
        return serverRe;
    }

    public static ServerSend getServerSe() {
        return serverSe;
    }

}
