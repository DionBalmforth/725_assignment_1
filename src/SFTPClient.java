//imports
import java.io.*;
import java.net.*;
import java.nio.file.Files;

//build class
class SFTPClient implements Runnable {
    private String socketName = "localhost";
    private int port = 6789;
    private boolean connOpen = true;
    private int fileSize = 0;
    private String filename = "";
    private String currentDir = System.getProperty("user.dir");

    private void SFTPClient() throws Exception {

        String sentence;
        String modifiedSentence;
        String[] cmd;
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

        while(connOpen){
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
            modifiedSentence = modifiedSentence.substring(0,modifiedSentence.length()-1);
            sb.setLength(0);

            cmd = sentence.split(" ");
            if (cmd[0].equals("RETR") && !modifiedSentence.substring(0, 1).equals("-")){
                fileSize = Integer.valueOf(modifiedSentence);
                filename = cmd[1];
            }

            if (sentence.equals("SEND")){
                File file = new File(currentDir + "/SEND/" + filename);
                file.createNewFile();
            }

            System.out.println("FROM SERVER: " + modifiedSentence);

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