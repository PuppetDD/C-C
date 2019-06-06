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
    private ArrayList<User> list = new ArrayList<>();
    private Selector selector;
    private SocketChannel socketChannel;
    private Boolean stop;
    private String name;
    private String ip;
    private int port;
    private int lport;
    public static User local;

    public NioClient(String name, String ip, int port, int lport) {
        this.name = name == null ? "user" : name;
        this.ip = ip;
        this.port = port;
        this.lport = lport;
        this.stop = false;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            System.out.println("Open succeed");
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
                //一字节一字节读
                byte b = buffer.get();
                //碰到换行符证明读了一整行，此时line包含一行的数据
                if (b == 10 || b == 13) {
                    line.flip();
                    String user = Charset.forName("UTF-8").decode(line).toString();
                    String[] attribute = user.split(",");
                    try {
                        update(attribute);
                    } catch (Exception e) {
                        System.out.println("Format error" + user);
                    }
                    System.out.println("Receive TCP:");
                    for (int i = 0; i < attribute.length; i++) {
                        System.out.print(attribute[i] + " ");
                    }
                    System.out.println("\n");
                    line.clear();
                } else {
                    //一个字节一个字节的存储一行数据
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
            System.out.println("finishConnect");
            //客户端连接成功
            String ip = client.socket().getInetAddress().toString();
            User u = new User();
            u.setName(this.name);
            u.setIp(ip.substring(1, ip.length()));
            u.setPort(this.lport);
            u.setStatus("online");
            client.keyFor(selector).attach(u);//通过attach绑定一个对象
            if (local != null) {
                list.remove(local);
            }
            local = u;
            list.add(u);
            byte[] request = new byte[0];
            try {
                request = u.unique().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("Coding failure");
            }
            ByteBuffer buffer = ByteBuffer.allocate(request.length);
            buffer.put(request);
            buffer.flip();//切换成写模式
            client.write(buffer);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientSe.getConnect().setText("Disconnect");
                }
            });
            clientRe.getRetext().appendText("Connection to " + client.getRemoteAddress() + " successful\n");
            clientSe.getTextip().setText(u.getIp());
            clientSe.getTextname().setText(u.getName());
            clientSe.getTextlport().setText(String.valueOf(u.getPort()));
            clientSe.getTextname().setEditable(false);
        }
    }

    private void update(String[] attribute) {
        //更新login，logout用户
        System.out.println("update");
        User u = new User();
        u.setName(attribute[1]);
        u.setIp(attribute[2]);
        u.setPort(Integer.valueOf(attribute[3]));
        if (attribute[0].compareTo("logout") == 0) {
            u.setStatus("offline");
            updateStatus(u);
        } else if (u.unique().compareTo(local.unique()) != 0) {
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
                    if (user.unique().compareTo(u.unique()) == 0) {
                        //如果本地列表已经有该用户就删除再添加
                        temp = user;
                        clientSe.getItems().remove(user);
                    }
                }
                if (temp.unique() != null) {
                    list.remove(temp);
                }
                list.add(u);
                clientSe.getItems().add(u);
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
                for (User user : list) {
                    if (user.unique().compareTo(local.unique()) != 0) {
                        //更改用户状态先删除再添加
                        clientSe.getItems().remove(user);
                        user.setStatus("offline");
                        clientSe.getItems().add(user);
                    }
                }
                clientSe.getConnect().setText("Connect");
            }
        });
        clientSe.getList().setItems(clientSe.getItems());
        clientSe.getTextname().setEditable(true);
        clientSe.getTextip().setText(null);
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

}
