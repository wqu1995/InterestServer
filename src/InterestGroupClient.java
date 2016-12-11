import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

// streams, serialization and the file system
// applications

/**
 * Created by Yuege on 12/6/2016.
 */
public class InterestGroupClient {
    static ArrayList<String> groups=new ArrayList<>(),
            contents=new ArrayList<>();
    static ArrayList<String> subsGroupsIDs=new ArrayList<>(),subsGroupsNames=new ArrayList<>();
    static ArrayList<String> readPosts=new ArrayList<>(),newPosts=new ArrayList<>();
    static ArrayList<String> postSubjects=new ArrayList<>();
    static BufferedReader inFromUser;
    static DataOutputStream outToServer;
    static BufferedReader inFromServer;
    static String groupName,userName;
    static final int N=5;
    public static void main(String argv[]) throws Exception
    {
        String sentence;
        String modifiedSentence;
        boolean logIn=false;

        /*URL         workDirURL  = InterestGroupClient.class.getClassLoader().getResource("");
        File userFile=new File(workDirURL.getFile());
        userFile=new File(userFile.toString()+File.separator+"user5"+".json");
        if(!userFile.exists())
            userFile.createNewFile();*/

        /*List<String> lines = Arrays.asList("The first line12", "The second line2");
        Path file = Paths.get("the-file-name1.txt");
        Files.write(file, lines, Charset.forName("UTF-8"));*/

        // get the server port form command line
        int lisPort = Integer.parseInt(argv[1]);

        // create an input stream from the System.in
        inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        // create a client socket (TCP)
        Socket clientSocket = new Socket(argv[0], lisPort);

        // create an output stream from the socket output steram
        outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        // create an input stream from the socket input steram
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // read a line form the standard input
        while (!logIn){
            sentence = inFromUser.readLine();
            if(sentence.split(" ").length!=2||!sentence.split(" ")[0].equals("login"))
                System.out.println("Invalid command, you have not logged in yet.");
            else if(sentence.split(" ")[0].equals("login")) {
                // read user info and store them into two sets.
                userName=sentence.split(" ")[1];
              //  outToServer.writeBytes("LOGIN IGP hihow are you\r\n");

                LogIn(userName);
                // send the AG IGP request and store all the groups' info in groups array
                AG();
                logIn=true;
                System.out.println("Welcome, "+sentence.split(" ")[1]+". Please enter one of the commands:" +
                        " \"ag\",\"sg\",\"rg\",\"help\",or \"logout\"");
            }
        }

        while(logIn){
            //send LOGIN request
            sentence=inFromUser.readLine();
            switch (sentence.split(" ")[0]){
                case "ag":
                    if(sentence.split(" ").length==1)
                        ag(N);// take user to ag interface
                    else if(sentence.split(" ").length==2) {
                        int n=Integer.parseInt(sentence.split(" ")[1]);
                        ag(n);
                    }
                    else
                        System.out.println("Invalid command.");
                    break;
                case "sg":

                    for (String s:subsGroupsIDs){
                        if(!subsGroupsNames.contains(groups.get(Integer.parseInt(s)-1)))
                            subsGroupsNames.add(groups.get(Integer.parseInt(s)-1));
                    }
                    // send the SG IGP request
                    SG();
                    if(sentence.split(" ").length==1)
                        sg(N);// take user to sg interface
                    else if(sentence.split(" ").length==2) {
                        int n=Integer.parseInt(sentence.split(" ")[1]);
                        sg(n);
                    }
                    else
                        System.out.println("Invalid command.");
                    break;
                case "rg":
                    if(sentence.split(" ").length==2) {
                        groupName=sentence.split(" ")[1];
                        // send the RG IGP request
                        if(!RG(groupName)) {
                            System.out.println("Wrong group name, or you have not subscribed this group");
                            break;
                        }
                        rg(N);// take user to rg interface
                    }
                    else if(sentence.split(" ").length==3) {
                        groupName=sentence.split(" ")[1];
                        // send the RG IGP request
                        if(!RG(groupName)) {
                            System.out.println("Wrong group name, or you have not subscribed this group");
                            break;
                        }
                        int n=Integer.parseInt(sentence.split(" ")[2]);
                        rg(n);
                    }
                    else
                        System.out.println("Invalid command.");
                    break;
                case "logout":
                    logIn=false;
                    break;
                default:
                    System.out.println("Invalid Command.");
            }
        }
        // close the socket
        clientSocket.close();
    }

    private static void rg(int n) throws IOException {
        boolean showedAll=false,rg=true,wrongIndex=false;
        int i=1,currentN=n, j, newPostNum=0;
        ArrayList<String> unreadSubjects=new ArrayList<>(),readSubjects=new ArrayList<>();
        for (String post:postSubjects){
            if(readPosts.contains(post.split(" ")[0])){
                readSubjects.add(post);
            }
            else {
                unreadSubjects.add(post);
                newPostNum++;
            }
        }
        System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
        //Check if there is new post from subscribed groups.
        String sTemp=CK();
        if(sTemp!=null)
            System.out.println(sTemp);
        for( j=1;j<=currentN;j++,i++){
            if(postSubjects.size()==i){
                if(i>newPostNum)
                    System.out.println(j+".     "+readSubjects.get(i-newPostNum-1));
                else
                    System.out.println(j+".N    "+unreadSubjects.get(i-1));
                showedAll=true;
                break;
            }
            if(i>newPostNum)
                System.out.println(j+".     "+readSubjects.get(i-newPostNum-1));
            else
                System.out.println(j+".N    "+unreadSubjects.get(i-1));
        }
        while (rg){
            String command=inFromUser.readLine();
            switch (command.split(" ")[0].toLowerCase()){
                case "r":
                    if(command.split(" ").length!=2) {
                        System.out.println("Invalid Command.");
                        break;
                    }
                    String num=command.split(" ")[1];
                    if(num.length()==1){
                        int index=Integer.parseInt(num);
                        int trueIndex;
                        if(i>currentN)
                            trueIndex=i-currentN+index-1;
                        else
                            trueIndex=index;
                        if(trueIndex>newPostNum)
                            break;
                        else {
                            String post=unreadSubjects.remove(trueIndex-1);
                            readSubjects.add(0,post);
                            readPosts.add(post.split(" ")[0]);
                            newPostNum--;
                            System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
                            for( j=1;j<=currentN;j++){
                                if(postSubjects.size()==i){
                                    if(trueIndex-index+j>newPostNum)
                                        System.out.println(j+".     "+readSubjects.get(trueIndex-index+j-newPostNum-1));
                                    else
                                        System.out.println(j+".N    "+unreadSubjects.get(trueIndex-index+j-1));
                                    showedAll=true;
                                    break;
                                }
                                if(trueIndex-index+j>newPostNum)
                                    System.out.println(j+".     "+readSubjects.get(trueIndex-index+j-newPostNum-1));
                                else
                                    System.out.println(j+".N    "+unreadSubjects.get(trueIndex-index+j-1));
                            }
                        }
                    }
                    else if(num.length()==3){
                        int firstIndex=Integer.parseInt(num.substring(0,1));
                        int lastIndex=Integer.parseInt(num.substring(2,3));
                        int trueIndex1;
                        if(i>currentN)
                            trueIndex1=i-currentN+firstIndex-1;
                        else
                            trueIndex1=firstIndex;
                        int trueIndex2;
                        if(i>currentN)
                            trueIndex2=i-currentN+lastIndex-1;
                        else
                            trueIndex2=lastIndex;
                        if(trueIndex2>newPostNum)
                            break;
                        else {
                            String[] posts=new String[trueIndex2-trueIndex1+1];
                            for(int q=0;q<posts.length;q++){
                                posts[q]=unreadSubjects.remove(trueIndex1+q-1);
                            }
                            for (String s:posts){
                                readSubjects.add(0,s);
                                readPosts.add(s.split(" ")[0]);
                                newPostNum--;
                            }
                            System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
                            for( j=1;j<=currentN;j++){
                                if(postSubjects.size()==i){
                                    if(trueIndex1-firstIndex+j>newPostNum)
                                        System.out.println(j+".     "+readSubjects.get(trueIndex1-firstIndex+j-newPostNum-1));
                                    else
                                        System.out.println(j+".N    "+unreadSubjects.get(trueIndex1-firstIndex+j-1));
                                    showedAll=true;
                                    break;
                                }
                                if(trueIndex1-firstIndex+j>newPostNum)
                                    System.out.println(j+".     "+readSubjects.get(trueIndex1-firstIndex+j-newPostNum-1));
                                else
                                    System.out.println(j+".N    "+unreadSubjects.get(trueIndex1-firstIndex+j-1));
                            }
                        }
                    }
                    else {
                        System.out.println("Invalid Command.");
                    }
                    break;
                case "p":
                    String postContent="Group: "+groupName+"\r\n";
                    System.out.println("Please enter the subject of the post: ");
                    String subject=inFromUser.readLine();
                    postContent=postContent+"Subject: "+subject+"\r\nAuthor: "+userName+"\r\nDate: "+new Date()+"\r\n\r\n";
                    System.out.println("Please enter content of the post, it ends by a dot by itself on a line");
                    String contentLine=inFromUser.readLine();
                    while (!contentLine.equals(".")){
                        postContent=postContent+contentLine+"\r\n";
                        contentLine=inFromUser.readLine();
                    }
                    postContent=postContent+".\r\n";
                    NP(postContent,groups.indexOf(groupName));
                    System.out.println("New post posted!");
                    System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
                    for( j=1;j<=currentN;j++){
                        if(postSubjects.size()==i){
                            if(i-currentN+j-1>newPostNum)
                                System.out.println(j+".     "+readSubjects.get(i-currentN+j-newPostNum-2));
                            else
                                System.out.println(j+".N    "+unreadSubjects.get(i-currentN+j-2));
                            showedAll=true;
                            break;
                        }
                        if(i-currentN+j-1>newPostNum)
                            System.out.println(j+".     "+readSubjects.get(i-currentN+j-newPostNum-2));
                        else
                            System.out.println(j+".N    "+unreadSubjects.get(i-currentN+j-2));
                    }
                    break;
                case "n":
                    sTemp=CK();
                    if(sTemp!=null)
                        System.out.println(sTemp);
                    if(showedAll) {
                        rg = false;
                        break;
                    }
                    System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
                    for( j=1;j<=currentN;j++,i++){
                        if(postSubjects.size()==i){
                            if(i>newPostNum)
                                System.out.println(j+".     "+readSubjects.get(i-newPostNum-1));
                            else
                                System.out.println(j+".N    "+unreadSubjects.get(i-1));
                            showedAll=true;
                            break;
                        }
                        if(i>newPostNum)
                            System.out.println(j+".     "+readSubjects.get(i-newPostNum-1));
                        else
                            System.out.println(j+".N    "+unreadSubjects.get(i-1));
                    }
                    break;
                case "q":
                    rg=false;
                    break;
                default:
                    try {
                       if(command.split(" ").length>1){
                            System.out.println("Invalid command.");break;
                        }
                        String postToRead;
                        int id=Integer.parseInt(command);
                        int trueID;
                        if(i>currentN)
                            trueID=i-currentN+id-1;
                        else
                            trueID=id;
                        if(trueID>newPostNum)
                            postToRead=readSubjects.remove(trueID-newPostNum-1);
                        else
                            postToRead=unreadSubjects.remove(trueID-1);
                        if(trueID<=newPostNum){
                            newPostNum--;
                            readSubjects.add(0,postToRead);
                            readPosts.add(postToRead.split(" ")[0]);
                        }
                        RP(postToRead.split(" ")[0]);
                        rp(currentN);
                        System.out.println("Read Group. Please enter one of the subcommands: [id],r,n,p,q");
                        for( j=1;j<=currentN;j++){
                            if(postSubjects.size()==trueID-id+j){
                                if(trueID-id+j>newPostNum)
                                    System.out.println(j+".     "+readSubjects.get(trueID-id+j-newPostNum-1));
                                else
                                    System.out.println(j+".N    "+unreadSubjects.get(trueID-id+j-1));
                                showedAll=true;
                                break;
                            }
                            if(trueID-id+j>newPostNum)
                                System.out.println(j+".     "+readSubjects.get(trueID-id+j-newPostNum-1));
                            else
                                System.out.println(j+".N    "+unreadSubjects.get(trueID-id+j-1));
                        }
                    }
                   catch (Exception e){
                        e.printStackTrace();
                       System.out.println("Invalid command.");break;
                   }
                   break;
            }
        }
        System.out.println("Exit read group. Please enter one of the commands: \"ag\",\"sg\",\"rg\",\"help\",or \"logout\"");
    }

    private static void rp(int currentN) throws IOException {
        boolean rp=true,showedAll=false;
        System.out.println("Read post. two sub-sub-commands: n,q");
        int line=1;
        for(int q=1;q<=currentN;q++,line++) {
            if (line==contents.size()) {
                System.out.println(contents.get(line-1));
                showedAll = true;
                break;
            }
            System.out.println(contents.get(line-1));
        }

        while (rp){
            String subcommand=inFromUser.readLine();
            switch (subcommand){
                case "n":
                    if(showedAll) {
                        System.out.print("Post ends. ");
                        rp = false;break;
                    }
                    for(int q=1;q<=currentN;q++,line++) {
                        if (line==contents.size()) {
                            System.out.println(contents.get(line-1));
                            showedAll = true;
                            break;
                        }
                        System.out.println(contents.get(line-1));
                    }
                    break;
                case "q":
                    rp=false;break;
            }
        }
        System.out.println("Exit read post.");
    }

    private static void sg(int n) throws IOException {
        boolean showedAll=false,sg=true,wrongIndex=false;
        int i,currentN=n;
        System.out.println("Subscribed Groups. Please enter one of the subcommands: u,n,q");
        //Check if there is new post from subscribed groups.
        String sTemp=CK();
        if(sTemp!=null)
            System.out.println(sTemp);
        for( i=1;i<=currentN;i++){
            if(subsGroupsIDs.size()==i){
                if(Integer.parseInt(newPosts.get(i-1))==0)
                    System.out.println(i+"         "+subsGroupsNames.get(i-1));
                else
                    System.out.println(i+"    "+newPosts.get(i-1)+"   "+subsGroupsNames.get(i-1));
                showedAll=true;
                break;
            }
            if(Integer.parseInt(newPosts.get(i-1))==0)
                System.out.println(i+"         "+subsGroupsNames.get(i-1));
            else
                System.out.println(i+"    "+newPosts.get(i-1)+"   "+subsGroupsNames.get(i-1));
        }
        while (sg){
            String command=inFromUser.readLine();
            switch (command.split(" ")[0].toLowerCase()){
                case "u":
                    try {
                        ArrayList temporaryGroups=new ArrayList();
                        for(String s:command.substring(2,command.length()).split(" ")) {
                            if(Integer.parseInt(s)>=i||Integer.parseInt(s)<i-currentN){
                                System.out.println("group not in the list!");
                                wrongIndex=true;
                                break;
                            }
                            temporaryGroups.add(Integer.parseInt(s));
                        }
                        if(wrongIndex) {
                            temporaryGroups.clear();
                            wrongIndex=false;
                            break;
                        }
                        // If group indices are valid, add it to user file.
                        for(Object s:temporaryGroups) {
                            newPosts.remove((int)(s)-1);
                            String groupToBeRemoved=subsGroupsNames.remove((int)(s)-1);
                            int IDtobeRemoved=groups.indexOf(groupToBeRemoved)+1;
                            subsGroupsIDs.remove(IDtobeRemoved+"");
                        }
                        System.out.println("You have successfully unsubscribed these groups. Please enter next command.");
                        for( int j=i-currentN;j<i;j++){
                            if(subsGroupsIDs.size()==j){
                                if(Integer.parseInt(newPosts.get(j-1))==0)
                                    System.out.println(j+"         "+subsGroupsNames.get(j-1));
                                else
                                    System.out.println(j+"    "+newPosts.get(j-1)+"   "+subsGroupsNames.get(j-1));
                                showedAll=true;
                                break;
                            }
                            if(Integer.parseInt(newPosts.get(j-1))==0)
                                System.out.println(j+"         "+subsGroupsNames.get(j-1));
                            else
                                System.out.println(j+"    "+newPosts.get(j-1)+"   "+subsGroupsNames.get(j-1));
                        }
                    }
                    catch (Exception e){
                        System.out.println("Invalid command.");
                        break;
                    }
                    break;
                case "n":
                    sTemp=CK();
                    if(sTemp!=null)
                        System.out.println(sTemp);
                    if(showedAll) {
                        sg = false;
                        break;
                    }
                    System.out.println("Subscribed Groups. Please enter one of the subcommands: u,n,q");
                    for( int j=i;j<currentN+i;j++){
                        if(subsGroupsIDs.size()==j){
                            if(Integer.parseInt(newPosts.get(j-1))==0)
                                System.out.println(j+"         "+subsGroupsNames.get(j-1));
                            else
                                System.out.println(j+"    "+newPosts.get(j-1)+"   "+subsGroupsNames.get(j-1));
                            showedAll=true;
                            break;
                        }
                        if(Integer.parseInt(newPosts.get(j-1))==0)
                            System.out.println(j+"         "+subsGroupsNames.get(j-1));
                        else
                            System.out.println(j+"    "+newPosts.get(j-1)+"   "+subsGroupsNames.get(j-1));
                    }
                    i=i+currentN;
                    break;
                case "q":
                    sg=false;
                    break;
                default:
                    System.out.println("Invalid command.");break;
            }
        }
        System.out.println("Exit Subscribed groups. Please enter one of the commands: \"ag\",\"sg\",\"rg\",\"help\",or \"logout\"");
    }

    private static void ag(int n) throws IOException {
        for(String x : groups){
            System.out.println(x);
        }
        boolean showedAll=false,ag=true;boolean wrongIndex=false;
        int i,currentN=n;
        ArrayList<String> temporaryGroups=new ArrayList();
        System.out.println("All Groups. Please enter one of the subcommands: s,u,n,q");
        //Check if there is new post from subscribed groups.

        String sTemp=CK();
        if(sTemp!=null)
            System.out.println(sTemp);
        for( i=1;i<=currentN;i++){
            if(groups.size()==i){
                if(subsGroupsIDs.contains(i+""))
                    System.out.println(i+". (s) "+groups.get(i-1));
                else
                    System.out.println(i+". ( ) "+groups.get(i-1));
                showedAll=true;
                break;
            }
            if(subsGroupsIDs.contains(i+""))
                System.out.println(i+". (s) "+groups.get(i-1));
            else
                System.out.println(i+". ( ) "+groups.get(i-1));
        }
        while (ag){
            String command=inFromUser.readLine();
            switch (command.split(" ")[0].toLowerCase()){
                case "s":
                    try {

                        for(String s:command.substring(2,command.length()).split(" ")) {
                            System.out.println(s);
                            if(Integer.parseInt(s)>=i||Integer.parseInt(s)<i-currentN){
                                System.out.println("group not in the list!");
                                wrongIndex=true;
                                break;
                            }
                            temporaryGroups.add(s);
                        }
                        if(wrongIndex) {
                            temporaryGroups.clear();
                            wrongIndex=false;
                            break;
                        }
                        // If group indices are valid, add it to user file.
                        for(String s:temporaryGroups) {
                            if(subsGroupsIDs.contains(s)){
                                continue;
                            }
                            subsGroupsIDs.add(s);
                        }
                        temporaryGroups.clear();
                        System.out.println("You have successfully subscribed these groups. Please enter next command.");
                        for( int q=i-currentN;q<i;q++){
                            if(groups.size()==i){
                                if(subsGroupsIDs.contains(i+""))
                                    System.out.println(i+". (s) "+groups.get(q-1));
                                else
                                    System.out.println(i+". ( ) "+groups.get(q-1));
                                showedAll=true;
                                break;
                            }
                            if(subsGroupsIDs.contains(q+""))
                                System.out.println(q+". (s) "+groups.get(q-1));
                            else
                                System.out.println(q+". ( ) "+groups.get(q-1));
                        }
                    }
                    catch (Exception e){
                        System.out.println("Invalid command.");
                        break;
                    }
                    break;
                case "u":
                    try {
                        for(String s:command.substring(2,command.length()).split(" ")) {
                            if(Integer.parseInt(s)>=i||Integer.parseInt(s)<i-currentN){
                                System.out.println("group not in the list!");
                                wrongIndex=true;
                                break;
                            }
                            temporaryGroups.add(s);
                        }
                        if(wrongIndex) {
                            temporaryGroups.clear();
                            wrongIndex=false;
                            break;
                        }
                        // If group indices are valid, add it to user file.
                        for(String s:temporaryGroups) {
                            if(!subsGroupsIDs.contains(s)){
                                continue;
                            }
                            subsGroupsIDs.remove(s);
                        }
                        temporaryGroups.clear();
                        System.out.println("You have successfully unsubscribed these groups. Please enter next command.");
                        for( int q=i-currentN;q<i;q++){
                            if(groups.size()==i){
                                if(subsGroupsIDs.contains(i+""))
                                    System.out.println(i+". (s) "+groups.get(q-1));
                                else
                                    System.out.println(i+". ( ) "+groups.get(q-1));
                                showedAll=true;
                                break;
                            }
                            if(subsGroupsIDs.contains(q+""))
                                System.out.println(q+". (s) "+groups.get(q-1));
                            else
                                System.out.println(q+". ( ) "+groups.get(q-1));
                        }
                    }
                    catch (Exception e){
                        System.out.println("Invalid command.");
                        break;
                    }
                    break;
                case "n":

                    sTemp=CK();
                    if(sTemp!=null)
                        System.out.println(sTemp);
                    if(showedAll) {
                        ag = false;
                        break;
                    }
                    System.out.println("All Groups. Please enter one of the subcommands: s,u,n,q");
                    for( int j=i;j<currentN+i;j++){
                        if(groups.size()==j){
                            if(subsGroupsIDs.contains(j+""))
                                System.out.println(j+". (s) "+groups.get(j-1));
                            else
                                System.out.println(j+". ( ) "+groups.get(j-1));
                            showedAll=true;
                            break;
                        }
                        if(subsGroupsIDs.contains(j+""))
                            System.out.println(j+". (s) "+groups.get(j-1));
                        else
                            System.out.println(j+". ( ) "+groups.get(j-1));
                    }
                    i=i+currentN;
                    break;
                case "q":
                    ag=false;
                    break;
                default:
                    System.out.println("Invalid command.");break;
            }
        }
        System.out.println("Exit All groups. Please enter one of the commands: \"ag\",\"sg\",\"rg\",\"help\",or \"logout\"");
    }

    private static String CK() throws IOException {
        Date now=new Date();
        outToServer.writeBytes("CK IGP\r\n");
        String subg="";
        for (String s:subsGroupsIDs)
            subg=subg+s+" ";
        outToServer.writeBytes("Subscribed-GroupIDs: "+subg+"\r\n");
        outToServer.writeBytes("Date: "+now+"\r\n\r\n");
        if(inFromServer.readLine().equals("IGP 251 No Update")) {
            inFromServer.readLine();
            return null;
        }
        else if(inFromServer.readLine().equals("IGP 250 New Posts")){
            inFromServer.readLine();
            String subject=inFromServer.readLine();
            String group=inFromServer.readLine();
            return "There is a new post: "+subject+" in group: "+group;
        }
        return null;
    }

    private static void NP(String post,int groupID) throws IOException {
        outToServer.writeBytes("NP "+(groupID+1)+" IGP\r\n\r\n");
        outToServer.writeBytes(post+"\r\n");
        if(inFromServer.readLine().equals("IGP 320 Created"))
            inFromServer.readLine();

    }

    private static void RP(String postID) throws IOException {
        outToServer.writeBytes("RP "+postID+" IGP\r\n\r\n");
        if(inFromServer.readLine().equals("IGP 207 OK")){
            inFromServer.readLine();
            contents=new ArrayList<>();
            String s=inFromServer.readLine();
            while (!s.equals(".")){
                    contents.add(s);
                s=inFromServer.readLine();
            }
        }
    }

    private static boolean RG(String gname) throws IOException {
        if(!subsGroupsIDs.contains(Integer.toString(groups.indexOf(gname)+1)))
            return false;
        outToServer.writeBytes("RG "+(groups.indexOf(gname)+1)+" IGP\r\n\r\n");
        if(inFromServer.readLine().equals("IGP 207 OK")){
            inFromServer.readLine();
            postSubjects=new ArrayList<>();
            String s=inFromServer.readLine();
            while (!s.isEmpty()){
                postSubjects.add(s);
                s=inFromServer.readLine();
            }
        }
        return true;
    }

    private static void SG() throws IOException {
        outToServer.writeBytes("SG IGP\r\n");
        String subg="";
        for(String s:subsGroupsIDs)
            subg=subg+s+" ";
        outToServer.writeBytes("Subscribed-GroupIDs: "+subg+"\r\n");
        String readP="";
        for(String s:readPosts)
            readP=readP+s+" ";
        outToServer.writeBytes("Read-Posts: "+readP+"\r\n\r\n");
        newPosts=new ArrayList<>();
        String temp = inFromServer.readLine();
        System.out.println(temp);
        if(temp.equals("IGP 207 OK")){
            String a =inFromServer.readLine();


            String newP=inFromServer.readLine();
            System.out.println(newPosts);
            for (String s:newP.split(" ")){
                newPosts.add(s);
            }
            inFromServer.readLine();
        }
    }

    private static void AG() throws IOException {
        outToServer.writeBytes("AG  IGP\r\n\r\n");
        String s;
        String temp = inFromServer.readLine();
        System.out.println(temp);
        if(temp.equals("IGP 310 ALL Groups")){
            System.out.println(inFromServer.readLine());
            s=inFromServer.readLine();
            while (!s.isEmpty()){
                groups.add(s);
                s=inFromServer.readLine();
            }


        }
    }

    private static void LogIn(String userID)
            throws IOException {
        try {
            BufferedReader reader=new BufferedReader(new FileReader(userID+".txt"));
            String s1=reader.readLine();
            if(!s1.isEmpty())
                for (String s:s1.split(" "))
                    subsGroupsIDs.add(s);
            String s2=reader.readLine();
            if(!s2.isEmpty())
                for (String s:s2.split(" "))
                    readPosts.add(s);
        }
        catch (FileNotFoundException e){
            PrintWriter writer = new PrintWriter(userID+".txt", "UTF-8");
            writer.println("");
            writer.println("");
            writer.close();
        }

        Date now=new Date();
        outToServer.writeBytes("LOGIN IGP\r\n");
        outToServer.writeBytes("Date: "+now+"\r\n");
        outToServer.writeBytes("\r\n");
        if(inFromServer.readLine().equals("IGP 214 No Content"))
            inFromServer.readLine();
    }
}
