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
                        System.out.println("In here");
                        clientInput = input.readLine();
                        System.out.println(clientInput);
                        int[] subGrouops = new int[clientInput.split(" ").length-1];
                        for(int i = 1, j=0; i<clientInput.split(" ").length; i++)
                            subGrouops[j++] = Integer.parseInt(clientInput.split(" ")[i]);

                        clientInput = input.readLine();
                        System.out.println(clientInput);

                        String newLastCheck =clientInput.substring(6, clientInput.length());
                        DateFormat newFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                        try {
                            lastChecked = newFormat.parse(newLastCheck);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        boolean newPostC = false;
                        BufferedReader groupNames = new BufferedReader(new FileReader("Groups.txt"));
                        ArrayList<String> subjects = new ArrayList<String>();
                        ArrayList<String> groupName = new ArrayList<String>();
                        ArrayList<String> xGroupName = new ArrayList<String>();
                        String name;
                        while((name = groupNames.readLine())!=null){
                            xGroupName.add(name);
                        }
                        groupNames.close();
                        for(int d = 0; d<subGrouops.length; d++){
                            BufferedReader postCheck = new BufferedReader(new FileReader("Group_"+subGrouops[d]+"_Post.txt"));
                            String line;
                            while((line = postCheck.readLine())!=null){
                                String postDate = line.substring(4,32);
                                Date postDate1 = newFormat.parse(postDate);
                                if(postDate1.after(lastChecked)){
                                    newPostC = true;
                                    subjects.add(line.substring(33, line.length()));
                                    groupName.add(xGroupName.get(subGrouops[d]-1));
                                }


                            }

                        }
                        if(newPostC){
                            output.writeBytes("IGP 250 New Posts\r\n\r\n");
                            for(int x = 0; x<subjects.size();x++){
                                output.writeBytes(subjects.get(x)+"\r\n");
                                output.writeBytes(groupName.get(x)+"\r\n");
                            }
                            output.writeBytes("\r\n");
                        }
                        else{
                            output.writeBytes("IGP 251 No Update\r\n\r\n");
                        }

                        break;
                    case "SG":
                        clientInput = input.readLine();
                        int[] subGroups = new int[clientInput.split(" ").length-1];
                        for(int i = 1, j = 0; i< clientInput.split(" ").length;i++){
                            subGroups[j++] = Integer.parseInt(clientInput.split(" ")[i]);
                        }
                        clientInput = input.readLine();

                        String[] readPosts = new String[clientInput.split(" ").length-1];

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
                                        if((groupPostIDs.get(x).equals(readPosts[j])))
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
                        String groupIndex  = clientInput.split(" ")[1];
                        BufferedReader posts = new BufferedReader(new FileReader("Group_"+groupIndex+"_Post.txt"));
                        int postIndex = 1;
                        String line;
                        while((line = posts.readLine())!=null){
                            postIndex++;
                        }
                        BufferedWriter newPost = new BufferedWriter(new FileWriter(groupIndex+"-"+postIndex+".txt"));
                        clientInput = input.readLine();
                        clientInput = input.readLine();
                        newPost.write(clientInput+"\n");

                        clientInput = input.readLine();
                        String subject = clientInput.substring(9, clientInput.length());
                        newPost.write(clientInput+"\n");

                        clientInput = input.readLine();
                        newPost.write(clientInput+"\n");

                        clientInput = input.readLine();
                        String newDate = clientInput.substring(6,clientInput.length());
                        newPost.write(clientInput+"\n");

                        while(!(clientInput = input.readLine()).equals(".")){
                            newPost.write(clientInput+"\n");
                        }
                        newPost.write(".");
                        newPost.close();
                        String newPostContent = groupIndex+"-"+postIndex+" "+newDate+" "+subject;
                        System.out.println(newPostContent);
                        BufferedWriter postModifier = new BufferedWriter(new FileWriter("Group_"+groupIndex+"_Post.txt", true));
                        postModifier.append("\n"+newPostContent);
                        postModifier.close();
                        output.writeBytes("IGP 320 Created\r\n\r\n");

                }

                //System.out.println(clientInput);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }
}