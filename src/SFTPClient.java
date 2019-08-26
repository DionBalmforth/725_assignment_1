//imports
import java.io.*;
import java.net.*;

//build class
class SFTPClient implements Runnable {
    private String socketName = "localhost";
    private int port = 6789;

    private void SFTPClient() throws Exception {

        String sentence;
        String modifiedSentence;

        Socket clientSocket = new Socket(socketName, port);
        clientSocket.setReuseAddress(true);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while(true){
            sentence = inFromUser.readLine();

            outToServer.writeBytes(sentence + '\n');

            modifiedSentence = inFromServer.readLine();

            System.out.println("FROM SERVER: " + modifiedSentence);
        }
        //clientSocket.close();
    }

    public void run() {
        System.out.println("Setting up Client...\n");
        try{
            while(true){
                SFTPClient();
            }
        }
        catch (Exception e){
            return;
        }
    }
}