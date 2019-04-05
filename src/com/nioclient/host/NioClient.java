package com.nioclient.host;

import com.nioclient.pane.ClientRecevied;
import com.nioclient.pane.ClientSend;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
                    System.out.println("hasnext");
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleKey(key);
                    } catch (Exception e) {
                        System.out.println("Read3");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                clientSe.getConnect().setText("Connect");
                            }
                        });
                        String s = socketChannel.getRemoteAddress().toString();
                        clientRe.getRetext().appendText("Disconnect from " + s + "\n");
                        clientSe.getTextip().setEditable(true);
                        clientSe.getTextport().setEditable(true);
                        clientSe.getTextip().setText(null);
                        clientSe.getTextport().setText(null);
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (Exception e1) {
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
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
            } else {//如果直连接连接未成功，则注册到多路复用器上，并注册SelectionKey.OP_CONNECT操作
                System.out.println("OP_CONNECT");
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isValid()) {
            System.out.println("Valid");
            if (key.isConnectable()) {
                SocketChannel client = (SocketChannel) key.channel();
                if (client.finishConnect()) {
                    //客户端连接成功
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            clientSe.getConnect().setText("Disconnect");
                        }
                    });
                    clientRe.getRetext().appendText("Connect " + socketChannel.getRemoteAddress() + " successfully\n");
                    clientSe.getTextip().clear();
                    clientSe.getTextip().setText(client.socket().getLocalAddress().toString());
                    clientSe.getTextport().clear();
                    clientSe.getTextport().setText(String.valueOf(client.socket().getLocalPort()));
                    clientSe.getTextip().setEditable(false);
                    clientSe.getTextport().setEditable(false);
                    client.register(selector, SelectionKey.OP_READ);
                    handleWrite(client);
                } else { //连接失败
                    System.exit(1);
                }
            }
            if (key.isReadable()) {
                //如果客户端接收到了服务器端发送的应答消息 则SocketChannel是可读的
                handleRead(key);
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("Read");
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytes = client.read(buffer);
        if (bytes > 0) {
            System.out.println("Read2");
            buffer.flip();
            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            String response = new String(byteArray, "UTF-8");
            System.out.println("=======The response message is：" + response);
            this.stop = true;
        } else if (bytes < 0) {
            key.cancel();
            client.close();
        }
    }

    private void handleWrite(SocketChannel client) throws IOException {
        System.out.println("Write");
        if (client.finishConnect()) {
            //客户端连接成功
            byte[] request = "request message from client".getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(request.length);
            buffer.put(request);
            buffer.flip();
            client.write(buffer);
            if (!buffer.hasRemaining()) {
                //如果缓冲区里面的所有内容全部发送完毕
                System.out.println("=======client send requst message to server successed!");
            }
        } else {//连接失败
            System.exit(1);
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
