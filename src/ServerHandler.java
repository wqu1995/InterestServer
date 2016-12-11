import javafx.scene.input.DataFormat;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Wenjun on 12/10/2016.
 */
public class ServerHandler implements Runnable{
    protected Socket clientSocket = null;
    protected String clientInput;
    protected Date lastChecked;
    protected BufferedReader input;
    protected DataOutputStream output;
    protected boolean isLogout;


    public ServerHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Connection established");
        isLogout = false;
        try {
             input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             output = new DataOutputStream(clientSocket.getOutputStream());
        }catch (IOException e) {
            e.printStackTrace();
        }


        while (!isLogout) {
            try {
                clientInput = input.readLine();
                System.out.println(clientInput);
                switch (clientInput.split(" ")[0]) {
                    case "LOGIN":
                        String date = input.readLine();
                        date = date.substring(6, date.length());
                        System.out.println(date);
                        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                        try {
                            lastChecked = format.parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        clientInput = input.readLine();
                        if (clientInput.isEmpty())
                            output.writeBytes("IGP 214 No Content\r\n\r\n");
                        break;
                    case "AG":
                        output.writeBytes("IGP 310 ALL Groups\r\n\r\n");
                        try {
                            BufferedReader Groups = new BufferedReader(new FileReader("Groups.txt"));
                            String line;
                            while ((line = Groups.readLine()) != null) {
                                output.writeBytes(line+"\r\n");
                            }
                            output.writeBytes("\r\n");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "CK":
                        output.writeBytes("IGP 251 No Update\r\n\r\n");
                        break;
                    case "SG":
                        clientInput = input.readLine();
                        int[] subGroups = new int[clientInput.split(" ").length-1];
                        for(int i = 1, j = 0; i< clientInput.split(" ").length;i++){
                            subGroups[j++] = Integer.parseInt(clientInput.split(" ")[i]);
                        }
                        clientInput = input.readLine();

                        String[] readPosts = new String[clientInput.split("").length-1];

                        for(int i =1, j = 0; i<clientInput.split(" ").length; i++){
                            readPosts[j++] = clientInput.split(" ")[i];
                        }
                        String result = "";
                        if(input.readLine().isEmpty()){
                            output.writeBytes("IGP 207 OK\r\n\r\n");

                            for(int i = 0; i<subGroups.length;i++){
                                BufferedReader Groups = new BufferedReader(new FileReader("Group_"+ subGroups[i]+"_Post.txt"));
                                String line;
                                ArrayList<String> groupPostIDs = new ArrayList<String>();
                                while((line = Groups.readLine())!= null){
                                    groupPostIDs.add(line.split(" ")[0]);
                                }
                                int counter = groupPostIDs.size();
                                for(int x = 0; x<groupPostIDs.size(); x++){
                                    for(int j = 0; j < readPosts.length; j++){
                                        if((groupPostIDs.contains(readPosts[j])))
                                            counter = counter -1;
                                    }
                                }
                                result = result+ counter+" ";

                            }

                            output.writeBytes(result+"\r\n\r\n");
                        }
                        break;
                    case "RG":
                        int groupID = Integer.parseInt(clientInput.split(" ")[1]);
                        if(input.readLine().isEmpty()){
                            output.writeBytes("IGP 207 OK\r\n\r\n");
                            BufferedReader Groups = new BufferedReader(new FileReader("Group_"+ groupID+"_Post.txt"));
                            String line;
                            while((line = Groups.readLine())!= null){
                                System.out.println(line);
                                output.writeBytes(line+"\r\n");
                            }
                            output.writeBytes("\r\n");
                        }
                        break;
                    case "RP":
                        String postNumber = clientInput.split(" ")[1];
                        if(input.readLine().isEmpty()){
                            output.writeBytes("IGP 207 OK\r\n\r\n");
                            BufferedReader post = new BufferedReader(new FileReader(postNumber+".txt"));
                            String line;
                            while((line = post.readLine())!=null){
                                System.out.println(line);
                                output.writeBytes(line+"\r\n");
                            }
                            output.writeBytes("\r\n");
                        }
                        break;
                    case "NP":
                        System.out.println("asd");


                }

                //System.out.println(clientInput);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}