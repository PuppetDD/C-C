package com.thread;

import com.nioclient.pane.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Send extends Thread {

    private ClientSend serverSend;
    private Socket socket;
    private PrintWriter printWriter;

    public Send(Socket socket, ClientSend serverSend) throws IOException {
        this.serverSend = serverSend;
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        serverSend.getSend().setOnAction(e->{
            if(socket.isConnected()&& serverSend.getMessage().getText()!=null){
                String s=socket.getLocalAddress()+":"+String.valueOf(socket.getLocalPort());
                printWriter.write(s+" Message:\n"+ serverSend.getMessage().getText() + "\n");
                serverSend.getMessage().clear();
                serverSend.getMessage().setText(null);
                printWriter.flush();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}



