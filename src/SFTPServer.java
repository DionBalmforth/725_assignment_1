//import stuff
import java.io.*;
import java.net.*;

//SFTP class
class SFTPServer implements Runnable {
    //global settings
    private int port = 6789;
    private boolean serverOn = true;

    private void SFTPServer() throws Exception
    {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();

        BufferedReader inFromClient =
                new BufferedReader(new
                        InputStreamReader(connectionSocket.getInputStream()));

        DataOutputStream  outToClient =
                new DataOutputStream(connectionSocket.getOutputStream());

        if (serverOn){
            outToClient.writeBytes("+MIT-XX SFTP Service\n");
        }
        else{
            outToClient.writeBytes("-MIT-XX Out to Lunch\n");
        }

        while(serverOn) {
            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + '\n';

            outToClient.writeBytes(capitalizedSentence);
        }
    }

    //USER command

    //ACCT command

    //PASS command

    //TYPE command

    //LIST command

    //CDIR command

    //KILL command

    //NAME command

    //DONE command

    //RETR command

    //STOR command

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


