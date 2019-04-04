package com.thread;

import com.pane.ReceviedPane;
import com.pane.SendPane;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Read extends Thread {

    private ReceviedPane receviedPane;
    private SendPane sendPane;
    private Socket socket;
    private BufferedReader bufferedReader;
    private String str = null;

    public Read(Socket socket, ReceviedPane receviedPane, SendPane sendPane){
        this.receviedPane=receviedPane;
        this.sendPane=sendPane;
        this.socket = socket;
    }

    @Override
    public void run() {
        while(true){
            try {
                socket.sendUrgentData(0xFF);
            } catch (Exception e) {
                SendPane.count--;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sendPane.getConnect().setText("Connect");
                        sendPane.getItems().remove(socket);
                        sendPane.getStatus().setText(SendPane.count+" Connecting");
                    }
                });
                receviedPane.getRetext().appendText("Disconnect from "+socket.getRemoteSocketAddress()+"\n");
                return;
            }
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while((str = bufferedReader.readLine())!=null){
                    receviedPane.getRetext().appendText(str+"\n");
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
