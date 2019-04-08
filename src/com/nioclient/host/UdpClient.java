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
    private static DatagramChannel channel = null;
    public static Boolean error;
    public Boolean one;

    public UdpClient(int port) {
        System.out.println("\nNew UdpClient!!!!!!!!");
        error = false;
        this.port = port;
        try {
            if (channel != null) {
                //关闭上一次的线程通道，造成线程异常退出，释放socket以及端口
                channel.close();
                System.out.println("channel.close()");
            }
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(this.port));
        } catch (IOException e) {
            NioClient.getClientRe().getRetext().appendText("The listening port is already in use\n");
            error = true;
        }
        this.one = true;
        System.out.println("New UdpClient end!!!!!!!!");
    }

    @Override
    public void run() {
        while (one) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                ByteBuffer line = ByteBuffer.allocate(1024);
                /*阻塞，等待发来的数据*/
                System.out.println(port + ": Thread is running!!!!!!!!");
                channel.receive(buffer);
                System.out.println("channel.receive(buffer);");
                /*设置缓冲区可读*/
                buffer.flip();
                /*循环读出所有字符*/
                int n = 0;
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    if (b == 10 || b == 13) {
                        line.flip();
                        String message = Charset.forName("UTF-8").decode(line).toString();
                        if (n == 0 && message.compareTo(NioClient.local.toString()) != 0) {
                            break;
                        } else if(message.compareTo(NioClient.local.toString()) != 0){
                            NioClient.getClientRe().getRetext().appendText(message + "\n");
                        }
                        n++;
                        System.out.println(message);
                        line.clear();
                    } else {
                        line.put(b);
                    }
                }
                buffer.clear();
            } catch (Exception e) {
                System.out.println("break");
                this.one = false;
            }
        }
        System.out.println(port + "; return");
    }

}
