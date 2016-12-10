import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Wenjun on 12/10/2016.
 */
public class InterestGroupServer implements Runnable {
    protected int serverPort = 7667;
    protected ServerSocket serverSocket = null;
    protected Thread runningThread = null;

    public InterestGroupServer(){};

    public static void main(String[] args){
        InterestGroupServer server = new InterestGroupServer();
        new Thread(server).start();


    }

    public void run(){
        synchronized (this){
            this.runningThread = Thread.currentThread();
        }

        try{
            this.serverSocket = new ServerSocket(this.serverPort);
        }catch (IOException e){
            throw new RuntimeException("Can not open port"+ this.serverPort);
        }

        while(true){
            Socket clientSocket = null;

            try{
                clientSocket = this.serverSocket.accept();
            }catch (IOException e){
                throw new RuntimeException("Error accepting client connection", e);
            }

            new Thread(new ServerHandler(clientSocket)).start();
        }
    }
}

