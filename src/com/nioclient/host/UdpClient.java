package com.nioclient.host;

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
    public static boolean one;

    public UdpClient(int port) {
        one=true;
        this.port = port;
    }

    @Override
    public void run() {
        while (one) {
            try {
                DatagramChannel channel = DatagramChannel.open();
                channel.socket().bind(new InetSocketAddress(9999));
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                ByteBuffer line = ByteBuffer.allocate(1024);
                buffer.clear();
                /*阻塞，等待发来的数据*/
                channel.receive(buffer);
                /*设置缓冲区可读*/
                buffer.flip();
                /*循环读出所有字符*/
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    if (b == 10 || b == 13) {
                        line.flip();
                        String user = Charset.forName("UTF-8").decode(line).toString();
                        String[] attribute = user.split(",");
                        System.out.println("UDP Receive:");
                        for (int i = 0; i < attribute.length; i++) {
                            System.out.print(attribute[i] + " ");
                        }
                        System.out.println("\n");
                        line.clear();
                    } else {
                        line.put(b);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(port+"; return");
    }

}
