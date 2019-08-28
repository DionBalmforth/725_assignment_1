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
        int letter;
        StringBuilder sb = new StringBuilder();

        Socket clientSocket = new Socket(socketName, port);
        clientSocket.setReuseAddress(true);

        PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        modifiedSentence = inFromServer.readLine();
        if (modifiedSentence.charAt(0) == '-'){
            clientSocket.close();
            return;
        }

        while(true){
            sentence = inFromUser.readLine();

            outToServer.println(sentence + '\0');

            // Get the reply from the server
            while (true) {
                letter = inFromServer.read();
                sb.append((char) letter);
                if (letter == 0) {
                    inFromServer.readLine();
                    break;
                }
            }
            modifiedSentence = sb.toString();
            sb.setLength(0);

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