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

    private void SFTPServer() throws Exception
    {
        String clientMessage;
        String returningMessage;

        ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();

        BufferedReader inFromClient =
                new BufferedReader(new
                        InputStreamReader(connectionSocket.getInputStream()));

        DataOutputStream  outToClient =
                new DataOutputStream(connectionSocket.getOutputStream());

        if (serverOn){
            outToClient.writeBytes(buildMessage("+UOA-725 SFTP Service"));
        }
        else{
            outToClient.writeBytes(buildMessage("-UOA-725 ice-cream machine broke"));
            return;
        }

        while(serverOn) {
            clientMessage = inFromClient.readLine();
            String[] messageSplit = clientMessage.split(" ");
            returningMessage = clientMessage.toUpperCase() + '\n';
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
                default:
                    System.out.println("unrecognised command");
            }

            outToClient.writeBytes(returningMessage);
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
            return buildMessage("- user not logged in");
        }
    }

    //LIST command

    //CDIR command

    //KILL command

    //NAME command

    //DONE command

    //RETR command

    //STOR command

    private String buildMessage(String message){
        return message + '\n';
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
        }
        catch (Exception e){
            return;
        }
    }
}


