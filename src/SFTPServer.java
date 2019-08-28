//import stuff
import java.io.*;
import java.net.*;

//SFTP class
class SFTPServer implements Runnable {
    //global settings
    private int port = 6789;
    private boolean serverOn = true;
    private String[] userInformation;
    private boolean userInfoStored = false;
    private boolean passwordAccepted = false;
    private boolean accountAccepted = false;
    private String typeMode;
    private String currentDir = System.getProperty("user.dir");
    private String checkDir = "";

    private void SFTPServer() throws Exception
    {
        //add some stuff for invalid params (length && if message "" say -invalid parameters) or somth
        String clientMessage;
        String returningMessage = "";

        ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();

        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
        OutputStream outputStream = connectionSocket.getOutputStream();

        if (serverOn){
            outToClient.println(buildMessage("+UOA-725 SFTP Service"));
        }
        else{
            outToClient.println(buildMessage("-UOA-725 ice-cream machine broke"));
            return;
        }

        while(serverOn) {
            clientMessage = inFromClient.readLine();
            clientMessage = clientMessage.substring(0, clientMessage.length()-1);
            String[] messageSplit = clientMessage.split(" ");
            switch(messageSplit[0])
            {
                case "USER":
                    returningMessage = USER(messageSplit[1]);
                    break;
                case "ACCT":
                    returningMessage = ACCT(messageSplit[1]);
                    break;
                case "PASS":
                    returningMessage = PASS(messageSplit[1]);
                    break;
                case "TYPE":
                    returningMessage = TYPE(messageSplit[1]);
                    break;
                case "LIST":
                    returningMessage = LIST(messageSplit);
                    break;
                case "CDIR":
                    returningMessage = CDIR(messageSplit[1]);
                    break;
                case "KILL":
                    returningMessage = KILL(messageSplit[1]);
                    break;
                case "DONE":
                    returningMessage = DONE();
                    break;
                default:
                    System.out.println("unrecognised command");
            }
            //System.out.println(returningMessage);
            outToClient.println(returningMessage);
            if (messageSplit[0].equals("DONE") && returningMessage.equals("+\0")){
                return;
            }
        }
    }

    //USER command
    private String USER(String user){
        if (loggedInCheck()){
            return buildMessage("!" + userInformation[0] + " logged in");
        }

        boolean foundUser = false;
        try {
            foundUser = readUserFromFile(user);
        }
        catch (Exception e){
            System.out.println("USER broke");
        }

        if (foundUser == true){
            return buildMessage("+User-id valid, send account and password");
        }
        else{
            return buildMessage("-Invalid user-id, try again");
        }
    }

    //ACCT command
    private String ACCT(String account){
        if (loggedInCheck()){
            return buildMessage("! Account valid, logged-in");
        }

        if ((userInfoStored == true) && (userInformation[1].equals(account))){
            accountAccepted = true;
            if (loggedInCheck()){
                return buildMessage("! Account valid, logged-in");
            }
            else{
                return buildMessage("+Account valid, send password");
            }
        }

        accountAccepted = false;
        return buildMessage("-Invalid account, try again");
    }

    //PASS command
    private String PASS(String password){
        if (loggedInCheck()){
            return buildMessage("!" + userInformation[0] + " logged in");
        }

        if ((userInfoStored == true) && (userInformation[2].equals(password))){
            passwordAccepted = true;
            if (loggedInCheck()){
                return buildMessage("!" + userInformation[0] + " logged in");
            }
            else{
                return buildMessage("+Send account");
            }
        }

        passwordAccepted = false;
        return buildMessage("-Wrong password, try again");
    }

    //TYPE command
    private String TYPE(String type){
        if (loggedInCheck()){
            switch(type)
            {
                case "A":
                    typeMode = type;
                    return buildMessage("+Using Ascii mode");
                case "B":
                    typeMode = type;
                    return buildMessage("+Using Binary mode");
                case "C":
                    typeMode = type;
                    return buildMessage("+Using Continuous mode");
                default:
                    return buildMessage("-Type not valid");
            }
        }
        else{
            return buildMessage("-user not logged in");
        }
    }

    //LIST command
    private String LIST(String[] list){
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        String output = "";
        // check if path is ok
        if (list.length == 3){
            output = checkPath(list[2]);
            if (output != ""){
                return buildMessage(output);
            }
        }

        //return the files in path
        File test = new File(checkDir);

        File[] files = test.listFiles();
        output = "+" + checkDir + "\r\n";
        for (File file: files){
            output = output + file.getName();

            if (list[1].equals("V")){
                output = output + "\tsize<" + file.length() + ">";
                output = output + "\tDirectory<" + String.valueOf(file.isDirectory()) + ">";
            }
            else if (!list[1].equals("F")){
                return buildMessage("-Invalid Format");
            }
            output = output + "\r\n";
        }

        return buildMessage(output);
    }

    //CDIR command
    private String CDIR(String path){
        String output = "";
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        output = checkPath(path);
        if (output != ""){
            return buildMessage("-Can't connect to directory because: " + output);
        }
        else{
            currentDir = checkDir;
            return buildMessage("!Changed working dir to " + currentDir);
        }

    }

    //KILL command
    private String KILL(String filename){
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        try{
            File file = new File(currentDir + "/" + filename);
            if(file.exists()){
                file.delete();
                return buildMessage("+" + filename + " deleted");
            }
        }
        catch (Exception e){
            return buildMessage("-" + e.toString());
        }
        return buildMessage("-file not in current directory");
    }

    //NAME command

    //DONE command
    private String DONE(){
        return buildMessage("+");
    }

    //RETR command

    //STOR command

    private String checkPath(String newPath){
        try {
            File test = new File(newPath);

            File[] files = test.listFiles();
            if (files[0].isDirectory()){
                checkDir = newPath;
            }
        } catch (Exception e) {
            checkDir = currentDir;
            return e.toString();
        }

        return "";
    }

    private String buildMessage(String message){
        String hold = message + "\0";
        return hold;
    }

    private boolean loggedInCheck(){
        if (userInfoStored == true && passwordAccepted == true && accountAccepted == true){
            return true;
        }
        else{
            return false;
        }
    }

    //https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
    private boolean readUserFromFile(String user) throws Exception
    {
        File file = new File("userinfo.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));

        String temp;
        while ((temp = br.readLine()) != null){
            userInformation = temp.split(" ");

            if (userInformation[0].equals(user)){
                userInfoStored = true;
                return true;
            }
        }

        userInfoStored = false;
        return false;
    }

    public void run() {
        System.out.println("Setting up Server...\n");
        try{
            SFTPServer();
            System.out.println("Server connection closed");
        }
        catch (Exception e){
            return;
        }
    }
}


