package com.nioclient.host;

import com.nioclient.pane.ClientRecevied;
import com.nioclient.pane.ClientSend;
import com.protocol.User;
import javafx.application.Platform;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    private static User lacal;
    private ArrayList<User> list = new ArrayList<>();
    private String name;
    private String ip;
    private int port;
    private int lport;
    private Selector selector;
    private SocketChannel socketChannel;
    private Boolean stop;

    public NioClient(String name, String ip, int port, int lport) {
        this.name = name == null ? "user" : name;
        this.ip = ip == null ? "127.0.0.1" : ip;
        this.port = 9999;
        this.lport = lport;
        this.stop = false;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
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
            System.out.println("Active disconnect");
            disConnect(socketChannel.keyFor(selector));
            selector.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void connect() {
        try {
            if (socketChannel.connect(new InetSocketAddress(ip, port))) {
                //直连成功发送请求消息，注册SelectionKey.OP_READ操作
                handleWrite(socketChannel);
                socketChannel.register(selector, SelectionKey.OP_READ);
            } else {
                //直连失败，注册SelectionKey.OP_CONNECT操作
                System.out.println("Register OP_CONNECT");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        clientSe.getItems().clear();
                    }
                });
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        System.out.println("handleKey");
        if (key.isValid()) {
            System.out.println("isValid");
            if (key.isConnectable()) {
                System.out.println("isConnectable");
                SocketChannel client = (SocketChannel) key.channel();
                try {
                    handleWrite(client);
                } catch (Exception e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            clientSe.getConnect().setText("Reconnect");
                            clientSe.getTextip().setText(null);
                        }
                    });
                    clientRe.getRetext().appendText("The connection fails\n");
                    System.out.println("connection error");
                    return;
                }
                client.register(selector, SelectionKey.OP_READ);
            }
            try {
                if (key.isReadable()) {
                    handleRead(key);
                }
            } catch (Exception e) {
                //服务器异常退出
                System.out.println("Passive disconnection:exception");
                disConnect(key);
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("handleRead");
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
                    String user = Charset.forName("UTF-8").decode(line).toString();
                    String[] attribute = user.split(",");
                    int port = client.socket().getLocalPort();
                    try {
                        update(attribute, port);
                    } catch (Exception e) {
                        System.out.println("Format error" + user);
                    }
                    System.out.println("Receive:");
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
            System.out.println("Passive disconnection:normal");
            disConnect(key);
        }
    }

    private void handleWrite(SocketChannel client) throws IOException {
        //login服务器时调用一次
        System.out.println("handleWrite");
        if (client.finishConnect()) {
            //客户端连接成功
            //关闭上一次的UDP监听线程
            String ip = client.socket().getInetAddress().toString();
            User u = new User();
            u.setName(this.name);
            u.setIp(ip.substring(1, ip.length()));
            u.setVport(client.socket().getLocalPort());
            u.setPort(Integer.valueOf(this.lport));
            u.setStatus("online");
            client.keyFor(selector).attach(u);
            lacal=u;
            list.add(u);
            byte[] request = new byte[0];
            try {
                request = u.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("Coding failure");
            }
            ByteBuffer buffer = ByteBuffer.allocate(request.length);
            buffer.put(request);
            buffer.flip();
            client.write(buffer);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientSe.getConnect().setText("Disconnect");
                }
            });
            clientRe.getRetext().appendText("Connection to " + client.getRemoteAddress() + " successful\n");
            clientSe.getTextip().setText(u.getIp());
            clientSe.getTextport().setText(String.valueOf(u.getVport()));
            clientSe.getTextname().setText(u.getName());
            clientSe.getTextlport().setText(String.valueOf(u.getPort()));
            clientSe.getTextip().setEditable(false);
            clientSe.getTextport().setEditable(false);
            clientSe.getTextname().setEditable(false);
            clientSe.getTextlport().setEditable(false);
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
        } else if (port != u.getVport()) {
            //在线用户列表添加非本地用户信息
            u.setStatus("online");
            updateStatus(u);
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
        System.out.println("disConnect\n");
        SocketChannel client = (SocketChannel) key.channel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientSe.getConnect().setText("Connect");
            }
        });
        clientSe.getTextip().setEditable(true);
        clientSe.getTextport().setEditable(true);
        clientSe.getTextname().setEditable(true);
        clientSe.getTextlport().setEditable(true);
        clientSe.getTextip().setText(null);
        clientSe.getTextport().setText(null);
        key.cancel();
        try {
            String s = client.getRemoteAddress().toString();
            clientRe.getRetext().appendText("Disconnect from " + s + "\n");
            client.close();
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

    public ArrayList<User> getList(){
        return list;
    }

}
