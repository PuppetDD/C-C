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
            socketChannel = SocketChannel.open(new InetSocketAddress(this.ip, this.port));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientSe.getConnect().setText("Disconnect");
                }
            });
            clientRe.getRetext().appendText("Connect " + socketChannel.getRemoteAddress() + " successfully\n");
            clientSe.getTextip().clear();
            clientSe.getTextip().setText(socketChannel.socket().getLocalAddress().toString());
            clientSe.getTextport().clear();
            clientSe.getTextport().setText(String.valueOf(socketChannel.socket().getLocalPort()));
            clientSe.getTextip().setEditable(false);
            clientSe.getTextport().setEditable(false);
        } catch (IOException e) {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            this.stop = true;
            clientRe.getRetext().appendText("Connect failed....\n");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientSe.getConnect().setText("Reconnect");
                }
            });
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    handleKey(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            selector.close();
            if(socketChannel!=null){
                clientRe.getRetext().appendText("Disconnect from "+socketChannel.getRemoteAddress()+"\n");
                socketChannel.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return;
    }

    private void handleKey(SelectionKey key) {
        try {
            if (key.isValid()) {
                SocketChannel socket = (SocketChannel) key.channel();
                if (key.isConnectable()) {
                    //处于连接状态
                    if (socket.finishConnect()) {
                        //客户端连接成功

                        //doWrite(socket);
                    } else {//连接失败
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

    public  void setStop() {
        this.stop = true;
    }

    public static ClientRecevied getClientRe() {
        return clientRe;
    }

    public static ClientSend getClientSe() {
        return clientSe;
    }

}
