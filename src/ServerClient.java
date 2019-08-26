//imports
import java.io.*; 
import java.net.*; 

//create class to run server and client
class ServerClient{
    public static void main(String argv[]) throws Exception 
    {
        Thread server = new Thread(new SFTPServer());
        server.start();

        Thread client = new Thread(new SFTPClient());
        client.start();
    } 
} 

