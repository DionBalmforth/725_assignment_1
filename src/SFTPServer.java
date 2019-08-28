//import stuff
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
    private String sendingFilename = "";
    private String oldFilename = "";
    private OutputStream outputStream;

    private void SFTPServer() throws Exception
    {
        //initialize variables
        String clientMessage;
        String clientCommand;
        String returningMessage = "";

        ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();

        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
        outputStream = connectionSocket.getOutputStream();

        //handshake
        if (serverOn){
            outToClient.println(buildMessage("+UOA-725 SFTP Service"));
        }
        else{
            outToClient.println(buildMessage("-UOA-725 ice-cream machine broke"));
            return;
        }

        while(serverOn) {
            //read clients message
            clientCommand = inFromClient.readLine();
            clientCommand = clientCommand.substring(0, clientCommand.length()-1);
            String[] messageSplit = clientCommand.split(" ");

            //ensure the client sent the correct information otherwise replace with ""
            if (messageSplit.length >= 2){
                clientMessage = messageSplit[1];
            }
            else{
                clientMessage = "";
            }

            //route to command given
            switch(messageSplit[0])
            {
                case "USER":
                    returningMessage = USER(clientMessage);
                    break;
                case "ACCT":
                    returningMessage = ACCT(clientMessage);
                    break;
                case "PASS":
                    returningMessage = PASS(clientMessage);
                    break;
                case "TYPE":
                    returningMessage = TYPE(clientMessage);
                    break;
                case "LIST":
                    returningMessage = LIST(messageSplit);
                    break;
                case "CDIR":
                    returningMessage = CDIR(clientMessage);
                    break;
                case "KILL":
                    returningMessage = KILL(clientMessage);
                    break;
                case "NAME":
                    returningMessage = NAME(clientMessage);
                    break;
                case "TOBE":
                    returningMessage = TOBE(clientMessage);
                    break;
                case "RETR":
                    returningMessage = RETR(clientMessage);
                    break;
                case "SEND":
                    returningMessage = SEND();
                    break;
                case "DONE":
                    returningMessage = DONE();
                    break;
                default:
                    returningMessage = buildMessage("unrecognised command");
            }

            //return information to client
            outToClient.println(returningMessage);
            //kill server
            if (messageSplit[0].equals("DONE") && returningMessage.equals("+\0")){
                return;
            }
        }
    }

    //USER command
    private String USER(String user){
        //send client error message
        if (user.equals("")){
            return buildMessage("-Invalid user-id, try again");
        }

        //ensure user logged in
        if (loggedInCheck()){
            return buildMessage("!" + userInformation[0] + " logged in");
        }

        boolean foundUser = false;
        try {
            foundUser = readUserFromFile(user);
        }
        catch (Exception e){
            System.out.println("-USER broke");
        }

        //create message for client based off information found
        if (foundUser == true){
            return buildMessage("+User-id valid, send account and password");
        }
        else{
            return buildMessage("-Invalid user-id, try again");
        }
    }

    //ACCT command
    private String ACCT(String account){
        //send client error message
        if (account.equals("")){
            return buildMessage("-Invalid account, try again");
        }

        //loggin check
        if (loggedInCheck()){
            return buildMessage("! Account valid, logged-in");
        }

        //build relevent response for client if account correct
        if ((userInfoStored == true) && (userInformation[1].equals(account))){
            accountAccepted = true;
            if (loggedInCheck()){
                return buildMessage("! Account valid, logged-in");
            }
            else{
                return buildMessage("+Account valid, send password");
            }
        }

        //build error message for client
        accountAccepted = false;
        return buildMessage("-Invalid account, try again");
    }

    //PASS command
    private String PASS(String password){
        //send client error message
        if (password.equals("")){
            return buildMessage("-Wrong password, try again");
        }

        //check client logged in
        if (loggedInCheck()){
            return buildMessage("!" + userInformation[0] + " logged in");
        }

        //check password and user info and give correct respone to client
        if ((userInfoStored == true) && (userInformation[2].equals(password))){
            passwordAccepted = true;
            if (loggedInCheck()){
                return buildMessage("!" + userInformation[0] + " logged in");
            }
            else{
                return buildMessage("+Send account");
            }
        }

        //error message for client
        passwordAccepted = false;
        return buildMessage("-Wrong password, try again");
    }

    //TYPE command
    private String TYPE(String type){
        //send client error message
        if (type.equals("")){
            return buildMessage("-Type not valid");
        }

        //ensure user is logged in
        if (loggedInCheck()){
            //store type given by user
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
        else{ //use current directory
            checkDir = currentDir;
        }

        //return error to client
        if (list.length == 1){
            return buildMessage("-Invalid Format");
        }

        //return the files in path
        File holdDir = new File(checkDir);

        File[] files = holdDir.listFiles();
        output = "+" + checkDir + "\r\n";
        for (File file: files){
            output = output + file.getName();

            //add extra information if client required
            if (list[1].equals("V")){
                output = output + "\tsize<" + file.length() + ">";
                output = output + "\tDirectory<" + String.valueOf(file.isDirectory()) + ">";
            }
            else if (!list[1].equals("F")){
                //send error message to client
                return buildMessage("-Invalid Format");
            }
            output = output + "\r\n";
        }
        return buildMessage(output);
    }

    //CDIR command
    private String CDIR(String path){
        //send client error message
        if (path.equals("")){
            return buildMessage("-Can't connect to directory because no path given");
        }

        String output = "";
        //check if client is logged in
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //send client response based off path correctness
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
        //send client error message
        if (filename.equals("")){
            return buildMessage("-file not in current directory");
        }

        //ensure user logged in
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //tr delete file for user
        try{
            File file = new File(currentDir + "/" + filename);
            if(file.exists()){
                file.delete();
                return buildMessage("+" + filename + " deleted");
            }
        }
        catch (Exception e){  //give client correct error message
            return buildMessage("-" + e.toString());
        }
        return buildMessage("-file not in current directory");
    }

    //NAME command
    private String NAME(String filename){
        //send client error message
        if (filename.equals("")){
            return buildMessage("-file not given");
        }

        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //find file and give client response
        try{
            File file = new File(currentDir + "/" + filename);
            if(file.exists()){
                oldFilename = filename;
                return buildMessage("+File exists");
            }
            else{  //give client error for unfound file
                return buildMessage("-Can't find " + filename);
            }
        }
        catch (Exception e){
            return buildMessage("-Can't find " + filename);
        }
    }

    //TOBE
    private String TOBE(String newFilename){
        //send client error message
        if (newFilename.equals("")){
            return buildMessage("-file not in given");
        }

        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //ensure NAME was called
        if (oldFilename == ""){
            return buildMessage("-File wasn't renamed because NAME not called");
        }

        try{
            File file = new File(currentDir + "/" + oldFilename);
            if(file.exists()){
                File newFile = new File(currentDir + "/" + newFilename);
                file.renameTo(newFile);
                //return successful file name change
                return buildMessage("+" + oldFilename + " renamed to " + newFilename);
            }
            else{//could not find file
                return buildMessage("-File wasn't renamed because not in current Directory");
            }
        }
        catch (Exception e){
            return buildMessage("-File wasn't renamed because" + e.toString());
        }
    }

    //DONE command
    private String DONE(){//kill server
        return buildMessage("+");
    }

    //RETR command
    private String RETR(String filename){
        //send client error message
        if (filename.equals("")){
            return buildMessage("-File doesn't exist");
        }

        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //if file exists in directory give client size
        try{
            File file = new File(currentDir + "/" + filename);
            if(file.exists()){
                sendingFilename = filename;
                return buildMessage(Integer.toString((int)file.length()));
            }
        }
        catch (Exception e){
            sendingFilename = "";
            return buildMessage("-" + e.toString());
        }
        sendingFilename = "";
        return buildMessage("-File doesn't exist");
    }

    //SEND command
    private String SEND(){
        if (!loggedInCheck()){
            return buildMessage("-user not logged in");
        }

        //ensure client has called retr
        if (sendingFilename == ""){
            return buildMessage("-RETR not called");
        }

        //send file to user
        File file = new File(currentDir + "/" + sendingFilename);
        if(file.exists()){
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                outputStream.write(encoded);
            }
            catch (IOException e) { //give error message
                System.out.println("SEND failed: " + e.toString());
            }
        }

        return buildMessage("");
    }


    //Helper methods
    private String checkPath(String newPath){
        //ensure client entered path is a vaid path
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

    //add \0 to all messages before sending to client
    private String buildMessage(String message){
        String hold = message + "\0";
        return hold;
    }

    //check all three USER PASS ACCT settings have been entered
    private boolean loggedInCheck(){
        if (userInfoStored == true && passwordAccepted == true && accountAccepted == true){
            return true;
        }
        else{
            return false;
        }
    }

    //read user info from file
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

    //begin thread
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


