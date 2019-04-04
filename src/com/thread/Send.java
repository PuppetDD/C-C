package com.thread;

import com.pane.SendPane;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Send extends Thread {

    private SendPane sendPane;
    private Socket socket;
    private PrintWriter printWriter;

    public Send(Socket socket, SendPane sendPane) throws IOException {
        this.sendPane=sendPane;
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        sendPane.getSend().setOnAction(e->{
            if(socket.isConnected()&&sendPane.getMessage().getText()!=null){
                String s=socket.getLocalAddress()+":"+String.valueOf(socket.getLocalPort());
                printWriter.write(s+" Message:\n"+sendPane.getMessage().getText() + "\n");
                sendPane.getMessage().clear();
                sendPane.getMessage().setText(null);
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



