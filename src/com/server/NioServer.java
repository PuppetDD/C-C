package com.server;

import com.thread.Read;
import com.pane.ReceviedPane;
import com.pane.SendPane;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioServer extends Thread {

    private static ReceviedPane serverRe=new ReceviedPane();
    private static SendPane serverSe=new SendPane("NioServer");
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Boolean stop=false;

    public NioServer(){
        try {
            selector=Selector.open();
            serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(9999),1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverRe.getRetext().appendText("Waiting for Connecting...\n");
        } catch (IOException e) {
            serverRe.getRetext().appendText("Server Startup Failure\n");
        }
    }

    @Override
    public void run(){
        while(!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys=selector.selectedKeys();
                Iterator<SelectionKey> iterator=selectionKeys.iterator();
                SelectionKey key=null;
                while (iterator.hasNext()){
                    key=iterator.next();
                    iterator.remove();
                    try{
                        HandleKey(key);
                    }catch(Exception e){
                        if(key!=null){
                            key.cancel();
                            if(key.channel()!=null){
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                serverRe.getRetext().appendText("Nobody connect\n");
            }
        }
        ServerSocket server=null;
        try{
            server = new ServerSocket(8888);
            serverSe.getTextip().setText(server.getInetAddress().toString());
            serverSe.getTextport().setText(String.valueOf(server.getLocalPort()));
            while(true){
                try{
                    Socket socket=server.accept();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            serverSe.getItems().add(socket);
                        }
                    });
                    serverSe.getList().setItems(serverSe.getItems());
                    SocketAddress address=socket.getRemoteSocketAddress();
                    socket.setKeepAlive(true);
                    SendPane.count++;
                    serverRe.getRetext().appendText("Connected from  "+address+"\n");
                    serverRe.getRetext().appendText(SendPane.count+" client connect successfully\n");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            serverSe.getStatus().setText(SendPane.count+" Connecting");
                        }
                    });
                    Read read=new Read(socket,serverRe,serverSe);
                    read.start();
                }catch (Exception ex){
                    serverRe.getRetext().appendText("Nobody connected\n");
                }
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            serverRe.getRetext().appendText("NioServer error:The port is already in use\n");
        }finally {
            if(server!=null){
                try{
                    server.close();
                }catch (Exception ex){

                }
            }
        }
    }

    private void HandleKey(SelectionKey key) throws IOException{
        if(key.isValid()){
            SocketChannel sc=(SocketChannel) key.channel();
            if(key.isAcceptable()){
                if(sc.finishConnect()){
                    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }else{
                    System.out.println("Client connecting failed");
                }
            }
            if(key.isReadable()){

            }
        }
    }

    public void Stop() { stop=true; }

    public static ReceviedPane getServerRe(){ return serverRe; }

    public static SendPane getServerSe(){ return serverSe; }

}
