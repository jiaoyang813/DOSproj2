package project2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class MultiServer {
	public static void main(String[] args) throws IOException {
		String userInput;
		BufferedReader stdIn =
	            new BufferedReader(new InputStreamReader(System.in));
		int portNumber=-1;
		System.out.println("Usage: Designate a port number the server will listen on");
		System.out.println("Please Enter Port Number(1025~65535):");
		System.out.print(">>");				
		portNumber = Integer.parseInt(stdIn.readLine());
		String serverName = InetAddress.getLocalHost().getHostName();
		
		//create chord node of this server
		String serverIP = InetAddress.getLocalHost().getHostAddress();
		//URL url = new URL("http", serverIP, portNumber, "");
		
        File dir = new File(System.getProperty("user.dir")+
        		          "/data/"+serverName+portNumber);
        if(!dir.isDirectory())
        	dir.mkdir();
       
        //contact chordserver to get succ, pred, fingertable.(localhost 40000)

        try (ServerSocket serverSocket = new ServerSocket(portNumber);){ 
        	System.out.println("Server is listenning on port "+portNumber);
            boolean listening = true;
            
            String chordServer = "localhost";
            int chordPort = 40000;
            String fromChord;
            String toChord;
        	try (Socket chordSocket = new Socket(chordServer, chordPort); //change later
    			    PrintWriter out = new PrintWriter(chordSocket.getOutputStream(), true);
    			    BufferedReader in = new BufferedReader(
    			        new InputStreamReader(chordSocket.getInputStream()));)
    		{
                fromChord = in.readLine();
               // System.out.println("ChordServer:"+fromChord);
                out.println("JOIN "+serverIP+" "+portNumber);//send ip and port to chord
                chordSocket.close();
    		}catch (IOException e) {
                    System.err.println("Could not connect ChordServer "+chordServer+":"+chordPort);
                    System.exit(-1);
            }
            while (listening) {
                new ServerThread(serverSocket.accept(),serverName,portNumber).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
		
	}

}
