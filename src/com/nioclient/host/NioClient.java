package com.nioclient.host;

import com.nioclient.pane.ClientRecevied;
import com.nioclient.pane.ClientSend;

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
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client error");
        }
    }

    @Override
    public void run() {
        connect();
        while (!stop) {
            try {
                //休眠1秒  无论是否有读写事件发生 selector每隔1秒被唤醒
                selector.select(1000);
                //获取注册在selector上的所有的就绪状态的serverSocketChannel中发生的事件
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() {
        try {
            if (socketChannel.connect(new InetSocketAddress(ip, port))) {
                socketChannel.register(selector, SelectionKey.OP_READ);
                //发送请求消息 读应答
                //doWrite(socketChannel);
                clientRe.getRetext().appendText("Connect " + socketChannel.getRemoteAddress() + " successfully\n");
                clientSe.getTextip().setText(socketChannel.getLocalAddress().toString());
                clientSe.getTextport().setText(String.valueOf(port));
            } else {//如果直连接连接未成功，则注册到多路复用器上，并注册SelectionKey.OP_CONNECT操作
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            clientRe.getRetext().appendText("Connect failed....\n");
            clientSe.getConnect().setText("Reconnect");
            e.printStackTrace();
        }
    }

    private void HandleKey(SelectionKey key) {
        try {
            if (key.isValid()) {
                SocketChannel socket = (SocketChannel) key.channel();
                if (key.isConnectable()) {
                    //处于连接状态
                    if (socket.finishConnect()) {
                        //客户端连接成功
                        socket.register(selector, SelectionKey.OP_READ);
                        //doWrite(socket);
                    } else { //连接失败
                        System.exit(1);
                    }
                }
                if (key.isReadable()) {
                    //如果客户端接收到了服务器端发送的应答消息 则SocketChannel是可读的
                    ByteBuffer bf = ByteBuffer.allocate(1024);
                    int bytes = socket.read(bf);
                    if (bytes > 0) {
                        bf.flip();
                        byte[] byteArray = new byte[bf.remaining()];
                        bf.get(byteArray);
                        String resopnseMessage = new String(byteArray, "UTF-8");
                        System.out.println("=======The response message is：" + resopnseMessage);
                        this.stop = true;
                    } else if (bytes < 0) {
                        key.cancel();
                        socket.close();
                    }
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

    public static ClientRecevied getClientRe() {
        return clientRe;
    }

    public static ClientSend getClientSe() {
        return clientSe;
    }

}
