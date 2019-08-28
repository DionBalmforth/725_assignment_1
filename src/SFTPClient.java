//imports
import java.io.*;
import java.net.*;
import java.nio.file.Files;

//build class
class SFTPClient implements Runnable {
    //store globals
    private String socketName = "localhost";
    private int port = 6789;
    private boolean connOpen = true;
    private int fileSize = 0;
    private String filename = "";
    private String currentDir = System.getProperty("user.dir");

    private void SFTPClient() throws Exception {
        //initialize variables
        String sentence;
        String modifiedSentence;
        String[] cmd;
        int letter;
        StringBuilder stringBuilder = new StringBuilder();

        Socket clientSocket = new Socket(socketName, port);
        clientSocket.setReuseAddress(true);

        PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //ensure server is open
        modifiedSentence = inFromServer.readLine();
        if (modifiedSentence.charAt(0) == '-'){
            clientSocket.close();
            return;
        }

        while(connOpen){
            //get user input
            sentence = inFromUser.readLine();

            //send to server
            outToServer.println(sentence + '\0');

            //Read back server responce
            while (true) {
                letter = inFromServer.read();
                stringBuilder.append((char) letter);
                if (letter == 0) {
                    inFromServer.readLine();
                    break;
                }
            }
            //write server response
            modifiedSentence = sb.toString();
            modifiedSentence = modifiedSentence.substring(0,modifiedSentence.length()-1);
            stringBuilder.setLength(0);

            //store info from RETR call
            cmd = sentence.split(" ");
            if (cmd[0].equals("RETR") && !modifiedSentence.substring(0, 1).equals("-")){
                fileSize = Integer.valueOf(modifiedSentence);
                filename = cmd[1];
            }

            //try store information from SEND call
            if (sentence.equals("SEND")){
                File file = new File(currentDir + "/SEND/" + filename);
                file.createNewFile();
            }

            //give user information
            System.out.println("FROM SERVER: " + modifiedSentence);

            //kill server
            if (cmd[0].equals("DONE") && modifiedSentence.substring(0, 1).equals("+")){
                connOpen = false;
            }
        }
        clientSocket.close();
    }

    public void run() {
        System.out.println("Setting up Client...\n");
        try{
            while(true){
                SFTPClient();
                System.out.println("Client connection closed");
            }
        }
        catch (Exception e){
            return;
        }
    }
}