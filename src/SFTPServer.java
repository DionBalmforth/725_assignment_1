//import stuff
import java.io.*;
import java.net.*;

//SFTP class
class SFTPServer implements Runnable {
    //global settings
    private int port = 6789;

    private void SFTPServer() throws Exception
    {
        String clientSentence;
        String capitalizedSentence;

        ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();

        while(true) {
            System.out.println("TOP");

            System.out.println("Wait for client info");
            BufferedReader inFromClient =
                    new BufferedReader(new
                            InputStreamReader(connectionSocket.getInputStream()));
            System.out.println("recieved client info");

            DataOutputStream  outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + '\n';

            outToClient.writeBytes(capitalizedSentence);
            System.out.println("BOTTOM");
        }
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

//TOBE command - what dis

//SEND command - what dis

//STOP command - what dis

//SIZE command - what dis