package com.thread;

import com.nioserver.pane.SeverRecevied;
import com.nioserver.pane.ServerSend;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Read extends Thread {

    private SeverRecevied severRecevied;
    private ServerSend serverSend;
    private Socket socket;
    private BufferedReader bufferedReader;
    private String str = null;

    public Read(Socket socket, SeverRecevied severRecevied, ServerSend serverSend){
        this.severRecevied = severRecevied;
        this.serverSend = serverSend;
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true){
            try {
                socket.sendUrgentData(0xFF);
            } catch (Exception e) {
                ServerSend.count--;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        serverSend.getBegin().setText("Connect");
                        serverSend.getItems().remove(socket);
                        serverSend.getStatus().setText(ServerSend.count+" Connecting");
                    }
                });
                severRecevied.getRetext().appendText("Disconnect from "+socket.getRemoteSocketAddress()+"\n");
                return;
            }
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while((str = bufferedReader.readLine())!=null){
                    severRecevied.getRetext().appendText(str+"\n");
                }
            } catch (IOException e) {

            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
