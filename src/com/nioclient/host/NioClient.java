package com.nioclient.host;

import com.nioclient.pane.ClientRecevied;
import com.nioclient.pane.ClientSend;
import com.protocol.User;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * C-C
 * com.nioclient.host
 *
 * @author GOLD
 * @date 2019/4/4
 */
public class NioClient extends Thread {

    private static ClientRecevied clientRe = new ClientRecevied();
    private static ClientSend clientSe = new ClientSend();
    private ArrayList<User> list = new ArrayList<User>();
    private String ip;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private Boolean stop;

    public NioClient(String ip, int port) {
        this.ip = ip == null ? "127.0.0.1" : ip;
        this.port = 9999;
        this.stop = false;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("Connect succeed");
        } catch (IOException e) {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        connect();
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
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
                            e1.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {//正常关闭client
            selector.close();
            if (socketChannel != null) {
                String s = socketChannel.getRemoteAddress().toString();
                clientRe.getRetext().appendText("Disconnect from " + s + "\n");
                socketChannel.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void connect() {
        try {
            if (socketChannel.connect(new InetSocketAddress(ip, port))) {
                socketChannel.register(selector, SelectionKey.OP_READ);
                //发送请求消息 读应答
                handleWrite(socketChannel);
            } else {
                //如果直连接连接未成功，则注册到多路复用器上，并注册SelectionKey.OP_CONNECT操作
                System.out.println("Register OP_CONNECT");
                ObservableList<String> t = clientSe.getItems();
                for (String s : t) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            clientSe.getItems().remove(s);
                        }
                    });
                }
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        System.out.println("handleKey");
        if (key.isValid()) {
            if (key.isConnectable()) {
                SocketChannel client = (SocketChannel) key.channel();
                if (client.finishConnect()) {
                    //客户端连接成功
                    client.register(selector, SelectionKey.OP_READ);
                    handleWrite(client);
                } else {
                    //连接失败
                    System.exit(1);
                }
            }
            try {
                if (key.isReadable()) {
                    handleRead(key);
                }
            } catch (Exception e) {
                //服务器异常退出
                System.out.println("Logout exception\n");
                disConnect(key);
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("handleRead");
        for (User user : list) {
            System.out.println(user.toString());
        }
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteBuffer line = ByteBuffer.allocate(1024);
        int bytes = client.read(buffer);
        if (bytes > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                if (b == 10 || b == 13) {
                    line.flip();
                    String user = Charset.forName("utf-8").decode(line).toString();
                    String[] attribute = user.split(",");
                    int port = client.socket().getLocalPort();
                    try {
                        update(attribute, port);
                    } catch (Exception e) {
                        System.out.println("Format error" + user);
                    }
                    System.out.println("Buffer Message:");
                    for (int i = 0; i < attribute.length; i++) {
                        System.out.print(attribute[i] + " ");
                    }
                    System.out.println("\n");
                    line.clear();
                } else {
                    line.put(b);
                }
            }
            buffer.clear();
        } else if (bytes < 0) {
            System.out.println("Logout normal\n");
            disConnect(key);
        }
    }

    private void handleWrite(SocketChannel client) throws IOException {
        //login服务器时调用一次
        System.out.println("handleWrite\n");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientSe.getConnect().setText("Disconnect");
            }
        });
        clientRe.getRetext().appendText("Connect " + client.getRemoteAddress() + " successfully\n");
        clientSe.getTextip().clear();
        clientSe.getTextip().setText(client.socket().getLocalAddress().toString());
        clientSe.getTextport().clear();
        clientSe.getTextport().setText(String.valueOf(client.socket().getLocalPort()));
        clientSe.getTextip().setEditable(false);
        clientSe.getTextport().setEditable(false);
        if (client.finishConnect()) {
            //客户端连接成功
            byte[] request = "user,1000".getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(request.length);
            buffer.put(request);
            buffer.flip();
            client.write(buffer);
        } else {//连接失败
            System.out.println("Connect error");
            System.exit(1);
        }
    }

    private void update(String[] attribute, int port) {
        //更新login，logout用户
        System.out.println("update");
        User u = new User();
        u.setName(attribute[1]);
        u.setIp(attribute[2]);
        u.setVport(Integer.valueOf(attribute[3]));
        u.setPort(Integer.valueOf(attribute[4]));
        if (attribute[0].compareTo("logout") == 0) {
            u.setStatus("offline");
            updateStatus(u);
        } else {
            if (port == u.getVport()) {
                //在线用户列表不用添加自身信息，只执行一次
                list.add(u);
            } else {
                u.setStatus("online");
                updateStatus(u);
            }
        }
    }

    private void updateStatus(User u) {
        System.out.println("updateStatus");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                User temp = new User();
                for (User user : list) {
                    if (user.toString().compareTo(u.toString()) == 0) {
                        //如果本地列表已经有该用户就删除再添加
                        temp = user;
                        clientSe.getItems().remove(user.uniqueName());
                    }
                }
                if (temp.toString() != null) {
                    list.remove(temp);
                }
                list.add(u);
                clientSe.getItems().add(u.uniqueName());
            }
        });
        clientSe.getList().setItems(clientSe.getItems());
    }

    private void disConnect(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientSe.getConnect().setText("Connect");
            }
        });
        clientSe.getTextip().setEditable(true);
        clientSe.getTextport().setEditable(true);
        clientSe.getTextip().setText(null);
        clientSe.getTextport().setText(null);
        key.cancel();
        try {
            String s = client.getRemoteAddress().toString();
            clientRe.getRetext().appendText("Disconnect from " + s + "\n");
            key.channel().close();
        } catch (Exception e1) {
        }
    }

    public void setStop() {
        this.stop = true;
    }

    public static ClientRecevied getClientRe() {
        return clientRe;
    }

    public static ClientSend getClientSe() {
        return clientSe;
    }

}
