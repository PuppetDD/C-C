package com.nioclient.host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

/**
 * C-C
 * com.nioclient.host
 *
 * @author GOLD
 * @date 2019/4/7
 */
public class UdpClient extends Thread {

    private int port;
    private DatagramChannel channel = null;
    public static boolean one;

    public UdpClient(int port) {
        System.out.println("New UdpClient!!!!!!!!");
        one=true;
        this.port = port;
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(this.port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (one) {
            System.out.println(port+": Thread is running!!!!!!!!");
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                ByteBuffer line = ByteBuffer.allocate(1024);
                /*阻塞，等待发来的数据*/
                channel.receive(buffer);
                System.out.println("channel.receive(buffer);");
                /*设置缓冲区可读*/
                buffer.flip();
                /*循环读出所有字符*/
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    if (b == 10 || b == 13) {
                        line.flip();
                        String user = Charset.forName("UTF-8").decode(line).toString();
                        System.out.println(user+"\n");
                        line.clear();
                    } else {
                        line.put(b);
                    }
                }
                buffer.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            channel.close();
            channel.socket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(port+"; return");
    }

    public DatagramChannel getChannel(){
        return  channel;
    }

}
