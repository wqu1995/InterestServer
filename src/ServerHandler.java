import javafx.scene.input.DataFormat;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Wenjun on 12/10/2016.
 */
public class ServerHandler implements Runnable{
    protected Socket clientSocket = null;
    protected String clientInput;
    protected Date lastChecked;

    public ServerHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void run(){
        System.out.println("Connection established");
        try{
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            clientInput = input.readLine();
            System.out.println(clientInput);
            switch(clientInput.split(" ")[0]){
                case "LOGIN":
                    String date = input.readLine();
                    date = date.substring(6,date.length());
                    System.out.println(date);
                    DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                    try{
                        lastChecked = format.parse(date);
                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                    clientInput = input.readLine();
                    if(clientInput.isEmpty())
                        output.writeBytes("IGP 214 No Cnotent\r\n\r\n");
            }
            //System.out.println(clientInput);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}